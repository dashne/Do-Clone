package android.accounts;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IAccountManagerResponse extends IInterface {

    void onResult(Bundle value) throws RemoteException;
    void onError(int errorCode, String errorMessage) throws RemoteException;

    abstract class Stub extends Binder implements IAccountManagerResponse {
        public static final String DESCRIPTOR = "android.accounts.IAccountManagerResponse";
        public static final int TRANSACTION_onResult = 1;
        public static final int TRANSACTION_onError = 2;

        public static IAccountManagerResponse asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
