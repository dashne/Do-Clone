package android.app;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IServiceConnection extends IInterface {

    void connected(ComponentName name, IBinder service) throws RemoteException;

    abstract class Stub extends Binder implements IServiceConnection {
        public static final String DESCRIPTOR = "android.app.IServiceConnection";
        public static final int TRANSACTION_connected = 1;

        public static IServiceConnection asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
