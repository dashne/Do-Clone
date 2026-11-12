package com.ims134.doclone.io

import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.R

interface IAppApiDelegate {}

// jadx CloneApiDelegate implements IAppApiDelegate
class CloneApiDelegate : IAppApiDelegate {

    // get(String). Returns the label to show for get clone.
    fun getCloneLabel(label: String?): String {
        val resources = appContext.resources
        return if (label == null) {
            resources.getString(R.string.app_name)
        } else {
            // string resource with one %s placeholder, e.g. "%s (Clone)"
            resources.getString( R.string.clone_label_tag, label)
        }
    }
}