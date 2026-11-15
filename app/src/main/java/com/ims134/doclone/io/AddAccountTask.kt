package com.ims134.doclone.io



import android.os.Bundle
import android.os.RemoteException
import com.ims134.doclone.clone.ChooseTypeAndAccountActivity


/**
 * Was `du8`. Concrete [AmsTask] for [VAccountManager.addAccount].
 */
class AddAccountTask(
    private val accountManager: VAccountManager,
    activity: ChooseTypeAndAccountActivity?,
    private val userId: Int,
    private val accountType: String?,
    private val authTokenType: String?,
    private val requiredFeatures: Array<String>?,
    private val options: Bundle?,
) : AmsTask(activity) {

    @Throws(RemoteException::class)
    override fun doWork() {
        try {
            accountManager.getService().addAccount(
                userId,
                response,
                accountType,
                authTokenType,
                requiredFeatures,
                false,
                options,
            )
        } catch (_: RemoteException) {
        }
    }
}