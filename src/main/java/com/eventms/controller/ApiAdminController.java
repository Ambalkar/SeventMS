package com.eventms.controller;

import com.eventms.auth.TokenStore;
import com.eventms.service.FileNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private FileNotificationService fileNotificationService;

    @Autowired
    private HttpServletRequest request;

    private TokenStore.UserInfo getAuthenticatedUser(HttpSession session) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            TokenStore.UserInfo user = TokenStore.getUser(token);
            if (user != null) {
                return user;
            }
        }
        if (session != null && session.getAttribute("authEmail") != null) {
            return new TokenStore.UserInfo(
                (String) session.getAttribute("authName"),
                (String) session.getAttribute("authEmail"),
                (String) session.getAttribute("authRole")
            );
        }
        return null;
    }

    private boolean isAdmin(HttpSession session) {
        TokenStore.UserInfo user = getAuthenticatedUser(session);
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized. Admin access required."));
        }

        List<Map<String, Object>> events = new ArrayList<>();
        List<Map<String, Object>> bookings = new ArrayList<>();
        int totalCapacity = 0;
        int totalGuests = 0;
        int majorEventCount = 0;
        int subEventCount = 0;

        try (Connection conn = dataSource.getConnection()) {
            ensureEventHierarchyColumns(conn);

            String sql = "SELECT e.event_id, e.name, e.date, e.location, e.description, e.guest_limit, "
                    + "e.current_guests, e.event_type, e.parent_event_id, e.image_path, p.name AS parent_event_name "
                    + "FROM events e "
                    + "LEFT JOIN events p ON e.parent_event_id = p.event_id "
                    + "ORDER BY COALESCE(e.parent_event_id, e.event_id), e.parent_event_id NULLS FIRST, e.date";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            Map<Integer, Map<String, Object>> eventMap = new LinkedHashMap<>();
            List<Map<String, Object>> topLevelEvents = new ArrayList<>();

            while (rs.next()) {
                int guestLimit = rs.getInt("guest_limit");
                int currentGuests = rs.getInt("current_guests");
                Integer parentEventId = rs.getObject("parent_event_id") == null ? null : rs.getInt("parent_event_id");

                Map<String, Object> event = new LinkedHashMap<>();
                event.put("id", rs.getInt("event_id"));
                event.put("name", rs.getString("name"));
                event.put("date", rs.getString("date") != null ? rs.getString("date").toString() : "");
                event.put("location", rs.getString("location"));
                event.put("description", rs.getString("description"));
                event.put("guestLimit", guestLimit);
                event.put("currentGuests", currentGuests);
                event.put("availableSpots", Math.max(guestLimit - currentGuests, 0));
                event.put("eventType", rs.getString("event_type"));
                event.put("parentEventId", parentEventId);
                event.put("parentEventName", rs.getString("parent_event_name"));
                event.put("imagePath", rs.getString("image_path"));
                event.put("subEvents", new ArrayList<Map<String, Object>>());

                eventMap.put((Integer) event.get("id"), event);
                if (parentEventId == null) {
                    if ("MAJOR".equalsIgnoreCase((String) event.get("eventType"))) {
                        majorEventCount++;
                    }
                    topLevelEvents.add(event);
                } else if (eventMap.containsKey(parentEventId)) {
                    subEventCount++;
                    List<Map<String, Object>> subs = (List<Map<String, Object>>) eventMap.get(parentEventId).get("subEvents");
                    subs.add(event);
                }
            }

            for (Map<String, Object> event : topLevelEvents) {
                totalGuests += (Integer) event.get("currentGuests");
                totalCapacity += (Integer) event.get("availableSpots");
            }

            String bookingSql = "SELECT b.booking_id, b.user_name, b.user_email, b.digital_id, b.booking_date, "
                    + "COALESCE(p.name || ' - ' || e.name, e.name) AS event_name, e.date AS event_date, b.booking_type "
                    + "FROM bookings b "
                    + "LEFT JOIN events e ON b.event_id = e.event_id "
                    + "LEFT JOIN events p ON e.parent_event_id = p.event_id "
                    + "ORDER BY b.booking_date DESC";
            PreparedStatement bookingPs = conn.prepareStatement(bookingSql);
            ResultSet bookingRs = bookingPs.executeQuery();
            while (bookingRs.next()) {
                Map<String, Object> booking = new LinkedHashMap<>();
                booking.put("id", bookingRs.getInt("booking_id"));
                booking.put("userName", bookingRs.getString("user_name"));
                booking.put("userEmail", bookingRs.getString("user_email"));
                booking.put("digitalId", bookingRs.getString("digital_id"));
                booking.put("eventName", bookingRs.getString("event_name"));
                booking.put("eventDate", bookingRs.getString("event_date"));
                booking.put("bookingDate", bookingRs.getString("booking_date"));
                booking.put("bookingType", bookingRs.getString("booking_type"));
                bookings.add(booking);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("events", topLevelEvents);
            data.put("bookings", bookings);
            data.put("totalEvents", majorEventCount + " / " + subEventCount);
            data.put("majorEventCount", majorEventCount);
            data.put("subEventCount", subEventCount);
            data.put("totalBookings", bookings.size());
            data.put("totalCapacity", totalCapacity);
            data.put("totalGuests", totalGuests);

            return ResponseEntity.ok(data);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Database error: " + e.getMessage()));
        }
    }

    @PostMapping("/events")
    public ResponseEntity<?> addEvent(
            @RequestParam("name") String name,
            @RequestParam("date") String date,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam("guest_limit") Integer guestLimit,
            @RequestParam(value = "event_type", defaultValue = "SIMPLE") String eventType,
            @RequestParam(value = "image", required = false) MultipartFile image,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized."));
        }

        try (Connection conn = dataSource.getConnection()) {
            ensureEventHierarchyColumns(conn);
            conn.setAutoCommit(false);

            String normalizedType = "MAJOR".equalsIgnoreCase(eventType) ? "MAJOR" : "SIMPLE";
            if (name == null || name.isBlank() || date == null || date.isBlank() || location == null || location.isBlank() || guestLimit == null || guestLimit < 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Name, date, location, and guest limit are required."));
            }
            if ("MAJOR".equals(normalizedType) && (image == null || image.isEmpty())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Major events require an image."));
            }

            String imagePath = saveEventImage(image);
            String sql = "INSERT INTO events (name, date, location, description, guest_limit, current_guests, event_type, parent_event_id, image_path) "
                    + "VALUES (?, ?, ?, ?, ?, 0, ?, NULL, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setString(3, location);
            ps.setString(4, description);
            ps.setInt(5, guestLimit);
            ps.setString(6, normalizedType);
            ps.setString(7, imagePath);
            ps.executeUpdate();
            conn.commit();

            try {
                fileNotificationService.notifyRegisteredUsersAboutEvent(name, date, location, description);
            } catch (IOException e) {
                // Ignore notification error
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Event added successfully."));
        } catch (SQLException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @PutMapping(value = "/events/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateEvent(
            @PathVariable("id") int id,
            @RequestParam("name") String name,
            @RequestParam("date") String date,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam("guest_limit") Integer guestLimit,
            @RequestParam(value = "event_type", defaultValue = "SIMPLE") String eventType,
            @RequestParam(value = "image", required = false) MultipartFile image,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized."));
        }

        try (Connection conn = dataSource.getConnection()) {
            ensureEventHierarchyColumns(conn);
            conn.setAutoCommit(false);

            if (name == null || name.isBlank() || date == null || date.isBlank() || location == null || location.isBlank() || guestLimit == null || guestLimit < 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Name, date, location, and guest limit are required."));
            }

            String normalizedType = "MAJOR".equalsIgnoreCase(eventType) ? "MAJOR" : "SIMPLE";
            Integer parentEventId = null;
            int currentGuests = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT current_guests, parent_event_id FROM events WHERE event_id = ? FOR UPDATE")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Event not found."));
                    }
                    currentGuests = rs.getInt("current_guests");
                    parentEventId = rs.getObject("parent_event_id") == null ? null : rs.getInt("parent_event_id");
                }
            }

            if (guestLimit < currentGuests) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Guest limit cannot be lower than current bookings (" + currentGuests + ")."));
            }

            if ("MAJOR".equals(normalizedType)) {
                int subTotal = getSubGuestLimitTotal(conn, id, null);
                if (guestLimit < subTotal) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Major event guest limit cannot be lower than combined sub-event limits (" + subTotal + ")."));
                }
            } else if (parentEventId != null) {
                validateSubEventCapacity(conn, parentEventId, id, guestLimit);
            }

            String imagePath = saveEventImage(image);
            String sql = imagePath == null
                    ? "UPDATE events SET name = ?, date = ?, location = ?, description = ?, guest_limit = ?, event_type = ? WHERE event_id = ?"
                    : "UPDATE events SET name = ?, date = ?, location = ?, description = ?, guest_limit = ?, event_type = ?, image_path = ? WHERE event_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setDate(2, java.sql.Date.valueOf(date));
                ps.setString(3, location);
                ps.setString(4, description);
                ps.setInt(5, guestLimit);
                ps.setString(6, normalizedType);
                if (imagePath == null) {
                    ps.setInt(7, id);
                } else {
                    ps.setString(7, imagePath);
                    ps.setInt(8, id);
                }
                ps.executeUpdate();
            }

            conn.commit();
            return ResponseEntity.ok(Map.of("success", true, "message", "Event updated successfully."));
        } catch (SQLException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/events/sub")
    public ResponseEntity<?> addSubEvent(
            @RequestParam("parent_event_id") Integer parentEventId,
            @RequestParam("name") String name,
            @RequestParam("date") String date,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam("guest_limit") Integer guestLimit,
            @RequestParam(value = "image", required = false) MultipartFile image,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized."));
        }

        try (Connection conn = dataSource.getConnection()) {
            ensureEventHierarchyColumns(conn);
            conn.setAutoCommit(false);

            if (parentEventId == null || name == null || name.isBlank() || date == null || date.isBlank() || location == null || location.isBlank() || guestLimit == null || guestLimit < 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "All fields are required."));
            }
            if (image == null || image.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Sub-events require an image."));
            }

            validateSubEventCapacity(conn, parentEventId, null, guestLimit);
            int existingMajorPasses = countMajorPassBookings(conn, parentEventId);
            if (existingMajorPasses > guestLimit) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Sub-event limit must cover major-event passes (" + existingMajorPasses + ")."));
            }

            String imagePath = saveEventImage(image);
            String sql = "INSERT INTO events (name, date, location, description, guest_limit, current_guests, event_type, parent_event_id, image_path) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 'SUB', ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setString(3, location);
            ps.setString(4, description);
            ps.setInt(5, guestLimit);
            ps.setInt(6, existingMajorPasses);
            ps.setInt(7, parentEventId);
            ps.setString(8, imagePath);
            ps.executeUpdate();
            conn.commit();

            try {
                fileNotificationService.notifyRegisteredUsersAboutEvent(name, date, location, description);
            } catch (IOException e) {
                // Ignore notification error
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Sub-event added successfully."));
        } catch (SQLException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable("id") int id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized."));
        }

        try (Connection conn = dataSource.getConnection()) {
            String sql = "DELETE FROM events WHERE event_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Event deleted successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Event not found."));
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Database error: " + e.getMessage()));
        }
    }

    @PutMapping("/bookings/{id}")
    public ResponseEntity<?> updateBooking(
            @PathVariable("id") int id,
            @RequestParam("user_name") String userName,
            @RequestParam("user_email") String userEmail,
            @RequestParam("digital_id") String digitalId,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized."));
        }

        if (userName == null || userName.isBlank() || userEmail == null || userEmail.isBlank() || digitalId == null || digitalId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Participant name, email, and digital ID are required."));
        }

        try (Connection conn = dataSource.getConnection()) {
            ensureEventHierarchyColumns(conn);
            String sql = "UPDATE bookings SET user_name = ?, user_email = ?, digital_id = ? WHERE booking_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userName.trim());
                ps.setString(2, userEmail.trim());
                ps.setString(3, digitalId.trim());
                ps.setInt(4, id);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Booking not found."));
                }
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "Participant details updated successfully."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Database error: " + e.getMessage()));
        }
    }

    private void ensureEventHierarchyColumns(Connection conn) throws SQLException {
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

    private void validateSubEventCapacity(Connection conn, Integer parentEventId, Integer subEventId, Integer guestLimit) throws SQLException {
        int parentLimit;
        try (PreparedStatement ps = conn.prepareStatement("SELECT guest_limit, event_type FROM events WHERE event_id = ? AND parent_event_id IS NULL")) {
            ps.setInt(1, parentEventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Sub-events must belong to a top-level major event.");
                }
                parentLimit = rs.getInt("guest_limit");
            }
        }
        int total = getSubGuestLimitTotal(conn, parentEventId, subEventId) + guestLimit;
        if (total > parentLimit) {
            throw new SQLException("Combined sub-event guest limits (" + total + ") cannot exceed parent major event guest limit (" + parentLimit + ").");
        }
    }

    private int getSubGuestLimitTotal(Connection conn, Integer parentEventId, Integer excludedSubEventId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(guest_limit), 0) FROM events WHERE parent_event_id = ?"
                + (excludedSubEventId == null ? "" : " AND event_id <> ?");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentEventId);
            if (excludedSubEventId != null) {
                ps.setInt(2, excludedSubEventId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int countMajorPassBookings(Connection conn, Integer parentEventId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM bookings WHERE event_id = ? AND booking_type = 'MAJOR_EVENT'")) {
            ps.setInt(1, parentEventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private String saveEventImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            return null;
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IOException("Only image files can be uploaded.");
        }

        BufferedImage originalImage = ImageIO.read(image.getInputStream());
        if (originalImage == null) {
            throw new IOException("Could not decode image file.");
        }

        int targetWidth = 1700;
        int targetHeight = 950;

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        double targetAspect = (double) targetWidth / targetHeight;
        double originalAspect = (double) originalWidth / originalHeight;

        int srcX = 0, srcY = 0, srcWidth = originalWidth, srcHeight = originalHeight;

        if (originalAspect > targetAspect) {
            srcWidth = (int) (originalHeight * targetAspect);
            srcX = (originalWidth - srcWidth) / 2;
        } else {
            srcHeight = (int) (originalWidth / targetAspect);
            srcY = (originalHeight - srcHeight) / 2;
        }

        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, srcX, srcY, srcX + srcWidth, srcY + srcHeight, null);
        g2d.dispose();

        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "images", "events");
        Files.createDirectories(uploadDir);

        String fileName = "event-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID() + ".jpg";
        Path target = uploadDir.resolve(fileName);

        boolean written = ImageIO.write(resizedImage, "jpg", target.toFile());
        if (!written) {
            throw new IOException("Failed to write image file.");
        }

        return "/images/events/" + fileName;
    }
}
