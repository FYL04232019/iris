'use strict';
// Runs at the start of every page (as Consora does). Puts a panel on top with four checks.
(() => {
  if (window.top !== window) return;
  const startedLoading = document.readyState === 'loading';
  const isTestTab = location.hash === '#iris-tab-test';
  const results = {};

  const show = () => {
    let host = document.getElementById('iris-test');
    if (!host) {
      host = document.createElement('div');
      host.id = 'iris-test';
      host.style.cssText = 'all:initial;position:fixed;left:8px;right:8px;top:8px;z-index:2147483647;';
      (document.body || document.documentElement).appendChild(host);
      host.attachShadow({ mode: 'open' });
    }
    const root = host.shadowRoot;
    const row = (k, label) => {
      const r = results[k];
      const mark = !r ? '…' : r.ok ? '✓' : '✗';
      const color = !r ? '#676a74' : r.ok ? '#0b6b2e' : '#962c2f';
      return '<div class="row"><b style="color:' + color + '">' + mark + '</b><span><span class="l">' + label + '</span>' +
        (r ? '<span class="d">' + escape(r.text) + '</span>' : '') + '</span></div>';
    };
    root.innerHTML = '<style>.box{font:13px/1.45 system-ui,sans-serif;color:#2d3039;background:#fff;border-radius:16px;padding:10px 12px;' +
      'box-shadow:0 4px 16px rgba(0,0,0,.18)}.h{display:flex;justify-content:space-between;align-items:center;font-weight:600;margin-bottom:4px}' +
      '.row{display:flex;gap:8px;margin:4px 0}.row b{width:14px;flex:none}.l{display:block}.d{display:block;color:#676a74;font-size:12px;word-break:break-all}' +
      'input{font:13px system-ui;width:100%;box-sizing:border-box;border:1px solid #cdd3e0;border-radius:8px;padding:6px 8px;margin:4px 0}' +
      'button{font:13px system-ui;border:0;border-radius:16px;padding:6px 12px;background:#e3e8f7;color:#2d3039;margin-right:8px}</style>' +
      '<div class="box"><div class="h"><span>Iris 测试' + (isTestTab ? '（新标签）' : '') + '</span><button id="x">收起</button></div>' +
      row('inject', '① 页面开头注入') +
      row('fetch', '② 后台请求带不带登录') +
      '<input id="w" placeholder="填一个登录后才看得到的字，比如你的用户名，再点重测 ②">' +
      row('page', '③ 进到网页里执行代码') +
      row('tab', '④ 开新标签再关掉') +
      '<button id="f">重测 ②</button><button id="t">测 ④</button></div>';
    root.getElementById('x').onclick = () => { host.remove(); };
    root.getElementById('f').onclick = () => testFetch(root.getElementById('w').value.trim());
    root.getElementById('t').onclick = testTab;
  };
  const escape = (s) => String(s).replace(/[&<>]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' }[c]));
  const set = (k, ok, text) => { results[k] = { ok, text }; show(); };

  // ① At the start: the page had not been parsed yet when this ran.
  results.inject = { ok: startedLoading, text: startedLoading ? '页面还在加载时就注入了（和油猴 document-start 一样）' : '注入时页面已经是 ' + document.readyState };

  // ② The extension asks this address with the site's cookies; compared with what the page shows.
  async function testFetch(word) {
    set('fetch', null, '请求中…');
    results.fetch = null;
    const r = await browser.runtime.sendMessage({ kind: 'fetch', url: location.href.split('#')[0], word: word || '' });
    if (!r || !r.ok) return set('fetch', false, '请求失败：' + (r && r.error));
    const pageLen = document.documentElement.outerHTML.length;
    let text = '状态 ' + r.status + '，' + r.length + ' 字（页面 ' + pageLen + ' 字），标题「' + r.title + '」';
    if (word) text += r.found ? '；里面有「' + word + '」→ 带上了登录' : '；里面没有「' + word + '」→ 没带上登录';
    else text += '；填一个只有登录后才有的字再测，才能判断';
    set('fetch', word ? r.found : true, text);
  }

  // ③ Inside the page: read the page's own variable, and run code there and here (SiteFlow's pageScript / trustedScript).
  function testPage() {
    const out = [];
    let ok = true;
    try {
      const page = window.wrappedJSObject;
      out.push('读网页的 document.title：' + (page.document.title || '(空)'));
    } catch (e) { ok = false; out.push('读网页变量失败：' + e.message); }
    try {
      const s = document.createElement('script');
      s.textContent = 'window.__irisProbe = 6 * 7;';
      (document.head || document.documentElement).appendChild(s);
      s.remove();
      const v = window.wrappedJSObject.__irisProbe;
      out.push(v === 42 ? '往网页里插 <script> 执行：成功' : '往网页里插 <script>：没执行（多半是网站的 CSP 不许）');
    } catch (e) { out.push('插 <script> 出错：' + e.message); }
    try {
      const v = window.wrappedJSObject.eval('6 * 7');
      out.push(v === 42 ? '在网页里 eval：成功' : 'eval 结果不对：' + v);
    } catch (e) { out.push('在网页里 eval：被拦（' + e.message + '）'); }
    try {
      const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
      new AsyncFunction('return 6 * 7')().then((v) => {
        out.push(v === 42 ? '脚本自己执行一段代码（trustedScript 那种）：成功' : '结果不对：' + v);
        set('page', ok, out.join('；'));
      }, (e) => { out.push('脚本自己执行代码：失败（' + e.message + '）'); set('page', false, out.join('；')); });
    } catch (e) { out.push('脚本自己执行代码：被拦（' + e.message + '）'); set('page', false, out.join('；')); }
  }

  // ④ A new tab with this page, closed again after 2.5 s.
  async function testTab() {
    set('tab', null, '开新标签中…（2.5 秒后自己关掉）');
    results.tab = null;
    const r = await browser.runtime.sendMessage({ kind: 'tab', url: location.href.split('#')[0] + '#iris-tab-test' });
    set('tab', !!(r && r.ok), r && r.ok ? '开了又关了（标签 ' + r.id + '）' : '失败：' + (r && r.error));
  }

  const start = () => {
    show();
    if (isTestTab) return;
    testFetch('');
    testPage();
  };
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start, { once: true });
  else start();
})();
