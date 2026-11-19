package android.app.job;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IJobService extends IInterface {

    void startJob(JobParameters jobParameters) throws RemoteException;
    void stopJob(JobParameters jobParameters) throws RemoteException;

    abstract class Stub extends Binder implements IJobService {
        public static final String DESCRIPTOR = "android.app.job.IJobService";
        public static final int TRANSACTION_startJob = 1;
        public static final int TRANSACTION_stopJob = 2;

        public static IJobService asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
