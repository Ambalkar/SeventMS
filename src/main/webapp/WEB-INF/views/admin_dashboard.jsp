<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Portal - SEVENT-MS</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        body::before {
            content: '';
            position: fixed;
            inset: 0;
            pointer-events: none;
            background-image:
                linear-gradient(rgba(255,255,255,0.025) 1px, transparent 1px),
                linear-gradient(90deg, rgba(255,255,255,0.025) 1px, transparent 1px);
            background-size: 36px 36px;
            mask-image: linear-gradient(to bottom, rgba(0,0,0,0.75), transparent 85%);
        }

        .admin-shell {
            width: min(1440px, calc(100% - 32px));
            margin: 0 auto;
            padding: 100px 0 48px;
            position: relative;
            z-index: 1;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 14px;
            margin-top: 20px;
        }

        .stat-card {
            min-height: 132px;
            padding: 18px;
            border-radius: var(--radius);
            border: 1px solid var(--color-border);
            background: var(--color-surface);
            box-shadow: var(--card-shadow);
            position: relative;
            overflow: hidden;
        }

        .stat-card::before {
            content: '';
            position: absolute;
            inset: 0 0 auto 0;
            height: 3px;
            background: linear-gradient(90deg, var(--color-accent-teal), var(--color-accent-gold), var(--color-accent-green));
        }

        .stat-card i {
            color: var(--color-accent-gold);
            font-size: 1.25rem;
        }

        .stat-card strong {
            display: block;
            margin-top: 18px;
            font-size: 2rem;
            line-height: 1;
            color: var(--color-text-primary);
        }

        .stat-card span {
            display: block;
            margin-top: 8px;
            color: var(--color-text-secondary);
            font-size: 0.9rem;
        }

        .console-grid {
            display: grid;
            grid-template-columns: minmax(320px, 420px) minmax(0, 1fr);
            gap: 20px;
            margin-top: 20px;
            align-items: start;
        }

        .panel {
            border-radius: var(--radius);
            border: 1px solid var(--color-border);
            background: var(--color-surface);
            box-shadow: var(--card-shadow);
            overflow: hidden;
        }

        .panel-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 14px;
            padding: 18px 20px;
            border-bottom: 1px solid var(--color-border);
            background:
                linear-gradient(135deg, rgba(45, 212, 191, 0.08), transparent),
                linear-gradient(135deg, rgba(255,255,255,0.08), rgba(255,255,255,0.02));
        }

        .panel-header h2 {
            margin: 0;
            font-size: 1.1rem;
            color: var(--color-text-primary);
        }

        .panel-header p {
            margin: 4px 0 0;
            color: var(--color-text-secondary);
            font-size: 0.86rem;
        }

        .panel-badge {
            white-space: nowrap;
            padding: 0.45rem 0.7rem;
            border-radius: var(--radius);
            background: rgba(255,255,255,0.08);
            border: 1px solid var(--color-border);
            color: var(--color-accent-teal);
            font-size: 0.78rem;
            font-weight: 700;
            text-transform: uppercase;
        }

        .event-form {
            padding: 20px;
        }

        .sub-event-builder {
            display: none;
            margin-top: 8px;
            padding: 14px;
            border-radius: var(--radius);
            border: 1px solid var(--color-border);
            background: rgba(255,255,255,0.045);
        }

        .sub-event-row {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 10px;
            padding: 12px 0;
            border-bottom: 1px solid var(--color-border);
        }

        .sub-event-row:last-child {
            border-bottom: 0;
        }

        .event-type-chip {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            margin-top: 8px;
            color: var(--color-accent-gold);
            font-size: 0.78rem;
            font-weight: 700;
            text-transform: uppercase;
        }

        .sub-event-item {
            margin-top: 12px;
            padding: 12px;
            border-radius: var(--radius);
            background: rgba(255,255,255,0.045);
            border: 1px solid var(--color-border);
        }

        .sub-event-form {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 10px;
            margin-top: 12px;
            padding-top: 12px;
            border-top: 1px solid var(--color-border);
        }

        .sub-event-form textarea,
        .sub-event-form .wide {
            grid-column: 1 / -1;
        }

        .event-thumb {
            width: 100%;
            max-width: 220px;
            height: 140px;
            object-fit: contain;
            object-position: center;
            background: rgba(255, 255, 255, 0.045);
            border-radius: 8px;
            border: 1px solid var(--color-border);
            margin-bottom: 10px;
            display: block;
        }

        .event-thumb.small {
            max-width: 150px;
            height: 96px;
            margin-bottom: 8px;
        }

        th {
            position: sticky;
            top: 0;
            z-index: 1;
        }

        tbody tr:hover {
            background: rgba(255,255,255,0.045);
        }

        .id-cell,
        .count-cell {
            color: var(--color-accent-teal);
            font-weight: 700;
        }

        .event-name,
        .user-name {
            color: var(--color-text-primary);
            font-weight: 700;
        }

        .muted {
            color: var(--color-text-muted);
        }

        .status-chip,
        .digital-chip {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 0.42rem 0.65rem;
            border-radius: var(--radius);
            border: 1px solid var(--color-border);
            background: rgba(255,255,255,0.065);
            color: var(--color-text-primary);
            font-weight: 700;
            font-size: 0.78rem;
            white-space: nowrap;
        }

        .status-chip.good {
            color: var(--color-success);
            border-color: rgba(52, 211, 153, 0.28);
            background: rgba(52, 211, 153, 0.08);
        }

        .row-actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }

        .row-actions .btn {
            min-height: 34px;
            padding: 0.45rem 0.65rem;
            font-size: 0.78rem;
        }

        .empty-state {
            text-align: center;
            color: var(--color-text-secondary);
            padding: 34px 16px;
        }

        .empty-state i {
            display: block;
            margin-bottom: 10px;
            color: var(--color-text-muted);
            font-size: 1.6rem;
        }

        @media (max-width: 980px) {
            .console-grid,
            .stats-grid {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 640px) {
            .admin-shell {
                width: min(100% - 20px, 1440px);
                padding-top: 100px;
            }

            .hero-panel,
            .event-form,
            .panel-header {
                padding: 16px;
            }

            .form-actions .btn {
                width: 100%;
            }

            .hero-copy h2 {
                font-size: 2.4rem;
            }
        }
    </style>
</head>
<body>
    <!-- Navigation Bar -->
    <nav class="nav-bar">
        <div class="nav-container">
            <a href="${pageContext.request.contextPath}/" class="nav-logo">
                <i class="fas fa-calendar-alt"></i> SeventMS Admin
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
                <a href="${pageContext.request.contextPath}/myEvents" class="nav-link">
                    <i class="fas fa-ticket-alt"></i> My Events
                </a>
                <a href="${pageContext.request.contextPath}/admin" class="nav-link active">
                    <i class="fas fa-user-shield"></i> Admin
                </a>
                <a href="${pageContext.request.contextPath}/logout" class="nav-link">
                    <i class="fas fa-sign-out-alt"></i> Logout
                </a>
            </div>
        </div>
    </nav>

    <main class="admin-shell">

        <section class="hero-panel">
            <div class="hero-copy">
                <p class="eyebrow">Control Room</p>
                <h2>Admin Dashboard</h2>
                <p>Track event capacity, manage live event records, and review participant booking details from one dark metallic console.</p>
            </div>
        </section>

        <section class="stats-grid" aria-label="Admin dashboard summary">
            <article class="stat-card">
                <i class="fas fa-calendar-check"></i>
                <strong>${majorEventCount}</strong>
                <span>Total major events</span>
            </article>
            <article class="stat-card">
                <i class="fas fa-stream"></i>
                <strong>${subEventCount}</strong>
                <span>Total sub events</span>
            </article>
            <article class="stat-card">
                <i class="fas fa-ticket-alt"></i>
                <strong>${totalBookings}</strong>
                <span>Total bookings</span>
            </article>
            <article class="stat-card">
                <i class="fas fa-chair"></i>
                <strong>${totalCapacity}</strong>
                <span>Seats available</span>
            </article>
        </section>

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

        <section class="console-grid">
            <aside class="panel">
                <div class="panel-header">
                    <div>
                        <h2 id="formTitle">Create Event</h2>
                        <p>Add a new event or edit a selected event.</p>
                    </div>
                    <span class="panel-badge">Event</span>
                </div>

                <form method="post" action="${pageContext.request.contextPath}/admin" id="eventForm" class="event-form" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="add" id="formAction">
                    <input type="hidden" name="event_id" id="eventId">

                    <div class="form-group">
                        <label for="event_type"><i class="fas fa-layer-group"></i> Event Type</label>
                        <select id="event_type" name="event_type">
                            <option value="SIMPLE">Simple Event</option>
                            <option value="MAJOR">Major Event</option>
                            <option value="SUB">Sub Event</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="name"><i class="fas fa-signature"></i> Event Name</label>
                        <input type="text" id="name" name="name" placeholder="Annual Tech Summit" required>
                    </div>

                    <div class="form-group">
                        <label for="date"><i class="fas fa-calendar-day"></i> Date</label>
                        <input type="date" id="date" name="date" required>
                    </div>

                    <div class="form-group">
                        <label for="location"><i class="fas fa-map-marker-alt"></i> Location</label>
                        <input type="text" id="location" name="location" placeholder="Main Auditorium" required>
                    </div>

                    <div class="form-group">
                        <label for="description"><i class="fas fa-align-left"></i> Description</label>
                        <textarea id="description" name="description" placeholder="Add event details for participants."></textarea>
                    </div>

                    <div class="form-group">
                        <label for="guest_limit"><i class="fas fa-users"></i> Guest Limit</label>
                        <input type="number" id="guest_limit" name="guest_limit" min="1" placeholder="100" required>
                    </div>

                    <div class="form-group">
                        <label for="image"><i class="fas fa-image"></i> Event Image (1700 x 950 px recommended)</label>
                        <input type="file" id="image" name="image" accept="image/*">
                    </div>

                    <div class="form-group sub-event-builder" id="subEventBuilder">
                        <label><i class="fas fa-stream"></i> Sub Events</label>
                        <div id="subEventRows"></div>
                        <div class="sub-event-actions">
                            <button type="button" class="btn btn-secondary" onclick="addSubEventRow()">
                                <i class="fas fa-plus"></i> Add Sub Event
                            </button>
                            <span class="muted" id="subCapacityHint">Sub-event limits must fit inside the major event limit.</span>
                        </div>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary" id="submitBtn">
                            <i class="fas fa-plus-circle"></i> Add Event
                        </button>
                        <button type="button" class="btn btn-secondary" id="cancelBtn" style="display: none;" onclick="resetForm()">
                            <i class="fas fa-times"></i> Cancel
                        </button>
                    </div>
                </form>
            </aside>

            <section class="panel">
                <div class="panel-header">
                    <div>
                        <h2>Event Inventory</h2>
                        <p>Update schedules, venue details, and booking limits.</p>
                    </div>
                    <span class="panel-badge">${majorEventCount} Major / ${subEventCount} Sub</span>
                </div>

                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Event</th>
                                <th>Date</th>
                                <th>Location</th>
                                <th>Capacity</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="event" items="${events}">
                                <tr>
                                    <td class="id-cell" data-label="ID">#${event.id}</td>
                                    <td class="wide-cell" data-label="Event">
                                        <c:if test="${not empty event.imagePath}">
                                            <img src="${pageContext.request.contextPath}${event.imagePath}" alt="${event.name}" class="event-thumb">
                                        </c:if>
                                        <div class="event-name">${event.name}</div>
                                        <div class="event-type-chip">
                                            <c:choose>
                                                <c:when test="${event.majorEvent}">
                                                    <i class="fas fa-layer-group"></i> Major Event
                                                </c:when>
                                                <c:otherwise>
                                                    <i class="fas fa-calendar-day"></i> Simple Event
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="muted">${event.description}</div>
                                        <c:if test="${event.majorEvent}">
                                            <div class="sub-event-list">
                                                <c:forEach var="subEvent" items="${event.subEvents}">
                                                    <div class="sub-event-item">
                                                        <c:if test="${not empty subEvent.imagePath}">
                                                            <img src="${pageContext.request.contextPath}${subEvent.imagePath}" alt="${subEvent.name}" class="event-thumb small">
                                                        </c:if>
                                                        <div class="event-name">${subEvent.name}</div>
                                                        <div class="muted">${subEvent.date} | ${subEvent.location}</div>
                                                        <div class="muted">${subEvent.description}</div>
                                                        <div class="count-cell">${subEvent.currentGuests} / ${subEvent.guestLimit} (${subEvent.availableSpots} open)</div>
                                                        <button type="button" class="btn btn-secondary update-btn sub-event-edit"
                                                            data-id="${subEvent.id}"
                                                            data-name="${subEvent.name}"
                                                            data-date="${subEvent.date}"
                                                            data-location="${subEvent.location}"
                                                            data-description="${subEvent.description}"
                                                            data-guest-limit="${subEvent.guestLimit}"
                                                            data-event-type="${subEvent.eventType}">
                                                            <i class="fas fa-edit"></i> Edit Sub Event
                                                        </button>
                                                    </div>
                                                </c:forEach>
                                                <form method="post" action="${pageContext.request.contextPath}/admin" class="sub-event-form" enctype="multipart/form-data">
                                                    <input type="hidden" name="action" value="addSubEvent">
                                                    <input type="hidden" name="parent_event_id" value="${event.id}">
                                                    <input type="text" name="name" placeholder="Sub-event name" required>
                                                    <input type="date" name="date" required>
                                                    <input type="number" name="guest_limit" min="1" max="${event.subCapacityRemaining}" placeholder="Guest limit, max ${event.subCapacityRemaining}" required>
                                                    <input type="text" name="location" placeholder="Location" class="wide" required>
                                                    <textarea name="description" placeholder="Description" class="wide"></textarea>
                                                    <span class="muted wide">Sub-Event Image (1700 x 950 px recommended)</span>
                                                    <input type="file" name="image" accept="image/*" class="wide" required>
                                                    <span class="muted wide">Remaining sub-event capacity: ${event.subCapacityRemaining}</span>
                                                    <button type="submit" class="btn btn-primary wide">
                                                        <i class="fas fa-plus-circle"></i> Add Sub Event
                                                    </button>
                                                </form>
                                            </div>
                                        </c:if>
                                    </td>
                                    <td data-label="Date">${event.date}</td>
                                    <td data-label="Location">${event.location}</td>
                                    <td class="count-cell" data-label="Capacity">
                                        ${event.currentGuests} / ${event.guestLimit}
                                        <c:if test="${event.majorEvent}">
                                            <div class="muted">Sub limits: ${event.subGuestLimitTotal} / ${event.guestLimit}</div>
                                        </c:if>
                                    </td>
                                    <td data-label="Status">
                                        <c:choose>
                                            <c:when test="${event.currentGuests lt event.guestLimit}">
                                                <span class="status-chip good"><i class="fas fa-circle"></i> Open</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-chip"><i class="fas fa-lock"></i> Full</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td data-label="Actions">
                                        <div class="row-actions">
                                            <button type="button" class="btn btn-primary update-btn"
                                                data-id="${event.id}"
                                                data-name="${event.name}"
                                                data-date="${event.date}"
                                                data-location="${event.location}"
                                                data-description="${event.description}"
                                                data-guest-limit="${event.guestLimit}"
                                                data-event-type="${event.eventType}">
                                                <i class="fas fa-edit"></i> Edit
                                            </button>
                                            <form method="post" action="${pageContext.request.contextPath}/admin">
                                                <input type="hidden" name="event_id" value="${event.id}">
                                                <button type="submit" name="action" value="delete" class="btn btn-danger"
                                                        onclick="return confirm('Are you sure you want to delete this event?');">
                                                    <i class="fas fa-trash"></i> Delete
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty events}">
                                <tr>
                                    <td colspan="7" class="empty-state">
                                        <i class="fas fa-calendar-times"></i>
                                        No events found.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </section>

        <section class="panel" id="bookings" style="margin-top: 20px;">
            <div class="panel-header">
                <div>
                    <h2>User Booking Details</h2>
                    <p>Participant contact details, assigned events, and digital IDs.</p>
                </div>
                <span class="panel-badge">${totalBookings} Bookings</span>
            </div>

            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>User</th>
                            <th>Email</th>
                            <th>Event</th>
                            <th>Event Date</th>
                            <th>Digital ID</th>
                            <th>Type</th>
                            <th>Booked On</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="booking" items="${bookings}">
                            <tr>
                                <td class="id-cell" data-label="ID">#${booking.id}</td>
                                <td class="user-name" data-label="User">${booking.userName}</td>
                                <td data-label="Email">${booking.userEmail}</td>
                                <td data-label="Event">${booking.eventName}</td>
                                <td data-label="Event Date">${booking.eventDate}</td>
                                <td data-label="Digital ID"><span class="digital-chip">${booking.digitalId}</span></td>
                                <td data-label="Type">${booking.bookingType}</td>
                                <td data-label="Booked On">${booking.bookingDate}</td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty bookings}">
                            <tr>
                                    <td colspan="8" class="empty-state">
                                    <i class="fas fa-user-clock"></i>
                                    No user bookings found.
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </main>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Mobile navigation toggle
            const navToggle = document.getElementById('navToggle');
            const navLinks = document.getElementById('navLinks');

            if (navToggle && navLinks) {
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
            }

            // Update event click listener
            document.querySelectorAll('.update-btn').forEach(button => {
                button.addEventListener('click', function() {
                    const eventData = {
                        id: this.dataset.id,
                        name: this.dataset.name,
                        date: this.dataset.date,
                        location: this.dataset.location,
                        description: this.dataset.description,
                        guestLimit: this.dataset.guestLimit,
                        eventType: this.dataset.eventType
                    };

                    document.getElementById('formTitle').textContent = 'Update Event';
                    document.getElementById('formAction').value = 'update';
                    document.getElementById('eventId').value = eventData.id;
                    document.getElementById('name').value = eventData.name;
                    document.getElementById('date').value = eventData.date;
                    document.getElementById('location').value = eventData.location;
                    document.getElementById('description').value = eventData.description;
                    document.getElementById('guest_limit').value = eventData.guestLimit;
                    document.getElementById('event_type').value = eventData.eventType || 'SIMPLE';
                    toggleSubEventBuilder();
                    document.getElementById('submitBtn').innerHTML = '<i class="fas fa-save"></i> Update Event';
                    document.getElementById('cancelBtn').style.display = 'inline-flex';
                    document.getElementById('eventForm').scrollIntoView({ behavior: 'smooth', block: 'start' });
                });
            });

            // Form validation and input listeners
            const eventTypeEl = document.getElementById('event_type');
            if (eventTypeEl) {
                eventTypeEl.addEventListener('change', toggleSubEventBuilder);
            }
            const guestLimitEl = document.getElementById('guest_limit');
            if (guestLimitEl) {
                guestLimitEl.addEventListener('input', updateSubCapacityHint);
            }
            
            const eventForm = document.getElementById('eventForm');
            if (eventForm) {
                eventForm.addEventListener('submit', function(event) {
                    const selectedType = document.getElementById('event_type').value;
                    if (selectedType === 'MAJOR' && !isSubCapacityValid()) {
                        event.preventDefault();
                        alert('Combined sub-event guest limits cannot exceed the major event guest limit.');
                        return;
                    }
                    if (selectedType === 'MAJOR' && document.getElementById('formAction').value === 'add'
                             && !document.getElementById('image').value) {
                        event.preventDefault();
                        alert('Major events require an image.');
                        return;
                    }
                    
                    const btn = this.querySelector('button[type="submit"]');
                    if (btn) {
                        btn.classList.add('loading');
                        setTimeout(() => { btn.disabled = true; }, 10);
                    }
                });
            }
            
            // Other forms loading states
            document.querySelectorAll('form:not(#eventForm)').forEach(form => {
                form.addEventListener('submit', function() {
                    const btn = this.querySelector('button[type="submit"]');
                    if (btn) {
                        btn.classList.add('loading');
                        setTimeout(() => { btn.disabled = true; }, 10);
                    }
                });
            });

            toggleSubEventBuilder();
            updateSubCapacityHint();
        });

        function resetForm() {
            document.getElementById('formTitle').textContent = 'Create Event';
            document.getElementById('formAction').value = 'add';
            document.getElementById('eventId').value = '';
            document.getElementById('eventForm').reset();
            document.getElementById('subEventRows').innerHTML = '';
            toggleSubEventBuilder();
            updateSubCapacityHint();
            document.getElementById('submitBtn').innerHTML = '<i class="fas fa-plus-circle"></i> Add Event';
            document.getElementById('cancelBtn').style.display = 'none';
        }

        function toggleSubEventBuilder() {
            const isMajor = document.getElementById('event_type').value === 'MAJOR';
            const isCreateMode = document.getElementById('formAction').value === 'add';
            document.getElementById('subEventBuilder').style.display = isMajor && isCreateMode ? 'block' : 'none';
            updateSubCapacityHint();
        }

        function addSubEventRow() {
            const row = document.createElement('div');
            row.className = 'sub-event-row';
            row.innerHTML = `
                <input type="text" name="sub_name" placeholder="Sub-event name">
                <input type="date" name="sub_date">
                <input type="text" name="sub_location" placeholder="Location" class="wide">
                <textarea name="sub_description" placeholder="Description" class="wide"></textarea>
                <input type="number" name="sub_guest_limit" min="1" placeholder="Guest limit" class="sub-guest-limit" oninput="updateSubCapacityHint()">
                <span class="muted wide" style="margin-top: 5px;">Sub-Event Image (1700 x 950 px recommended)</span>
                <input type="file" name="sub_image" accept="image/*" class="wide" required>
                <button type="button" class="btn btn-danger" onclick="this.closest('.sub-event-row').remove(); updateSubCapacityHint();">
                    <i class="fas fa-trash"></i> Remove
                </button>
            `;
            document.getElementById('subEventRows').appendChild(row);
            updateSubCapacityHint();
        }

        function getSubLimitTotal() {
            return Array.from(document.querySelectorAll('.sub-guest-limit'))
                .reduce((total, input) => total + (Number(input.value) || 0), 0);
        }

        function isSubCapacityValid() {
            const parentLimit = Number(document.getElementById('guest_limit').value) || 0;
            return getSubLimitTotal() <= parentLimit;
        }

        function updateSubCapacityHint() {
            const hint = document.getElementById('subCapacityHint');
            if (!hint) {
                return;
            }
            const parentLimit = Number(document.getElementById('guest_limit').value) || 0;
            const subTotal = getSubLimitTotal();
            const remaining = Math.max(parentLimit - subTotal, 0);
            hint.textContent = 'Sub-event limits: ' + subTotal + ' / ' + parentLimit + ' used. Remaining: ' + remaining + '.';
            hint.style.color = subTotal > parentLimit ? 'var(--danger)' : 'var(--muted)';
        }
    </script>
</body>
</html>
