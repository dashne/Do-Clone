package android.content.pm;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IPackageInstallObserver2 extends IInterface {

    void onPackageInstalled(String packageName, int returnCode, String msg, Bundle extras) throws RemoteException;
    void onUserActionRequired(Intent intent) throws RemoteException;

    abstract class Stub extends Binder implements IPackageInstallObserver2 {
        public static final String DESCRIPTOR = "android.content.pm.IPackageInstallObserver2";
        public static final int TRANSACTION_onUserActionRequired = 1;
        public static final int TRANSACTION_onPackageInstalled = 2;

        public static IPackageInstallObserver2 asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
