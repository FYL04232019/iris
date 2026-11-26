'use strict';
// The background page: what Tampermonkey's GM_xmlhttpRequest and GM_openInTab do today.
browser.runtime.onMessage.addListener(async (msg, sender) => {
  if (msg.kind === 'fetch') {
    // SiteFlow asks a site with the login it has (anonymous: false). Here: the same address the
    // page has, with its cookies, from the extension (not from the page).
    try {
      const r = await fetch(msg.url, { credentials: 'include', cache: 'no-store', redirect: 'follow' });
      const text = await r.text();
      const title = (text.match(/<title[^>]*>([\s\S]*?)<\/title>/i) || [])[1] || '';
      return { ok: true, status: r.status, length: text.length, title: title.trim().slice(0, 80),
        found: msg.word ? text.includes(msg.word) : null, finalUrl: r.url };
    } catch (e) {
      return { ok: false, error: String(e && e.message || e) };
    }
  }
  if (msg.kind === 'tab') {
    // Consora opens a tab for a task and closes it when done.
    try {
      const timeout = (ms, what) => new Promise((_, no) => setTimeout(() => no(new Error(what + ' 超过 ' + ms / 1000 + ' 秒没回应')), ms));
      const tab = await Promise.race([browser.tabs.create({ url: msg.url, active: true }), timeout(8000, 'tabs.create')]);
      await new Promise((r) => setTimeout(r, 2500));
      await Promise.race([browser.tabs.remove(tab.id), timeout(5000, 'tabs.remove')]);
      return { ok: true, id: tab.id };
    } catch (e) {
      return { ok: false, error: String(e && e.message || e) };
    }
  }
  return null;
});
