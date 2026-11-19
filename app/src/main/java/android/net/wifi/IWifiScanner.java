package android.net.wifi;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IWifiScanner extends IInterface {

    Bundle getAvailableChannels(int band) throws RemoteException;
    Messenger getMessenger() throws RemoteException;

    abstract class Stub extends Binder implements IWifiScanner {
        public static final String DESCRIPTOR = "android.net.wifi.IWifiScanner";
        public static final int TRANSACTION_getMessenger = 1;
        public static final int TRANSACTION_getAvailableChannels = 2;

        public static IWifiScanner asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
