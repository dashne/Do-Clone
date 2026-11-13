package com.ims134.doclone.clone.server

import android.accounts.Account
import android.os.Parcel
import android.os.Parcelable

/** One virtual account of one virtual user. Persisted by VAccountManagerService (account-list.ini). */
class VAccount private constructor(
    val userId: Int,                                   // a
    var name: String,                                  // b
    var previousName: String?,                         // c
    val type: String,                                  // d
    var password: String?,                             // e
    var lastAuthenticatedTime: Long,                   // f
    val authTokens: HashMap<String, String?>,          // g  authTokenType -> token
    val userData: HashMap<String, String?>             // h
) : Parcelable {

    constructor(userId: Int, account: Account) :
            this(userId, account.name, null, account.type, null, 0L, HashMap(), HashMap())

    /** Field order is the on-disk format: do not reorder. */
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readLong(),
        readStringMap(parcel),
        readStringMap(parcel)
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(userId)
        dest.writeString(name)
        dest.writeString(previousName)
        dest.writeString(type)
        dest.writeString(password)
        dest.writeLong(lastAuthenticatedTime)
        writeStringMap(dest, authTokens)
        writeStringMap(dest, userData)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<VAccount> {
            override fun createFromParcel(source: Parcel) = VAccount(source)
            override fun newArray(size: Int): Array<VAccount?> = arrayOfNulls(size)
        }

        private fun readStringMap(parcel: Parcel): HashMap<String, String?> {
            val count = parcel.readInt()
            val map = HashMap<String, String?>(count)
            repeat(count) { map[parcel.readString().orEmpty()] = parcel.readString() }
            return map
        }

        private fun writeStringMap(parcel: Parcel, map: Map<String, String?>) {
            parcel.writeInt(map.size)
            for ((key, value) in map) {
                parcel.writeString(key)
                parcel.writeString(value)
            }
        }
    }
}