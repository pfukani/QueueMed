package com.queuemed.models;

public class PatientQueueItem {
    private String name;
    private String email;
    private String checkInStatus; // Pending / Checked-in

    public PatientQueueItem() { }

    public PatientQueueItem(String name, String email, String checkInStatus) {
        this.name = name;
        this.email = email;
        this.checkInStatus = checkInStatus;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCheckInStatus() { return checkInStatus; }
    public void setCheckInStatus(String checkInStatus) { this.checkInStatus = checkInStatus; }
}
