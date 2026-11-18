package android.content.pm;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.os.Bundle;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class PackageParser {
    public static final int PARSE_COLLECT_CERTIFICATES = 32;
    public static final int PARSE_FORWARD_LOCK = 16;
    public static final int PARSE_IS_SYSTEM = 1;
    public static final int PARSE_IS_SYSTEM_DIR = 64;

    public static final class Activity extends Component<ActivityIntentInfo> {
        public ActivityInfo info;
    }

    public class ActivityIntentInfo extends IntentInfo {
        public Activity activity;

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public ActivityIntentInfo() {
        }
    }

    @TargetApi(28)
    public class Builder {
        private Signature[] mSignatures;

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public Builder() {
        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public SigningDetails build() {
            return new SigningDetails();
        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public Builder setSignatures(Signature[] signatureArr) {
            this.mSignatures = signatureArr;
            return this;
        }
    }

    public static class Component<II extends IntentInfo> {
        public String className;
        public ArrayList<II> intents;
        public Bundle metaData;
        public Package owner;

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public ComponentName getComponentName() {
            return null;
        }
    }

    public final class Instrumentation extends Component<IntentInfo> {
        public InstrumentationInfo info;

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public Instrumentation() {
        }
    }

    public static class IntentInfo extends IntentFilter {
        public int banner;
        public boolean hasDefault;
        public int icon;
        public int labelRes;
        public int logo;
        public CharSequence nonLocalizedLabel;
    }

    public class Package {
        public ApplicationInfo applicationInfo;
        public Bundle mAppMetaData;
        public Object mExtras;
        public int mPreferredOrder;
        public String mSharedUserId;
        public int mSharedUserLabel;
        public Signature[] mSignatures;
        public SigningDetails mSigningDetails;
        public int mVersionCode;
        public String mVersionName;
        public String packageName;
        public ArrayList<String> usesLibraries;
        public final ArrayList<Activity> activities = new ArrayList<>(0);
        public final ArrayList<Activity> receivers = new ArrayList<>(0);
        public final ArrayList<Provider> providers = new ArrayList<>(0);
        public final ArrayList<Service> services = new ArrayList<>(0);
        public final ArrayList<Instrumentation> instrumentation = new ArrayList<>(0);
        public final ArrayList<Permission> permissions = new ArrayList<>(0);
        public final ArrayList<PermissionGroup> permissionGroups = new ArrayList<>(0);
        public final ArrayList<String> requestedPermissions = new ArrayList<>();
        public ArrayList<ConfigurationInfo> configPreferences = null;
        public ArrayList<FeatureInfo> reqFeatures = null;

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public Package() {
        }
    }

    public final class Permission extends Component<IntentInfo> {
        public PermissionInfo info;

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public Permission() {
        }
    }

    public final class PermissionGroup extends Component<IntentInfo> {
        public PermissionGroupInfo info;

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public PermissionGroup() {
        }
    }

    public final class Provider extends Component<ProviderIntentInfo> {
        public ProviderInfo info;

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public Provider() {
        }
    }

    public class ProviderIntentInfo extends IntentInfo {
        public Provider provider;

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public ProviderIntentInfo() {
        }
    }

    public final class Service extends Component<ServiceIntentInfo> {
        public ServiceInfo info;

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public Service() {
        }
    }

    public class ServiceIntentInfo extends IntentInfo {
        public Service service;

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 1 */
        public ServiceIntentInfo() {
        }
    }

    @TargetApi(28)
    public static final class SigningDetails {
        public static final SigningDetails UNKNOWN = null;
        public Signature[] signatures;
    }
}