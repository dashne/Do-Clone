package com.ims134.doclone.io


import android.annotation.TargetApi
import android.os.IInterface
import com.ims134.doclone.reflect.RefStaticMethod


@TargetApi(21)
@Suppress("UNCHECKED_CAST")
class AppWidgetManagerStub : BinderInvocationProxy(
    IAppWidgetService.Stub.asInterface as RefStaticMethod<IInterface>?,
    "appwidget",
) {

    override fun injectMethodProxies() {
        super.injectMethodProxies()

        addMethodProxy(ResultStaticMethodProxy("startListening", IntArray(0)))
        addMethodProxy(ResultStaticMethodProxy("stopListening", 0))
        addMethodProxy(ResultStaticMethodProxy("allocateAppWidgetId", 0))
        addMethodProxy(ResultStaticMethodProxy("deleteAppWidgetId", 0))
        addMethodProxy(ResultStaticMethodProxy("deleteHost", 0))
        addMethodProxy(ResultStaticMethodProxy("deleteAllHosts", 0))
        addMethodProxy(ResultStaticMethodProxy("getAppWidgetViews", null))
        addMethodProxy(ResultStaticMethodProxy("getAppWidgetIdsForHost", null))
        addMethodProxy(ResultStaticMethodProxy("createAppWidgetConfigIntentSender", null))
        addMethodProxy(ResultStaticMethodProxy("updateAppWidgetIds", 0))
        addMethodProxy(ResultStaticMethodProxy("updateAppWidgetOptions", 0))
        addMethodProxy(ResultStaticMethodProxy("getAppWidgetOptions", null))
        addMethodProxy(ResultStaticMethodProxy("partiallyUpdateAppWidgetIds", 0))
        addMethodProxy(ResultStaticMethodProxy("updateAppWidgetProvider", 0))
        addMethodProxy(ResultStaticMethodProxy("notifyAppWidgetViewDataChanged", 0))
        addMethodProxy(ResultStaticMethodProxy("getInstalledProvidersForProfile", null))
        addMethodProxy(ResultStaticMethodProxy("getAppWidgetInfo", null))
        addMethodProxy(ResultStaticMethodProxy("hasBindAppWidgetPermission", false))
        addMethodProxy(ResultStaticMethodProxy("setBindAppWidgetPermission", 0))
        addMethodProxy(ResultStaticMethodProxy("bindAppWidgetId", false))
        addMethodProxy(ResultStaticMethodProxy("bindRemoteViewsService", 0))
        addMethodProxy(ResultStaticMethodProxy("unbindRemoteViewsService", 0))
        addMethodProxy(ResultStaticMethodProxy("getAppWidgetIds", IntArray(0)))
        addMethodProxy(ResultStaticMethodProxy("isBoundWidgetPackage", false))
        addMethodProxy(ResultStaticMethodProxy("isRequestPinAppWidgetSupported", false))
        addMethodProxy(ResultStaticMethodProxy("requestPinAppWidget", false))
    }
}