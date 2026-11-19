package android.os;


/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IUpdateEngineCallback extends IInterface {

    void onPayloadApplicationComplete(int errorCode) throws RemoteException;
    void onStatusUpdate(int statusCode, float percent) throws RemoteException;

    abstract class Stub extends Binder implements IUpdateEngineCallback {
        public static final String DESCRIPTOR = "android.os.IUpdateEngineCallback";
        public static final int TRANSACTION_onStatusUpdate = 1;
        public static final int TRANSACTION_onPayloadApplicationComplete = 2;

        public static IUpdateEngineCallback asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
