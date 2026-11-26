# Iris

一个跑脚本的安卓浏览器：内核是 Firefox 的 GeckoView，Consora、SiteFlow 这些脚本装在里面，各有自己的界面，用起来像 App。

现在是**试做版**，只验证内核承不承得住脚本：

1. 页面一开始就注入（油猴的 document-start）
2. 后台请求带上这个站的登录（SiteFlow 的 GM_xmlhttpRequest，anonymous: false）
3. 进到网页里执行代码（SiteFlow 的 pageScript / trustedScript）
4. 开新标签、再关掉（Consora 的 GM_openInTab、window.close）

另外直接在 App 里打开有人机验证的站，看能不能过。

## 安装

每次推送到 main，GitHub Actions 自动编一个 APK，放在 [Releases](../../releases)。手机打开、下载、安装（要允许「安装未知来源应用」）。

`keystore/test.jks` 是试做版专用的签名，公开在这里只是为了每次新版能直接覆盖安装；正式版换成不公开的签名。
