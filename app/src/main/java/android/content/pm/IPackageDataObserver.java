package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IPackageDataObserver extends IInterface {

    void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException;

    abstract class Stub extends Binder implements IPackageDataObserver {
        public static final String DESCRIPTOR = "android.content.pm.IPackageDataObserver";
        public static final int TRANSACTION_onRemoveCompleted = 1;

        public static IPackageDataObserver asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
