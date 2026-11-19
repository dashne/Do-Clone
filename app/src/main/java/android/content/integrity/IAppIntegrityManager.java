package android.content.integrity;

import android.content.IntentSender;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import java.util.List;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IAppIntegrityManager extends IInterface {

    String getCurrentRuleSetProvider() throws RemoteException;
    String getCurrentRuleSetVersion() throws RemoteException;
    Bundle getCurrentRules() throws RemoteException;
    List<String> getWhitelistedRuleProviders() throws RemoteException;
    void updateRuleSet(String version, Bundle rules, IntentSender statusReceiver) throws RemoteException;

    abstract class Stub extends Binder implements IAppIntegrityManager {
        public static final String DESCRIPTOR = "android.content.integrity.IAppIntegrityManager";
        public static final int TRANSACTION_updateRuleSet = 1;
        public static final int TRANSACTION_getCurrentRuleSetVersion = 2;
        public static final int TRANSACTION_getCurrentRuleSetProvider = 3;
        public static final int TRANSACTION_getCurrentRules = 4;
        public static final int TRANSACTION_getWhitelistedRuleProviders = 5;

        public static IAppIntegrityManager asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
