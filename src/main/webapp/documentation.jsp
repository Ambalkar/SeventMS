<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>SEVENT-MS - Documentation</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        .doc-header {
            background: linear-gradient(135deg, rgba(45, 212, 191, 0.12), rgba(7, 8, 10, 0.86)), var(--color-surface);
            padding: 2.5rem;
            border-radius: var(--radius);
            margin-bottom: 2rem;
            position: relative;
            box-shadow: var(--card-shadow);
            text-align: center;
            border: 1px solid var(--color-border);
            overflow: hidden;
        }

        .doc-header::after {
            content: '';
            position: absolute;
            left: 0;
            right: 0;
            bottom: 0;
            height: 4px;
            background: linear-gradient(90deg, var(--color-accent-teal), var(--color-accent-gold), var(--color-accent-green));
        }

        .doc-header h1 {
            margin: 0;
            font-size: 2.5em;
            color: var(--color-text-primary);
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        }

        .doc-nav {
            position: sticky;
            top: 90px;
            background: var(--color-surface);
            padding: 1.25rem;
            border-radius: var(--radius);
            margin-bottom: 2rem;
            box-shadow: var(--card-shadow);
            border: 1px solid var(--color-border);
            z-index: 10;
        }

        .doc-nav ul {
            list-style: none;
            padding: 0;
            margin: 0;
            display: flex;
            flex-wrap: wrap;
            gap: 0.5rem;
            justify-content: center;
        }

        .doc-nav li {
            margin: 0;
        }

        .doc-nav a {
            color: var(--color-text-primary);
            text-decoration: none;
            display: block;
            padding: 0.5rem 1rem;
            border-radius: var(--radius);
            transition: all var(--transition-speed) ease;
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid var(--color-border);
            font-weight: 500;
        }

        .doc-nav a:hover {
            background: rgba(45, 212, 191, 0.14);
            color: var(--color-accent-teal);
            border-color: rgba(45, 212, 191, 0.35);
            transform: translateY(-2px);
        }

        .doc-section {
            background: var(--color-surface);
            padding: 2rem;
            border-radius: var(--radius);
            margin-bottom: 2rem;
            box-shadow: var(--card-shadow);
            border: 1px solid var(--color-border);
        }

        .doc-section h2 {
            color: var(--color-accent-teal);
            margin-top: 0;
            border-bottom: 2px solid rgba(45, 212, 191, 0.28);
            padding-bottom: 0.5rem;
        }

        .doc-section h3 {
            color: var(--color-accent-gold);
            margin-top: 1.5rem;
        }

        .doc-section p {
            line-height: 1.6;
            color: var(--color-text-secondary);
        }

        .doc-section ul, .doc-section ol {
            color: var(--color-text-secondary);
            line-height: 1.6;
            margin-bottom: 1.5rem;
            padding-left: 1.5rem;
        }

        .doc-section code {
            background: rgba(0, 0, 0, 0.3);
            padding: 0.2rem 0.4rem;
            border-radius: 4px;
            font-family: monospace;
            color: var(--color-accent-teal);
        }

        .feature-list {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1.5rem;
            margin-top: 1.5rem;
        }

        .feature-card {
            background: rgba(255, 255, 255, 0.035);
            padding: 1.5rem;
            border-radius: var(--radius);
            border: 1px solid var(--color-border);
            position: relative;
            overflow: hidden;
            box-shadow: 0 16px 36px rgba(0,0,0,0.22);
            transition: all var(--transition-speed) ease;
        }

        .feature-card::before {
            content: '';
            position: absolute;
            inset: 0 0 auto 0;
            height: 3px;
            background: linear-gradient(90deg, var(--color-accent-teal), var(--color-accent-gold), var(--color-accent-green));
        }

        .feature-card:hover {
            transform: translateY(-5px);
            background: rgba(255, 255, 255, 0.06);
            border-color: var(--color-border-strong);
        }

        .feature-card h4 {
            color: var(--color-accent-teal);
            margin-top: 0;
            font-size: 1.15rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .feature-card p {
            margin-bottom: 0;
            color: var(--color-text-secondary);
        }

        .about-content {
            background: rgba(255, 255, 255, 0.02);
            border-radius: var(--radius);
            padding: 2rem;
            margin-bottom: 2rem;
            border: 1px solid var(--color-border);
        }

        .highlight-text {
            color: var(--color-text-primary);
            font-size: 1.1em;
            line-height: 1.6;
            font-weight: 500;
        }

        .about-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
            gap: 1rem;
            margin-bottom: 2rem;
        }

        .about-card {
            background: rgba(255, 255, 255, 0.03);
            border: 1px solid var(--color-border);
            border-radius: var(--radius);
            padding: 1.5rem;
        }

        .about-card h3 {
            margin-top: 0;
            color: var(--color-accent-teal);
        }

        .developer-profile {
            display: grid;
            grid-template-columns: auto 1fr;
            gap: 1.25rem;
            align-items: center;
            margin-top: 2rem;
            padding-top: 2rem;
            border-top: 1px solid var(--color-border);
        }

        .developer-profile img {
            width: 110px;
            height: 110px;
            border-radius: 50%;
            object-fit: cover;
            border: 3px solid var(--color-accent-teal);
        }

        .developer-profile h3 {
            margin: 0 0 0.5rem;
            color: var(--color-text-primary);
        }

        .contact-links {
            display: flex;
            flex-wrap: wrap;
            gap: 0.75rem;
            margin-top: 1rem;
        }

        .contact-links a {
            color: var(--color-text-primary);
            text-decoration: none;
            background: rgba(255, 255, 255, 0.06);
            border: 1px solid var(--color-border);
            border-radius: var(--radius);
            padding: 0.65rem 0.9rem;
            transition: all var(--transition-speed) ease;
        }

        .contact-links a:hover {
            background: rgba(45, 212, 191, 0.14);
            border-color: var(--color-accent-teal);
            color: var(--color-accent-teal);
        }

        .back-to-top {
            position: fixed;
            bottom: 20px;
            right: 20px;
            background: linear-gradient(135deg, var(--color-accent-teal), var(--color-accent-green));
            color: #04110e;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            text-decoration: none;
            box-shadow: var(--card-shadow);
            transition: all var(--transition-speed) ease;
            z-index: 99;
        }

        .back-to-top:hover {
            transform: translateY(-5px);
            background: linear-gradient(135deg, #5eead4, #86efac);
        }

        @media (max-width: 768px) {
            .container {
                padding: 5.5rem 1rem 2rem;
            }

            .doc-header {
                padding: 1.5rem;
            }

            .doc-header h1 {
                font-size: 2em;
            }

            .doc-nav {
                top: 70px;
            }

            .doc-nav ul {
                flex-direction: column;
                align-items: stretch;
            }

            .developer-profile {
                grid-template-columns: 1fr;
                text-align: center;
                justify-items: center;
            }

            .contact-links {
                justify-content: center;
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
                <a href="${pageContext.request.contextPath}/documentation.jsp" class="nav-link active">
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
        <div class="doc-header">
            <h1>SEVENT-MS Documentation</h1>
        </div>

        <div class="doc-nav">
            <ul>
                <li><a href="#overview">Overview</a></li>
                <li><a href="#user-features">User Features</a></li>
                <li><a href="#admin-features">Admin Features</a></li>
                <li><a href="#event-management">Event Setup</a></li>
                <li><a href="#registration">Booking System</a></li>
                <li><a href="#security">Security</a></li>
                <li><a href="#technical">Technical Stack</a></li>
            </ul>
        </div>

        <section id="overview" class="doc-section">
            <h2>About the Project</h2>
            <div class="about-content">
                <div class="developer-info">
                    <h3>Developer</h3>
                    <p class="highlight-text">This SEVENT-MS platform is developed by Devendra Ambalkar, showcasing expertise in Java, Spring Boot, H2/PostgreSQL database mapping, and responsive full-stack web development.</p>
                </div>
                
                <div class="project-overview">
                    <h3>Project Overview</h3>
                    <p>SEVENT-MS is a comprehensive web application designed to streamline the process of managing events, registrations, and attendee tracking. Built from the ground up, this project demonstrates expertise in both frontend design systems and backend MVC architectures.</p>
                </div>
                
                <div class="about-grid">
                    <div class="about-card">
                        <h3><i class="fas fa-bullseye"></i> Project Mission</h3>
                        <p>To make event management more accessible and efficient through a clear, reliable platform for organizers and participants.</p>
                    </div>
                    <div class="about-card">
                        <h3><i class="fas fa-eye"></i> Development Vision</h3>
                        <p>To provide a scalable, user-friendly system that brings event creation, booking, tracking, and reporting into one workflow.</p>
                    </div>
                </div>

                <div class="developer-profile">
                    <img src="${pageContext.request.contextPath}/images/developer.jpg" alt="Developer Profile">
                    <div>
                        <h3>Devendra Ambalkar</h3>
                        <p>Software developer specializing in Java, Spring Framework, and full-stack web development, with hands-on experience in Spring Boot, Spring Security, Hibernate, H2, and relational databases.</p>
                        <div class="contact-links">
                            <a href="mailto:devendraambalkar11@gmail.com">
                                <i class="fas fa-envelope"></i> Email
                            </a>
                            <a href="https://github.com/Ambalkar/Event_Management.git" target="_blank">
                                <i class="fab fa-github"></i> GitHub
                            </a>
                            <a href="https://credora.space" target="_blank">
                                <i class="fas fa-graduation-cap"></i> Opportunities
                            </a>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="feature-list">
                <div class="feature-card">
                    <h4><i class="fas fa-code"></i> Full-Stack Development</h4>
                    <p>Complete implementation of both frontend and backend components using Spring Boot and JSPs.</p>
                </div>
                <div class="feature-card">
                    <h4><i class="fas fa-shield-alt"></i> Secure Architecture</h4>
                    <p>Robust password hashing using BCrypt and session authentication filters.</p>
                </div>
                <div class="feature-card">
                    <h4><i class="fas fa-mobile-alt"></i> Responsive Design</h4>
                    <p>Polished layout structure optimized for all screen sizes, including stacked mobile cards.</p>
                </div>
            </div>
        </section>

        <section id="user-features" class="doc-section">
            <h2>User Features</h2>
            <h3>Event Discovery</h3>
            <p>Users can browse through all scheduled simple events and major events. Major events render dynamic sub-event listings inline. Details available include:</p>
            <ul>
                <li>Event name, type, and comprehensive description</li>
                <li>Scheduled date and location venue</li>
                <li>Seat availability metrics (total spots, current occupancy, and available spots)</li>
            </ul>

            <h3>Instant Digital Reservations</h3>
            <p>Participants can secure booking slots instantly:</p>
            <ul>
                <li>Self-service booking forms mapped directly onto specific events and sub-events</li>
                <li>Real-time capacity verification preventing overbooking</li>
                <li>Receipt of a unique, persistent digital ID for check-in validation</li>
                <li>Email-based booking lookup to review details at any time</li>
            </ul>
        </section>

        <section id="admin-features" class="doc-section">
            <h2>Admin Features</h2>
            <h3>Central Console</h3>
            <p>Organizers use a control dashboard containing live metrics:</p>
            <ul>
                <li>Live metrics counters: Major events totals, sub-events totals, capacity totals, and cumulative bookings</li>
                <li>Interactive form to add, update, and delete events</li>
                <li>Upload custom images for branding</li>
                <li>Sub-event builder with capacity checking logic (sub-events must fit within parent capacity)</li>
            </ul>

            <h3>Participant Records</h3>
            <p>Administrators have complete visibility into participant rosters:</p>
            <ul>
                <li>Live roster rendering user name, email, event, date, booking type, booking date, and assigned Digital ID</li>
                <li>H2 database schema updates and query optimization</li>
            </ul>
        </section>

        <section id="event-management" class="doc-section">
            <h2>Event Setup</h2>
            <h3>Event Types</h3>
            <p>The system supports three distinct event configurations:</p>
            <ul>
                <li><code>Simple Event</code>: A standard event with independent scheduling, location, and guest limits.</li>
                <li><code>Major Event</code>: A parent container that allows nested sub-events and maps tickets to passes.</li>
                <li><code>Sub Event</code>: A nested event linked directly to a parent Major Event, inheriting timeline rules.</li>
            </ul>

            <h3>Creation Workflow</h3>
            <ol>
                <li>Sign in using authorized Admin credentials</li>
                <li>Select the event type from the dropdown selector</li>
                <li>Fill in name, date, location, and descriptions</li>
                <li>Set the capacity limit and upload a banner image</li>
                <li>Submit the form to commit to the database</li>
            </ol>
        </section>

        <section id="registration" class="doc-section">
            <h2>Booking System</h2>
            <h3>Reservation Logic</h3>
            <p>Bookings are validated server-side to guarantee integrity:</p>
            <ul>
                <li>Capacity limits are checked before commit; full events show a custom "Housefull" indicator</li>
                <li>A sub-event reservation decreases the remaining capacity of the sub-event</li>
                <li>Admin operations ensure database schema columns exist and update tables using standard subqueries</li>
            </ul>

            <h3>Digital Confirmation Details</h3>
            <p>Every booking triggers the generation of a unique verification token:</p>
            <ul>
                <li>Assigned alphanumeric Digital ID shown in booking cards</li>
                <li>Lookup verification allows checking records using participant email</li>
            </ul>
        </section>

        <section id="security" class="doc-section">
            <h2>Security</h2>
            <h3>Account Protection</h3>
            <p>Authentication and credentials utilize industry standards:</p>
            <ul>
                <li>Admin credentials are externalized to <code>application.properties</code> and environments instead of being hardcoded</li>
                <li>Password validation matches input using <code>BCryptPasswordEncoder</code></li>
                <li>Session tracking filters unauthorized traffic from administrative endpoints</li>
            </ul>

            <h3>Data Validation</h3>
            <p>Input and database structures are guarded against integrity issues:</p>
            <ul>
                <li>Forms enforce required fields and input formats client-side</li>
                <li>File upload structures check for image attachments</li>
                <li>SQL queries use JDBC <code>PreparedStatement</code> parameters, preventing SQL injection</li>
            </ul>
        </section>

        <section id="technical" class="doc-section">
            <h2>Technical Stack</h2>
            <h3>Technologies Utilized</h3>
            <ul>
                <li><strong>Framework:</strong> Spring Boot (MVC, Data, Controllers, Filters)</li>
                <li><strong>Frontend:</strong> JSP (JavaServer Pages), JSTL, Shared Design Stylesheet system</li>
                <li><strong>Database:</strong> H2 Database Engine (Local) / PostgreSQL (Production)</li>
                <li><strong>Build System:</strong> Apache Maven</li>
            </ul>

            <h3>Local Deployment Requirements</h3>
            <ul>
                <li>Java JDK 8 / JDK 11</li>
                <li>Maven build tool (or <code>mvnw</code> wrapper)</li>
                <li>Active environment properties or <code>.env</code> configurations</li>
            </ul>
        </section>

        <a href="#" class="back-to-top" style="display: none;">
            <i class="fas fa-arrow-up"></i>
        </a>
    </div>

    <script>
        // Smooth scrolling for anchor links
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        });

        // Back to top button visibility
        window.addEventListener('scroll', function() {
            const backToTop = document.querySelector('.back-to-top');
            if (window.scrollY > 300) {
                backToTop.style.display = 'flex';
            } else {
                backToTop.style.display = 'none';
            }
        });

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
    </script>
</body>
</html>
