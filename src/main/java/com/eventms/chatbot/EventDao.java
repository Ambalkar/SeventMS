package com.eventms.chatbot;

import com.example.eventmanagement.model.Event;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EventDao {
    private final DataSource dataSource;

    public EventDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Event> findUpcoming(int limit) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT event_id, name, date, location, guest_limit, current_guests FROM events "
                + "WHERE date >= CURRENT_DATE ORDER BY date ASC, event_id ASC LIMIT ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(limit, 1));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Event event = new Event();
                    event.setId(rs.getInt("event_id"));
                    event.setName(rs.getString("name"));
                    event.setDate(rs.getString("date"));
                    event.setLocation(rs.getString("location"));
                    event.setGuestLimit(rs.getInt("guest_limit"));
                    event.setCurrentGuests(rs.getInt("current_guests"));
                    events.add(event);
                }
            }
        } catch (SQLException ignored) {
            return events;
        }
        return events;
    }
}
