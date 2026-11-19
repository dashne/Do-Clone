package android.accounts;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IAccountAuthenticator extends IInterface {

    void addAccount(IAccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws RemoteException;
    void addAccountFromCredentials(IAccountAuthenticatorResponse response, Account account, Bundle accountCredentials) throws RemoteException;
    void confirmCredentials(IAccountAuthenticatorResponse response, Account account, Bundle options) throws RemoteException;
    void editProperties(IAccountAuthenticatorResponse response, String accountType) throws RemoteException;
    void getAccountCredentialsForCloning(IAccountAuthenticatorResponse response, Account account) throws RemoteException;
    void getAccountRemovalAllowed(IAccountAuthenticatorResponse response, Account account) throws RemoteException;
    void getAuthToken(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws RemoteException;
    void getAuthTokenLabel(IAccountAuthenticatorResponse response, String authTokenType) throws RemoteException;
    void hasFeatures(IAccountAuthenticatorResponse response, Account account, String[] features) throws RemoteException;
    void updateCredentials(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws RemoteException;

    abstract class Stub extends Binder implements IAccountAuthenticator {
        public static final String DESCRIPTOR = "android.accounts.IAccountAuthenticator";
        public static final int TRANSACTION_addAccount = 1;
        public static final int TRANSACTION_confirmCredentials = 2;
        public static final int TRANSACTION_getAuthToken = 3;
        public static final int TRANSACTION_getAuthTokenLabel = 4;
        public static final int TRANSACTION_updateCredentials = 5;
        public static final int TRANSACTION_editProperties = 6;
        public static final int TRANSACTION_hasFeatures = 7;
        public static final int TRANSACTION_getAccountRemovalAllowed = 8;
        public static final int TRANSACTION_getAccountCredentialsForCloning = 9;
        public static final int TRANSACTION_addAccountFromCredentials = 10;

        public static IAccountAuthenticator asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
