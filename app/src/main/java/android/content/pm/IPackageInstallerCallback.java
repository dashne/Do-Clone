package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IPackageInstallerCallback extends IInterface {

    void onSessionCreated(int sessionId) throws RemoteException;
    void onSessionBadgingChanged(int sessionId) throws RemoteException;
    void onSessionActiveChanged(int sessionId, boolean active) throws RemoteException;
    void onSessionProgressChanged(int sessionId, float progress) throws RemoteException;
    void onSessionFinished(int sessionId, boolean success) throws RemoteException;

    abstract class Stub extends Binder implements IPackageInstallerCallback {
        public static final String DESCRIPTOR = "android.content.pm.IPackageInstallerCallback";
        public static final int TRANSACTION_onSessionCreated = 1;
        public static final int TRANSACTION_onSessionBadgingChanged = 2;
        public static final int TRANSACTION_onSessionActiveChanged = 3;
        public static final int TRANSACTION_onSessionProgressChanged = 4;
        public static final int TRANSACTION_onSessionFinished = 5;

        public static IPackageInstallerCallback asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
