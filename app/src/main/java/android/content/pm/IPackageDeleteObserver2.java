package android.content.pm;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IPackageDeleteObserver2 extends IInterface {

    void onPackageDeleted(String packageName, int returnCode, String msg) throws RemoteException;
    void onUserActionRequired(Intent intent) throws RemoteException;

    abstract class Stub extends Binder implements IPackageDeleteObserver2 {
        public static final String DESCRIPTOR = "android.content.pm.IPackageDeleteObserver2";
        public static final int TRANSACTION_onUserActionRequired = 1;
        public static final int TRANSACTION_onPackageDeleted = 2;

        public static IPackageDeleteObserver2 asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
