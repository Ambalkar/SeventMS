package com.eventms.controller;

import com.eventms.service.FileNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpSession;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@Controller

public class AdminController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private FileNotificationService fileNotificationService;

    public static class Event {
        private int id;
        private String name;
        private String date;
        private String location;
        private String description;
        private int guestLimit;
        private int currentGuests;
        private String eventType;
        private Integer parentEventId;
        private String parentEventName;
        private String imagePath;
        private List<Event> subEvents = new ArrayList<>();

        public Event(int id, String name, String date, String location, String description, int guestLimit,
                int currentGuests, String eventType, Integer parentEventId, String parentEventName, String imagePath) {
            this.id = id;
            this.name = name;
            this.date = date;
            this.location = location;
            this.description = description;
            this.guestLimit = guestLimit;
            this.currentGuests = currentGuests;
            this.eventType = eventType;
            this.parentEventId = parentEventId;
            this.parentEventName = parentEventName;
            this.imagePath = imagePath;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDate() { return date; }
        public String getLocation() { return location; }
        public String getDescription() { return description; }
        public int getGuestLimit() { return guestLimit; }
        public int getCurrentGuests() { return currentGuests; }
        public String getEventType() { return eventType; }
        public Integer getParentEventId() { return parentEventId; }
        public String getParentEventName() { return parentEventName; }
        public String getImagePath() { return imagePath; }
        public List<Event> getSubEvents() { return subEvents; }
        public boolean isMajorEvent() { return "MAJOR".equalsIgnoreCase(eventType); }
        public boolean isSubEvent() { return parentEventId != null; }
        public int getAvailableSpots() { return Math.max(guestLimit - currentGuests, 0); }
        public int getWholeEventAvailableSpots() {
            int availableSpots = getAvailableSpots();
            if (!isMajorEvent() || subEvents.isEmpty()) {
                return availableSpots;
            }

            for (Event subEvent : subEvents) {
                availableSpots = Math.min(availableSpots, subEvent.getAvailableSpots());
            }
            return availableSpots;
        }
        public int getSubGuestLimitTotal() {
            int total = 0;
            for (Event subEvent : subEvents) {
                total += subEvent.getGuestLimit();
            }
            return total;
        }
        public int getSubCapacityRemaining() { return Math.max(guestLimit - getSubGuestLimitTotal(), 0); }
    }

    public static class Booking {
        private int id;
        private String userName;
        private String userEmail;
        private String digitalId;
        private String eventName;
        private String eventDate;
        private String bookingDate;
        private String bookingType;

        public Booking(int id, String userName, String userEmail, String digitalId, String eventName, String eventDate, String bookingDate, String bookingType) {
            this.id = id;
            this.userName = userName;
            this.userEmail = userEmail;
            this.digitalId = digitalId;
            this.eventName = eventName;
            this.eventDate = eventDate;
            this.bookingDate = bookingDate;
            this.bookingType = bookingType;
        }

        public int getId() { return id; }
        public String getUserName() { return userName; }
        public String getUserEmail() { return userEmail; }
        public String getDigitalId() { return digitalId; }
        public String getEventName() { return eventName; }
        public String getEventDate() { return eventDate; }
        public String getBookingDate() { return bookingDate; }
        public String getBookingType() { return bookingType; }
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login?target=/admin";
        }

        List<Event> events = new ArrayList<>();
        List<Booking> bookings = new ArrayList<>();
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
            Map<Integer, Event> eventMap = new LinkedHashMap<>();
            while (rs.next()) {
                int guestLimit = rs.getInt("guest_limit");
                int currentGuests = rs.getInt("current_guests");
                Integer parentEventId = rs.getObject("parent_event_id") == null ? null : rs.getInt("parent_event_id");
                Event event = new Event(
                    rs.getInt("event_id"),
                    rs.getString("name"),
                    rs.getString("date"),
                    rs.getString("location"),
                    rs.getString("description"),
                    guestLimit,
                    currentGuests,
                    rs.getString("event_type"),
                    parentEventId,
                    rs.getString("parent_event_name"),
                    rs.getString("image_path")
                );
                eventMap.put(event.getId(), event);
                if (parentEventId == null) {
                    if ("MAJOR".equalsIgnoreCase(event.getEventType())) {
                        majorEventCount++;
                    }
                    events.add(event);
                } else if (eventMap.containsKey(parentEventId)) {
                    subEventCount++;
                    eventMap.get(parentEventId).getSubEvents().add(event);
                }
            }

            for (Event event : events) {
                // Sub-event seats are carved out of the parent major event capacity.
                // Counting both parent and children here inflates dashboard availability.
                totalGuests += event.getCurrentGuests();
                totalCapacity += event.getAvailableSpots();
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
                bookings.add(new Booking(
                    bookingRs.getInt("booking_id"),
                    bookingRs.getString("user_name"),
                    bookingRs.getString("user_email"),
                    bookingRs.getString("digital_id"),
                    bookingRs.getString("event_name"),
                    bookingRs.getString("event_date"),
                    bookingRs.getString("booking_date"),
                    bookingRs.getString("booking_type")
                ));
            }
        } catch (SQLException e) {
            model.addAttribute("errorMessage", "Error loading events: " + e.getMessage());
        }
        model.addAttribute("events", events);
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalEvents", majorEventCount + " / " + subEventCount);
        model.addAttribute("majorEventCount", majorEventCount);
        model.addAttribute("subEventCount", subEventCount);
        model.addAttribute("totalBookings", bookings.size());
        model.addAttribute("totalCapacity", totalCapacity);
        model.addAttribute("totalGuests", totalGuests);
        return "admin_dashboard"; // This will resolve to admin_dashboard.jsp
    }

    @PostMapping("/admin")
    public String handleEventAction(
            @RequestParam("action") String action,
            @RequestParam(value = "event_id", required = false) Integer eventId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "guest_limit", required = false) Integer guestLimit,
            @RequestParam(value = "event_type", required = false, defaultValue = "SIMPLE") String eventType,
            @RequestParam(value = "parent_event_id", required = false) Integer parentEventId,
            @RequestParam(value = "sub_name", required = false) List<String> subNames,
            @RequestParam(value = "sub_date", required = false) List<String> subDates,
            @RequestParam(value = "sub_location", required = false) List<String> subLocations,
            @RequestParam(value = "sub_description", required = false) List<String> subDescriptions,
            @RequestParam(value = "sub_guest_limit", required = false) List<String> subGuestLimits,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "sub_image", required = false) List<MultipartFile> subImages,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in as admin to manage events.");
            return "redirect:/login?target=/admin";
        }

        try (Connection conn = dataSource.getConnection()) {
            ensureEventHierarchyColumns(conn);
            conn.setAutoCommit(false);

            if ("add".equals(action)) {
                String normalizedType = "MAJOR".equalsIgnoreCase(eventType) ? "MAJOR" : "SIMPLE";
                validateEventInput(name, date, location, guestLimit);
                if ("MAJOR".equals(normalizedType)) {
                    if (image == null || image.isEmpty()) {
                        throw new SQLException("Major events require an image.");
                    }
                    validateNewSubEventTotal(subNames, subGuestLimits, guestLimit);
                }
                String imagePath = saveEventImage(image);
                String sql = "INSERT INTO events (name, date, location, description, guest_limit, current_guests, event_type, parent_event_id, image_path) "
                        + "VALUES (?, ?, ?, ?, ?, 0, ?, NULL, ?)";
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, name);
                ps.setDate(2, java.sql.Date.valueOf(date));
                ps.setString(3, location);
                ps.setString(4, description);
                ps.setInt(5, guestLimit);
                ps.setString(6, normalizedType);
                ps.setString(7, imagePath);
                ps.executeUpdate();

                ResultSet created = ps.getGeneratedKeys();
                if (!created.next()) {
                    throw new SQLException("Could not read the created event ID.");
                }
                int createdEventId = created.getInt(1);

                if ("MAJOR".equals(normalizedType)) {
                    insertSubEvents(conn, createdEventId, subNames, subDates, subLocations, subDescriptions, subGuestLimits, subImages);
                }
                conn.commit();
                addEventNotificationFlash(redirectAttributes, "Event added successfully.", name, date, location, description);
            } else if ("addSubEvent".equals(action)) {
                insertSubEvent(conn, parentEventId, name, date, location, description, guestLimit, saveEventImage(image));
                conn.commit();
                addEventNotificationFlash(redirectAttributes, "Sub-event added successfully.", name, date, location, description);
            } else if ("update".equals(action)) {
                String normalizedType;
                if ("MAJOR".equalsIgnoreCase(eventType)) {
                    normalizedType = "MAJOR";
                } else if ("SUB".equalsIgnoreCase(eventType)) {
                    normalizedType = "SUB";
                } else {
                    normalizedType = "SIMPLE";
                }
                validateEventInput(name, date, location, guestLimit);
                validateGuestLimitUpdate(conn, eventId, normalizedType, guestLimit);
                String imagePath = saveEventImage(image);
                String sql = imagePath == null
                        ? "UPDATE events SET name = ?, date = ?, location = ?, description = ?, guest_limit = ?, event_type = ? WHERE event_id = ?"
                        : "UPDATE events SET name = ?, date = ?, location = ?, description = ?, guest_limit = ?, event_type = ?, image_path = ? WHERE event_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setDate(2, java.sql.Date.valueOf(date));
                ps.setString(3, location);
                ps.setString(4, description);
                ps.setInt(5, guestLimit);
                ps.setString(6, normalizedType);
                if (imagePath == null) {
                    ps.setInt(7, eventId);
                } else {
                    ps.setString(7, imagePath);
                    ps.setInt(8, eventId);
                }
                ps.executeUpdate();
                conn.commit();
                redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully.");
            } else if ("delete".equals(action)) {
                String sql = "DELETE FROM events WHERE event_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, eventId);
                ps.executeUpdate();
                conn.commit();
                redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully.");
            } else {
                conn.rollback();
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid action.");
            }
        } catch (SQLException | IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Database error: " + e.getMessage());
        }

        return "redirect:/admin";
    }

    private void addEventNotificationFlash(RedirectAttributes redirectAttributes, String baseMessage,
            String eventName, String eventDate, String location, String description) {
        try {
            int notifiedUsers = fileNotificationService.notifyRegisteredUsersAboutEvent(eventName, eventDate, location, description);
            redirectAttributes.addFlashAttribute("successMessage",
                    baseMessage + " Notification saved for " + notifiedUsers + " registered user(s).");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("successMessage", baseMessage);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Event was saved, but notification records could not be written: " + e.getMessage());
        }
    }

    private boolean isAdmin(HttpSession session) {
        return session != null && "ADMIN".equals(session.getAttribute("authRole"));
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
                "ALTER TABLE events ADD COLUMN IF NOT EXISTS image_path TEXT")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "ALTER TABLE events ALTER COLUMN image_path TYPE TEXT")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
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
        if (isPostgreSql(conn)) {
            installPostgresCapacityTrigger(conn);
        }
    }

    private boolean isPostgreSql(Connection conn) throws SQLException {
        String databaseName = conn.getMetaData().getDatabaseProductName();
        return databaseName != null && databaseName.toLowerCase().contains("postgresql");
    }

    private void installPostgresCapacityTrigger(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("CREATE OR REPLACE FUNCTION validate_sub_event_capacity() RETURNS trigger AS $$ "
                    + "DECLARE parent_limit INTEGER; parent_type VARCHAR(20); sibling_total INTEGER; "
                    + "BEGIN "
                    + "IF NEW.parent_event_id IS NOT NULL THEN "
                    + "SELECT guest_limit, event_type INTO parent_limit, parent_type FROM events WHERE event_id = NEW.parent_event_id; "
                    + "IF parent_type <> 'MAJOR' THEN RAISE EXCEPTION 'Sub-events must belong to a major event'; END IF; "
                    + "SELECT COALESCE(SUM(guest_limit), 0) INTO sibling_total FROM events "
                    + "WHERE parent_event_id = NEW.parent_event_id AND event_id <> COALESCE(NEW.event_id, -1); "
                    + "IF sibling_total + NEW.guest_limit > parent_limit THEN "
                    + "RAISE EXCEPTION 'Combined sub-event guest limits cannot exceed parent major event guest limit'; "
                    + "END IF; "
                    + "END IF; "
                    + "RETURN NEW; END; $$ LANGUAGE plpgsql");
            statement.execute("DROP TRIGGER IF EXISTS trg_validate_sub_event_capacity ON events");
            statement.execute("CREATE TRIGGER trg_validate_sub_event_capacity BEFORE INSERT OR UPDATE OF guest_limit, parent_event_id ON events "
                    + "FOR EACH ROW EXECUTE FUNCTION validate_sub_event_capacity()");
        }
    }

    private void insertSubEvents(Connection conn, int parentEventId, List<String> names, List<String> dates,
            List<String> locations, List<String> descriptions, List<String> guestLimits, List<MultipartFile> images)
            throws SQLException, IOException {
        if (names == null) {
            return;
        }

        for (int i = 0; i < names.size(); i++) {
            String subName = names.get(i);
            String subDate = valueAt(dates, i);
            String subLocation = valueAt(locations, i);
            Integer subGuestLimit = parsePositiveInteger(valueAt(guestLimits, i));

            if (isBlank(subName) || isBlank(subDate) || isBlank(subLocation) || subGuestLimit == null || subGuestLimit < 1) {
                continue;
            }

            insertSubEvent(conn, parentEventId, subName, subDate, subLocation, valueAt(descriptions, i), subGuestLimit,
                    saveEventImage(fileAt(images, i)));
        }
    }

    private void insertSubEvent(Connection conn, Integer parentEventId, String name, String date, String location,
            String description, Integer guestLimit, String imagePath) throws SQLException {
        if (parentEventId == null || isBlank(name) || isBlank(date) || isBlank(location) || guestLimit == null || guestLimit < 1) {
            throw new SQLException("Sub-event details are incomplete.");
        }
        if (imagePath == null) {
            throw new SQLException("Sub-events require an image.");
        }
        validateSubEventCapacity(conn, parentEventId, null, guestLimit);
        int existingMajorPasses = countMajorPassBookings(conn, parentEventId);
        if (existingMajorPasses > guestLimit) {
            throw new SQLException("Sub-event guest limit must cover existing major-event pass holders (" + existingMajorPasses + ").");
        }

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
    }

    private void validateEventInput(String name, String date, String location, Integer guestLimit) throws SQLException {
        if (isBlank(name) || isBlank(date) || isBlank(location) || guestLimit == null || guestLimit < 1) {
            throw new SQLException("Event name, date, location, and a positive guest limit are required.");
        }
    }

    private void validateNewSubEventTotal(List<String> names, List<String> guestLimits, Integer parentGuestLimit) throws SQLException {
        int total = 0;
        if (names == null) {
            return;
        }
        for (int i = 0; i < names.size(); i++) {
            if (isBlank(names.get(i))) {
                continue;
            }
            Integer subGuestLimit = parsePositiveInteger(valueAt(guestLimits, i));
            if (subGuestLimit == null || subGuestLimit < 1) {
                throw new SQLException("Every filled sub-event row needs a positive guest limit.");
            }
            total += subGuestLimit;
        }
        if (total > parentGuestLimit) {
            throw new SQLException("Combined sub-event guest limits (" + total + ") cannot exceed major event guest limit (" + parentGuestLimit + ").");
        }
    }

    private void validateGuestLimitUpdate(Connection conn, Integer eventId, String eventType, Integer guestLimit) throws SQLException {
        if (eventId == null) {
            throw new SQLException("Missing event ID.");
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT current_guests, parent_event_id FROM events WHERE event_id = ? FOR UPDATE")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Event not found.");
                }
                int currentGuests = rs.getInt("current_guests");
                Integer parentEventId = rs.getObject("parent_event_id") == null ? null : rs.getInt("parent_event_id");
                if (guestLimit < currentGuests) {
                    throw new SQLException("Guest limit cannot be lower than current bookings (" + currentGuests + ").");
                }
                if ("MAJOR".equalsIgnoreCase(eventType)) {
                    int subTotal = getSubGuestLimitTotal(conn, eventId, null);
                    if (guestLimit < subTotal) {
                        throw new SQLException("Major event guest limit cannot be lower than combined sub-event limits (" + subTotal + ").");
                    }
                } else if (parentEventId != null) {
                    validateSubEventCapacity(conn, parentEventId, eventId, guestLimit);
                }
            }
        }
    }

    private void validateSubEventCapacity(Connection conn, Integer parentEventId, Integer subEventId, Integer guestLimit) throws SQLException {
        int parentLimit;
        try (PreparedStatement ps = conn.prepareStatement("SELECT guest_limit, event_type FROM events WHERE event_id = ? AND parent_event_id IS NULL FOR UPDATE")) {
            ps.setInt(1, parentEventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Sub-events must belong to a top-level major event.");
                }
                if (!"MAJOR".equalsIgnoreCase(rs.getString("event_type"))) {
                    throw new SQLException("Sub-events must belong to a major event.");
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String valueAt(List<String> values, int index) {
        return values == null || index >= values.size() ? null : values.get(index);
    }

    private MultipartFile fileAt(List<MultipartFile> values, int index) {
        return values == null || index >= values.size() ? null : values.get(index);
    }

    private Integer parsePositiveInteger(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
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

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        boolean written = ImageIO.write(resizedImage, "jpg", baos);
        if (!written) {
            throw new IOException("Failed to encode image.");
        }

        byte[] imageBytes = baos.toByteArray();
        String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
        return "data:image/jpeg;base64," + base64Image;
    }
}
