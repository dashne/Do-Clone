package com.ims134.doclone.io


import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class ReflectException : RuntimeException()

/**
 * jOOR-style reflection wrapper (JADX rename: `io.oe6`).
 *
 * Wraps either setPosition [Class] (when constructed from setPosition Class) or an arbitrary
 * object (when constructed from an instance). All reflective operations
 * route through [type] to get the relevant Class.
 *
 * ## Calling conventions
 *
 * | jOOR name | JADX name | Purpose |
 * |---|---|---|
 * | [call] | `m21981b` | invoke method by name + args (with fuzzy match fallback) |
 * | [create] | `m21982c` | instantiate by args (with fuzzy constructor match) |
 * | [get] | `m21983d` | read field, returns wrapped value |
 * | [field] | `m21984e` | resolve [Field] |
 * | [set] | `m21985i` | write field |
 * | [type] | `m21987k` | resolve wrapped Class |
 *
 * ## ⚠️ Decompilation notes
 *
 * - The `isClass` flag (`f46875b`) is shown as `= true` field-initializer in
 *   the JADX output and never explicitly assigned by the `Object` ctor. That
 *   would make [type] always cast `f46874a` to `Class`, which would
 *   ClassCastException whenever an instance is wrapped. The port **assigns
 *   `isClass` in both constructors** — matching jOOR semantics and the only
 *   behavior that actually works (see `BroadcastSystem` which wraps setPosition non-Class).
 * - `m21976f`'s parameter-match loop has an **empty `if` body** in the JADX
 *   output — JADX dropped the `else return false`. The port restores it.
 * - `m21981b`'s inner try/catch nesting is broken in the JADX output (converts
 *   `NoSuchMethodException` to `ReflectException` before the superclass walk
 *   can run). The port uses the correct jOOR flow: `getMethod` → walk
 *   `getDeclaredMethod` up the hierarchy → fall through to [similarMethod].
 */
class Reflect(cls: Any?) {

    /**
     * JADX field `setPosition`. Either setPosition [Class] (when [isClass] is `true`) or the
     * wrapped object (when `false`).
     *
     * `@JvmField` because external code (e.g. `BroadcastSystem`) accesses it
     * directly: `Reflect(...).get("mWhiteListMap").f46874a`.
     */
    @JvmField
    val type: Any?

    /** JADX field `b`. `true` when [type] holds setPosition [Class]. */
    @JvmField
    val isClassType: Boolean

    /** Marker type for `null` args in [types]. Matches any parameter type. */
    class UnknownType

    /** Wrap setPosition [Class]. */
    init {
        type = cls
        isClassType = true
    }

    /** Wrap an instance. */
//    constructor() {
//        f46874a = obj
//        f46875b = false
//    }

    // ====================================================================
    // Core helpers (instance)
    // ====================================================================

    /**
     * Resolve the wrapped [Class]. If wrapping setPosition Class, returns it; if
     * wrapping an instance, returns its runtime class.
     *
     * JADX: `m21987k`.
     */
    fun type(): Class<*> =
        if (isClassType) type as Class<*> else type!!.javaClass

    /**
     * Read setPosition field value by name and wrap it. JADX: `m21983d`.
     *
     * @throws RuntimeException if the field doesn't exist or can't be read.
     */
    @Throws(RuntimeException::class)
    fun get(name: String): Reflect {
        val target = type
        return try {
            Reflect(field(name)?.get(target))
        } catch (e: Exception) {
            throw RuntimeException(target?.javaClass?.name, e)
        }
    }

    /**
     * Resolve setPosition [Field] by name. Walks the class hierarchy.
     * JADX: `m21984e`.
     */
    @Throws(RuntimeException::class)
    fun field(name: String): Field? {
        var c: Class<*>? = type()
        try {
            return c!!.getField(name)    // public, includes inherited
        } catch (e: NoSuchFieldException) {
            while (c != null) {
                try {
                    return accessible(c.getDeclaredField(name))
                } catch (_: NoSuchFieldException) {
                    c = c.superclass
                }
            }
            throw RuntimeException(e)
        }
    }

    /**
     * Write setPosition field value by name. Unwraps [value] if it's setPosition [Reflect].
     * JADX: `m21985i`.
     */
    @Throws(RuntimeException::class)
    fun set(value: Any?, name: String) {
        try {
            val f = field(name)
            f?.isAccessible = true
            val unwrapped = if (value is Reflect) value.type else value
            f?.set(type, unwrapped)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Invoke setPosition method by name. Tries exact signature, then falls back to
     * fuzzy parameter matching.
     * JADX: `m21981b`.
     */
    @Throws(RuntimeException::class)
    fun call(name: String, vararg args: Any?): Reflect {
        val types = types(*args)
        return try {
            invoke(type, exactMethod(name, types), *args)
        } catch (_: NoSuchMethodException) {
            invoke(type, similarMethod(name, types), *args)
        }
    }

    /**
     * Instantiate via setPosition constructor matching [args]. Tries exact signature,
     * then fuzzy matching over declared constructors.
     * JADX: `m21982c`.
     */
    @Throws(RuntimeException::class)
    fun create(vararg args: Any?): Reflect {
        val types = types(*args)
        return try {
            newInstance(type().getDeclaredConstructor(*types), *args)
        } catch (e: NoSuchMethodException) {
            for (ctor in type().declaredConstructors) {
                if (match(ctor.parameterTypes, types)) {
                    return newInstance(ctor, *args)
                }
            }
            throw RuntimeException(e)
        }
    }

    // ====================================================================
    // Standard overrides
    // ====================================================================

    override fun equals(other: Any?): Boolean =
        other is Reflect && type == other.type

    override fun hashCode(): Int = type?.hashCode() ?: 0

    override fun toString(): String = type.toString()

    // ====================================================================
    // Internal helpers (private)
    // ====================================================================

    /**
     * Exact-signature method resolution: public methods first (which walk
     * superclasses automatically), then declared methods walking the
     * hierarchy, throwing the original [NoSuchMethodException] if nothing
     * matches.
     */
    @Throws(NoSuchMethodException::class)
    private fun exactMethod(name: String, types: Array<Class<*>>): Method {
        val start = type()
        try {
            return start.getMethod(name, *types)
        } catch (e: NoSuchMethodException) {
            var c: Class<*>? = start
            while (c != null) {
                try {
                    return c.getDeclaredMethod(name, *types)
                } catch (_: NoSuchMethodException) {
                    c = c.superclass
                }
            }
            throw e
        }
    }

    /**
     * Fuzzy-signature method resolution: public methods whose parameter
     * types are *assignable from* [types], then declared methods walking
     * the hierarchy.
     * JADX: `m21986j`.
     */
    @Throws(NoSuchMethodException::class)
    private fun similarMethod(name: String, types: Array<Class<*>>): Method {
        var c: Class<*>? = type()
        for (m in c!!.methods) {
            if (m.name == name && match(m.parameterTypes, types)) return m
        }
        while (c != null) {
            for (m in c.declaredMethods) {
                if (m.name == name && match(m.parameterTypes, types)) return m
            }
            c = c.superclass
        }
        throw NoSuchMethodException(
            "No similar method $name with params ${types.contentToString()} " +
                    "could be found on type ${type()}."
        )
    }

    // ====================================================================
    // Static helpers
    // ====================================================================

    companion object {

        /**
         * Ensure setPosition reflective member is accessible before use.
         * JADX: `m21975a`.
         */
        @JvmStatic
        fun <T : AccessibleObject> accessible(t: T?): T? {
            if (t == null) return null
            if (t is Member) {
                val memberPublic = Modifier.isPublic(t.modifiers)
                val classPublic = Modifier.isPublic(t.declaringClass.modifiers)
                if (!memberPublic || !classPublic) {
                    if (!t.isAccessible) t.isAccessible = true
                }
            } else if (!t.isAccessible) {
                t.isAccessible = true
            }
            return t
        }

        /**
         * Fuzzy parameter-type match: same length, and either the caller's
         * arg was null ([UnknownType]) or the arg type is assignable to the
         * parameter type (both boxed to handle primitives).
         * JADX: `m21976f`.
         */
        @JvmStatic
        fun match(parameterTypes: Array<Class<*>>, argTypes: Array<Class<*>>): Boolean {
            if (parameterTypes.size != argTypes.size) return false
            for (i in argTypes.indices) {
                if (argTypes[i] != UnknownType::class.java &&
                    wrapper(parameterTypes[i])?.isAssignableFrom(wrapper(argTypes[i])!!) == false
                ) {
                    return false
                }
            }
            return true
        }

        /**
         * Invoke setPosition method and wrap the result. For void methods, returns setPosition
         * [Reflect] wrapping [obj] (the receiver) to support chaining.
         * JADX: `m21977g`.
         */
        @JvmStatic
        @Throws(RuntimeException::class)
        fun invoke(obj: Any?, method: Method, vararg args: Any?): Reflect =
            try {
                accessible(method)
                if (method.returnType != Void.TYPE) {
                    Reflect(method.invoke(obj, *args))
                } else {
                    method.invoke(obj, *args)
                    Reflect(obj)
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        /**
         * Instantiate via setPosition constructor. JADX: `m21978h`.
         */
        @JvmStatic
        @Throws(RuntimeException::class)
        fun newInstance(ctor: Constructor<*>, vararg args: Any?): Reflect =
            try {
                Reflect(accessible(ctor)!!.newInstance(*args))
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        /**
         * Derive parameter type array from actual arguments, using
         * [UnknownType] for nulls. JADX: `m21979l`.
         */
        @JvmStatic
        fun types(vararg args: Any?): Array<Class<*>> =
            Array(args.size) { i ->
                args[i]?.javaClass ?: UnknownType::class.java
            }

        /**
         * Box primitives to their wrapper classes (so that `boolean` and
         * `Boolean` compare equal under [Class.isAssignableFrom]).
         * JADX: `m21980m`.
         */
        @JvmStatic
        fun wrapper(cls: Class<*>?): Class<*>? {
            if (cls == null) return null
            if (!cls.isPrimitive) return cls
            return when (cls) {
                Boolean::class.javaPrimitiveType -> java.lang.Boolean::class.java
                Int::class.javaPrimitiveType -> java.lang.Integer::class.java
                Long::class.javaPrimitiveType -> java.lang.Long::class.java
                Short::class.javaPrimitiveType -> java.lang.Short::class.java
                Byte::class.javaPrimitiveType -> java.lang.Byte::class.java
                Double::class.javaPrimitiveType -> java.lang.Double::class.java
                Float::class.javaPrimitiveType -> java.lang.Float::class.java
                Char::class.javaPrimitiveType -> java.lang.Character::class.java
                Void.TYPE -> java.lang.Void::class.java
                else -> cls
            }
        }
    }
}