package app.iris

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout

/**
 * The home screen: the scripts, one row each (for now only the test), and an address to open.
 * Later each script (Consora, SiteFlow…) is a row here, opening its own screen.
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val c = this
        val pad = Ui.dp(c, 12)
        val root = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.BG)
            setPadding(pad, Ui.dp(c, 24), pad, pad)
        }
        root.addView(Ui.text(c, "Iris", 22f, bold = true))
        root.addView(Ui.text(c, "试做版：验证 GeckoView 内核能不能承载脚本", 12f, Ui.MUTED).apply { setPadding(0, Ui.dp(c, 4), 0, Ui.dp(c, 16)) })

        val url = EditText(c).apply {
            hint = "要测的网址，比如你的签到站"
            setText(getSharedPreferences("iris", MODE_PRIVATE).getString("lastUrl", "https://"))
            setSingleLine()
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO
            background = Ui.round(c, Ui.CARD, 12)
            setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0)
            minHeight = Ui.dp(c, 48)
        }
        root.addView(url, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(c, 48)))

        val open = {
            var u = url.text.toString().trim()
            if (!u.contains("://")) u = "https://$u"
            getSharedPreferences("iris", MODE_PRIVATE).edit().putString("lastUrl", u).apply()
            startActivity(Intent(c, BrowserActivity::class.java).putExtra("url", u))
        }
        url.setOnEditorActionListener { _, _, _ -> open(); true }

        val row = Ui.text(c, "测试 · 打开这个网址并跑四项检查", 14f, Ui.CARD, bold = true).apply {
            background = Ui.round(c, Ui.ACCENT, 24)
            setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0)
            setOnClickListener { open() }
        }
        root.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(c, 48)).apply { topMargin = Ui.dp(c, 12) })

        root.addView(Ui.text(c, "页面顶上会出一块结果：①页面开头注入 ②后台请求带不带登录 ③进到网页里执行代码 ④开新标签再关掉。人机验证直接在页面里过，看能不能过。截图发回来。", 12f, Ui.MUTED).apply { setPadding(0, Ui.dp(c, 16), 0, 0) })
        val outer = android.widget.FrameLayout(c).apply { setBackgroundColor(Ui.BG); addView(root) }
        Ui.keepOutOfBars(outer)
        setContentView(outer)

        IrisApp.instance.withTestExtension { }
    }
}
