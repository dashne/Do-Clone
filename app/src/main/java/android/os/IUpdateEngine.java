package android.os;


/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IUpdateEngine extends IInterface {

    void applyPayload(String url, long offset, long size, String[] headerKeyValuePairs) throws RemoteException;
    boolean bind(IUpdateEngineCallback callback) throws RemoteException;
    void cancel() throws RemoteException;
    void resetStatus() throws RemoteException;
    void resume() throws RemoteException;
    void suspend() throws RemoteException;
    boolean unbind(IUpdateEngineCallback callback) throws RemoteException;
    boolean verifyPayloadApplicable(String metadataFilename) throws RemoteException;

    abstract class Stub extends Binder implements IUpdateEngine {
        public static final String DESCRIPTOR = "android.os.IUpdateEngine";
        public static final int TRANSACTION_applyPayload = 1;
        public static final int TRANSACTION_bind = 2;
        public static final int TRANSACTION_unbind = 3;
        public static final int TRANSACTION_suspend = 4;
        public static final int TRANSACTION_resume = 5;
        public static final int TRANSACTION_cancel = 6;
        public static final int TRANSACTION_resetStatus = 7;
        public static final int TRANSACTION_verifyPayloadApplicable = 8;

        public static IUpdateEngine asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
