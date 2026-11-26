package app.iris

import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebExtension

/**
 * Tabs, as the app keeps them: GeckoView has none of its own. A tab is a GeckoSession; the one on
 * screen is the last one. An extension's tabs.create opens one on top (shown at once), tabs.remove
 * closes it and the one under it comes back. One screen shows them (BrowserActivity).
 */
object Tabs {
    val list = mutableListOf<GeckoSession>()
    var onChange: (() -> Unit)? = null

    val current: GeckoSession? get() = list.lastOrNull()

    fun open(url: String?): GeckoSession {
        val session = GeckoSession()
        session.open(IrisApp.instance.runtime)
        IrisApp.instance.testExtension?.let { watchClose(session, it) }
        list += session
        if (url != null) session.loadUri(url)
        onChange?.invoke()
        return session
    }

    fun close(session: GeckoSession) {
        list.remove(session)
        session.close()
        onChange?.invoke()
    }

    fun closeAll() {
        list.toList().forEach { it.close() }
        list.clear()
    }

    /** What the extension may do with tabs: open one (tabs.create), close one (tabs.remove). */
    fun attach(ext: WebExtension) {
        ext.setTabDelegate(object : WebExtension.TabDelegate {
            override fun onNewTab(source: WebExtension, createDetails: WebExtension.CreateTabDetails): GeckoResult<GeckoSession>? {
                val session = GeckoSession()
                session.open(IrisApp.instance.runtime)
                watchClose(session, ext)
                list += session
                onChange?.invoke()
                // The extension loads its URL itself once it has the session.
                return GeckoResult.fromValue(session)
            }
        })
        list.forEach { watchClose(it, ext) }
    }

    private fun watchClose(session: GeckoSession, ext: WebExtension) {
        session.webExtensionController.setTabDelegate(ext, object : WebExtension.SessionTabDelegate {
            override fun onCloseTab(source: WebExtension?, session: GeckoSession): GeckoResult<AllowOrDeny> {
                close(session)
                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
            }
        })
    }
}
