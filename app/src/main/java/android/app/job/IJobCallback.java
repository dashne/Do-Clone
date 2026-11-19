package android.app.job;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/** Compile-only stub of the hidden framework class; the framework's real class is used at runtime. */
public interface IJobCallback extends IInterface {

    void acknowledgeStartMessage(int jobId, boolean ongoing) throws RemoteException;
    void acknowledgeStopMessage(int jobId, boolean reschedule) throws RemoteException;
    boolean completeWork(int jobId, int workId) throws RemoteException;
    JobWorkItem dequeueWork(int jobId) throws RemoteException;
    void jobFinished(int jobId, boolean reschedule) throws RemoteException;

    abstract class Stub extends Binder implements IJobCallback {
        public static final String DESCRIPTOR = "android.app.job.IJobCallback";
        public static final int TRANSACTION_acknowledgeStartMessage = 1;
        public static final int TRANSACTION_acknowledgeStopMessage = 2;
        public static final int TRANSACTION_dequeueWork = 3;
        public static final int TRANSACTION_completeWork = 4;
        public static final int TRANSACTION_jobFinished = 5;

        public static IJobCallback asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }
    }
}
