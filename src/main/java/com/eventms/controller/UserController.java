package com.eventms.controller;

import com.example.eventmanagement.model.Event;
import com.example.eventmanagement.model.Participation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpSession;

@Controller

public class UserController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/client")
    public String clientPage() {
        return "redirect:/documentation.jsp";
    }

    @GetMapping({"/", "/user"})
    public String userEvents(Model model, HttpSession session) {
        List<Event> events = new ArrayList<>();
        String sessionEmail = session.getAttribute("authEmail") == null ? null : session.getAttribute("authEmail").toString();
        try (Connection conn = dataSource.getConnection()) {
            ensureBookingSchema(conn);

            String sql = "SELECT e.event_id, e.name, e.date, e.location, e.description, e.guest_limit, "
                    + "e.current_guests, e.event_type, e.parent_event_id, e.image_path, p.name AS parent_event_name "
                    + "FROM events e "
                    + "LEFT JOIN events p ON e.parent_event_id = p.event_id "
                    + "ORDER BY COALESCE(e.parent_event_id, e.event_id), e.parent_event_id NULLS FIRST, e.date";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            Map<Integer, Event> eventMap = new LinkedHashMap<>();
            while (rs.next()) {
                Event event = new Event();
                event.setId(rs.getInt("event_id"));
                event.setName(rs.getString("name"));
                event.setDate(rs.getString("date"));
                event.setLocation(rs.getString("location"));
                event.setDescription(rs.getString("description"));
                event.setGuestLimit(rs.getInt("guest_limit"));
                event.setCurrentGuests(rs.getInt("current_guests"));
                event.setEventType(rs.getString("event_type"));
                event.setImagePath(rs.getString("image_path"));
                Integer parentEventId = rs.getObject("parent_event_id") == null ? null : rs.getInt("parent_event_id");
                event.setParentEventId(parentEventId);
                event.setParentEventName(rs.getString("parent_event_name"));

                eventMap.put(event.getId(), event);
                if (parentEventId == null) {
                    events.add(event);
                } else if (eventMap.containsKey(parentEventId)) {
                    eventMap.get(parentEventId).getSubEvents().add(event);
                }
            }

            applyBookingState(conn, events, sessionEmail);
        } catch (SQLException e) {
            model.addAttribute("errorMessage", "Error loading events: " + e.getMessage());
        }
        model.addAttribute("events", events);
        return "user_events"; // This will resolve to user_events.jsp
    }

    @GetMapping("/myEvents")
    public String myEvents(@RequestParam(value = "email", required = false) String email, Model model, HttpSession session) {
        List<Participation> participations = new ArrayList<>();
        if ((email == null || email.trim().isEmpty()) && session.getAttribute("authEmail") != null) {
            email = session.getAttribute("authEmail").toString();
        }

        if (email != null && !email.trim().isEmpty()) {
            try (Connection conn = dataSource.getConnection()) {
                ensureBookingSchema(conn);

                String sql = "SELECT b.user_name AS participant_name, b.user_email, b.digital_id, "
                        + "e.event_id, e.name AS event_name, e.date, e.location, e.description, "
                        + "e.event_type, e.parent_event_id, e.image_path, p.name AS parent_event_name, b.booking_type "
                        + "FROM bookings b "
                        + "JOIN events e ON b.event_id = e.event_id "
                        + "LEFT JOIN events p ON e.parent_event_id = p.event_id "
                        + "WHERE b.user_email = ? "
                        + "ORDER BY e.date DESC, b.booking_date DESC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Participation participation = new Participation();
                    participation.setName(rs.getString("participant_name"));
                    participation.setEmail(rs.getString("user_email"));
                    participation.setParticipantId(rs.getString("digital_id"));

                    Event event = new Event();
                    event.setId(rs.getInt("event_id"));
                    event.setName(rs.getString("event_name"));
                    event.setDate(rs.getString("date"));
                    event.setLocation(rs.getString("location"));
                    event.setDescription(rs.getString("description"));
                    event.setEventType(rs.getString("event_type"));
                    event.setImagePath(rs.getString("image_path"));
                    event.setParentEventId(rs.getObject("parent_event_id") == null ? null : rs.getInt("parent_event_id"));
                    event.setParentEventName(rs.getString("parent_event_name"));
                    event.setBookingStatus("MAJOR_EVENT".equals(rs.getString("booking_type")) ? "Major Event Pass" : "Sub Event Booking");

                    participation.setEvent(event);
                    participations.add(participation);
                }
            } catch (SQLException e) {
                model.addAttribute("errorMessage", "Error loading your events: " + e.getMessage());
            }
        }

        model.addAttribute("email", email);
        model.addAttribute("participations", participations);
        return "my_events";
    }

    @PostMapping({"/", "/user"})
    public String bookEvent(
            @RequestParam("event_id") int eventId,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        try (Connection conn = dataSource.getConnection()) {
            ensureBookingSchema(conn);
            conn.setAutoCommit(false);

            BookingPlan plan = buildBookingPlan(conn, eventId, email);
            if (!plan.isBookable()) {
                conn.rollback();
                redirectAttributes.addFlashAttribute("errorMessage", plan.message);
                return "redirect:/";
            }

            reserveSeats(conn, plan.eventIdsToIncrement);
            insertBooking(conn, eventId, name, email, plan.bookingType);
            conn.commit();

            redirectAttributes.addFlashAttribute("successMessage", plan.message);
        } catch (SQLException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error booking event: " + e.getMessage());
        }

        return "redirect:/";
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
        boolean hasExistingSubBooking = "MAJOR_EVENT".equals(bookingType) && hasAnyExistingSubBooking(conn, selected.id, existingBookings);
        String message = "MAJOR_EVENT".equals(bookingType)
                ? "Major event booked successfully. Your pass includes all remaining sub events."
                : "Event booked successfully.";
        if (hasExistingSubBooking) {
            message = "Major event pass added. Your previous sub-event seats were preserved.";
        }
        return BookingPlan.allowed(bookingType, toIncrement, message);
    }

    private EventRow loadSelectedEventForUpdate(Connection conn, int eventId) throws SQLException {
        String sql = "SELECT event_id, event_type, parent_event_id FROM events WHERE event_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
            // Atomic guarded update prevents concurrent requests from pushing a row over capacity.
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE events SET current_guests = current_guests + 1 WHERE event_id = ? AND current_guests < guest_limit")) {
                ps.setInt(1, eventId);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("The event is Housefull you can explore other events.");
                }
            }
        }
    }

    private void insertBooking(Connection conn, int eventId, String name, String email, String bookingType) throws SQLException {
        String sql = "INSERT INTO bookings (event_id, user_name, user_email, digital_id, booking_type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, java.util.UUID.randomUUID().toString());
            ps.setString(5, bookingType);
            ps.executeUpdate();
        }
    }

    private void applyBookingState(Connection conn, List<Event> events, String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        Set<Integer> bookedEventIds = loadExistingBookings(conn, email);
        for (Event event : events) {
            boolean hasMajorPass = bookedEventIds.contains(event.getId());
            event.setBookedByCurrentUser(hasMajorPass);
            event.setCanBook(!hasMajorPass && canBookTopLevelEvent(event, bookedEventIds));
            if (hasMajorPass) {
                event.setBookingStatus("Already Booked");
            } else if (event.isMajorEvent() && hasAnySubBooking(event, bookedEventIds)) {
                event.setBookingStatus("Upgrade Available");
            }

            for (Event subEvent : event.getSubEvents()) {
                boolean bookedSub = bookedEventIds.contains(subEvent.getId());
                subEvent.setBookedByCurrentUser(bookedSub);
                subEvent.setIncludedInMajorPass(hasMajorPass);
                subEvent.setCanBook(subEvent.getAvailableSpots() > 0 && !bookedSub && !hasMajorPass);
                if (hasMajorPass) {
                    subEvent.setBookingStatus("Included in Major Event Pass");
                } else if (bookedSub) {
                    subEvent.setBookingStatus("Already Booked");
                }
            }
        }
    }

    private boolean canBookTopLevelEvent(Event event, Set<Integer> bookedEventIds) {
        if (!event.isMajorEvent()) {
            return event.getAvailableSpots() > 0;
        }

        boolean alreadyOccupiesMajorSeat = hasAnySubBooking(event, bookedEventIds);
        if (!alreadyOccupiesMajorSeat && event.getAvailableSpots() <= 0) {
            return false;
        }

        for (Event subEvent : event.getSubEvents()) {
            if (!bookedEventIds.contains(subEvent.getId()) && subEvent.getAvailableSpots() <= 0) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAnySubBooking(Event event, Set<Integer> bookedEventIds) {
        for (Event subEvent : event.getSubEvents()) {
            if (bookedEventIds.contains(subEvent.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyExistingSubBooking(Connection conn, int parentEventId, Set<Integer> bookedEventIds) throws SQLException {
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
                "ALTER TABLE events ADD COLUMN IF NOT EXISTS image_path TEXT")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "ALTER TABLE events ALTER COLUMN image_path TYPE TEXT")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
            // Might fail if column doesn't exist yet or depending on DB dialect, but safe to ignore
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "ALTER TABLE bookings ADD COLUMN IF NOT EXISTS booking_type VARCHAR(20) NOT NULL DEFAULT 'SUB_EVENT'")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE bookings SET booking_type = 'MAJOR_EVENT' "
                        + "WHERE event_id IN (SELECT event_id FROM events WHERE parent_event_id IS NULL AND event_type = 'MAJOR') "
                        + "AND booking_type <> 'MAJOR_EVENT'")) {
            ps.executeUpdate();
        }
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
