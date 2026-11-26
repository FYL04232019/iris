package app.iris

import android.app.Application
import android.util.Log
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.WebExtension

/**
 * One Gecko engine for the whole app (GeckoView allows only one), and the test extension built in.
 * The extension is what the scripts will become: content scripts at the start of each page and a
 * background page, as Tampermonkey gives them today.
 */
class IrisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    val runtime: GeckoRuntime by lazy { GeckoRuntime.create(this) }

    /** The built-in test extension, once it is installed (null until then). */
    var testExtension: WebExtension? = null
        private set

    private val waiting = mutableListOf<(WebExtension) -> Unit>()

    fun withTestExtension(use: (WebExtension) -> Unit) {
        testExtension?.let { use(it); return }
        waiting += use
        if (waiting.size > 1) return
        runtime.webExtensionController
            .ensureBuiltIn("resource://android/assets/iristest/", "test@iris.app")
            .accept({ ext ->
                if (ext == null) return@accept
                testExtension = ext
                Tabs.attach(ext)
                waiting.toList().forEach { it(ext) }
                waiting.clear()
            }, { e -> Log.e("Iris", "test extension not installed", e) })
    }

    companion object {
        lateinit var instance: IrisApp
            private set
    }
}
