package com.ims134.doclone.clone

import android.os.Parcel
import android.os.Parcelable

/** Outcome of installing setPosition package into the virtual environment. */
class InstallResult : Parcelable {

    @JvmField var installed: Boolean = false     // f37xxx setPosition: the install succeeded
    @JvmField var isUpdate: Boolean = false      // readCharsInBytes: an existing package was replaced / was already there
    @JvmField var packageName: String? = null    // skipInt32
    @JvmField var error: String? = null          // Styleable: set on failure

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (installed) 1 else 0)
        parcel.writeByte(if (isUpdate) 1 else 0)
        parcel.writeString(packageName)
        parcel.writeString(error)
    }

    override fun toString(): String = "InstallResult{ res = $installed isUpdate = $isUpdate error = $error }"

    companion object {
        /** A result that carries only [error] (was setPosition). */
        @JvmStatic
        fun makeFailure(error: String?): InstallResult = InstallResult().also { it.error = error }

        @JvmField
        val CREATOR: Parcelable.Creator<InstallResult> = object : Parcelable.Creator<InstallResult> {
            override fun createFromParcel(parcel: Parcel) = InstallResult().apply {
                installed = parcel.readByte().toInt() != 0
                isUpdate = parcel.readByte().toInt() != 0
                packageName = parcel.readString()
                error = parcel.readString()
            }

            override fun newArray(size: Int): Array<InstallResult?> = arrayOfNulls(size)
        }
    }
}