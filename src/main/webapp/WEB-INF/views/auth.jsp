<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Sign In - SEVENT-MS</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            margin: 0;
            min-height: 100vh;
            font-family: 'Poppins', sans-serif;
            color: var(--color-text-primary);
            background:
                linear-gradient(90deg, rgba(7, 8, 10, 0.92), rgba(7, 8, 10, 0.72)),
                url('${pageContext.request.contextPath}/images/eventbg0.jpg') center/cover fixed no-repeat;
            display: grid;
            place-items: center;
            padding: 1.5rem;
            box-sizing: border-box;
        }

        .auth-shell {
            width: min(100%, 440px);
            background: linear-gradient(145deg, rgba(20, 23, 28, 0.98), rgba(7, 8, 10, 0.96));
            border: 1px solid var(--color-border);
            border-radius: var(--radius);
            box-shadow: var(--card-shadow);
            padding: 2rem;
        }

        .brand {
            display: inline-flex;
            align-items: center;
            gap: 0.75rem;
            color: var(--color-text-primary);
            text-decoration: none;
            font-size: 1.4rem;
            font-weight: 700;
            margin-bottom: 1.5rem;
        }

        .brand i {
            color: var(--color-accent-teal);
        }

        h1 {
            margin: 0;
            font-size: 2rem;
            color: var(--color-text-primary);
        }

        .lede {
            color: var(--color-text-secondary);
            margin: 0.6rem 0 1.5rem;
            line-height: 1.6;
        }

        .switch-link {
            margin: 1.25rem 0 0;
            color: var(--color-text-secondary);
            text-align: center;
        }

        .switch-link a {
            color: var(--color-accent-gold);
            text-decoration: none;
            font-weight: 700;
        }
    </style>
</head>
<body>
    <main class="auth-shell">
        <a href="${pageContext.request.contextPath}/" class="brand">
            <i class="fas fa-calendar-alt"></i> SeventMS
        </a>

        <c:choose>
            <c:when test="${mode == 'signup'}">
                <h1>Create account</h1>
                <p class="lede">Sign up to book events faster and keep your event details connected to your email.</p>
            </c:when>
            <c:otherwise>
                <h1>Sign in</h1>
                <p class="lede">Log in as a participant, or use the admin credentials to open the dashboard.</p>
            </c:otherwise>
        </c:choose>

        <c:if test="${not empty errorMessage}">
            <div class="alert error" role="alert" aria-live="polite">
                <i class="fas fa-exclamation-circle"></i> ${errorMessage}
            </div>
        </c:if>

        <c:choose>
            <c:when test="${mode == 'signup'}">
                <form method="post" action="${pageContext.request.contextPath}/signup">
                    <input type="hidden" name="target" value="${target}">
                    <div class="form-group">
                        <label for="name" class="form-label">Name</label>
                        <input id="name" type="text" name="name" required autocomplete="name">
                    </div>
                    <div class="form-group">
                        <label for="email" class="form-label">Email</label>
                        <input id="email" type="email" name="email" required autocomplete="email">
                    </div>
                    <div class="form-group">
                        <label for="password" class="form-label">Password</label>
                        <input id="password" type="password" name="password" minlength="6" required autocomplete="new-password">
                    </div>
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-user-plus"></i> Sign Up
                    </button>
                </form>
                <p class="switch-link">
                    Already have an account?
                    <a href="${pageContext.request.contextPath}/login?target=${target}">Log in</a>
                </p>
            </c:when>
            <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/login">
                    <input type="hidden" name="target" value="${target}">
                    <div class="form-group">
                        <label for="email" class="form-label">Email</label>
                        <input id="email" type="email" name="email" required autocomplete="email">
                    </div>
                    <div class="form-group">
                        <label for="password" class="form-label">Password</label>
                        <input id="password" type="password" name="password" required autocomplete="current-password">
                    </div>
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-sign-in-alt"></i> Log In
                    </button>
                </form>
                <p class="switch-link">
                    New here?
                    <a href="${pageContext.request.contextPath}/signup?target=${target}">Create an account</a>
                </p>
            </c:otherwise>
        </c:choose>
    </main>

    <script>
        // Form submit loading states
        document.querySelectorAll('form').forEach(form => {
            form.addEventListener('submit', function() {
                const btn = this.querySelector('button[type="submit"]');
                if (btn) {
                    btn.classList.add('loading');
                    setTimeout(() => { btn.disabled = true; }, 10);
                }
            });
        });
    </script>
</body>
</html>
