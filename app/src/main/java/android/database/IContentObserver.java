package android.database;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IContentObserver extends IInterface {

    void onChange(boolean z, Uri uri, int i) throws RemoteException;

    abstract class Stub extends Binder implements IContentObserver {
        public static final String DESCRIPTOR = "android.database.IContentObserver";
        public static final int TRANSACTION_onChange = 1;

        public static IContentObserver asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
