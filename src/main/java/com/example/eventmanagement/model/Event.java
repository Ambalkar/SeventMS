
package com.example.eventmanagement.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private int id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "date", nullable = false)
    private String date;
    
    @Column(name = "location", nullable = false)
    private String location;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "guest_limit", nullable = false)
    private int guestLimit;
    
    @Column(name = "current_guests")
    private int currentGuests;

    @Column(name = "event_type")
    private String eventType = "SIMPLE";

    @Column(name = "parent_event_id")
    private Integer parentEventId;

    @Column(name = "image_path", columnDefinition = "TEXT")
    private String imagePath;

    @Transient
    private String parentEventName;

    @Transient
    private List<Event> subEvents = new ArrayList<>();

    @Transient
    private boolean bookedByCurrentUser;

    @Transient
    private boolean includedInMajorPass;

    @Transient
    private boolean canBook = true;

    @Transient
    private String bookingStatus;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getGuestLimit() { return guestLimit; }
    public void setGuestLimit(int guestLimit) { this.guestLimit = guestLimit; }
    
    public int getCurrentGuests() { return currentGuests; }
    public void setCurrentGuests(int currentGuests) { this.currentGuests = currentGuests; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Integer getParentEventId() { return parentEventId; }
    public void setParentEventId(Integer parentEventId) { this.parentEventId = parentEventId; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getParentEventName() { return parentEventName; }
    public void setParentEventName(String parentEventName) { this.parentEventName = parentEventName; }

    public List<Event> getSubEvents() { return subEvents; }
    public void setSubEvents(List<Event> subEvents) { this.subEvents = subEvents; }

    public boolean isBookedByCurrentUser() { return bookedByCurrentUser; }
    public void setBookedByCurrentUser(boolean bookedByCurrentUser) { this.bookedByCurrentUser = bookedByCurrentUser; }

    public boolean isIncludedInMajorPass() { return includedInMajorPass; }
    public void setIncludedInMajorPass(boolean includedInMajorPass) { this.includedInMajorPass = includedInMajorPass; }

    public boolean isCanBook() { return canBook; }
    public void setCanBook(boolean canBook) { this.canBook = canBook; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public boolean isMajorEvent() {
        return "MAJOR".equalsIgnoreCase(eventType);
    }

    public boolean isSubEvent() {
        return parentEventId != null;
    }

    public int getAvailableSpots() {
        return Math.max(guestLimit - currentGuests, 0);
    }

    public int getWholeEventAvailableSpots() {
        int availableSpots = getAvailableSpots();
        if (!isMajorEvent() || subEvents == null || subEvents.isEmpty()) {
            return availableSpots;
        }

        for (Event subEvent : subEvents) {
            availableSpots = Math.min(availableSpots, subEvent.getAvailableSpots());
        }
        return availableSpots;
    }

    public boolean isWholeEventOpen() {
        return getWholeEventAvailableSpots() > 0;
    }

    // Validation method
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               date != null && !date.trim().isEmpty() &&
               location != null && !location.trim().isEmpty() &&
               guestLimit > 0;
    }
}
