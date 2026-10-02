/*
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
// Runs in the page through WebDriver (body of a function: arguments[0] = max items).
// Framework-agnostic: what counts is what the browser renders (computed style, geometry, hit testing,
// ARIA), never the name of a framework, a vendor or a site. Open shadow roots and same-origin frames are
// walked; cross-origin frames are reported so the caller can enter them.
var MAX = arguments[0] || 400;
var CONSENT = /cookie|consent|gdpr|rgpd|dsgvo|lgpd|privacy|privacidad|privacit|confidentialit|datenschutz|traceur|tracker/i;
var TEST_WORDS = /(^|[-_:])(test|testid|test-id|qa|cy|e2e|automation|auto-id|cerberus|selenium|hook|tid)([-_:]|id|$)/i;
var STANDARD = /^(id|class|style|href|src|srcset|sizes|alt|title|type|name|value|placeholder|role|tabindex|target|rel|for|action|method|width|height|loading|decoding|fetchpriority|lang|dir|hidden|disabled|checked|selected|readonly|required|autocomplete|autofocus|autocapitalize|inputmode|enterkeyhint|maxlength|minlength|pattern|min|max|step|form|accept|multiple|download|draggable|spellcheck|contenteditable|translate|nonce|integrity|crossorigin|referrerpolicy|xmlns.*|viewbox|fill|stroke|d|aria-.*|on.*|data-src|data-srcset|data-sizes|data-lazy.*|data-bg.*|data-href|data-url|open|inert|content|charset|http-equiv|scrolling|frameborder|allow|allowfullscreen|allowtransparency|sandbox|srcdoc|marginwidth|marginheight|align|valign|border|cellpadding|cellspacing|bgcolor|color|face|size|cols|rows|wrap|colspan|rowspan|headers|scope|span|start|reversed|label|datetime|cite|coords|shape|usemap|ismap|poster|preload|autoplay|controls|loop|muted|playsinline|kind|srclang|default|media|async|defer|ping|hreflang|enctype|novalidate|formaction|formmethod|formtarget|list|dirname|accesskey|slot|part|is|itemprop|itemscope|itemtype|itemid|itemref|property|typeof|vocab|prefix|about|resource|datatype|rev|xml:.*|xlink:.*|focusable|clip-rule|fill-rule|stroke-.*|transform|x|y|cx|cy|r|rx|ry|points|version|data-.*-(dir|direction|state|status|index|position|size|count|length|width|height|theme|variant|mode))$/i;

function ws(s) { return (s || '').replace(/[ \t\r\n\f]+/g, ' ').trim(); }   // XPath normalize-space(): NBSP is kept
function cut(t) { return t.length > 70 ? t.slice(0, 69) + '\u2026' : t; }
function up(e) { return e.parentElement || (e.parentNode && e.parentNode.host) || null; }
function style(e) { return e.ownerDocument.defaultView.getComputedStyle(e); }
// A value no one wrote by hand: hashes, UUIDs, long counters, generated ids (":r1:").
function hashy(v) {
  if (!v) return true;
  if (/[0-9a-f]{8}-[0-9a-f]{4}-/i.test(v) || /\d{5,}/.test(v) || /^:|:$/.test(v)) return true;
  var toks = v.split(/[^A-Za-z0-9]+/);
  for (var i = 0; i < toks.length; i++) {
    var k = toks[i];
    if (k.length >= 5 && /\d/.test(k) && /[a-z]/i.test(k) && (k.match(/[a-z](?=\d)|\d(?=[a-z])/gi) || []).length >= 3) return true;
  }
  return false;
}
function codey(v) { return /[{}();<>]|=>|\bfunction\b/.test(v); }

// ---- 1. collect every element, through open shadow roots and same-origin frames
var nodes = [], frames = [];
function collect(root, ctx) {
  var all = root.querySelectorAll('*');
  for (var i = 0; i < all.length; i++) {
    var e = all[i], t = e.tagName.toLowerCase();
    if (t === 'script' || t === 'style' || t === 'template' || t === 'noscript' || t === 'meta' || t === 'link') continue;
    if (e.closest('svg') && t !== 'svg') continue;
    nodes.push({ e: e, root: root, ctx: ctx });
    if (e.shadowRoot) collect(e.shadowRoot, { doc: ctx.doc, frame: ctx.frame, hosts: ctx.hosts.concat([e]) });
    if (t === 'iframe' || t === 'frame') {
      var d = null;
      try { d = e.contentDocument; } catch (x) { d = null; }
      if (d && d.documentElement) collect(d, { doc: d, frame: e, hosts: [] });
      else frames.push(e);
    }
  }
}
collect(document, { doc: document, frame: null, hosts: [] });

// ---- 2. which attributes identify an element on this page (statistics, not names)
var stats = {};
nodes.forEach(function (n) {
  var at = n.e.attributes;
  for (var i = 0; i < at.length; i++) {
    var a = at[i].name, v = at[i].value;
    if (STANDARD.test(a) || hashy(a)) continue;
    var s = stats[a] || (stats[a] = { n: 0, vals: {}, distinct: 0, bad: 0 });
    s.n++;
    if (!v || hashy(v) || codey(v) || v.length > 60 || /^(\d+|true|false|null|undefined)$/i.test(v)) { s.bad++; continue; }
    if (!s.vals[v]) { s.vals[v] = 1; s.distinct++; }
  }
});
var identityAttrs = Object.keys(stats).filter(function (a) {
  var s = stats[a];
  if (s.bad > s.n / 2) return false;
  if (TEST_WORDS.test(a)) return true;                       // test hooks may repeat (one per card of a list)
  // otherwise a data- attribute used as a naming scheme: on several elements, mostly distinct values
  return /^data-/.test(a) && s.n - s.bad >= 2 && s.distinct >= 0.6 * (s.n - s.bad) && s.n <= Math.max(20, nodes.length / 4);
}).sort(function (a, b) { return (TEST_WORDS.test(b) ? 1 : 0) - (TEST_WORDS.test(a) ? 1 : 0); });
function identity(e) {
  var out = [];
  identityAttrs.forEach(function (a) { var v = e.getAttribute(a); if (v && !hashy(v) && !codey(v) && v.length <= 60) out.push([a, v]); });
  return out;
}

// ---- 3. visibility as rendered
function vis(e) {
  var w = e.ownerDocument.defaultView, s = w.getComputedStyle(e), r = e.getBoundingClientRect();
  if (s.display === 'none' || s.visibility === 'hidden' || s.visibility === 'collapse' || r.width < 2 || r.height < 2) return false;
  if (r.right <= 0 || r.bottom <= -w.scrollY) return false;
  if (e.closest('[inert]')) return false;
  for (var p = e; p && p.nodeType === 1; p = up(p)) {
    var ps = p === e ? s : w.getComputedStyle(p);
    if (parseFloat(ps.opacity) === 0) return false;
    if (p !== e && /hidden|clip/.test(ps.overflow + ps.overflowX + ps.overflowY)) {
      var pr = p.getBoundingClientRect();
      if (pr.width < 2 || pr.height < 2) return false;
      if (r.right <= pr.left || r.left >= pr.right || r.bottom <= pr.top || r.top >= pr.bottom) return false;
    }
    if (p === p.ownerDocument.documentElement) break;
  }
  return true;
}

// ---- 4. layers above the page: dialogs, banners, floating widgets
function modal(q) {
  var rl = q.getAttribute('role');
  return (q.tagName === 'DIALOG' && q.open) || rl === 'dialog' || rl === 'alertdialog' || q.getAttribute('aria-modal') === 'true';
}
function layerOf(e) {
  var L = null;
  for (var q = e; q && q.nodeType === 1; q = up(q)) {
    if (q === q.ownerDocument.body || q === q.ownerDocument.documentElement) break;
    var p = style(q).position;
    if (modal(q) || p === 'fixed' || p === 'sticky') L = q;
  }
  return L;
}
var kinds = new Map();
function kindOf(L) {
  if (kinds.has(L)) return kinds.get(L);
  var k, txt = ws(L.innerText || '').slice(0, 3000), named = (L.id || '') + ' ' + (typeof L.className === 'string' ? L.className : '') + ' ' + (L.getAttribute('aria-label') || '');
  // A layer drawn by a frame (consent platforms often are) says what it is in the frame's title or address.
  L.querySelectorAll('iframe,frame').forEach(function (f) { named += ' ' + (f.getAttribute('title') || '') + ' ' + (f.getAttribute('name') || '') + ' ' + (f.getAttribute('src') || '').split('?')[0]; });
  var consent = (txt.length < 1500 && CONSENT.test(txt)) || /cookie|consent/i.test(named);
  var t = L.tagName.toLowerCase(), rl = L.getAttribute('role') || '';
  var w = L.ownerDocument.defaultView, r = L.getBoundingClientRect();
  var area = Math.max(0, Math.min(r.right, w.innerWidth) - Math.max(r.left, 0)) * Math.max(0, Math.min(r.bottom, w.innerHeight) - Math.max(r.top, 0)) / (w.innerWidth * w.innerHeight);
  if (L.querySelector('main,[role=main]')) k = 'shell';                        // the whole app sits in a fixed container
  else if (modal(L)) k = consent ? 'consent' : 'dialog';
  else if (t === 'header' || t === 'nav' || /banner|navigation/.test(rl) || L.querySelector('nav,header,[role=navigation],[role=banner]')) k = consent && !L.querySelector('nav,[role=navigation]') ? 'consent' : 'bar';
  else if (consent) k = 'consent';
  else if (area < 0.15) k = 'floating';
  else k = 'overlay';
  kinds.set(L, k);
  return k;
}
function label(p) {
  var al = p.getAttribute('aria-label');
  return p.id && !hashy(p.id) ? ' #' + p.id : (al ? ' "' + cut(ws(al)).slice(0, 30) + '"' : '');
}
function layerName(L) {
  var k = kindOf(L);
  return k === 'consent' ? 'cookie/consent banner' : k === 'dialog' ? 'dialog' + label(L) : k === 'floating' ? 'floating' : 'overlay' + label(L);
}
function region(e) {
  var L = layerOf(e);
  if (L) { var k = kindOf(L); if (k !== 'bar' && k !== 'shell') return layerName(L); }
  for (var p = e; p && p.nodeType === 1; p = up(p)) {
    var t = p.tagName.toLowerCase(), role = p.getAttribute('role');
    if (t === 'dialog' || role === 'dialog' || role === 'alertdialog') return 'dialog' + label(p);
    var par = up(p), top = !par || !par.closest || !par.closest('main,article,section,aside,[role=main]');
    if ((t === 'header' && top) || role === 'banner') return 'header';
    if (t === 'nav' || role === 'navigation') return 'navigation' + label(p);
    if ((t === 'footer' && top) || role === 'contentinfo') return 'footer';
    if (t === 'main' || role === 'main') return 'main';
    if ((t === 'aside' && top) || role === 'complementary') return 'aside';
    if (role === 'search') return 'search';
  }
  return 'page';
}
// What a click at the element's centre would really hit: another layer means the element is covered.
function coveredBy(e) {
  var w = e.ownerDocument.defaultView, r = e.getBoundingClientRect(), cx = r.left + r.width / 2, cy = r.top + r.height / 2;
  if (cx < 0 || cy < 0 || cx >= w.innerWidth || cy >= w.innerHeight) return null;
  var root = e.getRootNode(), hit = (root.elementFromPoint ? root : e.ownerDocument).elementFromPoint(cx, cy);
  if (!hit || hit === e || e.contains(hit) || hit.contains(e)) return null;
  var L = layerOf(hit);
  if (!L || L.contains(e)) return null;
  var k = kindOf(L);
  return k === 'bar' || k === 'shell' ? null : L;
}

// ---- 5. the selector Cerberus will use, checked unique where it applies
function lit(s) {
  if (s.indexOf("'") < 0) return "'" + s + "'";
  if (s.indexOf('"') < 0) return '"' + s + '"';
  return "concat('" + s.split("'").join("',\"'\",'") + "')";
}
function cq(v) { return '"' + v.replace(/\\/g, '\\\\').replace(/"/g, '\\"') + '"'; }
function count(root, css) { try { return root.querySelectorAll(css).length; } catch (x) { return 99; } }
function xcount(doc, xp) { try { return doc.evaluate('count(' + xp + ')', doc, null, 1, null).numberValue; } catch (x) { return 99; } }
function xindex(doc, xp, e) {
  try { var r = doc.evaluate(xp, doc, null, 7, null); for (var i = 0; i < r.snapshotLength; i++) if (r.snapshotItem(i) === e) return i + 1; } catch (x) { }
  return 0;
}
// A CSS selector whose FIRST match in root is e: what Cerberus' querySelector= takes, one per shadow level.
function cssIn(e, root) {
  var t = e.tagName.toLowerCase(), c = [], ids = identity(e);
  for (var i = 0; i < ids.length; i++) c.push('[' + ids[i][0] + '=' + cq(ids[i][1]) + ']');
  if (e.id && !hashy(e.id)) c.push('#' + CSS.escape(e.id));
  ['name', 'aria-label', 'title', 'placeholder', 'type', 'href', 'label', 'value'].forEach(function (a) {
    var v = e.getAttribute(a); if (v && !hashy(v) && v.length <= 80) c.push(t + '[' + a + '=' + cq(v) + ']');
  });
  for (var j = 0; j < e.attributes.length; j++) {
    var at = e.attributes[j];
    if (!STANDARD.test(at.name) && !hashy(at.name) && at.value && !hashy(at.value) && !codey(at.value) && at.value.length <= 60) c.push(t + '[' + at.name + '=' + cq(at.value) + ']');
  }
  c.push(t);
  for (var k = 0; k < c.length; k++) { try { if (root.querySelector(c[k]) === e) return c[k]; } catch (x) { } }
  // a short structural path, checked the same way
  var path = [], p = e;
  for (var d = 0; d < 4 && p && p.nodeType === 1; d++) {
    var tg = p.tagName.toLowerCase(), n = 1;
    for (var s = p.previousElementSibling; s; s = s.previousElementSibling) if (s.tagName === p.tagName) n++;
    path.unshift(tg + ':nth-of-type(' + n + ')');
    var css = path.join(' > ');
    try { if (root.querySelector(css) === e) return css; } catch (x) { }
    p = p.parentElement;
  }
  return '';
}
var LANDMARK = 'header,nav,main,footer,aside,form,dialog,[role=dialog],[role=navigation],[role=banner],[role=contentinfo],[role=main],[role=search],[role=menu],[role=tabpanel]';
// Scopes that can make a selector unique, nearest first: the layer the element sits in, then its landmarks.
function prefixes(e, doc) {
  var out = [], L = layerOf(e);
  if (L) { var k = kindOf(L); if (k !== 'bar' && k !== 'shell') scope(L, doc, out); }
  for (var p = up(e); p && p.nodeType === 1 && out.length < 12; p = up(p)) {
    if (p.matches(LANDMARK) || (p.id && !hashy(p.id) && /menu|nav|panel|modal|drawer|dropdown|popover|list|grid|card|form|section/i.test(p.id))) scope(p, doc, out);
  }
  return out;
}
function scope(L, doc, out) {
  var t = L.tagName.toLowerCase(), c = [];
  c.push('//' + t);
  var al = L.getAttribute('aria-label'); if (al) c.push('//' + t + '[@aria-label=' + lit(al) + ']');
  if (L.id && !hashy(L.id)) c.push("//*[@id=" + lit(L.id) + "]");
  identity(L).forEach(function (p) { c.push('//' + t + '[@' + p[0] + '=' + lit(p[1]) + ']'); });
  var rl = L.getAttribute('role'); if (rl) c.push("//*[@role=" + lit(rl) + "]");
  c.forEach(function (x) { if (out.indexOf(x) < 0 && xcount(doc, x) > 0) out.push(x); });
}
// The first visible text node: the label a reader sees when an element also holds hidden or glued text.
function firstText(e) {
  var w = e.ownerDocument.createTreeWalker(e, 4), n;
  while ((n = w.nextNode())) { var t = ws(n.nodeValue); if (t.length >= 2 && n.parentElement && vis(n.parentElement)) return t; }
  return '';
}
function sel(n) {
  var e = n.e, root = n.root, doc = n.ctx.doc, t = e.tagName.toLowerCase(), c;
  if (n.ctx.hosts.length) {                                    // inside shadow DOM: Cerberus' querySelector=host>>inner
    var parts = [];
    for (var h = 0; h < n.ctx.hosts.length; h++) { var host = n.ctx.hosts[h], hr = host.getRootNode(); c = cssIn(host, hr); if (!c) return ''; parts.push(c); }
    c = cssIn(e, root); if (!c || /'/.test(parts.join('') + c)) return '';
    return 'querySelector=' + parts.concat([c]).join('>>');
  }
  if (t === 'body') return 'xpath=//body';
  var ids = identity(e);
  for (var i = 0; i < ids.length; i++) {
    c = '[' + ids[i][0] + '=' + cq(ids[i][1]) + ']';
    if (count(root, c) === 1) return ids[i][0] === 'data-cerberus' ? 'data-cerberus=' + ids[i][1] : 'css=' + c;
  }
  if (e.id && !hashy(e.id) && count(root, '#' + CSS.escape(e.id)) === 1) return 'id=' + e.id;
  var nm = e.getAttribute('name');
  if (nm && !hashy(nm) && count(root, '[name=' + cq(nm) + ']') === 1) return 'name=' + nm;
  var full = ws(e.textContent), shown = ws(e.innerText || ''), clean = !!full && full === shown && full.length <= 60;
  if (t === 'a' && clean && !/\u00a0/.test(shown) && !/\n/.test(e.innerText || '')
      && xcount(doc, '//a[normalize-space()=' + lit(shown) + ']') === 1) return 'link=' + shown;
  var preds = [];
  if (clean) preds.push('normalize-space()=' + lit(full));
  var href = e.getAttribute('href');
  // An address that carries a generated id (/product/01M3XK5ZKF…) changes whenever the data is recreated: not a selector.
  if (t === 'a' && href && href.length <= 100 && !/^javascript:/i.test(href) && !href.split(/[\/?#=&]/).some(function (p) { return p && hashy(p); })) preds.push('@href=' + lit(href));
  var al = e.getAttribute('aria-label'); if (al) preds.push('@aria-label=' + lit(al));
  var ph = e.getAttribute('placeholder'); if (ph) preds.push('@placeholder=' + lit(ph));
  var ti = e.getAttribute('title'); if (ti && !full) preds.push('@title=' + lit(ti));
  if (!clean && full) { var ft = firstText(e); if (ft && ft.length <= 60) preds.push('.//text()[normalize-space()=' + lit(ft) + ']'); }
  // A field inside its label: the label's words reach it ("//label[normalize-space()='Pliers']//input").
  var lab = e.closest && e.closest('label');
  if (lab && lab !== e) { var lt = ws(lab.textContent); if (lt && lt.length <= 60) {
    var xl = '//label[normalize-space()=' + lit(lt) + ']//' + t; if (xcount(doc, xl) === 1) return 'xpath=' + xl; } }
  ['aria-controls', 'value', 'data-value', 'title', 'alt', 'for', 'type'].forEach(function (a) {
    var v = e.getAttribute(a); if (v && !hashy(v) && v.length <= 60 && preds.indexOf('@' + a + '=' + lit(v)) < 0) preds.push('@' + a + '=' + lit(v));
  });
  var pres = null, first = '';
  for (var j = 0; j < preds.length; j++) {
    var xp = '//' + t + '[' + preds[j] + ']', n = xcount(doc, xp);
    if (n === 1) return 'xpath=' + xp;
    if (n === 0) continue;
    if (!pres) pres = prefixes(e, doc);
    for (var q = 0; q < pres.length; q++) if (xcount(doc, pres[q] + xp) === 1) return 'xpath=' + pres[q] + xp;
    if (!first) first = xp;
  }
  if (first) { var k = xindex(doc, first, e); if (k) return 'xpath=(' + first + ')[' + k + ']'; }
  return '';
}

// ---- 6. what is worth listing
var INTERACTIVE = /^(a|button|input|select|textarea|summary|label|option|details)$/;
var ROLES = /^(button|link|tab|menuitem|menuitemcheckbox|menuitemradio|checkbox|radio|switch|option|combobox|textbox|searchbox|slider|spinbutton|treeitem)$/;
function interesting(e) {
  var t = e.tagName.toLowerCase();
  if (t === 'input' && e.type === 'hidden') return false;
  if (t === 'a' && !e.hasAttribute('href') && !e.getAttribute('role')) return false;
  if (INTERACTIVE.test(t) || ROLES.test(e.getAttribute('role') || '') || e.hasAttribute('onclick') || e.hasAttribute('aria-expanded')
      || e.hasAttribute('aria-haspopup') || e.isContentEditable && !(up(e) && up(e).isContentEditable)) return true;
  if (/^h[1-3]$/.test(t) || t === 'iframe' || t === 'frame') return true;
  if (t === 'html' || (t === 'body' && !e.isContentEditable)) return false;
  var named = identity(e).length > 0 || (e.id && !hashy(e.id)), tx = ws(e.innerText || '');
  return named && e.children.length <= 3 && tx.length > 0 && tx.length <= 60;
}
function text(e) {
  var t = e.tagName.toLowerCase(), x = '';
  if (t === 'body') return 'editable document';
  if (t === 'input' || t === 'textarea' || t === 'select') {
    var lab = e.labels && e.labels.length ? e.labels[0].innerText : '';
    x = ws(e.getAttribute('aria-label') || lab || e.getAttribute('placeholder') || (/^(submit|button|reset)$/.test(e.type) ? e.value : '') || e.getAttribute('title'));
  } else if (t === 'iframe' || t === 'frame') {
    x = ws(e.getAttribute('title') || e.getAttribute('name') || '');
  } else {
    x = ws(e.innerText || e.textContent) || ws(e.getAttribute('aria-label') || e.getAttribute('alt') || e.getAttribute('title'));
    if (!x) { var im = e.querySelector('img[alt],[aria-label],title'); if (im) x = ws(im.getAttribute('alt') || im.getAttribute('aria-label') || im.textContent); }
  }
  return cut(x);
}

var picked = [];
nodes.forEach(function (n) { try { if (interesting(n.e)) picked.push(n); } catch (x) { } });
var shown = [], hiddenOnes = [], layered = [];
picked.forEach(function (n) {
  try {
    if (!vis(n.e)) { hiddenOnes.push(n); return; }
    var L = layerOf(n.e), k = L ? kindOf(L) : '';
    (k && k !== 'bar' && k !== 'shell' && k !== 'floating' ? layered : shown).push(n);
  } catch (x) { hiddenOnes.push(n); }
});
shown = layered.concat(shown);   // what sits above the page first: it decides whether the rest can be clicked
var more = shown.length + hiddenOnes.length > MAX;
var chosen = shown.slice(0, MAX).concat(hiddenOnes.slice(0, Math.max(0, MAX - shown.length)));
var out = [], seen = new Set(), covers = new Map();
chosen.forEach(function (n) {
  try {
    var e = n.e, t = e.tagName.toLowerCase(), v = shown.indexOf(n) >= 0, x = text(e);
    var a = {};
    ['name', 'type', 'href', 'aria-expanded', 'aria-controls', 'placeholder', 'src'].forEach(function (k) {
      var val = e.getAttribute(k);
      if (val && !(k === 'src' && t !== 'iframe' && t !== 'frame')) a[k] = k === 'href' || k === 'src' ? val.replace(location.origin, '') : val;
    });
    if (t === 'input' && /^(checkbox|radio)$/.test(e.type)) a.checked = String(e.checked);
    if (e.disabled) a.disabled = 'true';
    identity(e).forEach(function (p) { a[p[0]] = p[1]; });
    var key = t + '|' + x + '|' + (a.href || '') + '|' + (e.id || '') + '|' + (n.ctx.frame ? 'f' : '');
    if (seen.has(key)) return;
    seen.add(key);
    var item = { r: region(e), t: t, x: x, a: a, s: sel(n), v: v };
    if (n.ctx.frame) item.f = sel({ e: n.ctx.frame, root: n.ctx.frame.getRootNode(), ctx: { doc: n.ctx.frame.ownerDocument, frame: null, hosts: [] } }) || 'iframe';
    if (n.ctx.hosts.length) item.sh = 1;
    if (v) {
      var L = coveredBy(e);
      if (L) { item.c = layerName(L); covers.set(item.c, (covers.get(item.c) || 0) + 1); }
    }
    out.push(item);
  } catch (x) { }
});
var cross = [];
frames.forEach(function (f) {
  try {
    if (!vis(f)) return;
    var r = f.getBoundingClientRect();
    cross.push({ s: sel({ e: f, root: f.getRootNode(), ctx: { doc: f.ownerDocument, frame: null, hosts: [] } }), title: text(f),
      src: (f.getAttribute('src') || '').slice(0, 120), r: region(f), big: r.width * r.height > 20000, el: f });
  } catch (x) { }
});
var cov = [];
covers.forEach(function (n, k) { cov.push({ layer: k, n: n }); });
return { title: document.title, url: location.href, items: out, more: more, covered: cov, frames: cross };
