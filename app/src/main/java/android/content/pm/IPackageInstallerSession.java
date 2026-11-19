package android.content.pm;

import android.content.IntentSender;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IPackageInstallerSession extends IInterface {

    void abandon() throws RemoteException;
    void addClientProgress(float f) throws RemoteException;
    void close() throws RemoteException;
    void commit(IntentSender intentSender) throws RemoteException;
    String[] getNames() throws RemoteException;
    ParcelFileDescriptor openRead(String str) throws RemoteException;
    ParcelFileDescriptor openWrite(String str, long j, long j2) throws RemoteException;
    void removeSplit(String str) throws RemoteException;
    void setClientProgress(float f) throws RemoteException;

    abstract class Stub extends Binder implements IPackageInstallerSession {
        public static final String DESCRIPTOR = "android.content.pm.IPackageInstallerSession";
        public static final int TRANSACTION_setClientProgress = 1;
        public static final int TRANSACTION_addClientProgress = 2;
        public static final int TRANSACTION_getNames = 3;
        public static final int TRANSACTION_openWrite = 4;
        public static final int TRANSACTION_openRead = 5;
        public static final int TRANSACTION_removeSplit = 6;
        public static final int TRANSACTION_close = 7;
        public static final int TRANSACTION_commit = 8;
        public static final int TRANSACTION_abandon = 9;

        public static IPackageInstallerSession asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
