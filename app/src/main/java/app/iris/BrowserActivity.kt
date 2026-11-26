package app.iris

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

/**
 * One screen for the tabs: the address on top (with how many tabs are open), the page under it.
 * The tab on screen is the last one (Tabs). ‹ goes back in it; at its start, closes it.
 */
class BrowserActivity : Activity() {
    private lateinit var view: GeckoView
    private lateinit var address: EditText
    private lateinit var count: TextView
    private var shown: GeckoSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val c = this
        val root = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Ui.BG) }
        val bar = LinearLayout(c).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(Ui.dp(c, 12), Ui.dp(c, 8), Ui.dp(c, 12), Ui.dp(c, 8))
        }
        address = EditText(c).apply {
            setSingleLine()
            textSize = 14f
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO
            background = Ui.round(c, Ui.CARD, 18)
            setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0)
            setOnEditorActionListener { _, _, _ ->
                var u = text.toString().trim()
                if (!u.contains("://")) u = "https://$u"
                Tabs.current?.loadUri(u)
                clearFocus()
                true
            }
        }
        bar.addView(address, LinearLayout.LayoutParams(0, Ui.dp(c, 36), 1f))
        count = Ui.text(c, "1", 12f, Ui.FG, bold = true).apply {
            gravity = android.view.Gravity.CENTER
            background = Ui.round(c, Ui.CARD, 18)
        }
        bar.addView(count, LinearLayout.LayoutParams(Ui.dp(c, 36), Ui.dp(c, 36)).apply { leftMargin = Ui.dp(c, 12) })
        root.addView(bar)
        view = GeckoView(c)
        root.addView(view, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)

        Tabs.onChange = { runOnUiThread { show() } }
        val url = intent.getStringExtra("url") ?: "https://"
        // The extension first, so its content script is there from the first page on.
        IrisApp.instance.withTestExtension {
            if (Tabs.list.isEmpty()) Tabs.open(url) else show()
        }
    }

    private fun show() {
        val s = Tabs.current
        if (s == null) { finish(); return }
        count.text = Tabs.list.size.toString()
        if (s === shown) return
        shown = s
        s.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) { backable[session] = canGoBack }
        }
        s.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                if (session === Tabs.current) runOnUiThread { address.setText(url) }
            }
        }
        view.releaseSession()
        view.setSession(s)
    }

    private val backable = java.util.WeakHashMap<GeckoSession, Boolean>()

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val s = Tabs.current ?: return super.onBackPressed()
        if (backable[s] == true) s.goBack() else Tabs.close(s)
    }

    override fun onDestroy() {
        if (isFinishing) { Tabs.onChange = null; view.releaseSession(); Tabs.closeAll() }
        super.onDestroy()
    }
}
