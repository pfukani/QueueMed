package com.queuemed.models;

public class Appointment {
    private String id;
    private String doctorName;
    private String patientName;
    private String patientEmail;
    private String date;
    private String time;
    private String status;

    // Required empty constructor for Firebase
    public Appointment() {}

    //  Constructor WITH doctorName
    public Appointment(String id, String doctorName, String patientName, String patientEmail, String date, String time, String status) {
        this.id = id;
        this.doctorName = doctorName;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    //  New constructor WITHOUT doctorName (for BookAppointmentActivity)
    public Appointment(String id, String patientName, String patientEmail, String date, String time, String status) {
        this.id = id;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.date = date;
        this.time = time;
        this.status = status;
        this.doctorName = ""; // default empty string
    }

    public String getId() { return id; }
    public String getDoctorName() { return doctorName; }
    public String getPatientName() { return patientName; }
    public String getPatientEmail() { return patientEmail; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }

    public void setId(String id) { this.id = id; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setStatus(String status) { this.status = status; }
}
