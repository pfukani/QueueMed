package com.queuemed.models;

public class QueueEntry {
    private String appointmentId;
    private String patientName;
    private String time;
    private String status; // "Waiting", "Being Seen", "Done"

    public QueueEntry() { }

    public QueueEntry(String appointmentId, String patientName, String time, String status) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.time = time;
        this.status = status;
    }

    public String getAppointmentId() { return appointmentId; }
    public String getPatientName() { return patientName; }
    public String getTime() { return time; }
    public String getStatus() { return status; }

    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setTime(String time) { this.time = time; }
    public void setStatus(String status) { this.status = status; }
}
