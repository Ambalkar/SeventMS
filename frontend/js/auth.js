// Intercept fetch requests globally to inject Bearer token automatically.
(function() {
    const originalFetch = window.fetch;
    window.fetch = async function(resource, init) {
        const token = localStorage.getItem("token");
        if (token) {
            init = init || {};
            init.credentials = 'include';
            init.headers = init.headers || {};
            if (init.headers instanceof Headers) {
                if (!init.headers.has('Authorization')) {
                    init.headers.set('Authorization', 'Bearer ' + token);
                }
            } else if (!init.headers.Authorization && !init.headers.authorization) {
                init.headers.Authorization = 'Bearer ' + token;
            }
        }
        return originalFetch(resource, init);
    };
})();

const AUTH_STORAGE_KEYS = ["token", "user"];
let authPromise = null;

function readCachedUser() {
    const cachedUserStr = localStorage.getItem("user");
    if (!cachedUserStr) {
        return { authenticated: false };
    }

    try {
        const cachedUser = JSON.parse(cachedUserStr);
        return {
            name: cachedUser.name || "",
            email: cachedUser.email || "",
            role: cachedUser.role || "USER",
            authenticated: Boolean(cachedUser.authenticated)
        };
    } catch (error) {
        return { authenticated: false };
    }
}

function clearAuthStorage() {
    AUTH_STORAGE_KEYS.forEach((key) => {
        localStorage.removeItem(key);
        sessionStorage.removeItem(key);
    });
    clearAuthCookies();
}

function clearAuthCookies() {
    const cookieNames = document.cookie
        .split(";")
        .map((cookie) => cookie.split("=")[0].trim())
        .filter(Boolean);

    cookieNames.forEach((name) => {
        document.cookie = `${name}=; Max-Age=0; path=/`;
        document.cookie = `${name}=; Max-Age=0; path=/; domain=${window.location.hostname}`;
    });
}

function renderLoggedOutNavbar() {
    renderNavbar({ authenticated: false });
}

async function checkAuth() {
    renderLoggedOutNavbar();

    if (authPromise) return authPromise;

    authPromise = (async () => {
        try {
            const res = await fetch(CONFIG.API_BASE_URL + '/api/auth/me', { credentials: 'include' });
            if (!res.ok) {
                throw new Error("HTTP " + res.status);
            }

            const data = await res.json();
            if (data && data.authenticated) {
                localStorage.setItem("user", JSON.stringify({
                    name: data.name,
                    email: data.email,
                    role: data.role,
                    authenticated: true
                }));
                renderNavbar(data);
                return data;
            }

            clearAuthStorage();
            renderLoggedOutNavbar();
            return { authenticated: false };
        } catch (err) {
            clearAuthStorage();
            renderLoggedOutNavbar();
            return { authenticated: false };
        } finally {
            authPromise = null;
        }
    })();

    return authPromise;
}

function renderNavbar(user) {
    const navLinks = document.getElementById('navLinks');
    if (!navLinks) return;

    // Determine active page
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';

    const getActiveClass = (page) => currentPage === page ? 'active' : '';

    let html = `
        <a href="index.html" class="nav-link ${getActiveClass('index.html')}">
            <i class="fas fa-home"></i> Home
        </a>
        <a href="events.html" class="nav-link ${getActiveClass('events.html')}">
            <i class="fas fa-calendar-alt"></i> Browse Events
        </a>
        <a href="my-events.html" class="nav-link ${getActiveClass('my-events.html')}">
            <i class="fas fa-ticket-alt"></i> My Events
        </a>
    `;

    if (user && user.authenticated) {
        if (user.role === 'ADMIN') {
            html += `
                <a href="admin.html" class="nav-link ${getActiveClass('admin.html')}">
                    <i class="fas fa-user-shield"></i> Admin
                </a>
            `;
        }
        html += `
            <a href="#" id="logoutBtn" class="nav-link">
                <i class="fas fa-sign-out-alt"></i> Logout (${user.name})
            </a>
        `;
    } else {
        html += `
            <a href="login.html" class="nav-link ${getActiveClass('login.html')}">
                <i class="fas fa-sign-in-alt"></i> Sign In
            </a>
        `;
    }

    navLinks.innerHTML = html;

    // Attach logout event
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async (e) => {
            e.preventDefault();
            try {
                await fetch(CONFIG.API_BASE_URL + '/api/auth/logout', { 
                    method: 'POST',
                    credentials: 'include'
                });
            } catch (err) {
                console.error('Logout error:', err);
            } finally {
                clearAuthStorage();
                authPromise = null;
                window.location.href = 'index.html';
            }
        });
    }
}

// Perform instant rendering immediately as script runs (no DOMContentLoaded blocking)
checkAuth();

// Setup UI toggle event listeners once DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    const navToggle = document.getElementById('navToggle');
    const navLinks = document.getElementById('navLinks');
    if (navToggle && navLinks) {
        navToggle.addEventListener('click', () => {
            navLinks.classList.toggle('active');
            const expanded = navToggle.getAttribute('aria-expanded') === 'true' || false;
            navToggle.setAttribute('aria-expanded', !expanded);
        });
    }
});

