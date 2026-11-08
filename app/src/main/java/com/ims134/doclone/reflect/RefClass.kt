package com.ims134.doclone.reflect

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
            for (field in cls.declaredFields) {
                try {
                    if (Modifier.isStatic(field.modifiers) && (byConstructor[field.type]
                            .also { constructor = it }) != null
                    ) {
                        field.set(null, constructor!!.newInstance(cls2, field))
                    }
                } catch (unused: Exception) {
                }
            }
            return cls2
        }
    }
}