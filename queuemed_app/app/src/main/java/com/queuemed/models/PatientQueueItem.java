package com.queuemed.models;

public class PatientQueueItem {

    private String appointmentId;
    private String patientName;
    private String patientEmail;
    private String status; // e.g., "Waiting", "In Progress", "Done"
    private long timestamp;
    private int position; // <-- new field

    // Default constructor (required for Firebase)
    public PatientQueueItem() { }

    // Constructor
    public PatientQueueItem(String appointmentId, String patientName, String patientEmail, String status, long timestamp) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getPosition() { return position; } // <-- getter
    public void setPosition(int position) { this.position = position; } // <-- setter
}
