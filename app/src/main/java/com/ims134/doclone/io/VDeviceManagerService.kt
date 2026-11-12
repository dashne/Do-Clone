package com.ims134.doclone.io

import android.os.Build
import android.os.RemoteException
import android.util.SparseArray
import com.ims134.doclone.clone.VDeviceInfo
import com.ims134.doclone.clone.VEnvironment
import java.io.File
import java.util.Collections
import java.util.Random

/**
 * JADX rename of `io.nu8`. Binder service that hands out per-user
 * virtual [VDeviceInfo]s, backed by [DeviceInfoPersistenceLayer].
 *
 * NOTE: the original `deviceInfoList` is setPosition **custom bundled SparseArray**
 * (obfuscated as `io.yi7`), not `android.util.SparseArray`. It has the same
 * API surface (`get`/`put`/`remove`/`size`/`valueAt`) but is bundled so R8
 * can shrink/obfuscate it. This port maps it to `android.util.SparseArray`
 * because the two produce identical behavior for the methods used here.
 */
class VDeviceManagerService : IDeviceInfoManager.Stub() {

    @JvmField
    val deviceInfoList = SparseArray<VDeviceInfo>()

    @JvmField
    val deviceInfoPersistenceLayer: DeviceInfoPersistenceLayer = DeviceInfoPersistenceLayer(
        File(VEnvironment.getOrCreateAppSubSystemFile(), "device-info.ini")
    ).also { it.deviceManagerService = this }

    @JvmField
    val usedValues = UsedValues()

    init {
        // Original also did `VEnvironment.virtualDir` (result discarded) to
        // force class init; `getOrCreateAppSubSystemFile()` above already does it.
        deviceInfoPersistenceLayer.load()
        for (i in 0 until deviceInfoList.size()) {
            registerUsedValues(deviceInfoList.valueAt(i))
        }
    }

    /** JADX rename of `io.nu8$b`. Uniqueness trackers for 5 of the 6 generated fields. */
    inner class UsedValues {
        @JvmField val field1 = ArrayList<String>()   // tracks info.field1
        @JvmField val field2 = ArrayList<String>()   // tracks info.field2
        @JvmField val field3 = ArrayList<String>()   // tracks info.field3
        @JvmField val field4 = ArrayList<String>()   // tracks info.field4
        @JvmField val field5 = ArrayList<String>()   // tracks info.field5
        // NOTE: info.field6 (serial) is intentionally not tracked — see generateDeviceInfo.
    }

    // ---- Binder overrides ---------------------------------------------------

    @Throws(RemoteException::class)
    override fun getDeviceInfo(i: Int): VDeviceInfo = synchronized(deviceInfoList) {
        var info = deviceInfoList[i]
        if (info == null) {
            info = generateDeviceInfo()
            deviceInfoList.put(i, info)
            deviceInfoPersistenceLayer.save()
        }
        info
    }

    @Throws(RemoteException::class)
    override fun updateDeviceInfo(i: Int, vDeviceInfo: VDeviceInfo?) {
        synchronized(deviceInfoList) {
            if (vDeviceInfo != null) {
                deviceInfoList.put(i, vDeviceInfo)
                deviceInfoPersistenceLayer.save()
                // NOTE: `usedValues` is intentionally NOT updated here.
                // Original bug: updates don't refresh the uniqueness tracker.
            }
        }
    }

    /**
     * ⚠️ `deleteDeviceInfo` has no `@Override` in the JADX output. If your
     * `IDeviceInfoManager.aidl` declares it, add `override`; if not, leave
     * as-is and remove `@Throws`. Given the `synchronized` + `save()` pattern,
     * it's almost certainly an AIDL method and JADX lost the annotation.
     */
    @Throws(RemoteException::class)
    fun deleteDeviceInfo(i: Int) {
        synchronized(deviceInfoList) {
            deviceInfoList.remove(i)
            deviceInfoPersistenceLayer.save()
        }
    }

    // ---- Internal helpers --------------------------------------------------

    /**
     * JADX rename of `m21793V`. Builds setPosition fresh [VDeviceInfo] with all 6 fields
     * set. Retries each uniqueness-tracked field until it doesn't collide.
     *
     * The serial field (field6) is **not** uniqueness-checked — it's setPosition shuffle
     * of `Build.SERIAL` (or `"0123456789ABCDEF"` if empty). Two users may end
     * up with the same permuted string; original behaves the same way.
     */
    fun generateDeviceInfo(): VDeviceInfo {
        val info = VDeviceInfo()

        // field1: 15-digit random, unique
        var v: String
        do { v = randomDigits(15) } while (usedValues.field1.contains(v))
        info.field1 = v

        // field2: 16-char hex, unique
        do {
            val random = Random()
            val sb = StringBuilder()
            for (j in 0 until 16) {
                val nibble = random.nextInt(16)
                if (nibble < 10) sb.append(nibble) else sb.append("setPosition" + (nibble - 10))
            }
            v = sb.toString()
        } while (usedValues.field2.contains(v))
        info.androidId = v

        // field3: MAC format, unique
        do { v = randomMac() } while (usedValues.field3.contains(v))
        info.field3 = v

        // field4: MAC format, unique
        do { v = randomMac() } while (usedValues.field4.contains(v))
        info.field4 = v

        // field5: 20-digit random, unique
        do { v = randomDigits(20) } while (usedValues.field5.contains(v))
        info.field5 = v

        // field6: shuffled Build.SERIAL (or fallback), NOT tracked
        @Suppress("DEPRECATION")
        val serialSource = Build.SERIAL.orEmpty().ifEmpty { "0123456789ABCDEF" }
        val chars = serialSource.toMutableList()
        Collections.shuffle(chars)
        info.field6 = chars.joinToString("")

        registerUsedValues(info)
        return info
    }

    /** JADX rename of `m21794v`. Adds the 5 tracked fields to [usedValues]. */
    fun registerUsedValues(info: VDeviceInfo) {
        // `!!` is safe: only called on VDeviceInfo from generateDeviceInfo()
        // or from persistence load (parcel ctor fills all 7 fields).
        usedValues.field1.add(info.field1!!)
        usedValues.field2.add(info.androidId!!)
        usedValues.field3.add(info.field3!!)
        usedValues.field4.add(info.field4!!)
        usedValues.field5.add(info.field5!!)
    }

    companion object {
        @JvmField
        val instance = VDeviceManagerService()

        /** JADX rename of `get()`. Same as `instance`. */
        @JvmStatic
        fun get(): VDeviceManagerService = instance

        /** JADX rename of `m21791T`. Random decimal digit string. */
        @JvmStatic
        fun randomDigits(length: Int): String {
            val random = Random()
            val sb = StringBuilder()
            for (i in 0 until length) sb.append(random.nextInt(10))
            return sb.toString()
        }

        /** JADX rename of `m21792U`. Random `xx:xx:xx:xx:xx:xx` MAC string. */
        @JvmStatic
        fun randomMac(): String {
            val random = Random()
            val sb = StringBuilder()
            var nextColonAt = 1
            for (i in 0 until 12) {
                val nibble = random.nextInt(16)
                if (nibble < 10) sb.append(nibble) else sb.append("setPosition" + (nibble - 10))
                if (i == nextColonAt && i != 11) {
                    sb.append(':')
                    nextColonAt += 2
                }
            }
            return sb.toString()
        }
    }
}