package com.ims134.doclone.reflect

import android.util.Log
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Modifier


class RefClass {
    companion object
    {
        val byConstructor: HashMap<Class<*>?, Constructor<*>?>

        init {
            val map = HashMap<Class<*>?, Constructor<*>?>()
            byConstructor = map
            register(map, RefObject::class.java)
            register(map, RefMethod::class.java)
            register(map, RefStaticInt::class.java)
            register(map, RefStaticMethod::class.java)
            register(map, RefInt::class.java)
            register(map, RefLong::class.java)
            register(map, RefFloat::class.java)
            register(map, RefDouble::class.java)
            register(map, RefBoolean::class.java)
            register(map, RefStaticObject::class.java)
            register(map, RefConstructor::class.java)
//            try {
//                map[RefObject::class.java] =
//                    RefObject::class.java.getConstructor(Class::class.java, Field::class.java)
//                map[RefMethod::class.java] =
//                    RefMethod::class.java.getConstructor(Class::class.java, Field::class.java)
//                map[RefStaticInt::class.java] =
//                    RefStaticInt::class.java.getConstructor(Class::class.java, Field::class.java)
//                map[RefStaticMethod::class.java] =
//                    RefStaticMethod::class.java.getConstructor(Class::class.java, Field::class.java)

                /*
                map.put(
                    ce6::class.java,
                    ce6::class.java.getConstructor(Class::class.java, Field::class.java)
                )
                map.put(
                    de6::class.java,
                    de6::class.java.getConstructor(Class::class.java, Field::class.java)
                )
                map.put(
                    be6::class.java,
                    be6::class.java.getConstructor(Class::class.java, Field::class.java)
                )
                map.put(
                    ae6::class.java,
                    ae6::class.java.getConstructor(Class::class.java, Field::class.java)
                )
                map.put(
                    wd6::class.java,
                    wd6::class.java.getConstructor(Class::class.java, Field::class.java)
                )
                map.put(
                    ie6::class.java,
                    ie6::class.java.getConstructor(Class::class.java, Field::class.java)
                )
                map.put(
                    yd6::class.java,
                    yd6::class.java.getConstructor(Class::class.java, Field::class.java)
                )
                */

//            } catch (unused: Exception) {
//            }
        }

        private fun register(map: HashMap<Class<*>?, Constructor<*>?>, type: Class<*>) {
            try {
                map[type] = type.getConstructor(Class::class.java, Field::class.java)
            } catch (unused: Exception) {
            }
        }


        fun load(cls: Class<*>, str: String): Class<*>? {
            try {
                return load(cls, Class.forName(str))
            } catch (unused: Exception) {
                return null
            }
        }


        fun load(cls: Class<*>, cls2: Class<*>?): Class<*>? {
            var constructor: Constructor<*>? = null
            Log.e("MirrorCheck", "Loading mirror=${cls.name}, target=${cls2?.name}")

            for (field in cls.declaredFields) {
                Log.e(
                    "MirrorCheck",
                    "Processing ${field.name}: " +
                            "static=${Modifier.isStatic(field.modifiers)}, " +
                            "type=${field.type.name}, " +
                            "registered=${byConstructor.containsKey(field.type)}"
                )
                try
                {
                    if (Modifier.isStatic(field.modifiers)) {
                        val constructor = byConstructor[field.type]

                        if (constructor != null) {
                            Log.e("MirrorCheck", "Constructing wrapper for ${field.name}")

                            val wrapper = constructor.newInstance(cls2, field)
                            field.set(null, wrapper)

                            Log.e(
                                "MirrorCheck",
                                "SUCCESS ${field.name}: value=${field.get(null)}"
                            )
                        }
                    }
//                    if (Modifier.isStatic(field.modifiers) && (byConstructor[field.type].also { constructor = it }) != null )
//                    {
//                        field.set(null, constructor!!.newInstance(cls2, field))
//                    }
                }
                catch (unused: Exception)
                {
                }
            }
            return cls2
        }

        @JvmStatic
        fun logMirrorFields(mirrorClass: Class<*>, targetClassName: String) {
            val tag = "MirrorCheck"

            Log.e(tag, "========== ${mirrorClass.name} ==========")

            val targetClass = try {
                Class.forName(targetClassName)
            } catch (e: Throwable) {
                Log.e(tag, "Target class NOT FOUND: $targetClassName", e)
                null
            }

            for (field in mirrorClass.declaredFields) {
                val modifiers = field.modifiers

                Log.e(
                    tag,
                    "Mirror field=${field.name}, " +
                            "type=${field.type.name}, " +
                            "static=${Modifier.isStatic(modifiers)}, " +
                            "final=${Modifier.isFinal(modifiers)}, " +
                            "synthetic=${field.isSynthetic}"
                )

                if (targetClass != null &&
                    byConstructor.containsKey(field.type)
                ) {
                    try {
                        val targetField = targetClass.getDeclaredField(field.name)

                        Log.e(
                            tag,
                            "  Target field=${targetField.name}, " +
                                    "type=${targetField.type.name}, " +
                                    "static=${Modifier.isStatic(targetField.modifiers)}"
                        )
                    } catch (e: Throwable) {
                        Log.e(tag, "  Target field NOT FOUND: ${field.name}", e)
                    }
                }
            }
        }
    }

}