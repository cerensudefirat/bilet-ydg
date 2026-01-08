const $ = (id) => document.getElementById(id);

function base() {
  return $("baseUrl").value.replace(/\/$/, "");
}

function resetLog() {
  $("log").textContent = "";
}

function logLine(s) {
  $("log").textContent += s + "\n";
}

function setStatus(text, ok = null) {
  $("status").textContent = `Durum: ${text}`;
  const badge = $("resultBadge");
  if (ok === true) { badge.className = "ok"; badge.textContent = "RESULT: PASS"; }
  else if (ok === false) { badge.className = "bad"; badge.textContent = "RESULT: FAIL"; }
  else { badge.className = ""; badge.textContent = "RESULT: -"; }
}

function authHeader(user, pass) {
  const token = btoa(`${user}:${pass}`);
  return { "Authorization": `Basic ${token}` };
}

async function http(method, path, { auth = null, body = null, timeoutMs = 8000 } = {}) {
  const headers = {};
  if (body != null) headers["Content-Type"] = "application/json";
  if (auth) Object.assign(headers, auth);

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);

  let r;
  try {
    r = await fetch(base() + path, {
      method,
      headers,
      body: body != null ? JSON.stringify(body) : null,
      signal: controller.signal
    });
  } catch (e) {
    const name = e?.name ? `${e.name}: ` : "";
    throw new Error(`FETCH FAILED ${method} ${path} -> ${name}${e?.message ?? e}`);
  } finally {
    clearTimeout(timer);
  }

  const text = await r.text();
  let data;
  try { data = JSON.parse(text); } catch { data = text; }

  if (!r.ok) {
    throw new Error(`HTTP ${r.status} ${path} -> ${text}`);
  }
  return data;
}

function isoPlusDays(days) {
  const d = new Date();
  d.setDate(d.getDate() + days);
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

async function scenario1_adminCreateEvent() {
  const adminUser = $("adminUser").value.trim();
  const adminPass = $("adminPass").value.trim();

  if (!adminUser || !adminPass) {
    throw new Error("Admin kullanıcı adı/şifre boş olamaz.");
  }

  const adminAuth = authHeader(adminUser, adminPass);

  logLine("0) GET /api/mekan (Basic Auth kontrol + ilk mekanId seçilecek)");
  const mekanlar = await http("GET", "/api/mekan", { auth: adminAuth, timeoutMs: 60000 });

  if (!Array.isArray(mekanlar) || mekanlar.length === 0) {
    throw new Error("Mekan listesi boş. SC1 için en az 1 mekan gerekli.");
  }

  const mekanId = mekanlar[0].id ?? mekanlar[0].mekanId;
  if (!mekanId) throw new Error("Mekan DTO'da id/mekanId alanı bulunamadı.");

  logLine(`   -> mekanId = ${mekanId}`);

  const unique = Date.now();
  const req = {
    baslik: `SC1 YDG Event ${unique}`,
    tur: "Konser",
    sehir: "Malatya",
    tarih: isoPlusDays(10),
    kapasite: 250,
    temelFiyat: 150.00,
    mekanId: mekanId
  };

  logLine("1) POST /api/admin/etkinlik (create)");
  logLine("   body = " + JSON.stringify(req));
  const created = await http("POST", "/api/admin/etkinlik", { auth: adminAuth, body: req, timeoutMs: 10000 });

  const createdId = created?.id;
  if (!createdId) throw new Error("Create response içinde 'id' dönmedi.");

  logLine(`   -> createdId = ${createdId}`);

  logLine("2) GET /api/etkinlik (public listede arama)");
  const list = await http("GET", "/api/etkinlik", { timeoutMs: 10000 });

  const found =
    Array.isArray(list) &&
    list.some(e => String(e?.id) === String(createdId));

  if (!found) throw new Error("Oluşturulan etkinlik public listede bulunamadı.");

  logLine("✅ SC1 doğrulandı: etkinlik public listede görünüyor.");
  return { createdId, baslik: req.baslik };
}

$("btnSc1").onclick = async () => {
  resetLog();
  setStatus("SC1 çalışıyor...", null);

  try {
    const result = await scenario1_adminCreateEvent();
    setStatus(`SC1 PASS (createdId=${result.createdId})`, true);
  } catch (e) {
    logLine("❌ HATA: " + (e?.message ?? String(e)));
    setStatus("SC1 FAIL", false);
  }
};
