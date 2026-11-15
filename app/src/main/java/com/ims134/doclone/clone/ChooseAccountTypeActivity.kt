package com.ims134.doclone.clone


import android.accounts.AuthenticatorDescription
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.ims134.doclone.component.ChooseAccountTypeScreen
import com.ims134.doclone.io.VAccountManager


class ChooseAccountTypeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val allowableTypes = intent.getStringArrayExtra("allowableAccountTypes")?.toHashSet()

        // Was `this.Stub` — preserves insertion order, so display order matches the
        // order returned by VAccountManager (usually only 1-2 entries anyway).
        val map = LinkedHashMap<String, AccountTypeItem>()
        val descriptions = VAccountManager.instance.getAuthenticatorTypes() ?: emptyArray()
        for (desc in descriptions) {
            map[desc.type] = loadAccountTypeItem(desc)
        }

        // Was `this.b` — filtered display list
        val displayList = ArrayList<AccountTypeItem>(map.size)
        for ((type, item) in map) {
            if (allowableTypes == null || allowableTypes.contains(type)) {
                displayList.add(item)
            }
        }

        // Case 1: nothing to show — return the error to the caller
        if (displayList.isEmpty()) {
            val b = Bundle()
            b.putString("errorMessage", "no allowable account types")
            setResult(
                RESULT_OK,
                Intent().putExtras(b),
            )
            finish()
            return
        }

        // Case 2: exactly one type — pick it automatically
        if (displayList.size == 1) {
            val b = Bundle()
            b.putString("accountType", displayList[0].desc.type)
            setResult(
                RESULT_OK,
                Intent().putExtras(b),
            )
            finish()
            return
        }

        // Case 3: multiple types — show the picker
        setContent {
            MaterialTheme {
                ChooseAccountTypeScreen(displayList) {
                    val b = Bundle()
                    b.putString("accountType", it.desc.type)
                    setResult(
                        RESULT_OK,
                        Intent().putExtras(b),
                    )
                    finish()
                }
            }
        }
    }

    /**
     * Loads the label and icon for [desc] from the clone's resources.
     * Failures on either resource fall through to null — matches the original's
     * nested try/catch, which would leave the label/icon null on `NotFoundException`.
     */
    private fun loadAccountTypeItem(desc: AuthenticatorDescription): AccountTypeItem {
        var icon: ImageBitmap? = null
        var label: String? = null
        try {
            val res = VirtualCore.gCore.getCloneResources(desc.packageName)
            try {
                val d = res.getDrawable(desc.iconId)
                icon = d.toBitmap(
                    width = d.intrinsicWidth.coerceAtLeast(1),
                    height = d.intrinsicHeight.coerceAtLeast(1),
                ).asImageBitmap()
            } catch (_: Resources.NotFoundException) {
            } catch (_: IllegalArgumentException) {
                // Some Drawables have no intrinsic size (ColorDrawable, etc.)
            }
            try {
                label = res.getText(desc.labelId).toString()
            } catch (_: Resources.NotFoundException) {
            }
        } catch (_: Resources.NotFoundException) {
        }
        return AccountTypeItem(desc, label, icon)
    }
}

/**
 * Was inner class `c`. Holds the description, display label, and icon for one
 * authenticator type. The icon is stored as an [ImageBitmap] because Compose's
 * [Image] draws bitmaps, not Drawables.
 */
data class AccountTypeItem(
    val desc: AuthenticatorDescription,
    val label: String?,
    val icon: ImageBitmap?,
)