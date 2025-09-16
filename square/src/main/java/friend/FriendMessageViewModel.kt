package friend

import android.app.Application
import android.util.Log
import com.example.corekit.http.BaseViewModel
import com.example.corekit.http.bean.ErrorResponse
import com.example.corekit.http.bean.ResLiveData
import com.example.corekit.http.listener.LiveDataCallback
import com.example.corekit.http.request
import com.example.loding.friend.FriendRepository
import entity.ApplicationMessage

class FriendMessageViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val repository: FriendRepository by lazy {
        Log.d("TAG", ":2")
        FriendRepository()
    }

    val applicationLiveData: ResLiveData<List<ApplicationMessage>> by lazy { ResLiveData() }

    fun getApplications() {
        request(
            applicationLiveData,
            object : LiveDataCallback<List<ApplicationMessage>, List<ApplicationMessage>> {
                override fun success(
                    emit: ResLiveData<List<ApplicationMessage>>,
                    msg: String?,
                    data: List<ApplicationMessage>?,
                ) {
                    data?.let {
                        emit.success(ArrayList(it))
                    }
                }

                override fun otherCode(
                    emit: ResLiveData<List<ApplicationMessage>>,
                    code: Int?,
                    msg: String?,
                    data: List<ApplicationMessage>?,
                ) {
                    emit.error(ErrorResponse.otherCode(code, msg))
                }

                override fun error(
                    emit: ResLiveData<List<ApplicationMessage>>,
                    e: ErrorResponse,
                ) {
                    emit.error(e, null)
                }
            },
            {
                repository.getApplications()
            },
        )
    }
}
