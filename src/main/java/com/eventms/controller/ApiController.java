package com.eventms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/events")
    public ResponseEntity<?> events(@RequestParam(value = "email", required = false) String email,
            HttpServletRequest request) {
        try (Connection conn = dataSource.getConnection()) {
            ensureBookingSchema(conn);
            List<EventDto> events = loadEvents(conn, email, request);
            return ResponseEntity.ok(events);
        } catch (SQLException e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load events: " + e.getMessage());
        }
    }

    @GetMapping("/my-events")
    public ResponseEntity<?> myEvents(@RequestParam("email") String email, HttpServletRequest request) {
        if (isBlank(email)) {
            return error(HttpStatus.BAD_REQUEST, "Email is required.");
        }

        List<ParticipationDto> participations = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            ensureBookingSchema(conn);

            String sql = "SELECT b.user_name AS participant_name, b.user_email, b.digital_id, "
                    + "e.event_id, e.name AS event_name, e.date, e.location, e.description, "
                    + "e.event_type, e.parent_event_id, e.image_path, p.name AS parent_event_name, b.booking_type "
                    + "FROM bookings b "
                    + "JOIN events e ON b.event_id = e.event_id "
                    + "LEFT JOIN events p ON e.parent_event_id = p.event_id "
                    + "WHERE lower(b.user_email) = lower(?) "
                    + "ORDER BY e.date DESC, b.booking_date DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        EventDto event = new EventDto();
                        event.id = rs.getInt("event_id");
                        event.name = rs.getString("event_name");
                        event.date = rs.getString("date");
                        event.location = rs.getString("location");
                        event.description = rs.getString("description");
                        event.eventType = rs.getString("event_type");
                        event.parentEventId = rs.getObject("parent_event_id") == null ? null : rs.getInt("parent_event_id");
                        event.parentEventName = rs.getString("parent_event_name");
                        event.imagePath = rs.getString("image_path");
                        event.imageUrl = absoluteUrl(request, event.imagePath);
                        event.bookingStatus = "MAJOR_EVENT".equals(rs.getString("booking_type"))
                                ? "Major Event Pass"
                                : "Sub Event Booking";

                        ParticipationDto participation = new ParticipationDto();
                        participation.name = rs.getString("participant_name");
                        participation.email = rs.getString("user_email");
                        participation.digitalId = rs.getString("digital_id");
                        participation.bookingType = rs.getString("booking_type");
                        participation.event = event;
                        participations.add(participation);
                    }
                }
            }
            return ResponseEntity.ok(participations);
        } catch (SQLException e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load bookings: " + e.getMessage());
        }
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> bookEvent(@RequestBody BookingRequest requestBody) {
        if (requestBody == null || requestBody.eventId == null || isBlank(requestBody.name) || isBlank(requestBody.email)) {
            return error(HttpStatus.BAD_REQUEST, "eventId, name, and email are required.");
        }

        try (Connection conn = dataSource.getConnection()) {
            ensureBookingSchema(conn);
            conn.setAutoCommit(false);

            BookingPlan plan = buildBookingPlan(conn, requestBody.eventId, requestBody.email);
            if (!plan.isBookable()) {
                conn.rollback();
                return error(HttpStatus.CONFLICT, plan.message);
            }

            reserveSeats(conn, plan.eventIdsToIncrement);
            String digitalId = insertBooking(conn, requestBody.eventId, requestBody.name.trim(),
                    requestBody.email.trim(), plan.bookingType);
            conn.commit();

            BookingResponse response = new BookingResponse();
            response.success = true;
            response.message = plan.message;
            response.eventId = requestBody.eventId;
            response.bookingType = plan.bookingType;
            response.digitalId = digitalId;
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SQLException e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Could not book event: " + e.getMessage());
        }
    }

    private List<EventDto> loadEvents(Connection conn, String email, HttpServletRequest request) throws SQLException {
        List<EventDto> events = new ArrayList<>();
        Map<Integer, EventDto> eventMap = new LinkedHashMap<>();

        String sql = "SELECT e.event_id, e.name, e.date, e.location, e.description, e.guest_limit, "
                + "e.current_guests, e.event_type, e.parent_event_id, e.image_path, p.name AS parent_event_name "
                + "FROM events e "
                + "LEFT JOIN events p ON e.parent_event_id = p.event_id "
                + "ORDER BY COALESCE(e.parent_event_id, e.event_id), e.parent_event_id NULLS FIRST, e.date";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                EventDto event = new EventDto();
                event.id = rs.getInt("event_id");
                event.name = rs.getString("name");
                event.date = rs.getString("date");
                event.location = rs.getString("location");
                event.description = rs.getString("description");
                event.guestLimit = rs.getInt("guest_limit");
                event.currentGuests = rs.getInt("current_guests");
                event.availableSpots = Math.max(event.guestLimit - event.currentGuests, 0);
                event.eventType = rs.getString("event_type");
                event.parentEventId = rs.getObject("parent_event_id") == null ? null : rs.getInt("parent_event_id");
                event.parentEventName = rs.getString("parent_event_name");
                event.imagePath = rs.getString("image_path");
                event.imageUrl = absoluteUrl(request, event.imagePath);

                eventMap.put(event.id, event);
                if (event.parentEventId == null) {
                    events.add(event);
                } else if (eventMap.containsKey(event.parentEventId)) {
                    eventMap.get(event.parentEventId).subEvents.add(event);
                }
            }
        }

        applyBookingState(conn, events, email);
        return events;
    }

    private void applyBookingState(Connection conn, List<EventDto> events, String email) throws SQLException {
        if (isBlank(email)) {
            for (EventDto event : events) {
                event.canBook = event.availableSpots > 0;
                for (EventDto subEvent : event.subEvents) {
                    subEvent.canBook = subEvent.availableSpots > 0;
                }
            }
            return;
        }

        Set<Integer> bookedEventIds = loadExistingBookings(conn, email);
        for (EventDto event : events) {
            boolean hasMajorPass = bookedEventIds.contains(event.id);
            event.bookedByCurrentUser = hasMajorPass;
            event.canBook = !hasMajorPass && canBookTopLevelEvent(event, bookedEventIds);
            if (hasMajorPass) {
                event.bookingStatus = "Already Booked";
            } else if (event.isMajorEvent() && hasAnySubBooking(event, bookedEventIds)) {
                event.bookingStatus = "Upgrade Available";
            }

            for (EventDto subEvent : event.subEvents) {
                boolean bookedSub = bookedEventIds.contains(subEvent.id);
                subEvent.bookedByCurrentUser = bookedSub;
                subEvent.includedInMajorPass = hasMajorPass;
                subEvent.canBook = subEvent.availableSpots > 0 && !bookedSub && !hasMajorPass;
                if (hasMajorPass) {
                    subEvent.bookingStatus = "Included in Major Event Pass";
                } else if (bookedSub) {
                    subEvent.bookingStatus = "Already Booked";
                }
            }
        }
    }

    private BookingPlan buildBookingPlan(Connection conn, int eventId, String email) throws SQLException {
        EventRow selected = loadSelectedEventForUpdate(conn, eventId);
        if (selected == null) {
            return BookingPlan.blocked("Event not found.");
        }

        Set<Integer> existingBookings = loadExistingBookings(conn, email);
        int majorEventId = selected.parentEventId == null ? selected.id : selected.parentEventId;
        boolean hasMajorPass = existingBookings.contains(majorEventId);

        if (selected.parentEventId != null) {
            if (hasMajorPass) {
                return BookingPlan.blocked("Included in Major Event Pass.");
            }
            if (existingBookings.contains(selected.id)) {
                return BookingPlan.blocked("You already booked this sub event.");
            }

            List<Integer> toIncrement = new ArrayList<>();
            if (!hasAnyExistingSubBooking(conn, majorEventId, existingBookings)) {
                toIncrement.add(majorEventId);
            }
            toIncrement.add(selected.id);
            lockEventRows(conn, toIncrement);
            return BookingPlan.allowed("SUB_EVENT", toIncrement, "Sub event booked successfully.");
        }

        if (existingBookings.contains(selected.id)) {
            return BookingPlan.blocked("You already booked this event.");
        }

        List<Integer> toIncrement = new ArrayList<>();
        boolean alreadyOccupiesMajorSeat = "MAJOR".equalsIgnoreCase(selected.eventType)
                && hasAnyExistingSubBooking(conn, selected.id, existingBookings);
        if (!hasMajorPass && !alreadyOccupiesMajorSeat) {
            toIncrement.add(selected.id);
        }

        if ("MAJOR".equalsIgnoreCase(selected.eventType)) {
            for (Integer subEventId : loadSubEventIds(conn, selected.id)) {
                if (!existingBookings.contains(subEventId)) {
                    toIncrement.add(subEventId);
                }
            }
        }

        lockEventRows(conn, toIncrement);
        String bookingType = "MAJOR".equalsIgnoreCase(selected.eventType) ? "MAJOR_EVENT" : "SUB_EVENT";
        boolean hasExistingSubBooking = "MAJOR_EVENT".equals(bookingType)
                && hasAnyExistingSubBooking(conn, selected.id, existingBookings);
        String message = "MAJOR_EVENT".equals(bookingType)
                ? "Major event booked successfully. Your pass includes all remaining sub events."
                : "Event booked successfully.";
        if (hasExistingSubBooking) {
            message = "Major event pass added. Your previous sub-event seats were preserved.";
        }
        return BookingPlan.allowed(bookingType, toIncrement, message);
    }

    private EventRow loadSelectedEventForUpdate(Connection conn, int eventId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT event_id, event_type, parent_event_id FROM events WHERE event_id = ? FOR UPDATE")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new EventRow(rs.getInt("event_id"), rs.getString("event_type"),
                        rs.getObject("parent_event_id") == null ? null : rs.getInt("parent_event_id"));
            }
        }
    }

    private Set<Integer> loadExistingBookings(Connection conn, String email) throws SQLException {
        Set<Integer> bookings = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT event_id FROM bookings WHERE lower(user_email) = lower(?)")) {
            ps.setString(1, email == null ? "" : email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(rs.getInt("event_id"));
                }
            }
        }
        return bookings;
    }

    private List<Integer> loadSubEventIds(Connection conn, int parentEventId) throws SQLException {
        List<Integer> subEventIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT event_id FROM events WHERE parent_event_id = ? ORDER BY date, event_id")) {
            ps.setInt(1, parentEventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    subEventIds.add(rs.getInt("event_id"));
                }
            }
        }
        return subEventIds;
    }

    private void lockEventRows(Connection conn, List<Integer> eventIds) throws SQLException {
        if (eventIds.isEmpty()) {
            return;
        }
        StringBuilder sql = new StringBuilder("SELECT event_id FROM events WHERE event_id IN (");
        appendPlaceholders(sql, eventIds.size());
        sql.append(") ORDER BY event_id FOR UPDATE");
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindIds(ps, eventIds);
            ps.executeQuery().close();
        }
    }

    private void reserveSeats(Connection conn, List<Integer> eventIds) throws SQLException {
        for (Integer eventId : eventIds) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE events SET current_guests = current_guests + 1 WHERE event_id = ? AND current_guests < guest_limit")) {
                ps.setInt(1, eventId);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("The event is Housefull you can explore other events.");
                }
            }
        }
    }

    private String insertBooking(Connection conn, int eventId, String name, String email, String bookingType)
            throws SQLException {
        String digitalId = java.util.UUID.randomUUID().toString();
        String sql = "INSERT INTO bookings (event_id, user_name, user_email, digital_id, booking_type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, digitalId);
            ps.setString(5, bookingType);
            ps.executeUpdate();
        }
        return digitalId;
    }

    private boolean canBookTopLevelEvent(EventDto event, Set<Integer> bookedEventIds) {
        if (!event.isMajorEvent()) {
            return event.availableSpots > 0;
        }

        boolean alreadyOccupiesMajorSeat = hasAnySubBooking(event, bookedEventIds);
        if (!alreadyOccupiesMajorSeat && event.availableSpots <= 0) {
            return false;
        }

        for (EventDto subEvent : event.subEvents) {
            if (!bookedEventIds.contains(subEvent.id) && subEvent.availableSpots <= 0) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAnySubBooking(EventDto event, Set<Integer> bookedEventIds) {
        for (EventDto subEvent : event.subEvents) {
            if (bookedEventIds.contains(subEvent.id)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyExistingSubBooking(Connection conn, int parentEventId, Set<Integer> bookedEventIds)
            throws SQLException {
        for (Integer subEventId : loadSubEventIds(conn, parentEventId)) {
            if (bookedEventIds.contains(subEventId)) {
                return true;
            }
        }
        return false;
    }

    private void ensureBookingSchema(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "ALTER TABLE events ADD COLUMN IF NOT EXISTS event_type VARCHAR(20) NOT NULL DEFAULT 'SIMPLE'")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "ALTER TABLE events ADD COLUMN IF NOT EXISTS parent_event_id INTEGER REFERENCES events(event_id) ON DELETE CASCADE")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "ALTER TABLE events ADD COLUMN IF NOT EXISTS image_path VARCHAR(500)")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "ALTER TABLE bookings ADD COLUMN IF NOT EXISTS booking_type VARCHAR(20) NOT NULL DEFAULT 'SUB_EVENT'")) {
            ps.executeUpdate();
        }
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message) {
        ApiError error = new ApiError();
        error.success = false;
        error.message = message;
        return ResponseEntity.status(status).body(error);
    }

    private String absoluteUrl(HttpServletRequest request, String path) {
        if (isBlank(path)) {
            return null;
        }
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("data:")) {
            return path;
        }
        return request.getScheme() + "://" + request.getServerName()
                + (isDefaultPort(request) ? "" : ":" + request.getServerPort())
                + request.getContextPath() + path;
    }

    private boolean isDefaultPort(HttpServletRequest request) {
        return ("http".equals(request.getScheme()) && request.getServerPort() == 80)
                || ("https".equals(request.getScheme()) && request.getServerPort() == 443);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void appendPlaceholders(StringBuilder sql, int count) {
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
    }

    private void bindIds(PreparedStatement ps, List<Integer> ids) throws SQLException {
        for (int i = 0; i < ids.size(); i++) {
            ps.setInt(i + 1, ids.get(i));
        }
    }

    public static class EventDto {
        public int id;
        public String name;
        public String date;
        public String location;
        public String description;
        public int guestLimit;
        public int currentGuests;
        public int availableSpots;
        public String eventType;
        public Integer parentEventId;
        public String parentEventName;
        public String imagePath;
        public String imageUrl;
        public boolean bookedByCurrentUser;
        public boolean includedInMajorPass;
        public boolean canBook = true;
        public String bookingStatus;
        public List<EventDto> subEvents = new ArrayList<>();

        public boolean isMajorEvent() {
            return "MAJOR".equalsIgnoreCase(eventType);
        }
    }

    public static class ParticipationDto {
        public String name;
        public String email;
        public String digitalId;
        public String bookingType;
        public EventDto event;
    }

    public static class BookingRequest {
        public Integer eventId;
        public String name;
        public String email;
    }

    public static class BookingResponse {
        public boolean success;
        public String message;
        public int eventId;
        public String bookingType;
        public String digitalId;
    }

    public static class ApiError {
        public boolean success;
        public String message;
    }

    private static class EventRow {
        private final int id;
        private final String eventType;
        private final Integer parentEventId;

        private EventRow(int id, String eventType, Integer parentEventId) {
            this.id = id;
            this.eventType = eventType;
            this.parentEventId = parentEventId;
        }
    }

    private static class BookingPlan {
        private final String bookingType;
        private final List<Integer> eventIdsToIncrement;
        private final String message;

        private BookingPlan(String bookingType, List<Integer> eventIdsToIncrement, String message) {
            this.bookingType = bookingType;
            this.eventIdsToIncrement = eventIdsToIncrement;
            this.message = message;
        }

        private boolean isBookable() {
            return bookingType != null;
        }

        private static BookingPlan allowed(String bookingType, List<Integer> eventIdsToIncrement, String message) {
            return new BookingPlan(bookingType, eventIdsToIncrement, message);
        }

        private static BookingPlan blocked(String message) {
            return new BookingPlan(null, new ArrayList<>(), message);
        }
    }
}
