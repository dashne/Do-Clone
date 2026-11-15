package com.ims134.doclone.repo

import androidx.room3.Room
import com.ims134.doclone.DoApplication.Companion.appContext
import com.ims134.doclone.model.Clone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

object CloneManager {
    val dbName = "DoCloneDb"

    lateinit var dao : CloneStore

    init {
        val db = Room.databaseBuilder(appContext,
            CloneDb::class.java,dbName).build()
        dao = db.dao
    }

    suspend fun addClone(clone: Clone) : Clone?
    {
        return try
        {
            dao.insertClone(clone)
            clone
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            null
        }
    }
    suspend fun updateClone(clone: Clone) : Clone?
    {
        return try
        {
            dao.updateClone(clone)
            clone
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            null
        }
    }
    suspend fun deleteClone(clone: Clone): Boolean
    {
        return try
        {
            dao.deleteClone(clone)
            true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
    }

    suspend fun getClones(): List<Clone>
    {
        return try
        {
            dao.getClones().first()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getClonesFlow(): Flow<List<Clone>>
    {
        return dao.getClones()
    }

    suspend fun getClones(pkg: String) : List<Clone>
    {
        return try
        {
            dao.getClones(pkg).first()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteCloneById(id: Long): Boolean
    {
        return try
        {
            dao.deleteCloneById(id)
            true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
    }

    suspend fun removeAllClones(): Boolean
    {
        return try
        {
            dao.deleteAllClones()
            true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            false
        }
    }

    fun appendInt(i: Int, str: String?): String {
        return "$str:$i"
    }

    fun getIntAfter(str: String?): Int {
        if (str == null) {
            return 0
        }
        val strArrSplit: Array<String?> = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strArrSplit.size == 2) {
            return strArrSplit[1]!!.toInt()
        }
        return 0
    }


}