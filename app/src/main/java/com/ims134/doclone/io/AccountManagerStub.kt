package com.ims134.doclone.io


import android.accounts.Account
import android.accounts.IAccountManagerResponse
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.IInterface
import android.os.RemoteException
import android.util.Log
import com.ims134.doclone.clone.MethodProxy
import com.ims134.doclone.clone.VUserHandle
import com.ims134.doclone.clone.VirtualRuntime
import com.ims134.doclone.reflect.RefStaticMethod
import java.lang.reflect.Method



@Suppress("UNCHECKED_CAST")
class AccountManagerStub : BinderInvocationProxy(
    IAccountManagerMirror.Stub.asInterface as RefStaticMethod<IInterface>?,
    "account",
) {

    // ===== Simple delegators =====

    /** Was `a0`. */
    class GetUserData : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            val key = arr[1] as String
            return try {
                instance.getService().getUserData(VUserHandle.getVUid(), account, key)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e); throw e
            }
        }
        override fun getMethodName(): String = "getUserData"
    }

    /** Was `b`. */
    class AccountAuthenticated : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            return try {
                instance.getService().accountAuthenticated(VUserHandle.getVUid(), account)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e); throw e
            }
        }
        override fun getMethodName(): String = "accountAuthenticated"
    }

    /** Was `b0`. */
    class HasFeatures : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val account = arr[1] as Account
            val features = arr[2] as Array<String>
            try {
                instance.getService().hasFeatures(VUserHandle.getVUid(), response, account, features)
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "hasFeatures"
    }

    /** Was `c`. Outer try/catch swallows any exception; inner swallows RemoteException. */
    class AddAccount : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            try {
                val arr = args!!
                val response = arr[0] as IAccountManagerResponse
                val type = arr[1] as String?
                val tokenType = arr[2] as String?
                val features = arr[3] as Array<String>?
                val expectActivityLaunch = arr[4] as Boolean
                val options = arr[5] as Bundle?
                try {
                    instance.getService().addAccount(
                        VUserHandle.getVUid(), response, type, tokenType, features,
                        expectActivityLaunch, options,
                    )
                } catch (_: RemoteException) {
                }
            } catch (e: Exception) {
                Log.d("Account", Log.getStackTraceString(e))
            }
            return 0
        }
        override fun getMethodName(): String = "addAccount"
    }

    /** Was `c0`. */
    class InvalidateAuthToken : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val type = arr[0] as String
            val token = arr[1] as String
            try {
                instance.getService().invalidateAuthToken(VUserHandle.getVUid(), type, token)
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "invalidateAuthToken"
    }

    /** Was `d`. */
    class AddAccountAsUser : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val type = arr[1] as String?
            val tokenType = arr[2] as String?
            val features = arr[3] as Array<String>?
            val expectActivityLaunch = arr[4] as Boolean
            val options = arr[5] as Bundle?
            try {
                instance.getService().addAccount(
                    VUserHandle.getVUid(), response, type, tokenType, features,
                    expectActivityLaunch, options,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "addAccountAsUser"
    }

    /** Was `d0`. @TargetApi(26) */
    @TargetApi(26)
    class IsCredentialsUpdateSuggested : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val account = arr[1] as Account
            val token = arr[2] as String
            try {
                instance.getService().isCredentialsUpdateSuggested(
                    VUserHandle.getVUid(), response, account, token,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "isCredentialsUpdateSuggested"
    }

    /** Was `e`. */
    class AddAccountExplicitly : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            val password = arr[1] as String?
            val userdata = arr[2] as Bundle?
            return try {
                instance.getService().addAccountExplicitly(
                    VUserHandle.getVUid(), account, password, userdata,
                )
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e); throw e
            }
        }
        override fun getMethodName(): String = "addAccountExplicitly"
    }

    /** Was `e0`. */
    class PeekAuthToken : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            val tokenType = arr[1] as String
            return try {
                instance.getService().peekAuthToken(VUserHandle.getVUid(), account, tokenType)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e); throw e
            }
        }
        override fun getMethodName(): String = "peekAuthToken"
    }

    /** Was `f`. @TargetApi(26). Returns FALSE (not null) — preserved. */
    @TargetApi(26)
    class AddAccountExplicitlyWithVisibility : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            val password = arr[1] as String?
            val userdata = arr[2] as Bundle?
            @Suppress("UNCHECKED_CAST")
            val visibility = arr[3] as Map<String, Int>?
            try {
                instance.getService().addAccountExplicitlyWithVisibility(
                    VUserHandle.getVUid(), account, password, userdata, visibility,
                )
            } catch (_: RemoteException) {
            }
            return false
        }
        override fun getMethodName(): String = "addAccountExplicitlyWithVisibility"
    }

    /** Was `f0`. @TargetApi(26). */
    @TargetApi(26)
    class RegisterAccountListener : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val accountTypes = arr[0] as Array<String>
            val opPackageName = arr[1] as String
            try {
                instance.getService().registerAccountListener(
                    VUserHandle.getVUid(), accountTypes, opPackageName,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "registerAccountListener"
    }

    /** Was `g0`. Handles missing `expectActivityLaunch` argument. */
    class RemoveAccount : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val account = arr[1] as Account
            val expectActivityLaunch = if (arr.size >= 3) arr[2] as Boolean else false
            try {
                instance.getService().removeAccount(
                    VUserHandle.getVUid(), response, account, expectActivityLaunch,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "removeAccount"
    }

    /** Was `h`. */
    class ClearPassword : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            try {
                instance.getService().clearPassword(VUserHandle.getVUid(), account)
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "clearPassword"
    }

    /** Was `h0`. */
    class RemoveAccountAsUser : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val account = arr[1] as Account
            val expectActivityLaunch = if (arr.size >= 3) arr[2] as Boolean else false
            try {
                instance.getService().removeAccount(
                    VUserHandle.getVUid(), response, account, expectActivityLaunch,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "removeAccountAsUser"
    }

    /** Was `i`. */
    class ConfirmCredentialsAsUser : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val account = arr[1] as Account
            val options = arr[2] as Bundle?
            val expectActivityLaunch = arr[3] as Boolean
            try {
                instance.getService().confirmCredentials(
                    VUserHandle.getVUid(), response, account, options, expectActivityLaunch,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "confirmCredentialsAsUser"
    }

    /** Was `i0`. */
    class RemoveAccountExplicitly : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            return try {
                instance.getService().removeAccountExplicitly(VUserHandle.getVUid(), account)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e); throw e
            }
        }
        override fun getMethodName(): String = "removeAccountExplicitly"
    }

    /** Was `k`. */
    class EditProperties : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val accountType = arr[1] as String
            val expectActivityLaunch = arr[2] as Boolean
            try {
                instance.getService().editProperties(
                    VUserHandle.getVUid(), response, accountType, expectActivityLaunch,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "editProperties"
    }

    /** Was `k0`. */
    class RenameAccount : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val account = arr[1] as Account
            val newName = arr[2] as String
            try {
                instance.getService().renameAccount(VUserHandle.getVUid(), response, account, newName)
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "renameAccount"
    }

    /** Was `l`. @TargetApi(26). */
    @TargetApi(26)
    class FinishSessionAsUser : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val sessionBundle = arr[1] as Bundle?
            val expectActivityLaunch = arr[2] as Boolean
            val appInfo = arr[3] as Bundle?
            val userId = arr[4] as Int
            try {
                instance.getService().finishSessionAsUser(
                    VUserHandle.getVUid(), response, sessionBundle, expectActivityLaunch,
                    appInfo, userId,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "finishSessionAsUser"
    }

    /** Was `m`. @TargetApi(26). Preserved bug: returns 0, not the service result. */
    @TargetApi(26)
    class GetAccountVisibility : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            val packageName = arr[1] as String
            try {
                instance.getService().getAccountVisibility(VUserHandle.getVUid(), account, packageName)
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "getAccountVisibility"
    }

    /** Was `m0`. @TargetApi(26). Returns FALSE. */
    @TargetApi(26)
    class SetAccountVisibility : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            val packageName = arr[1] as String
            val visibility = arr[2] as Int
            try {
                instance.getService().setAccountVisibility(
                    VUserHandle.getVUid(), account, packageName, visibility,
                )
            } catch (_: RemoteException) {
            }
            return false
        }
        override fun getMethodName(): String = "setAccountVisibility"
    }

    /** Was `n0`. */
    class SetAuthToken : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            val authTokenType = arr[1] as String
            val authToken = arr[2] as String
            try {
                instance.getService().setAuthToken(
                    VUserHandle.getVUid(), account, authTokenType, authToken,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "setAuthToken"
    }

    /** Was `o`. @TargetApi(26). */
    @TargetApi(26)
    class GetAccountsAndVisibilityForPackage : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val packageName = arr[0] as String
            val accountType = arr[1] as String?
            return try {
                instance.getService().getAccountsAndVisibilityForPackage(
                    VUserHandle.getVUid(), packageName, accountType,
                )
            } catch (_: RemoteException) {
                null
            }
        }
        override fun getMethodName(): String = "getAccountsAndVisibilityForPackage"
    }

    /** Was `o0`. */
    class SetPassword : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            val password = arr[1] as String
            try {
                instance.getService().setPassword(VUserHandle.getVUid(), account, password)
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "setPassword"
    }

    /** Was `p0`. */
    class SetUserData : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            val key = arr[1] as String
            val value = arr[2] as String
            try {
                instance.getService().setUserData(VUserHandle.getVUid(), account, key, value)
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "setUserData"
    }

    /** Was `q`. */
    class GetAccountsByFeatures : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val accountType = arr[1] as String?
            val features = arr[2] as Array<String>?
            try {
                Log.d("VAccount", "getAccountsByFeatures $accountType")
                instance.getService().getAccountsByFeatures(
                    VUserHandle.getVUid(), response, accountType, features,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "getAccountsByFeatures"
    }

    /** Was `q0`. @TargetApi(26). */
    @TargetApi(26)
    class StartAddAccountSession : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val accountType = arr[1] as String?
            val authTokenType = arr[2] as String?
            val requiredFeatures = arr[3] as Array<String>?
            val expectActivityLaunch = arr[4] as Boolean
            val options = arr[5] as Bundle?
            try {
                instance.getService().startAddAccountSession(
                    VUserHandle.getVUid(), response, accountType, authTokenType,
                    requiredFeatures, expectActivityLaunch, options,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "startAddAccountSession"
    }

    /** Was `r0`. @TargetApi(26). */
    @TargetApi(26)
    class StartUpdateCredentialsSession : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val account = arr[1] as Account
            val authTokenType = arr[2] as String
            val expectActivityLaunch = arr[3] as Boolean
            val options = arr[4] as Bundle?
            try {
                instance.getService().startUpdateCredentialsSession(
                    VUserHandle.getVUid(), response, account, authTokenType,
                    expectActivityLaunch, options,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "startUpdateCredentialsSession"
    }

    /** Was `s0`. @TargetApi(26). */
    @TargetApi(26)
    class UnregisterAccountListener : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val accountTypes = arr[0] as Array<String>
            val opPackageName = arr[1] as String
            try {
                instance.getService().unregisterAccountListener(
                    VUserHandle.getVUid(), accountTypes, opPackageName,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "unregisterAccountListener"
    }

    /** Was `t`. */
    class GetAuthToken : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val account = arr[1] as Account
            val authTokenType = arr[2] as String
            val notifyOnAuthFailure = arr[3] as Boolean
            val expectActivityLaunch = arr[4] as Boolean
            val options = arr[5] as Bundle?
            try {
                instance.getService().getAuthToken(
                    VUserHandle.getVUid(), response, account, authTokenType,
                    notifyOnAuthFailure, expectActivityLaunch, options,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "getAuthToken"
    }

    /** Was `u`. */
    class GetAuthTokenLabel : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val accountType = arr[1] as String
            val authTokenType = arr[2] as String
            try {
                instance.getService().getAuthTokenLabel(
                    VUserHandle.getVUid(), response, accountType, authTokenType,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "getAuthTokenLabel"
    }

    /** Was `u0`. */
    class UpdateCredentials : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val response = arr[0] as IAccountManagerResponse
            val account = arr[1] as Account
            val authTokenType = arr[2] as String
            val expectActivityLaunch = arr[3] as Boolean
            val options = arr[4] as Bundle?
            try {
                instance.getService().updateCredentials(
                    VUserHandle.getVUid(), response, account, authTokenType,
                    expectActivityLaunch, options,
                )
            } catch (_: RemoteException) {
            }
            return 0
        }
        override fun getMethodName(): String = "updateCredentials"
    }

    /** Was `w`. @TargetApi(26). */
    @TargetApi(26)
    class GetPackagesAndVisibilityForAccount : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            return try {
                instance.getService().getPackagesAndVisibilityForAccount(VUserHandle.getVUid(), account)
            } catch (_: RemoteException) {
                null
            }
        }
        override fun getMethodName(): String = "getPackagesAndVisibilityForAccount"
    }

    /** Was `x`. */
    class GetPassword : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            return try {
                instance.getService().getPassword(VUserHandle.getVUid(), account)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e); throw e
            }
        }
        override fun getMethodName(): String = "getPassword"
    }

    /** Was `y`. */
    class GetPreviousName : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            val account = arr[0] as Account
            return try {
                instance.getService().getPreviousName(VUserHandle.getVUid(), account)
            } catch (e: RemoteException) {
                VirtualRuntime.crash(e); throw e
            }
        }
        override fun getMethodName(): String = "getPreviousName"
    }

    // ===== Self-routing proxies =====

    /** Was `n`. */
    class GetAccounts : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            instance.getAccounts(args!![0] as String?)
        override fun getMethodName(): String = "getAccounts"
    }

    /** Was `p`. */
    class GetAccountsAsUser : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            instance.getAccounts(args!![0] as String?)
        override fun getMethodName(): String = "getAccountsAsUser"
    }

    /** Was `r`. */
    class GetAccountsByTypeForPackage : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            instance.getAccounts(args!![0] as String?)
        override fun getMethodName(): String = "getAccountsByTypeForPackage"
    }

    /** Was `s`. Ignores arguments, always passes null (preserved bug). */
    class GetAccountsForPackage : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            instance.getAccounts(null)
        override fun getMethodName(): String = "getAccountsForPackage"
    }

    /** Was `v`. Ignores arguments. */
    class GetAuthenticatorTypes : MethodProxy() {
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? =
            instance.getAuthenticatorTypes()
        override fun getMethodName(): String = "getAuthenticatorTypes"
    }

    // ===== Passthrough proxies (invoke the real method directly) =====

    /** Was `g`. */
    class AddSharedAccountAsUser : MethodProxy() {
        @Suppress("UNUSED_EXPRESSION")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            arr[1] as Int
            return method.invoke(who, *arr)
        }
        override fun getMethodName(): String = "addSharedAccountAsUser"
    }

    /** Was `j`. */
    class CopyAccountToUser : MethodProxy() {
        @Suppress("UNUSED_EXPRESSION")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            arr[2] as Int
            arr[3] as Int
            method.invoke(who, *arr)
            return 0
        }
        override fun getMethodName(): String = "copyAccountToUser"
    }

    /** Was `j0`. */
    class RemoveSharedAccountAsUser : MethodProxy() {
        @Suppress("UNUSED_EXPRESSION")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            arr[1] as Int
            return method.invoke(who, *arr)
        }
        override fun getMethodName(): String = "removeSharedAccountAsUser"
    }

    /** Was `l0`. */
    class RenameSharedAccountAsUser : MethodProxy() {
        @Suppress("UNUSED_EXPRESSION")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            arr[2] as Int
            return method.invoke(who, *arr)
        }
        override fun getMethodName(): String = "renameSharedAccountAsUser"
    }

    /** Was `t0`. */
    class UpdateAppPermission : MethodProxy() {
        @Suppress("UNUSED_EXPRESSION")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            arr[2] as Int
            arr[3] as Boolean
            method.invoke(who, *arr)
            return 0
        }
        override fun getMethodName(): String = "updateAppPermission"
    }

    /** Was `z`. */
    class GetSharedAccountsAsUser : MethodProxy() {
        @Suppress("UNUSED_EXPRESSION")
        override fun call(who: Any?, method: Method, args: Array<Any?>?): Any? {
            val arr = args!!
            arr[0] as Int
            return method.invoke(who, *arr)
        }
        override fun getMethodName(): String = "getSharedAccountsAsUser"
    }

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(GetPassword())
        addMethodProxy(GetUserData())
        addMethodProxy(GetAuthenticatorTypes())
        addMethodProxy(GetAccounts())
        addMethodProxy(GetAccountsForPackage())
        addMethodProxy(GetAccountsByTypeForPackage())
        addMethodProxy(GetAccountsAsUser())
        addMethodProxy(HasFeatures())
        addMethodProxy(GetAccountsByFeatures())
        addMethodProxy(AddAccountExplicitly())
        addMethodProxy(RemoveAccount())
        addMethodProxy(RemoveAccountAsUser())
        addMethodProxy(RemoveAccountExplicitly())
        addMethodProxy(CopyAccountToUser())
        addMethodProxy(InvalidateAuthToken())
        addMethodProxy(PeekAuthToken())
        addMethodProxy(SetAuthToken())
        addMethodProxy(SetPassword())
        addMethodProxy(ClearPassword())
        addMethodProxy(SetUserData())
        addMethodProxy(UpdateAppPermission())
        addMethodProxy(GetAuthToken())
        addMethodProxy(AddAccount())
        addMethodProxy(AddAccountAsUser())
        addMethodProxy(UpdateCredentials())
        addMethodProxy(EditProperties())
        addMethodProxy(ConfirmCredentialsAsUser())
        addMethodProxy(AccountAuthenticated())
        addMethodProxy(GetAuthTokenLabel())
        addMethodProxy(AddSharedAccountAsUser())
        addMethodProxy(GetSharedAccountsAsUser())
        addMethodProxy(RemoveSharedAccountAsUser())
        addMethodProxy(RenameAccount())
        addMethodProxy(GetPreviousName())
        addMethodProxy(RenameSharedAccountAsUser())

        if (Build.VERSION.SDK_INT >= 26) {
            addMethodProxy(RegisterAccountListener())
            addMethodProxy(UnregisterAccountListener())
            addMethodProxy(StartAddAccountSession())
            addMethodProxy(StartUpdateCredentialsSession())
            addMethodProxy(FinishSessionAsUser())
            addMethodProxy(IsCredentialsUpdateSuggested())
            addMethodProxy(GetPackagesAndVisibilityForAccount())
            addMethodProxy(AddAccountExplicitlyWithVisibility())
            addMethodProxy(SetAccountVisibility())
            addMethodProxy(GetAccountVisibility())
            addMethodProxy(GetAccountsAndVisibilityForPackage())
        }
    }

    companion object {
        /**
         * Was the static field `c`. Aliased to the [VAccountManager] singleton.
         * `@JvmField` preserves the bytecode shape so existing Java code can still
         * reference `AccountManagerStub.c`.
         */
        @JvmField
        val instance: VAccountManager = VAccountManager.instance
    }
}