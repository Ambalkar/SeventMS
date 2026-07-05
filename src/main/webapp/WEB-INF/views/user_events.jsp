<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
    <title>Event Booking - SEVENT-MS</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        .header-section {
            min-height: 430px;
            display: flex;
            align-items: flex-end;
            padding: 3rem;
            margin-bottom: 1.5rem;
            background:
                linear-gradient(90deg, rgba(7, 8, 10, 0.92), rgba(7, 8, 10, 0.58) 52%, rgba(7, 8, 10, 0.72)),
                linear-gradient(180deg, rgba(7, 8, 10, 0.05), rgba(7, 8, 10, 0.86)),
                url('${pageContext.request.contextPath}/images/eventbg0.jpg') center/cover no-repeat;
            border: 1px solid var(--color-border);
            border-radius: var(--radius);
        }

        .hero-content {
            width: min(720px, 100%);
            position: relative;
            z-index: 1;
        }

        .eyebrow {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.45rem 0.7rem;
            margin: 0 0 1rem;
            border-radius: 999px;
            color: #a7f3d0;
            background: rgba(45, 212, 191, 0.12);
            border: 1px solid rgba(45, 212, 191, 0.28);
            font-size: 0.78rem;
            font-weight: 700;
            text-transform: uppercase;
        }

        .header-section h1 {
            text-align: left;
            max-width: 680px;
            font-size: clamp(2.2rem, 7vw, 4.6rem);
            line-height: 1.02;
            letter-spacing: 0;
            text-shadow: 0 18px 34px rgba(0, 0, 0, 0.48);
            margin: 0;
        }

        .hero-copy {
            max-width: 620px;
            margin: 1.2rem 0 0;
            color: #e4e4e7;
            font-size: 1.06rem;
            line-height: 1.7;
        }

        .hero-actions {
            display: flex;
            flex-wrap: wrap;
            gap: 0.85rem;
            margin-top: 1.7rem;
        }

        .hero-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 0.6rem;
            min-height: 46px;
            padding: 0.75rem 1.1rem;
            border-radius: var(--radius);
            color: #04110e;
            background: linear-gradient(135deg, #5eead4, #86efac);
            text-decoration: none;
            font-weight: 700;
            box-shadow: 0 18px 44px rgba(45, 212, 191, 0.22);
        }

        .hero-btn.secondary {
            color: var(--color-text-primary);
            background: rgba(255, 255, 255, 0.10);
            border: 1px solid rgba(255, 255, 255, 0.22);
            box-shadow: none;
        }

        .hero-stats {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 0.85rem;
            margin-top: 2rem;
            max-width: 640px;
        }

        .hero-stat {
            padding: 0.9rem;
            border-radius: var(--radius);
            background: rgba(7, 8, 10, 0.58);
            border: 1px solid var(--color-border);
        }

        .hero-stat strong {
            display: block;
            color: var(--color-text-primary);
            font-size: 1.35rem;
        }

        .hero-stat span {
            color: var(--color-text-secondary);
            font-size: 0.82rem;
        }

        .events-heading {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            gap: 1rem;
            margin: 2rem 0 0;
        }

        .events-heading h2 {
            margin: 0;
            font-size: 1.8rem;
            color: var(--color-text-primary);
        }

        .events-heading p {
            margin: 0.45rem 0 0;
            color: var(--color-text-secondary);
        }

        .event-count {
            flex: 0 0 auto;
            padding: 0.65rem 0.9rem;
            border-radius: var(--radius);
            color: var(--color-accent-gold);
            background: rgba(251, 191, 36, 0.08);
            border: 1px solid rgba(251, 191, 36, 0.24);
            font-weight: 700;
        }

        .events-list {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
            gap: 1.25rem;
            margin-top: 1.25rem;
        }

        .event-card {
            display: flex;
            flex-direction: column;
            padding: 1.25rem;
            background: linear-gradient(145deg, rgba(20, 23, 28, 0.98), rgba(7, 8, 10, 0.96));
            border-radius: var(--radius);
            box-shadow: var(--card-shadow);
            border: 1px solid var(--color-border);
            position: relative;
            overflow: hidden;
            backdrop-filter: blur(10px);
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

        .event-card h2 {
            font-size: 1.35rem;
            line-height: 1.3;
            margin-top: 0;
            margin-bottom: 1rem;
            color: var(--color-text-primary);
            padding-bottom: 1rem;
            border-bottom: 1px solid var(--color-border);
        }

        .event-card p {
            margin: 1rem 0;
            color: var(--color-text-secondary);
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }

        .event-image {
            width: 100%;
            height: auto;
            border-radius: var(--radius);
            border: 1px solid var(--color-border);
            margin-bottom: 1rem;
            display: block;
        }

        .event-card .date,
        .event-card .location,
        .event-card .guests {
            width: fit-content;
            max-width: 100%;
            border-radius: var(--radius);
        }

        .event-card .date, .event-card .location {
            background: rgba(45, 212, 191, 0.10);
            padding: 0.75rem 1.25rem;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            margin: 0.5rem 0;
            border: 1px solid rgba(45, 212, 191, 0.28);
        }

        .event-card .date i, .event-card .location i {
            color: var(--color-accent-teal);
        }

        .event-card .guests {
            background: rgba(52, 211, 153, 0.15);
            color: var(--color-success);
            padding: 0.75rem 1.25rem;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            margin: 1rem 0;
            font-weight: 500;
            border: 1px solid rgba(52, 211, 153, 0.3);
        }

        .booking-form {
            background: rgba(255, 255, 255, 0.045);
            margin-top: 1.25rem;
            padding: 1rem;
            border: 1px solid var(--color-border);
            border-radius: var(--radius);
        }

        .booking-form-title {
            display: flex;
            align-items: center;
            gap: 0.55rem;
            margin: 0 0 0.9rem;
            color: var(--color-text-primary);
            font-weight: 700;
        }

        .event-kind {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            margin-bottom: 1rem;
            padding: 0.45rem 0.7rem;
            border-radius: var(--radius);
            border: 1px solid rgba(251, 191, 36, 0.28);
            color: var(--color-accent-gold);
            background: rgba(251, 191, 36, 0.08);
            font-size: 0.78rem;
            font-weight: 700;
            text-transform: uppercase;
            width: fit-content;
        }

        .booking-status {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            width: fit-content;
            max-width: 100%;
            margin: 0.4rem 0 0.8rem;
            padding: 0.55rem 0.75rem;
            border-radius: var(--radius);
            color: var(--color-accent-gold);
            background: rgba(251, 191, 36, 0.09);
            border: 1px solid rgba(251, 191, 36, 0.26);
            font-size: 0.82rem;
            font-weight: 700;
        }

        .fully-booked {
            background: rgba(248, 113, 113, 0.15);
            color: var(--color-danger);
            padding: 0.75rem;
            border-radius: var(--radius);
            text-align: center;
            margin-top: 1rem;
            font-weight: 500;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            border: 1px solid rgba(248, 113, 113, 0.3);
        }

        .sub-events {
            margin-top: 1.5rem;
            padding-top: 1rem;
            border-top: 1px solid var(--color-border);
        }

        .sub-events h3 {
            margin: 0 0 1rem;
            color: var(--color-text-primary);
            font-size: 1rem;
            display: flex;
            align-items: center;
            gap: 0.55rem;
        }

        .sub-event-card {
            margin-top: 1rem;
            padding: 1rem;
            border-radius: var(--radius);
            background: rgba(255, 255, 255, 0.065);
            border: 1px solid var(--color-border);
        }

        .sub-event-card h4 {
            margin: 0 0 0.75rem;
            color: var(--color-text-primary);
            font-size: 1.05rem;
        }

                opacity: 1;
                transform: translateY(0);
            }
        }

        .no-events i {
            font-size: 3.5em;
            color: var(--primary-color);
            margin-bottom: 1.25rem;
            opacity: 0.5;
        }

        /* Navigation Styles */
        .nav-bar {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            z-index: 1000;
            background: rgba(6, 7, 8, 0.86);
            backdrop-filter: blur(18px);
            border-bottom: 1px solid rgba(255, 255, 255, 0.14);
            box-shadow: 0 12px 34px rgba(0, 0, 0, 0.32);
        }

        .nav-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 1rem 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .nav-logo {
            color: var(--text-primary);
            text-decoration: none;
            font-size: 1.75rem;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 0.75rem;
            transition: transform 0.3s ease;
        }

        .nav-logo:hover {
            transform: scale(1.05);
        }

        .nav-toggle {
            display: none;
            cursor: pointer;
            font-size: 1.5rem;
            color: var(--text-primary);
        }

        .nav-links {
            display: flex;
            gap: 1.5rem;
        }

        .nav-link.active {
            background: linear-gradient(135deg, var(--accent-teal), var(--accent-green));
            color: #04110e;
            border-color: rgba(45, 212, 191, 0.45);
        }

        /* Adjust container padding for navbar */
        .container {
            padding-top: 5rem;
        }

        /* Mobile Navigation */
        @media (max-width: 768px) {
            .nav-toggle {
                display: block;
            }

            .nav-links {
                display: none;
                position: absolute;
                top: 100%;
                left: 0;
                right: 0;
                flex-direction: column;
                background: rgba(8, 9, 11, 0.98);
                padding: 1rem;
                gap: 0.5rem;
            }

            .nav-links.active {
                display: flex;
            }

            .nav-link {
                width: 100%;
                text-align: center;
            }
        }

        body {
            background:
                linear-gradient(180deg, rgba(7, 8, 10, 0.10), #07080a 82%),
                linear-gradient(115deg, rgba(45, 212, 191, 0.10), transparent 34%),
                linear-gradient(245deg, rgba(251, 191, 36, 0.09), transparent 36%),
                #07080a;
        }

        .container {
            max-width: 1700px;
            padding: 6.25rem 1.5rem 3rem;
            background: transparent;
            border: 0;
            box-shadow: none;
            backdrop-filter: none;
        }

        .header-section {
            min-height: 430px;
            display: flex;
            align-items: flex-end;
            padding: 3rem;
            margin-bottom: 1.5rem;
            background:
                linear-gradient(90deg, rgba(7, 8, 10, 0.92), rgba(7, 8, 10, 0.58) 52%, rgba(7, 8, 10, 0.72)),
                linear-gradient(180deg, rgba(7, 8, 10, 0.05), rgba(7, 8, 10, 0.86)),
                url('${pageContext.request.contextPath}/images/eventbg0.jpg') center/cover no-repeat;
        }

        .header-section::before {
            display: none;
        }

        .hero-content {
            width: min(720px, 100%);
            position: relative;
            z-index: 1;
        }

        .eyebrow {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.45rem 0.7rem;
            margin: 0 0 1rem;
            border-radius: 999px;
            color: #a7f3d0;
            background: rgba(45, 212, 191, 0.12);
            border: 1px solid rgba(45, 212, 191, 0.28);
            font-size: 0.78rem;
            font-weight: 700;
            text-transform: uppercase;
        }

        .header-section h1 {
            text-align: left;
            max-width: 680px;
            font-size: clamp(2.2rem, 7vw, 4.6rem);
            line-height: 1.02;
            letter-spacing: 0;
            text-shadow: 0 18px 34px rgba(0, 0, 0, 0.48);
        }

        .hero-copy {
            max-width: 620px;
            margin: 1.2rem 0 0;
            color: #e4e4e7;
            font-size: 1.06rem;
            line-height: 1.7;
        }

        .hero-actions {
            display: flex;
            flex-wrap: wrap;
            gap: 0.85rem;
            margin-top: 1.7rem;
        }

        .hero-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 0.6rem;
            min-height: 46px;
            padding: 0.75rem 1.1rem;
            border-radius: 8px;
            color: #04110e;
            background: linear-gradient(135deg, #5eead4, #86efac);
            text-decoration: none;
            font-weight: 700;
            box-shadow: 0 18px 44px rgba(45, 212, 191, 0.22);
        }

        .hero-btn.secondary {
            color: var(--text-primary);
            background: rgba(255, 255, 255, 0.10);
            border: 1px solid rgba(255, 255, 255, 0.22);
            box-shadow: none;
        }

        .hero-stats {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 0.85rem;
            margin-top: 2rem;
            max-width: 640px;
        }

        .hero-stat {
            padding: 0.9rem;
            border-radius: 8px;
            background: rgba(7, 8, 10, 0.58);
            border: 1px solid rgba(255, 255, 255, 0.14);
        }

        .hero-stat strong {
            display: block;
            color: var(--text-primary);
            font-size: 1.35rem;
        }

        .hero-stat span {
            color: var(--text-secondary);
            font-size: 0.82rem;
        }

        .events-heading {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            gap: 1rem;
            margin: 2rem 0 0;
        }

        .events-heading h2 {
            margin: 0;
            font-size: 1.8rem;
            color: var(--text-primary);
        }

        .events-heading p {
            margin: 0.45rem 0 0;
            color: var(--text-secondary);
        }

        .event-count {
            flex: 0 0 auto;
            padding: 0.65rem 0.9rem;
            border-radius: 8px;
            color: var(--accent-gold);
            background: rgba(251, 191, 36, 0.08);
            border: 1px solid rgba(251, 191, 36, 0.24);
            font-weight: 700;
        }

        .events-list {
            display: flex;
            flex-direction: column;
            gap: 2.5rem;
            margin-top: 1.5rem;
        }

        .event-card {
            display: flex;
            flex-direction: column;
            padding: 1.5rem;
            background: linear-gradient(145deg, rgba(20, 23, 28, 0.98), rgba(7, 8, 10, 0.96));
            border-radius: var(--radius);
            box-shadow: var(--card-shadow);
            border: 1px solid var(--color-border);
            position: relative;
            overflow: hidden;
            backdrop-filter: blur(10px);
            transition: all var(--transition-speed) ease;
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

        .event-card h2 {
            font-size: 1.8rem;
            line-height: 1.3;
            margin-top: 0;
            margin-bottom: 1rem;
            color: var(--color-text-primary);
            padding-bottom: 1rem;
            border-bottom: 1px solid var(--color-border);
        }

        .event-card .description {
            margin: 1rem 0;
            color: var(--color-text-secondary);
            line-height: 1.6;
            font-size: 1.05rem;
        }

        .event-image {
            width: 100%;
            aspect-ratio: 1700 / 950;
            object-fit: cover;
            border-radius: var(--radius);
            border: 1px solid var(--color-border);
            margin-bottom: 0;
            display: block;
        }

        .event-main-split {
            display: flex;
            flex-direction: column;
            gap: 1.5rem;
        }

        .event-image-container {
            width: 100%;
        }

        .event-details-container {
            width: 100%;
            display: flex;
            flex-direction: column;
            justify-content: flex-start;
        }

        @media (min-width: 1025px) {
            .event-card {
                padding: 2.5rem;
            }
            .event-main-split {
                flex-direction: row;
                gap: 2.5rem;
                align-items: stretch;
            }
            .event-image-container {
                flex: 1 1 60%;
                max-width: 60%;
            }
            .event-details-container {
                flex: 1 1 40%;
                max-width: 40%;
            }
        }

        .event-card .date,
        .event-card .location,
        .event-card .guests {
            width: fit-content;
            max-width: 100%;
            border-radius: 8px;
        }

        .booking-form {
            background: rgba(255, 255, 255, 0.045);
            margin-top: 1.25rem;
            padding: 1rem;
            border: 1px solid rgba(255, 255, 255, 0.12);
            border-radius: 8px;
        }

        .booking-form-title {
            display: flex;
            align-items: center;
            gap: 0.55rem;
            margin: 0 0 0.9rem;
            color: var(--text-primary);
            font-weight: 700;
        }

        .form-group input {
            box-sizing: border-box;
            border-radius: 8px;
        }

        .btn-primary {
            display: inline-flex;
            justify-content: center;
            align-items: center;
            gap: 0.6rem;
            min-height: 44px;
        }

        .sub-events h3 {
            display: flex;
            align-items: center;
            gap: 0.55rem;
        }

        .sub-event-card {
            background: rgba(255, 255, 255, 0.065);
        }

        .no-events {
            border-radius: 8px;
        }

        @media (max-width: 768px) {
            .container {
                padding: 5.5rem 1rem 2rem;
            }

            .header-section {
                min-height: 520px;
                padding: 1.4rem;
                align-items: flex-end;
            }

            .hero-stats {
                grid-template-columns: 1fr;
            }

            .events-heading {
                align-items: flex-start;
                flex-direction: column;
            }

            .hero-btn {
                width: 100%;
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
                <a href="${pageContext.request.contextPath}/" class="nav-link active">
                    <i class="fas fa-home"></i> Home
                </a>
                <a href="${pageContext.request.contextPath}/documentation.jsp" class="nav-link">
                    <i class="fas fa-info-circle"></i> About
                </a>
                <a href="${pageContext.request.contextPath}/myEvents" class="nav-link">
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
            <div class="hero-content">
                <p class="eyebrow"><i class="fas fa-star"></i> Book your next experience</p>
                <h1>Find events worth showing up for.</h1>
                <p class="hero-copy">
                    Explore upcoming events, reserve your spot in seconds, and keep your booking details ready for check-in.
                </p>
                <div class="hero-actions">
                    <a href="#available-events" class="hero-btn">
                        <i class="fas fa-ticket-alt"></i> Browse Events
                    </a>
                    <a href="${pageContext.request.contextPath}/myEvents" class="hero-btn secondary">
                        <i class="fas fa-receipt"></i> View My Bookings
                    </a>
                </div>
                <div class="hero-stats">
                    <div class="hero-stat">
                        <strong>${fn:length(events)}</strong>
                        <span>Available events</span>
                    </div>
                    <div class="hero-stat">
                        <strong>Instant</strong>
                        <span>Booking confirmation</span>
                    </div>
                    <div class="hero-stat">
                        <strong>Digital</strong>
                        <span>Event ID after booking</span>
                    </div>
                </div>
            </div>
        </div>
        
        <c:if test="${not empty successMessage}">
            <div class="alert success" role="alert" aria-live="polite">
                <i class="fas fa-check-circle"></i> ${successMessage}
            </div>
        </c:if>
        
        <c:if test="${not empty errorMessage}">
            <div class="alert error" role="alert" aria-live="polite">
                <i class="fas fa-exclamation-circle"></i> ${errorMessage}
            </div>
        </c:if>

        <div class="events-heading" id="available-events">
            <div>
                <h2>Available Events</h2>
                <p>Pick an event, add your details, and reserve your seat.</p>
            </div>
            <div class="event-count">${fn:length(events)} listed</div>
        </div>

        <div class="events-list">
            <c:forEach var="event" items="${events}">
                <div class="event-card">
                    <div class="event-main-split">
                        <div class="event-image-container">
                            <c:choose>
                                <c:when test="${not empty event.imagePath}">
                                    <img src="${pageContext.request.contextPath}${event.imagePath}" alt="${event.name}" class="event-image">
                                </c:when>
                                <c:otherwise>
                                    <img src="${pageContext.request.contextPath}/images/eventbg0.jpg" alt="${event.name}" class="event-image">
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="event-details-container">
                            <div class="event-kind">
                                <c:choose>
                                    <c:when test="${event.majorEvent}">
                                        <i class="fas fa-layer-group"></i> Major Event
                                    </c:when>
                                    <c:otherwise>
                                        <i class="fas fa-calendar-day"></i> Simple Event
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <h2>${event.name}</h2>
                            <p class="date">
                                <i class="far fa-calendar-alt"></i>
                                Date: ${event.date}
                            </p>
                            <p class="location">
                                <i class="fas fa-map-marker-alt"></i>
                                Location: ${event.location}
                            </p>
                            <p class="description">${event.description}</p>
                            <p class="guests">
                                <i class="fas fa-users"></i>
                                Available Spots: ${event.availableSpots}
                            </p>

                            <c:if test="${not empty event.bookingStatus}">
                                <div class="booking-status">
                                    <i class="fas fa-check-circle"></i> ${event.bookingStatus}
                                </div>
                            </c:if>
                            
                            <c:if test="${event.canBook}">
                                <form method="post" action="${pageContext.request.contextPath}/" class="booking-form">
                                    <input type="hidden" name="event_id" value="${event.id}">
                                    <div class="booking-form-title">
                                        <i class="fas fa-pen"></i>
                                        <c:choose>
                                            <c:when test="${event.bookingStatus == 'Upgrade Available'}">Upgrade to major event pass</c:when>
                                            <c:otherwise>Reserve your spot</c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="form-group">
                                        <label for="name-${event.id}" class="form-label">Your Name</label>
                                        <input type="text" id="name-${event.id}" name="name" placeholder="e.g. John Doe" value="${sessionScope.authName}" required>
                                    </div>
                                    <div class="form-group">
                                        <label for="email-${event.id}" class="form-label">Your Email</label>
                                        <input type="email" id="email-${event.id}" name="email" placeholder="e.g. john@example.com" value="${sessionScope.authEmail}" required>
                                    </div>
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-ticket-alt"></i>
                                        <c:choose>
                                            <c:when test="${event.bookingStatus == 'Upgrade Available'}">Upgrade Pass</c:when>
                                            <c:otherwise>Book Event</c:otherwise>
                                        </c:choose>
                                    </button>
                                </form>
                            </c:if>
                            <c:if test="${not event.canBook and empty event.bookingStatus}">
                                <p class="fully-booked">
                                    <i class="fas fa-exclamation-circle"></i> The event is Housefull you can explore other events
                                </p>
                            </c:if>
                        </div>
                    </div>
                    
                    <c:if test="${event.majorEvent}">
                        <div class="sub-events">
                            <h3><i class="fas fa-stream"></i> Sub Events</h3>
                            <c:forEach var="subEvent" items="${event.subEvents}">
                                <div class="sub-event-card">
                                    <div class="event-main-split">
                                        <div class="event-image-container">
                                            <c:choose>
                                                <c:when test="${not empty subEvent.imagePath}">
                                                    <img src="${pageContext.request.contextPath}${subEvent.imagePath}" alt="${subEvent.name}" class="event-image">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}/images/eventbg0.jpg" alt="${subEvent.name}" class="event-image">
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="event-details-container">
                                            <h4>${subEvent.name}</h4>
                                            <p class="date">
                                                <i class="far fa-calendar-alt"></i>
                                                Date: ${subEvent.date}
                                            </p>
                                            <p class="location">
                                                <i class="fas fa-map-marker-alt"></i>
                                                Location: ${subEvent.location}
                                            </p>
                                            <p class="description">${subEvent.description}</p>
                                            <p class="guests">
                                                <i class="fas fa-users"></i>
                                                Available Spots: ${subEvent.availableSpots}
                                            </p>

                                            <c:if test="${not empty subEvent.bookingStatus}">
                                                <div class="booking-status">
                                                    <i class="fas fa-check-circle"></i> ${subEvent.bookingStatus}
                                                </div>
                                            </c:if>

                                            <c:if test="${subEvent.canBook}">
                                                <form method="post" action="${pageContext.request.contextPath}/" class="booking-form">
                                                    <input type="hidden" name="event_id" value="${subEvent.id}">
                                                    <div class="booking-form-title">
                                                        <i class="fas fa-pen"></i> Reserve this sub event
                                                    </div>
                                                    <div class="form-group">
                                                        <label for="name-${subEvent.id}" class="form-label">Your Name</label>
                                                        <input type="text" id="name-${subEvent.id}" name="name" placeholder="e.g. John Doe" value="${sessionScope.authName}" required>
                                                    </div>
                                                    <div class="form-group">
                                                        <label for="email-${subEvent.id}" class="form-label">Your Email</label>
                                                        <input type="email" id="email-${subEvent.id}" name="email" placeholder="e.g. john@example.com" value="${sessionScope.authEmail}" required>
                                                    </div>
                                                    <button type="submit" class="btn btn-primary">
                                                        <i class="fas fa-ticket-alt"></i> Book Sub Event
                                                    </button>
                                                </form>
                                            </c:if>
                                            <c:if test="${not subEvent.canBook and empty subEvent.bookingStatus}">
                                                <p class="fully-booked">
                                                    <i class="fas fa-exclamation-circle"></i> The event is Housefull you can explore other events
                                                </p>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:if>
                </div>
            </c:forEach>
            
            <c:if test="${empty events}">
                <p class="no-events">
                    <i class="fas fa-calendar-times"></i><br>
                    No events available at the moment.
                </p>
            </c:if>
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
                    // We must wait briefly or use a hidden input before disabling to prevent submit cancel
                    setTimeout(() => { btn.disabled = true; }, 10);
                }
            });
        });
    </script>
</body>
</html>
