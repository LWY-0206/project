#include <jni.h>
#include <string>
#include <android/log.h>
#define LOGE(...) __android_log_print(ANDROID_LOG_INFO,"rtmp",__VA_ARGS__)

extern "C"{
    #include  "librtmp/rtmp.h"
}

//保存PPS和SPS，因为我们需要在每次发送关键帧之前发送PPS和SPS（因为不知道客户端会在什么时候开始播放）
typedef  struct {
    RTMP *rtmp;
    int16_t sps_len;
    int8_t *sps;
    int16_t pps_len;
    int8_t *pps;
}Live;
Live *live = NULL;

void close();

extern "C" JNIEXPORT jstring JNICALL
Java_com_jxdx_classroom_activity_ScreenLive_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_jxdx_classroom_activity_ScreenLive_connect(JNIEnv *env, jobject thiz, jstring url_) {
    const char *url = env->GetStringUTFChars(url_, 0);

    live = (Live*)malloc(sizeof(Live));

    live->rtmp = RTMP_Alloc();//分配空间
    RTMP_Init(live->rtmp);//初始化

    live->rtmp->Link.timeout = 10;//连接超时时长为10秒
    live->rtmp->Link.lFlags |= RTMP_LF_LIVE;//设置类型为直播
    RTMP_SetupURL(live->rtmp, (char*)url);//设置推流URL

    RTMP_EnableWrite(live->rtmp);//设置为可写状态

    LOGE("RTMP start Connect");

    int ret;
    if (!(ret = RTMP_Connect(live->rtmp, NULL))){
        //连接服务器
        close();
    }

    if (ret&&!(ret = RTMP_ConnectStream(live->rtmp, 0))){
        //连接流
        close();
    }

    if (ret){
        LOGE("RTMP Connect success");
    }

    env->ReleaseStringUTFChars(url_, url);
    return ret;
}

// 传递第一帧 00 00 00 01 67 64 00 28ACB402201E3CBCA41408081B4284D4  00000001 68 EE 06 F2 C0
void prepareVideo(int8_t *data, int len, Live *live) {
    for (int i = 0; i < len; i++) {
        if (i + 4 < len) {
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 && data[i + 3] == 0x01) {
                if (data[i + 4]  == 0x68) {
                    //sps解析
                    live->sps_len = i - 4;
                    live->sps = static_cast<int8_t *>(malloc(live->sps_len));
                    memcpy(live->sps, data + 4, live->sps_len);

                    //pps解析
                    live->pps_len = len - (4 + live->sps_len) - 4;
                    live->pps = static_cast<int8_t *>(malloc(live->pps_len));
                    memcpy(live->pps, data + 4 + live->sps_len + 4, live->pps_len);
                    break;
                }
            }
        }
    }
}

//spspps 的 packaet
RTMPPacket *createVideoPackage(Live *live) {
    int body_size = 16 + live->sps_len + live->pps_len;//参考rtmp视频包结构
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, body_size);
    int i = 0;
    packet->m_body[i++] = 0x17;//非关键帧为0x0010，视频帧为0x0111，故而为0x0001 0111，即0x17
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;//1字节~4字节固定

    packet->m_body[i++] = 0x01;//版本，固定为1
    packet->m_body[i++] = live->sps[1]; //profile 如baseline、main、 high
    packet->m_body[i++] = live->sps[2]; //profile_compatibility 兼容性
    packet->m_body[i++] = live->sps[3]; //profile level

    packet->m_body[i++] = 0xFF;//包长数据所使用的字节数，一般固定为0xFF
    packet->m_body[i++] = 0xE1;//SPS个数，一般固定为0xE1

    //sps长度
    packet->m_body[i++] = (live->sps_len >> 8) & 0xFF;//高八位
    packet->m_body[i++] = live->sps_len & 0xff;//低八位

    //拷贝sps的内容
    memcpy(&packet->m_body[i], live->sps, live->sps_len);

    i +=live->sps_len;

    packet->m_body[i++] = 0x01;//PPS的个数

    //pps长度
    packet->m_body[i++] = (live->pps_len >> 8) & 0xff; //高八位
    packet->m_body[i++] = live->pps_len & 0xff;//低八位

    // 拷贝pps内容
    memcpy(&packet->m_body[i], live->pps, live->pps_len);

    //视频类型
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x04;//固定，代表VIDEO获取Audio
    packet->m_nTimeStamp = 0;//固定，确保无论从什么时候进入直播间，都是从0计时
    packet->m_hasAbsTimestamp = 0;//固定，确保无论从什么时候进入直播间，都是从0计时
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;//大小为中等，当然改成大也是可以的
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}

RTMPPacket *createVideoPackage(int8_t *buf, int len, const long tms, Live *live) {
    buf += 4;
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    int body_size = len + 9;

    //初始化RTMP内部的body数组
    RTMPPacket_Alloc(packet, body_size);

    //第一个字节前4bit（1关键帧，2非关键帧），后4bit为7（0x0111）代表视频
    if (buf[0] == 0x65) {
        packet->m_body[0] = 0x17;//0x0001 0111
        LOGE("发送关键帧 data");
    } else{
        packet->m_body[0] = 0x27;
        LOGE("发送非关键帧 data");//0x0010 0111
    }

    packet->m_body[1] = 0x01;
    packet->m_body[2] = 0x00;
    packet->m_body[3] = 0x00;
    packet->m_body[4] = 0x00;//1字节~4字节固定

    //H264裸数据长度
    packet->m_body[5] = (len >> 24) & 0xff;
    packet->m_body[6] = (len >> 16) & 0xff;
    packet->m_body[7] = (len >> 8) & 0xff;
    packet->m_body[8] = (len) & 0xff;

    //H264裸数据
    memcpy(&packet->m_body[9], buf, len);

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x04;//固定，代表VIDEO或Audio
    packet->m_nTimeStamp = tms;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}

int sendPacket(RTMPPacket *packet) {
    int r = RTMP_SendPacket(live->rtmp, packet, 1);
    if(r){
        LOGE("发送rtmp包成功");
    }
    RTMPPacket_Free(packet);
    free(packet);
    return r;
}

// 传递第一帧 00 00 00 01 67 64 00 28ACB402201E3CBCA41408081B4284D4  0000000168 EE 06 F2 C0
int sendVideo(int8_t *buf, int len, long tms) {
    int ret = 0;
    if (buf[4] == 0x67) {
        // 缓存sps 和pps 到全局遍历 不需要推流
        if (live && (!live->pps || !live->sps)) {
            prepareVideo(buf, len, live);
        }
        return ret;
    }

    if (buf[4] == 0x65) {//关键帧
        RTMPPacket *packet = createVideoPackage(live);
        sendPacket(packet);
    }

    RTMPPacket *packet2 = createVideoPackage(buf, len, tms, live);
    ret = sendPacket(packet2);
    return ret;
}

void close(){
    if (live != NULL){
        RTMP_Close(live->rtmp);//关闭连接
        RTMP_Free(live->rtmp);
        free(live);
    }

    live->rtmp = NULL;
    live = NULL;
}



extern "C"
JNIEXPORT jboolean JNICALL
Java_com_jxdx_classroom_activity_ScreenLive_sendData(JNIEnv *env, jobject thiz, jbyteArray data_, jint len,
                                            jlong tms) {
    int ret;
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    ret = sendVideo(data, len, tms);
    env->ReleaseByteArrayElements(data_, data, 0);
    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_jxdx_classroom_activity_ScreenLive_close(JNIEnv *env, jobject thiz) {
    close();
}