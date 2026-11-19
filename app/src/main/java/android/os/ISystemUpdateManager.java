package android.os;


/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface ISystemUpdateManager extends IInterface {

    Bundle retrieveSystemUpdateInfo() throws RemoteException;
    void updateSystemUpdateInfo(PersistableBundle info) throws RemoteException;

    abstract class Stub extends Binder implements ISystemUpdateManager {
        public static final String DESCRIPTOR = "android.os.ISystemUpdateManager";
        public static final int TRANSACTION_retrieveSystemUpdateInfo = 1;
        public static final int TRANSACTION_updateSystemUpdateInfo = 2;

        public static ISystemUpdateManager asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
