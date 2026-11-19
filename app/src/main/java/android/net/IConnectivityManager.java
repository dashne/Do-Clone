package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IConnectivityManager extends IInterface {

    LinkProperties getActiveLinkProperties() throws RemoteException;
    NetworkInfo getActiveNetworkInfo() throws RemoteException;
    NetworkInfo getActiveNetworkInfoForUid(int uid, boolean connectedOnly) throws RemoteException;
    NetworkInfo[] getAllNetworkInfo() throws RemoteException;
    LinkProperties getLinkProperties(int netId) throws RemoteException;
    NetworkInfo getNetworkInfo(int netId) throws RemoteException;
    boolean isActiveNetworkMetered() throws RemoteException;
    boolean requestRouteToHostAddress(int networkType, int hostAddress) throws RemoteException;

    abstract class Stub extends Binder implements IConnectivityManager {
        public static final String DESCRIPTOR = "android.net.IConnectivityManager";
        public static final int TRANSACTION_getActiveNetworkInfo = 1;
        public static final int TRANSACTION_getActiveNetworkInfoForUid = 2;
        public static final int TRANSACTION_getNetworkInfo = 3;
        public static final int TRANSACTION_getAllNetworkInfo = 4;
        public static final int TRANSACTION_isActiveNetworkMetered = 5;
        public static final int TRANSACTION_requestRouteToHostAddress = 6;
        public static final int TRANSACTION_getActiveLinkProperties = 7;
        public static final int TRANSACTION_getLinkProperties = 8;

        public static IConnectivityManager asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
