<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>My Events - SEVENT-MS</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        .header-section {
            min-height: 140px;
            display: flex;
            align-items: center;
            padding: 2rem;
            margin-bottom: 2rem;
            background: linear-gradient(135deg, rgba(45, 212, 191, 0.12), rgba(7, 8, 10, 0.86)), var(--color-surface);
            border: 1px solid var(--color-border);
            border-radius: var(--radius);
        }
        .header-section h1 {
            font-size: 2.2rem;
            margin: 0;
            color: var(--color-text-primary);
            text-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
        }

        .my-events-list {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
            gap: 2.5rem;
            margin-top: 2rem;
        }

        .event-card {
            background: linear-gradient(145deg, rgba(20, 23, 28, 0.98), rgba(7, 8, 10, 0.96));
            border-radius: var(--radius);
            box-shadow: var(--card-shadow);
            padding: 2rem;
            transition: all var(--transition-speed) ease;
            position: relative;
            overflow: hidden;
            border: 1px solid var(--color-border);
            backdrop-filter: blur(10px);
            min-width: 400px;
        }

        .event-card::before {
            content: '';
            position: absolute;
            inset: 0 0 auto 0;
            height: 4px;
            background: linear-gradient(90deg, var(--color-accent-teal), var(--color-accent-gold), var(--color-accent-green));
        }

        .event-card:hover {
            transform: translateY(-8px);
            border-color: var(--color-border-strong);
            box-shadow: 0 24px 54px -14px rgba(45, 212, 191, 0.28);
        }

        .event-title {
            color: var(--color-text-primary);
            font-size: 2em;
            margin-bottom: 15px;
            padding: 1.5rem;
            border-radius: var(--radius);
            border: 2px solid rgba(255, 255, 255, 0.3);
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            background: linear-gradient(135deg, #181a1f, #090a0d);
            text-align: center;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 2px;
            text-shadow: 
                2px 2px 4px rgba(0, 0, 0, 0.5),
                0 0 20px rgba(220, 225, 230, 0.28);
            position: relative;
            overflow: hidden;
        }

        .event-title::after {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, var(--color-accent-teal), var(--color-accent-gold));
            box-shadow: 0 0 18px rgba(45, 212, 191, 0.35);
        }

        .event-details {
            margin-bottom: 2rem;
            background: linear-gradient(135deg, #15171c, #07080a);
            padding: 1.5rem;
            border-radius: var(--radius);
            border: 1px solid var(--color-border);
            box-shadow: var(--card-shadow);
        }

        .event-details p {
            margin: 1rem 0;
            color: var(--color-text-primary);
            display: flex;
            align-items: center;
            gap: 0.75rem;
            font-size: 1.1em;
            background: rgba(45, 212, 191, 0.08);
            padding: 1rem;
            border-radius: 10px;
            border: 1px solid rgba(45, 212, 191, 0.18);
        }

        .event-details i {
            color: var(--color-accent-teal);
            font-size: 1.2em;
            text-shadow: 0 0 10px rgba(180, 185, 192, 0.26);
        }

        .event-details strong {
            color: var(--color-accent-teal);
            font-weight: 600;
            text-shadow: 0 0 10px rgba(180, 185, 192, 0.26);
        }

        .event-context {
            margin: 0 0 1rem;
            padding: 0.8rem 1rem;
            border-radius: 10px;
            border: 1px solid rgba(251, 191, 36, 0.22);
            background: rgba(251, 191, 36, 0.08);
            color: var(--color-accent-gold);
            font-weight: 700;
            text-align: center;
        }

        .digital-id {
            background: linear-gradient(135deg, #181a1f, #090a0d);
            padding: 2rem;
            border-radius: var(--radius);
            margin-top: 2rem;
            position: relative;
            overflow: hidden;
            border: 2px solid rgba(255, 255, 255, 0.3);
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
        }

        .digital-id h3 {
            color: var(--color-text-primary);
            margin: 0 0 1.5rem 0;
            font-size: 1.5em;
            text-align: center;
            text-shadow: 
               2px 2px 4px rgba(0, 0, 0, 0.5),
               0 0 20px rgba(220, 225, 230, 0.28);
            font-weight: 700;
        }

        .participant-details {
            background: linear-gradient(135deg, #15171c, #07080a);
            padding: 2rem;
            border-radius: var(--radius);
            margin-top: 1.5rem;
            border: 1px solid var(--color-border);
            box-shadow: var(--card-shadow);
        }

        .participant-details p {
            margin: 1rem 0;
            color: var(--color-text-primary);
            display: flex;
            align-items: center;
            gap: 0.75rem;
            font-size: 1.1em;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.2);
            background: rgba(251, 191, 36, 0.08);
            padding: 1rem;
            border-radius: 10px;
            border: 1px solid rgba(251, 191, 36, 0.18);
        }

        .participant-details i {
            color: var(--color-accent-gold);
            font-size: 1.2em;
            text-shadow: 0 0 10px rgba(180, 185, 192, 0.26);
        }

        .participant-details strong {
            color: var(--color-accent-gold);
            font-weight: 600;
            text-shadow: 0 0 10px rgba(180, 185, 192, 0.26);
        }

        .email-form {
            background:
                linear-gradient(135deg, rgba(45, 212, 191, 0.12), transparent 36%),
                linear-gradient(225deg, rgba(251, 191, 36, 0.12), transparent 40%),
                linear-gradient(135deg, #181a1f, #090a0d);
            padding: 2rem;
            border-radius: var(--radius);
            margin-bottom: 2rem;
            box-shadow: var(--card-shadow);
            border: 1px solid var(--color-border);
            backdrop-filter: blur(10px);
            position: relative;
            overflow: hidden;
        }

        .email-form::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, var(--color-accent-teal), var(--color-accent-gold), var(--color-accent-green));
        }

        .email-form h2 {
            color: var(--color-text-primary);
            margin: 0 0 1.5rem 0;
            font-size: 1.5em;
            text-align: center;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.2);
        }

        .email-form form {
            display: flex;
            gap: 1rem;
            align-items: center;
            justify-content: center;
            flex-wrap: wrap;
        }

        .email-form input[type="email"] {
            flex: 1;
            min-width: 300px;
            padding: 1rem 1.5rem;
            border: 1px solid var(--color-border);
            border-radius: 25px;
            font-size: 1rem;
            background: rgba(24, 26, 31, 0.82);
            color: var(--color-text-primary);
            transition: all var(--transition-speed) ease;
        }

        .email-form input[type="email"]:focus {
            outline: none;
            border-color: var(--color-accent-teal);
            box-shadow: 0 0 0 3px rgba(45, 212, 191, 0.18);
        }

        .participant-card {
            background: var(--color-surface);
            border-radius: var(--radius);
            padding: 2rem;
            border: 1px solid var(--color-border);
            box-shadow: var(--card-shadow);
        }

        .participant-card h2 {
            margin: 0;
            padding: 0;
            border: none;
            background: none;
            box-shadow: none;
            color: var(--color-text-primary);
            text-shadow: 
                2px 2px 4px rgba(0, 0, 0, 0.5),
                0 0 20px rgba(220, 225, 230, 0.28);
        }

        @media (max-width: 768px) {
            .container {
                padding: 5.5rem 1rem 2rem;
            }
            
            .my-events-list {
                grid-template-columns: 1fr;
            }
            
            .event-card {
                min-width: unset;
                padding: 1.25rem;
            }
            
            .email-form form {
                flex-direction: column;
                gap: 1rem;
            }

            .email-form input[type="email"] {
                width: 100%;
                min-width: unset;
            }
        }
    </style>
</head>
<body>
    <!-- Navigation Bar -->
    <nav class="nav-bar">
        <div class="nav-container">
            <a href="${pageContext.request.contextPath}/" class="nav-logo">
                <i class="fas fa-calendar-alt"></i> SeventMS
            </a>
            <button class="nav-toggle" id="navToggle" aria-label="Toggle navigation menu" aria-expanded="false">
                <i class="fas fa-bars"></i>
            </button>
            <div class="nav-links" id="navLinks">
                <a href="${pageContext.request.contextPath}/" class="nav-link">
                    <i class="fas fa-home"></i> Home
                </a>
                <a href="${pageContext.request.contextPath}/documentation.jsp" class="nav-link">
                    <i class="fas fa-info-circle"></i> About
                </a>
                <a href="${pageContext.request.contextPath}/myEvents" class="nav-link active">
                    <i class="fas fa-ticket-alt"></i> My Events
                </a>
                <c:choose>
                    <c:when test="${not empty sessionScope.authEmail}">
                        <c:if test="${sessionScope.authRole == 'ADMIN'}">
                            <a href="${pageContext.request.contextPath}/admin" class="nav-link">
                                <i class="fas fa-user-shield"></i> Admin
                            </a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/logout" class="nav-link">
                            <i class="fas fa-sign-out-alt"></i> Logout
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login" class="nav-link">
                            <i class="fas fa-sign-in-alt"></i> Sign In
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </nav>

    <div class="container">
        <div class="header-section">
            <h1>My Events</h1>
        </div>

        <div class="email-form">
            <h2>View Your Bookings</h2>
            <form method="get" action="${pageContext.request.contextPath}/myEvents">
                <div class="form-group" style="flex: 1; min-width: 300px; margin-bottom: 0;">
                    <label for="email-lookup" class="form-label">Email Address</label>
                    <input type="email" id="email-lookup" name="email" value="${email}" placeholder="e.g. john@example.com" required autocomplete="email" style="border-radius: var(--radius);">
                </div>
                <button type="submit" class="btn btn-primary" style="margin-top: 1.5rem; min-height: 48px;">
                    <i class="fas fa-search"></i> View Bookings
                </button>
            </form>
        </div>

        <div class="my-events-list">
            <c:forEach var="participation" items="${participations}">
                <div class="event-card">
                    <div class="participant-card">
                        <h2 class="event-title">${participation.event.name}</h2>
                        <c:if test="${participation.event.subEvent}">
                            <div class="event-context">
                                <i class="fas fa-layer-group"></i>
                                Part of ${participation.event.parentEventName}
                            </div>
                        </c:if>
                        <c:if test="${participation.event.majorEvent}">
                            <div class="event-context">
                                <i class="fas fa-layer-group"></i>
                                Entire Major Event
                            </div>
                        </c:if>
                        <div class="participant-details">
                            <p>
                                <strong><i class="fas fa-user"></i> Name:</strong>
                                ${participation.name}
                            </p>
                            <p>
                                <strong><i class="fas fa-envelope"></i> Email:</strong>
                                ${participation.email}
                            </p>
                            <p>
                                <strong><i class="fas fa-id-card"></i> Digital ID:</strong>
                                <span class="digital-id">${participation.participantId}</span>
                            </p>
                        </div>
                        <div class="event-details">
                            <p>
                                <strong><i class="far fa-calendar-alt"></i> Date:</strong>
                                ${participation.event.date}
                            </p>
                            <p>
                                <strong><i class="fas fa-map-marker-alt"></i> Location:</strong>
                                ${participation.event.location}
                            </p>
                            <p>
                                <strong><i class="fas fa-info-circle"></i> Description:</strong>
                                ${participation.event.description}
                            </p>
                        </div>
                    </div>
                </div>
            </c:forEach>
            
            <c:if test="${empty participations}">
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <i class="fas fa-ticket-alt"></i>
                    <h3>No Bookings Found</h3>
                    <p>We couldn't find any event bookings for the email address: <strong><c:out value="${email}"/></strong></p>
                    <div style="margin-top: 1.5rem; display: flex; justify-content: center; gap: 1rem; flex-wrap: wrap;">
                        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">
                            <i class="fas fa-calendar-alt"></i> Browse Upcoming Events
                        </a>
                    </div>
                </div>
            </c:if>
        </div>
        
        <div class="navigation" style="margin-top: 2rem; text-align: center;">
            <a href="${pageContext.request.contextPath}/" class="btn">
                <i class="fas fa-arrow-left"></i> Back to Events
            </a>
        </div>
    </div>

    <script>
        // Mobile navigation toggle
        const navToggle = document.getElementById('navToggle');
        const navLinks = document.getElementById('navLinks');

        navToggle.addEventListener('click', () => {
            const active = navLinks.classList.toggle('active');
            navToggle.setAttribute('aria-expanded', active);
        });

        // Close mobile menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!navToggle.contains(e.target) && !navLinks.contains(e.target)) {
                navLinks.classList.remove('active');
                navToggle.setAttribute('aria-expanded', 'false');
            }
        });

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
