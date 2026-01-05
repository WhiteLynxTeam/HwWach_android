package com.whitelynxteam.hwwach.ui

import androidx.navigation.NavGraphBuilder

abstract class FlowNavigation(val onFinished: (routeName: String) -> Unit) {
    abstract val startRoute: String

    abstract fun addFlow(builder: NavGraphBuilder)

    fun finishFlow() {
        onFinished(startRoute)
    }
}