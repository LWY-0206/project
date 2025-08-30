package com.example.corekit.recyclerview

interface BaseSelectBehavior {
    fun dispatchState(status: Int)

    fun stateHandle(status: Int)

    fun onNormalState()

    fun onSelectedState()
}
