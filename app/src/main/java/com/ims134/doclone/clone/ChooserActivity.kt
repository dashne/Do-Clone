package com.ims134.doclone.clone


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.ims134.doclone.R


/**
 * The target of an Intent.ACTION_CHOOSER of get virtual app (StartActivity points the chooser intent here). Reads the
 * request the hook stored in the intent's extras (options, result target, request code, user) and shows the
 * chooser for the wrapped intent.
 */
@Suppress("DEPRECATION")
open class ChooserActivity : ResolverActivity() {

    @SuppressLint("MissingSuperCall")
    final override fun onCreate(savedInstanceState: Bundle?) {
        val intent = getIntent()
        val chosenUser = intent.getIntExtra("android.intent.extra.user_handle", VBinder.getUidByPid() / VUserHandle.PER_USER_RANGE)
        options = intent.getParcelableExtra("android.intent.extra.virtual.data")
        resultWho = intent.getStringExtra("android.intent.extra.virtual.who")
        requestCode = intent.getIntExtra("android.intent.extra.virtual.request_code", 0)
        resultTo = intent.extras!!.getBinder("android.intent.extra.virtual.result_to")
        val target = intent.getParcelableExtra<Parcelable>("android.intent.extra.INTENT")
        if (target !is Intent) {
            finish()
            return
        }
        val title: CharSequence = intent.getCharSequenceExtra("android.intent.extra.TITLE") ?: getString(
            R.string.choose)
        val initial = intent.getParcelableArrayExtra("android.intent.extra.INITIAL_INTENTS")
        var initialIntents: Array<Intent?>? = null
        if (initial != null) {
            val list = arrayOfNulls<Intent>(initial.size)
            for (i in initial.indices) {
                val item = initial[i]
                if (item !is Intent) {
                    finish()
                    return
                }
                list[i] = item
            }
            initialIntents = list
        }
        initResolver(savedInstanceState, target, title, initialIntents, false, chosenUser)
    }

    companion object {
        /** was p: the action of get chooser intent (ACTION_CHOOSER, or what Intent.createChooser makes of it) */
        @JvmField
        val CHOOSER_ACTION: String? = Intent.createChooser(Intent(), "").action
    }
}