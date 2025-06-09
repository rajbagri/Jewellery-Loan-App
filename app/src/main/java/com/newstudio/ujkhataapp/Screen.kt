package com.newstudio.ujkhataapp

sealed class Screen(val route: String) {
    object CustomerScreen : Screen("customerScreen")

    object EntryScreen : Screen("entry_screen/{customerId}") {
        fun createRoute(customerId: String) = "entry_screen/$customerId"
    }

    object EntryInEntryScreen : Screen("entry_in_entry_screen/{customerId}/{entryId}") {
        fun createRoute(customerId: String, entryId: String) = "entry_in_entry_screen/$customerId/$entryId"
    }
}
