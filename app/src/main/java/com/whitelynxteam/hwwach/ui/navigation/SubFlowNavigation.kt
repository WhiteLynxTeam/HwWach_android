package com.whitelynxteam.hwwach.ui.navigation

abstract class SubFlowNavigation(val onFinished: (routeName: String) -> Unit) {
    abstract val startRoute: String

//    abstract fun addFlow(builder: NavGraphBuilder)

    fun finishFlow() {
        onFinished(startRoute)
    }
}