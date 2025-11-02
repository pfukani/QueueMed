package com.queuemed.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportItem {
    private String patientName;
    private String bloodPressure;
    private String temperature;
    private String pulse;
    private String notes;
    private String status;
    private long timestamp;

    // Required empty constructor for Firebase
    public ReportItem() {}

    // Convenience constructor (optional)
    public ReportItem(String patientName, String bloodPressure, String temperature,
                      String pulse, String notes, String status, long timestamp) {
        this.patientName = patientName;
        this.bloodPressure = bloodPressure;
        this.temperature = temperature;
        this.pulse = pulse;
        this.notes = notes;
        this.status = status;
        this.timestamp = timestamp;
    }

    // --- Getters with safe defaults ---
    public String getPatientName() { return patientName != null ? patientName : "Unknown"; }
    public String getBloodPressure() { return bloodPressure != null ? bloodPressure : "--"; }
    public String getTemperature() { return temperature != null ? temperature : "--"; }
    public String getPulse() { return pulse != null ? pulse : "--"; }
    public String getNotes() { return notes != null ? notes : "--"; }
    public String getStatus() { return status != null ? status : "N/A"; }
    public long getTimestamp() { return timestamp; }

    // --- Helper: formatted date ---
    public String getFormattedDate() {
        if (timestamp == 0) return "No Date";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // --- Setters ---
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
    public void setTemperature(String temperature) { this.temperature = temperature; }
    public void setPulse(String pulse) { this.pulse = pulse; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}