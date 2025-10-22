package com.queuemed.models;

public class Appointment {
    private String id; // Firebase key
    private String patientName;
    private String patientEmail;
    private String date;
    private String time;
    private String status;

    // Empty constructor required for Firebase
    public Appointment() {}

    public Appointment(String id, String patientName, String patientEmail, String date, String time, String status) {
        this.id = id;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
