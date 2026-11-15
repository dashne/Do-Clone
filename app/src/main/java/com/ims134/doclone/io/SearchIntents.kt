package com.ims134.doclone.io

class SearchIntents private constructor() {

    companion object {
        @JvmField
        val ACTION_SEARCH: String = "com.google.android.gms.actions.SEARCH_ACTION"

        @JvmField
        val EXTRA_QUERY: String = "query"
    }
}