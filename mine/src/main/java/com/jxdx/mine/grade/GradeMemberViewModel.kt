//package com.jxdx.mine.grade
//
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.jxdx.mine.Member
//import com.jxdx.mine.adapter.ClassMemberAdapter
//import kotlinx.coroutines.launch
//class GradeMemberViewModel(private val repository: GradeMemberRepository) : ViewModel() {
//    val memberListLiveData = MutableLiveData<List<Member>>()
//
//    fun loadMembers() {
//        viewModelScope.launch {
//            try {
//                val apiMembers = repository.getMembers()
//                val list = mutableListOf<Member>()
//
//                val teacherSection = Member("section_teacher", "老师", null,"",0 )
//                val studentSection = Member("section_student", "学生", null, "",0)
//
//                val teacherList = mutableListOf<Member>()
//                val studentList = mutableListOf<Member>()
//
//                apiMembers.forEach { member ->
//                    val mapped = Member(
//                        member.id,
//                        member.name,
//                        member.avatarUrl,
//                        member.role,
//                        when (member.type) {
//                            1 -> MemberAdapter.TYPE_TEACHER
//                            2 -> MemberAdapter.TYPE_STUDENT
//                            else -> MemberAdapter.TYPE_STUDENT
//                        }
//                    )
//                    if (member.type == 1) teacherList.add(mapped)
//                    else studentList.add(mapped)
//                }
//
//                if (teacherList.isNotEmpty()) {
//                    list.add(teacherSection)
//                    list.addAll(teacherList)
//                }
//                if (studentList.isNotEmpty()) {
//                    list.add(studentSection)
//                    list.addAll(studentList)
//                }
//
//                memberListLiveData.postValue(list)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//}