package com.ims134.doclone.reflect

import android.util.Log
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class MethodParams(val value: Array<KClass<*>>)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class MethodReflectParams(val value: Array<String>)

/**
 * Binds setPosition static field of setPosition mirror class to the static method of the same name
 * on the real (hidden) class.
 */
class RefStaticMethod<T> @Throws(NoSuchMethodException::class) constructor(cls: Class<*>, field: Field)
{


    @JvmField
    val method: Method = findMethod(cls, field)

    @Suppress("UNCHECKED_CAST")
    fun call(vararg args: Any?): T? =
        try {
            method.invoke(null, *args) as T?
        } catch (unused: Exception) {
            null
        }

    @Suppress("UNCHECKED_CAST")
    @Throws(Throwable::class)
    fun callWithException(vararg args: Any?): T? =
        try {
            method.invoke(null, *args) as T?
        } catch (e: InvocationTargetException) {
            throw e.cause ?: e
        }

    companion object {

        @JvmStatic
        fun primitiveClass(name: String): Class<*>? = when (name) {
            "int" -> Int::class.javaPrimitiveType
            "long" -> Long::class.javaPrimitiveType
            "boolean" -> Boolean::class.javaPrimitiveType
            "byte" -> Byte::class.javaPrimitiveType
            "short" -> Short::class.javaPrimitiveType
            "char" -> Char::class.javaPrimitiveType
            "float" -> Float::class.javaPrimitiveType
            "double" -> Double::class.javaPrimitiveType
            "void" -> Void.TYPE
            else -> null
        }

        private fun findMethod(cls: Class<*>, field: Field): Method {
            val methodParams = field.getAnnotation(MethodParams::class.java)
            val reflectParams = field.getAnnotation(MethodReflectParams::class.java)

            return when {
                methodParams != null -> {
                    // KClass -> Class; mirror classes are swapped for the real class held in their TYPE field
                    val types: Array<Class<*>> = Array(methodParams.value.size) { methodParams.value[it].java }
                    val loader = RefStaticMethod::class.java.classLoader
                    for (i in types.indices) {
                        val type = types[i]
                        if (type.classLoader === loader) {
                            try {
                                Class.forName(type.name)
                                types[i] = type.getField("TYPE").get(null) as Class<*>
                            } catch (th: Throwable) {
                                throw RuntimeException(th)
                            }
                        }
                    }
                    cls.getDeclaredMethod(field.name, *types).also { it.isAccessible = true }
                }

                reflectParams != null -> {
                    val names = reflectParams.value
                    val types = arrayOfNulls<Class<*>>(names.size)
                    val altTypes = arrayOfNulls<Class<*>>(names.size)
                    var hasHashSet = false
                    for (i in names.indices) {
                        val name = names[i]
                        var type: Class<*>? = primitiveClass(name)
                        if (type == null) {
                            try {
                                type = Class.forName(name)
                            } catch (unused: ClassNotFoundException) {
                            }
                        }
                        types[i] = type
                        if (name == "java.util.HashSet") {
                            // some Android versions use ArraySet where HashSet is declared
                            val arraySet = try {
                                Class.forName("android.util.ArraySet")
                            } catch (unused: ClassNotFoundException) {
                                type
                            }
                            altTypes[i] = arraySet ?: type
                            hasHashSet = true
                        } else {
                            altTypes[i] = type
                        }
                    }
                    val found = try {
                        cls.getDeclaredMethod(field.name, *types)
                    } catch (unused: Exception) {
                        if (!hasHashSet) throw NoSuchMethodException(field.name)
                        cls.getDeclaredMethod(field.name, *altTypes)
                    }
                    found.also { it.isAccessible = true }
                }

//                else -> cls.declaredMethods
//                    .firstOrNull { it.name == field.name }
//                    ?.also { it.isAccessible = true }
//                    ?: throw NoSuchMethodException(field.name)
                else -> {
                    // by-name first: fresh hidden-API check (honors unseal);
                    // getDeclaredMethods() returns a cached/filtered array that can omit getDefault
                    val m = try {
                        cls.getDeclaredMethod(field.name)
                    } catch (_: NoSuchMethodException) {
                        cls.declaredMethods.firstOrNull { it.name == field.name }
                    }
                    m?.also { it.isAccessible = true } ?: throw NoSuchMethodException(field.name)
                }
            }
        }
    }
}

class RefObject<T>(cls: Class<*>, f: Field)
{
    var field: Field? = null

    init {
        this.field = cls.getDeclaredField(f.name).also { it.isAccessible=true }
    }

    @Suppress("UNCHECKED_CAST")
    fun get(obj: Any?): T? {
        return try {
            this.field!!.get(obj) as T?
        } catch (e: Exception) {
            Log.d("RefObject", "Reflect ${e.message}")
            null
        }
    }

    operator fun set(obj: Any?, t: T?) {
        try {
            this.field!!.set(obj, t)
        } catch (e: Exception) {
            Log.d("RefObject", "Reflect ${e.message}")
        }
    }
}

class RefMethod<T>(cls: Class<*>, field: Field) {

    var method: Method? = null

    init {
        val methodParams = field.getAnnotation(MethodParams::class.java)
        val reflectParams = field.getAnnotation(MethodReflectParams::class.java)

        if (methodParams != null) {
            val types: Array<Class<*>> = Array(methodParams.value.size) { methodParams.value[it].java }
            val loader = RefMethod::class.java.classLoader
            for (i in types.indices) {
                val type = types[i]
                if (type.classLoader === loader) {
                    try {
                        Class.forName(type.name)
                        types[i] = type.getField("TYPE").get(null) as Class<*>
                    } catch (th: Throwable) {
                        throw RuntimeException(th)
                    }
                }
            }
            method = cls.getDeclaredMethod(field.name, *types).also { it.isAccessible = true }
        } else if (reflectParams != null) {
            val names = reflectParams.value
            val types = arrayOfNulls<Class<*>>(names.size)
            for (i in names.indices) {
                var type: Class<*>? = RefStaticMethod.primitiveClass(names[i])
                if (type == null) {
                    try {
                        type = Class.forName(names[i])
                    } catch (unused: ClassNotFoundException) {
                    }
                }
                types[i] = type
            }
            method = cls.getDeclaredMethod(field.name, *types).also { it.isAccessible = true }
        } else {
            method = cls.declaredMethods
                .firstOrNull { it.name == field.name }
                ?.also { it.isAccessible = true }
        }
        if (method == null) {
            throw NoSuchMethodException(field.name)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun call(obj: Any?, vararg objArr: Any?): T? {
        try {
            return this.method?.invoke(obj, *objArr) as T?
        } catch (e: InvocationTargetException) {
            Log.d("PLIB", "Caught method call InvocationTargetException")
            Log.d("PLIB", Log.getStackTraceString(e.cause ?: e))
            return null
        } catch (th: Throwable) {
            Log.d("PLIB", "Caught method call exception")
            Log.d("PLIB", Log.getStackTraceString(th.cause ?: th))
            return null
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Throwable::class)
    fun callWithException(obj: Any?, vararg objArr: Any?): T? {
        try {
            return this.method?.invoke(obj, *objArr) as T?
        } catch (e: InvocationTargetException) {
            throw e.cause ?: e
        }
    }

    fun paramList(): Array<out Class<*>?>? = this.method?.parameterTypes
}

/** Instance int field accessor (was ce6). */
class RefInt @Throws(NoSuchFieldException::class) constructor(cls: Class<*>, mirrorField: Field) {

    val field: Field = cls.getDeclaredField(mirrorField.name).also { it.isAccessible = true }

    fun get(obj: Any?): Int =
        try {
            field.getInt(obj)
        } catch (unused: Exception) {
            0
        }

    fun set(obj: Any?, value: Int) {
        try {
            field.setInt(obj, value)
        } catch (unused: Exception) {
        }
    }
}

/** Instance long field accessor (was de6). */
class RefLong @Throws(NoSuchFieldException::class) constructor(cls: Class<*>, mirrorField: Field) {

    val field: Field = cls.getDeclaredField(mirrorField.name).also { it.isAccessible = true }

    fun get(obj: Any?): Long =
        try {
            field.getLong(obj)
        } catch (unused: Exception) {
            0L
        }

    fun set(obj: Any?, value: Long) {
        try {
            field.setLong(obj, value)
        } catch (unused: Exception) {
        }
    }
}

/** Instance float field accessor (was be6). */
class RefFloat @Throws(NoSuchFieldException::class) constructor(cls: Class<*>, mirrorField: Field) {

    val field: Field = cls.getDeclaredField(mirrorField.name).also { it.isAccessible = true }

    fun get(obj: Any?): Float =
        try {
            field.getFloat(obj)
        } catch (unused: Exception) {
            0.0f
        }

    fun set(obj: Any?, value: Float) {
        try {
            field.setFloat(obj, value)
        } catch (unused: Exception) {
        }
    }
}

/** Instance double field accessor (was ae6). */
class RefDouble @Throws(NoSuchFieldException::class) constructor(cls: Class<*>, mirrorField: Field) {

    val field: Field = cls.getDeclaredField(mirrorField.name).also { it.isAccessible = true }

    fun get(obj: Any?): Double =
        try {
            field.getDouble(obj)
        } catch (unused: Exception) {
            0.0
        }

    fun set(obj: Any?, value: Double) {
        try {
            field.setDouble(obj, value)
        } catch (unused: Exception) {
        }
    }
}

/** Instance boolean field accessor (was wd6). */
class RefBoolean @Throws(NoSuchFieldException::class) constructor(cls: Class<*>, mirrorField: Field) {

    val field: Field = cls.getDeclaredField(mirrorField.name).also { it.isAccessible = true }

    fun get(obj: Any?): Boolean =
        try {
            field.getBoolean(obj)
        } catch (unused: Exception) {
            false
        }

    fun set(obj: Any?, value: Boolean) {
        try {
            field.setBoolean(obj, value)
        } catch (unused: Exception) {
        }
    }
}

/** Static object field accessor (was ie6). */
class RefStaticObject<T> @Throws(NoSuchFieldException::class) constructor(cls: Class<*>, mirrorField: Field) {

    val field: Field = cls.getDeclaredField(mirrorField.name).also { it.isAccessible = true }

    @Suppress("UNCHECKED_CAST")
    fun get(): T? =
        try {
            field.get(null) as T?
        } catch (unused: Exception) {
            null
        }

    fun set(value: T?) {
        try {
            field.set(null, value)
        } catch (unused: Exception) {
        }
    }

    fun type(): Class<*> = field.type
}

/** Constructor accessor (was yd6). */
class RefConstructor<T> @Throws(NoSuchMethodException::class) constructor(cls: Class<*>, mirrorField: Field) {

    val ctor: Constructor<*>

    init {
        val methodParams = mirrorField.getAnnotation(MethodParams::class.java)
        val reflectParams = mirrorField.getAnnotation(MethodReflectParams::class.java)

        ctor = when {
            methodParams != null -> {
                val types: Array<Class<*>> = Array(methodParams.value.size) { methodParams.value[it].java }
                cls.getDeclaredConstructor(*types)
            }

            reflectParams != null -> {
                val names = reflectParams.value
                val types = arrayOfNulls<Class<*>>(names.size)
                for (i in names.indices) {
                    try {
                        types[i] = RefStaticMethod.primitiveClass(names[i]) ?: Class.forName(names[i])
                    } catch (unused: Exception) {
                    }
                }
                cls.getDeclaredConstructor(*types)
            }

            else -> cls.getDeclaredConstructor()
        }
        ctor.isAccessible = true
    }

    @Suppress("UNCHECKED_CAST")
    fun newInstance(): T? =
        try {
            ctor.newInstance() as T?
        } catch (unused: Exception) {
            null
        }

    @Suppress("UNCHECKED_CAST")
    fun newInstance(vararg args: Any?): T? =
        try {
            ctor.newInstance(*args) as T?
        } catch (unused: Exception) {
            null
        }
}

class RefStaticInt @Throws(NoSuchFieldException::class) constructor(cls: Class<*>, mirrorField: Field) {

    val field: Field = cls.getDeclaredField(mirrorField.name).also { it.isAccessible = true }

    fun get(): Int = try {
        field.getInt(null)
    } catch (unused: Exception) {
        0
    }

    fun set(i: Int) {
        try {
            field.setInt(null, i)
        } catch (unused: Exception) {
        }
    }
}