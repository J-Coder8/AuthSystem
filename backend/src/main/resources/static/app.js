/* =========================================================
   AuthSystem – Frontend JavaScript
   ========================================================= */

// ── Constants ────────────────────────────────────────────
const TOKEN_KEY   = 'authsystem_token';
const USER_KEY    = 'authsystem_user';

// ── Token helpers ─────────────────────────────────────────
function saveToken(token)  { localStorage.setItem(TOKEN_KEY, token); }
function getToken()        { return localStorage.getItem(TOKEN_KEY); }
function clearToken()      { localStorage.removeItem(TOKEN_KEY); }

function saveUser(user)    { localStorage.setItem(USER_KEY, JSON.stringify(user)); }
function getUser()         {
  try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch { return null; }
}
function clearUser()       { localStorage.removeItem(USER_KEY); }

// ── Temporary state (OTP / TOTP flow) ────────────────────
let pendingUsername = null;

// ── Page navigation ───────────────────────────────────────
function showPage(pageId) {
  document.querySelectorAll('.page').forEach(p => p.classList.add('hidden'));
  const target = document.getElementById(pageId);
  if (target) target.classList.remove('hidden');
}

// ── Utility: show / hide alert banners ───────────────────
function showAlert(id, message, type = 'error') {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = message;
  el.className = el.className
    .replace(/hidden/, '')
    .replace(/bg-\S+|border-\S+|text-\S+/g, '');
  const colours = {
    error:   'bg-red-50 border border-red-200 text-red-700',
    success: 'bg-green-50 border border-green-200 text-green-700',
    info:    'bg-blue-50 border border-blue-200 text-blue-700',
  };
  el.className = `mb-4 p-3 rounded-lg text-sm ${colours[type] || colours.error}`;
}

function hideAlert(id) {
  const el = document.getElementById(id);
  if (el) el.className = (el.className || '') + ' hidden';
}

// ── Utility: set button loading state ────────────────────
function setLoading(btnId, loading, label = 'Submit') {
  const btn = document.getElementById(btnId);
  if (!btn) return;
  btn.disabled = loading;
  btn.textContent = loading ? 'Please wait…' : label;
}

// ── Utility: toggle password visibility ──────────────────
function togglePassword(inputId, btn) {
  const input = document.getElementById(inputId);
  if (!input) return;
  input.type = input.type === 'password' ? 'text' : 'password';
}

// ── API client ────────────────────────────────────────────
async function apiRequest(method, path, body = null, token = null) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const opts = { method, headers };
  if (body) opts.body = JSON.stringify(body);

  const res = await fetch(path, opts);

  // Try to parse JSON; fall back to plain text
  let data;
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) {
    data = await res.json();
  } else {
    data = await res.text();
  }

  return { ok: res.ok, status: res.status, data };
}

// ── Extract a human-readable error message ───────────────
function extractError(data) {
  if (!data) return 'An unexpected error occurred.';
  if (typeof data === 'string') return data;
  if (data.message) return data.message;
  // Validation errors come back as { field: message, … }
  const entries = Object.entries(data);
  if (entries.length) return entries.map(([k, v]) => `${k}: ${v}`).join('; ');
  return 'An unexpected error occurred.';
}

// ── Format date ───────────────────────────────────────────
function fmtDate(iso) {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleDateString(undefined, {
      year: 'numeric', month: 'short', day: 'numeric',
    });
  } catch { return iso; }
}

// ── LOGIN ─────────────────────────────────────────────────
document.getElementById('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  hideAlert('login-error');
  hideAlert('login-info');

  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value;

  if (!username || !password) {
    showAlert('login-error', 'Please enter your username and password.');
    return;
  }

  setLoading('login-btn', true, 'Sign In');
  const { ok, data } = await apiRequest('POST', '/api/auth/login', { username, password });
  setLoading('login-btn', false, 'Sign In');

  if (!ok) {
    showAlert('login-error', extractError(data));
    return;
  }

  pendingUsername = data.username || username;

  if (data.requiresOtp) {
    showAlert('login-info', `A verification code has been sent to your email.`, 'info');
    showPage('page-otp');
    return;
  }

  if (data.requiresTotp) {
    showPage('page-totp');
    return;
  }

  // Fully authenticated
  if (data.token) {
    saveToken(data.token);
    saveUser({ username: data.username, email: data.email, fullName: data.fullName, role: data.role });
    enterDashboard(data);
  } else {
    showAlert('login-error', data.message || 'Login failed.');
  }
});

// ── OTP VERIFICATION ──────────────────────────────────────
document.getElementById('otp-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  hideAlert('otp-error');

  const code = document.getElementById('otp-code').value.trim();
  if (!code) { showAlert('otp-error', 'Please enter the verification code.'); return; }

  setLoading('otp-btn', true, 'Verify Code');
  const { ok, data } = await apiRequest('POST', '/api/auth/verify-otp', {
    username: pendingUsername,
    code,
  });
  setLoading('otp-btn', false, 'Verify Code');

  if (!ok) { showAlert('otp-error', extractError(data)); return; }

  if (data.token) {
    saveToken(data.token);
    saveUser({ username: data.username, email: data.email, fullName: data.fullName, role: data.role });
    enterDashboard(data);
  } else {
    showAlert('otp-error', data.message || 'Verification failed.');
  }
});

// ── TOTP VERIFICATION ─────────────────────────────────────
document.getElementById('totp-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  hideAlert('totp-error');

  const code = document.getElementById('totp-code').value.trim();
  if (!code) { showAlert('totp-error', 'Please enter the authenticator code.'); return; }

  setLoading('totp-btn', true, 'Verify');
  const { ok, data } = await apiRequest('POST', '/api/auth/verify-otp', {
    username: pendingUsername,
    code,
  });
  setLoading('totp-btn', false, 'Verify');

  if (!ok) { showAlert('totp-error', extractError(data)); return; }

  if (data.token) {
    saveToken(data.token);
    saveUser({ username: data.username, email: data.email, fullName: data.fullName, role: data.role });
    enterDashboard(data);
  } else {
    showAlert('totp-error', data.message || 'Verification failed.');
  }
});

// ── RESEND OTP ────────────────────────────────────────────
async function resendOtp() {
  if (!pendingUsername) return;
  hideAlert('otp-error');
  const btn = document.getElementById('resend-otp-btn');
  if (btn) { btn.disabled = true; btn.textContent = 'Sending…'; }

  const { ok, data } = await apiRequest('POST', '/api/auth/resend-otp', {
    username: pendingUsername,
    code: '',
  });

  if (btn) { btn.disabled = false; btn.textContent = 'Resend code'; }

  if (ok) {
    showAlert('otp-error', 'A new code has been sent to your email.', 'info');
  } else {
    showAlert('otp-error', extractError(data));
  }
}

// ── REGISTER ──────────────────────────────────────────────
document.getElementById('register-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  hideAlert('register-error');
  hideAlert('register-success');

  const fullName = document.getElementById('reg-fullname').value.trim();
  const username = document.getElementById('reg-username').value.trim();
  const email    = document.getElementById('reg-email').value.trim();
  const password = document.getElementById('reg-password').value;

  if (!fullName || !username || !email || !password) {
    showAlert('register-error', 'Please fill in all fields.');
    return;
  }
  if (password.length < 8) {
    showAlert('register-error', 'Password must be at least 8 characters.');
    return;
  }

  setLoading('register-btn', true, 'Create Account');
  const { ok, data } = await apiRequest('POST', '/api/auth/register', {
    fullName, username, email, password,
  });
  setLoading('register-btn', false, 'Create Account');

  if (!ok) {
    showAlert('register-error', extractError(data));
    return;
  }

  // Registration may return a token directly or require OTP
  if (data.token) {
    saveToken(data.token);
    saveUser({ username: data.username, email: data.email, fullName: data.fullName, role: data.role });
    enterDashboard(data);
    return;
  }

  if (data.requiresOtp) {
    pendingUsername = data.username || username;
    showAlert('register-success', 'Account created! Check your email for a verification code.', 'success');
    setTimeout(() => showPage('page-otp'), 1500);
    return;
  }

  showAlert('register-success', data.message || 'Account created! You can now sign in.', 'success');
  setTimeout(() => showPage('page-login'), 2000);
});

// ── FORGOT PASSWORD ───────────────────────────────────────
let forgotIdentifier = null;

document.getElementById('forgot-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  hideAlert('forgot-error');
  hideAlert('forgot-success');

  const identifier = document.getElementById('forgot-identifier').value.trim();
  if (!identifier) { showAlert('forgot-error', 'Please enter your username or email.'); return; }

  setLoading('forgot-btn', true, 'Send Reset Code');
  const { ok, data } = await apiRequest('POST', '/api/auth/forgot-password', { identifier });
  setLoading('forgot-btn', false, 'Send Reset Code');

  if (!ok) { showAlert('forgot-error', extractError(data)); return; }

  forgotIdentifier = identifier;
  showAlert('forgot-success', data.message || 'Reset code sent! Check your email.', 'success');
  document.getElementById('forgot-step1').classList.add('hidden');
  document.getElementById('forgot-step2').classList.remove('hidden');
});

document.getElementById('reset-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  hideAlert('forgot-error');
  hideAlert('forgot-success');

  const code        = document.getElementById('reset-code').value.trim();
  const newPassword = document.getElementById('reset-password').value;

  if (!code || !newPassword) { showAlert('forgot-error', 'Please fill in all fields.'); return; }
  if (newPassword.length < 8) { showAlert('forgot-error', 'Password must be at least 8 characters.'); return; }

  setLoading('reset-btn', true, 'Reset Password');
  const { ok, data } = await apiRequest('POST', '/api/auth/reset-password', {
    identifier: forgotIdentifier,
    code,
    newPassword,
  });
  setLoading('reset-btn', false, 'Reset Password');

  if (!ok) { showAlert('forgot-error', extractError(data)); return; }

  showAlert('forgot-success', data.message || 'Password reset successfully!', 'success');
  setTimeout(() => {
    document.getElementById('forgot-step1').classList.remove('hidden');
    document.getElementById('forgot-step2').classList.add('hidden');
    showPage('page-login');
  }, 2000);
});

// ── DASHBOARD ─────────────────────────────────────────────
function enterDashboard(authData) {
  // Populate nav + banner from auth response
  const username = authData.username || '';
  const fullName = authData.fullName || username;
  const email    = authData.email    || '';
  const role     = authData.role     || 'USER';

  document.getElementById('nav-username').textContent  = username;
  document.getElementById('dash-fullname').textContent = fullName;
  document.getElementById('dash-email').textContent    = email;
  document.getElementById('dash-role').textContent     = role;
  document.getElementById('dash-2fa').textContent      = '—';
  document.getElementById('dash-lastlogin').textContent = '—';

  showPage('page-dashboard');
  loadProfile();
}

async function loadProfile() {
  const token = getToken();
  if (!token) { logout(); return; }

  const loading = document.getElementById('profile-loading');
  const grid    = document.getElementById('profile-grid');
  const errEl   = document.getElementById('profile-error');

  loading.classList.remove('hidden');
  grid.classList.add('hidden');
  errEl.classList.add('hidden');

  const { ok, data } = await apiRequest('GET', '/api/auth/profile', null, token);

  loading.classList.add('hidden');

  if (!ok) {
    if (typeof data === 'string' && data.toLowerCase().includes('unauthorized') || !ok && (typeof data === 'string')) {
      // token may be expired
    }
    errEl.textContent = extractError(data);
    errEl.classList.remove('hidden');
    return;
  }

  // Populate profile grid
  setText('p-username', data.username);
  setText('p-fullname', data.fullName);
  setText('p-email',    data.email);
  setText('p-phone',    data.phone    || '—');
  setText('p-bio',      data.bio      || '—');
  setText('p-created',  fmtDate(data.createdAt));

  const city    = data.city    || '';
  const country = data.country || '';
  setText('p-location', [city, country].filter(Boolean).join(', ') || '—');

  // Update stats row
  const role = data.role || 'USER';
  setText('dash-role', role);

  const totpOn = data.totpEnabled;
  const faceOn = data.faceEnabled;
  const twoFa  = [totpOn && 'TOTP', faceOn && 'Face'].filter(Boolean).join(' + ') || 'Disabled';
  setText('dash-2fa', twoFa);
  setText('dash-lastlogin', fmtDate(data.lastLoginAt));

  // Security badges
  setSecurityBadge('sec-totp-icon', 'sec-totp-status', totpOn, 'Enabled', 'Not configured');
  setSecurityBadge('sec-face-icon', 'sec-face-status', faceOn, 'Enabled', 'Not configured');

  grid.classList.remove('hidden');
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value || '—';
}

function setSecurityBadge(iconId, statusId, enabled, onLabel, offLabel) {
  const icon   = document.getElementById(iconId);
  const status = document.getElementById(statusId);
  if (!icon || !status) return;

  if (enabled) {
    icon.className = 'w-9 h-9 rounded-full flex items-center justify-center bg-green-100';
    icon.querySelector('svg').classList.replace('text-gray-500', 'text-green-600');
    status.textContent = onLabel;
    status.className   = 'text-xs text-green-600 font-medium';
  } else {
    icon.className = 'w-9 h-9 rounded-full flex items-center justify-center bg-gray-200';
    icon.querySelector('svg').classList.replace('text-green-600', 'text-gray-500');
    status.textContent = offLabel;
    status.className   = 'text-xs text-gray-400';
  }
}

// ── LOGOUT ────────────────────────────────────────────────
function logout() {
  clearToken();
  clearUser();
  pendingUsername = null;
  showPage('page-login');
}

// ── INIT ──────────────────────────────────────────────────
(function init() {
  const token = getToken();
  const user  = getUser();

  if (token && user) {
    // Restore dashboard from cached user data, then refresh profile
    enterDashboard(user);
  } else {
    showPage('page-login');
  }
})();
