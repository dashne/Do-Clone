package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IStopUserCallback extends IInterface {

    void userStopAborted(int i) throws RemoteException;
    void userStopped(int i) throws RemoteException;

    abstract class Stub extends Binder implements IStopUserCallback {
        public static final String DESCRIPTOR = "android.app.IStopUserCallback";
        public static final int TRANSACTION_userStopped = 1;
        public static final int TRANSACTION_userStopAborted = 2;

        public static IStopUserCallback asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
