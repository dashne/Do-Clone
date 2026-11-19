package android.content;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IIntentReceiver extends IInterface {

    void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException;

    abstract class Stub extends Binder implements IIntentReceiver {
        public static final String DESCRIPTOR = "android.content.IIntentReceiver";
        public static final int TRANSACTION_performReceive = 1;

        public static IIntentReceiver asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
