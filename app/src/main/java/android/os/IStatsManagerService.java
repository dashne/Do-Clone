package android.os;

import android.app.PendingIntent;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IStatsManagerService extends IInterface {

    void addConfiguration(long configKey, byte[] config, String packageName) throws RemoteException;
    byte[] getData(long configKey, String packageName) throws RemoteException;
    byte[] getMetadata(String packageName) throws RemoteException;
    long[] getRegisteredExperimentIds() throws RemoteException;
    void registerPullAtomCallback(int atomTag, long coolDownMillis, long timeoutMillis, int[] additiveFields, IBinder callback) throws RemoteException;
    void removeActiveConfigsChangedOperation(String packageName) throws RemoteException;
    void removeConfiguration(long configKey, String packageName) throws RemoteException;
    void removeDataFetchOperation(long configKey, String packageName) throws RemoteException;
    long[] setActiveConfigsChangedOperation(PendingIntent pendingIntent, String packageName) throws RemoteException;
    void setBroadcastSubscriber(long configKey, long subscriberId, PendingIntent pendingIntent, String packageName) throws RemoteException;
    void setDataFetchOperation(long configKey, PendingIntent pendingIntent, String packageName) throws RemoteException;
    void unregisterPullAtomCallback(int atomTag) throws RemoteException;
    void unsetBroadcastSubscriber(long configKey, long subscriberId, String packageName) throws RemoteException;

    abstract class Stub extends Binder implements IStatsManagerService {
        public static final String DESCRIPTOR = "android.os.IStatsManagerService";
        public static final int TRANSACTION_setDataFetchOperation = 1;
        public static final int TRANSACTION_removeDataFetchOperation = 2;
        public static final int TRANSACTION_setActiveConfigsChangedOperation = 3;
        public static final int TRANSACTION_removeActiveConfigsChangedOperation = 4;
        public static final int TRANSACTION_setBroadcastSubscriber = 5;
        public static final int TRANSACTION_unsetBroadcastSubscriber = 6;
        public static final int TRANSACTION_getRegisteredExperimentIds = 7;
        public static final int TRANSACTION_getMetadata = 8;
        public static final int TRANSACTION_getData = 9;
        public static final int TRANSACTION_addConfiguration = 10;
        public static final int TRANSACTION_removeConfiguration = 11;
        public static final int TRANSACTION_registerPullAtomCallback = 12;
        public static final int TRANSACTION_unregisterPullAtomCallback = 13;

        public static IStatsManagerService asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
