package com.ims134.doclone.io

import android.os.IBinder
import android.os.IInterface
import com.ims134.doclone.reflect.MethodParams
import com.ims134.doclone.reflect.RefClass
import com.ims134.doclone.reflect.RefStaticMethod

// jadx: io.e43 (mirror of android.app.IGrammaticalInflectionManager)
object IGrammaticalInflectionManager {

    @JvmField
    var TYPE: Class<*>? = RefClass.load(IGrammaticalInflectionManager::class.java, "android.app.IGrammaticalInflectionManager")

    // jadx: io.e43$Stub
    object Stub {
        @JvmField
        var TYPE: Class<*>? = RefClass.load(Stub::class.java, "android.app.IGrammaticalInflectionManager\$Stub")

        @field:MethodParams([IBinder::class])
        lateinit var asInterface: RefStaticMethod<IInterface>
    }
}

// jadx: io.ir2. Hooks "grammatical_inflection": the guest always sees "not specified" (0).
class GrammaticalInflectionManagerStub : BinderInvocationProxy(IGrammaticalInflectionManager.Stub.asInterface, "grammatical_inflection") {

    override fun injectMethodProxies() {
        super.injectMethodProxies()
        addMethodProxy(ResultStaticMethodProxy("getSystemGrammaticalGender", 0))
        addMethodProxy(ResultStaticMethodProxy("peekSystemGrammaticalGenderByUserId", 0))
    }
}