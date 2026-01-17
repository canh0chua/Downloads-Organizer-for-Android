package com.canh0chua.downloadsorganizer.ui

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Detail : Screen("detail/{fileType}") {
        fun createRoute(fileType: String) = "detail/$fileType"
    }
}
