package com.queuemed.models;

public class Appointment {
    private String id;
    private String doctorName;
    private String patientName;
    private String patientEmail;
    private String date;   // e.g. "2025-11-01"
    private String time;   // e.g. "14:30"
    private String status; // "booked", "checked_in", "vitals_taken"

    public Appointment() {} // Required for Firebase

    public Appointment(String id, String doctorName, String patientName,
                       String patientEmail, String date, String time, String status) {
        this.id = id;
        this.doctorName = doctorName != null ? doctorName : "";
        this.patientName = patientName != null ? patientName : "";
        this.patientEmail = patientEmail != null ? patientEmail : "";
        this.date = date != null ? date : "";
        this.time = time != null ? time : "";
        this.status = status != null ? status : "booked";
    }

    public Appointment(String id, String patientName, String patientEmail,
                       String date, String time, String status) {
        this(id, "", patientName, patientEmail, date, time, status);
    }

    public String getId() { return id != null ? id : ""; }
    public String getDoctorName() { return doctorName != null ? doctorName : ""; }
    public String getPatientName() { return patientName != null ? patientName : "Unknown"; }
    public String getPatientEmail() { return patientEmail != null ? patientEmail : ""; }
    public String getDate() { return date != null ? date : ""; }
    public String getTime() { return time != null ? time : ""; }
    public String getStatus() { return status != null ? status : "booked"; }

    public void setId(String id) { this.id = id; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setStatus(String status) { this.status = status; }
}