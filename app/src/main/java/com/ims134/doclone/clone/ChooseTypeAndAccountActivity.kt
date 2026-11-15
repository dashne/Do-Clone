package com.ims134.doclone.clone


import android.accounts.Account
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ims134.doclone.R
import com.ims134.doclone.component.AccountChooserScreen
import com.ims134.doclone.io.VAccountManager

import java.io.IOException
import java.util.ArrayList
import java.util.HashSet

class ChooseTypeAndAccountActivity : ComponentActivity(), AccountManagerCallback<Bundle> {

    // ---- Business fields (unobserved by Compose) ----
    private var allowableAccounts: HashSet<Account>? = null
    private var allowableAccountTypes: HashSet<String> = HashSet()
    private var existingAccounts: Array<out Parcelable>? = null
    private var pendingRequest: Int = 0
    private var userId: Int = 0

    // ---- Compose-observed state ----
    // Any assignment (from this Activity or from the Composable) triggers recomposition.
    private var selectedAccountName: String? by mutableStateOf(null)
    private var selectedAddAccount: Boolean by mutableStateOf(false)
    private var descriptionTextOverride: String? by mutableStateOf(null)
    private var accountList: ArrayList<Account> by mutableStateOf(ArrayList())
    private var selectedIndex: Int by mutableStateOf(-1)
    
    val TAG = "AccountChooser"

    // ---- Was `Stub`. ----
    private fun runAddAccountForAuthenticator(accountType: String) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "runAddAccountForAuthenticator: $accountType")
        }
        val addAccountOptions = intent.getBundleExtra("addAccountOptions")
        VAccountManager.instance.addAccount(
            userId,
            accountType,
            intent.getStringExtra("authTokenType"),
            intent.getStringArrayExtra("addAccountRequiredFeatures"),
            addAccountOptions,
            this,
        )
    }

    // ---- Was `b`. ----
    private fun setResultAndFinish(accountName: String?, accountType: String?) {
        val extras = Bundle().apply {
            putString("authAccount", accountName)
            putString("accountType", accountType)
        }
        setResult(RESULT_OK, Intent().putExtras(extras))
        finish()
    }

    // ---- Was `c`. ----
    private fun startChooseAccountTypeActivity() {
        val chooserIntent = Intent(this, ChooseAccountTypeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT
            putExtra("allowableAccountTypes", intent.getStringArrayExtra("allowableAccountTypes"))
            putExtra("addAccountOptions", intent.getBundleExtra("addAccountOptions"))
            putExtra(
                "addAccountRequiredFeatures",
                intent.getStringArrayExtra("addAccountRequiredFeatures"),
            )
            putExtra("authTokenType", intent.getStringExtra("authTokenType"))
        }
        startActivityForResult(chooserIntent, 1)
        pendingRequest = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1) Restore or initialise business state
        if (savedInstanceState != null) {
            pendingRequest = savedInstanceState.getInt("pendingRequest")
            existingAccounts = savedInstanceState.getParcelableArray("existingAccounts")
            selectedAccountName = savedInstanceState.getString("selectedAccountName")
            selectedAddAccount = savedInstanceState.getBoolean("selectedAddAccount", false)
            accountList = savedInstanceState.getParcelableArrayList<Account>("accountList")
                ?: ArrayList()
            userId = savedInstanceState.getInt("userId")
        } else {
            userId = intent.getIntExtra("userId", -1)
            intent.getParcelableExtra<Account>("selectedAccount")?.let {
                selectedAccountName = it.name
            }
        }

        allowableAccounts = intent
            .getParcelableArrayListExtra<Account>("allowableAccounts")
            ?.toHashSet()

        val extraTypes = intent.getStringArrayExtra("allowableAccountTypes")
        val authTypes = VAccountManager.instance.getAuthenticatorTypes()!!
            .mapTo(HashSet(extraTypes?.size ?: 0)) { it.type }
        allowableAccountTypes = if (extraTypes != null) {
            extraTypes.toHashSet().apply { retainAll(authTypes) }
        } else {
            authTypes
        }
        descriptionTextOverride = intent.getStringExtra("descriptionTextOverride")

        val accounts = VAccountManager.instance.getAccounts(userId)!!
        accountList = accounts.filterTo(ArrayList(accounts.size)) { account ->
            (allowableAccounts?.contains(account) ?: true) &&
                    allowableAccountTypes.contains(account.type)
        }

        // 2) Show dialog theme before super.onCreate if we're going straight to
        //    the add-account flow. (The original called super.onCreate twice —
        //    decompiler artifact; we call it once, at the top of this method.)
        if (pendingRequest == 0 && accountList.isEmpty()) {
            //setTheme(R.style.Theme_Material_Light_Dialog_NoActionBar)
        }
        super.onCreate(savedInstanceState)

        // 3) Kick off add flow if no accounts
        if (pendingRequest == 0 && accountList.isEmpty()) {
            if (allowableAccountTypes.size == 1) {
                runAddAccountForAuthenticator(allowableAccountTypes.iterator().next())
            } else {
                startChooseAccountTypeActivity()
            }
        }

        // 4) Compute initial selection
        selectedIndex = if (selectedAddAccount) {
            accountList.size
        } else {
            accountList.indexOfFirst { it.name == selectedAccountName }
        }

        // 5) Compose
        setContent {
            MaterialTheme {
                AccountChooserScreen(
                    description = descriptionTextOverride,
                    items = buildItemLabels(),
                    selectedIndex = selectedIndex,
                    onItemSelected = { selectedIndex = it },
                    onOk = ::onOkClicked,
                    onCancel = ::onCancelClicked,
                )
            }
        }
    }

    /**
     * Builds the display strings: each account name, then Stub trailing "Add account"
     * label. Kept as Stub function so it re-runs whenever [accountList] changes.
     */
    private fun buildItemLabels(): List<String> {
        val out = ArrayList<String>(accountList.size + 1)
        for (a in accountList) out.add(a.name)
        out.add(getString(R.string.add_account_button_label))
        return out
    }

    private fun onOkClicked() {
        if (selectedIndex == accountList.size) {
            startChooseAccountTypeActivity()
            return
        }
        if (selectedIndex != -1) {
            val account = accountList[selectedIndex]
            Log.d(TAG, "selected account $account")
            setResultAndFinish(account.name, account.type)
        }
    }

    private fun onCancelClicked() {
        onBackPressedDispatcher.onBackPressed()
    }

    // ---- Preserved from the original XML layout `android:onClick` targets so
    //      external callers / tests still find them. ----
    fun onCancelButtonClicked(view: View?) = onCancelClicked()
    fun onOkButtonClicked(view: View?) = onOkClicked()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)   // <-- add this line
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            val extras = data?.extras
            Log.v(
                TAG,
                "ChooseTypeAndAccountActivity.onActivityResult(reqCode=$requestCode" +
                        ", resCode=$resultCode, extras=$extras)",
            )
        }
        pendingRequest = 0

        if (resultCode == RESULT_CANCELED) {
            if (accountList.isEmpty()) {
                setResult(RESULT_CANCELED)
                finish()
            }
            return
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                val accountType = data?.getStringExtra("accountType")
                if (accountType != null) {
                    runAddAccountForAuthenticator(accountType)
                    return
                }
                Log.d(
                    TAG,
                    "onActivityResult: unable to find account type, pretending the request was canceled",
                )
            } else if (requestCode == 2) {
                var accountName = data?.getStringExtra("authAccount")
                var accountType = data?.getStringExtra("accountType")
                if (accountName == null || accountType == null) {
                    val accounts = VAccountManager.instance.getAccounts(userId)!!
                    val existing = HashSet<Account>().apply {
                        for (p in existingAccounts!!) add(p as Account)
                    }
                    for (a in accounts) {
                        if (!existing.contains(a)) {
                            accountName = a.name
                            accountType = a.type
                            break
                        }
                    }
                }
                if (accountName != null || accountType != null) {
                    setResultAndFinish(accountName, accountType)
                    return
                }
            }
            Log.d(
                TAG,
                "onActivityResult: unable to find added account, pretending the request was canceled",
            )
        }

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "onActivityResult: canceled")
        }
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("pendingRequest", pendingRequest)
        if (pendingRequest == 2) outState.putParcelableArray("existingAccounts", existingAccounts)
        if (selectedIndex != -1) {
            if (selectedIndex == accountList.size) {
                outState.putBoolean("selectedAddAccount", true)
            } else {
                outState.putBoolean("selectedAddAccount", false)
                outState.putString("selectedAccountName", accountList[selectedIndex].name)
            }
        }
        outState.putParcelableArrayList("accountList", accountList)
    }

    override fun onDestroy() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "onDestroy()")
        }
        super.onDestroy()
    }

    override fun run(future: AccountManagerFuture<Bundle>) {
        try {
            val resultIntent = future.result!!.getParcelable<Intent>("intent")
            if (resultIntent != null) {
                pendingRequest = 2
                existingAccounts = VAccountManager.instance.getAccounts(userId)
                resultIntent.flags = resultIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK.inv()
                startActivityForResult(resultIntent, 2)
                return
            }
        } catch (_: AuthenticatorException) {
        } catch (_: IOException) {
        } catch (_: OperationCanceledException) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        val b = Bundle()
        b.putString("errorMessage", "error communicating with server")
        setResult(
            RESULT_OK,
            Intent().putExtras(b),
        )
        finish()
    }
}