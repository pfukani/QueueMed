package com.queuemed.models;

public class DailySummary {
    private String date; // yyyy-MM-dd
    private int totalAppointments;
    private int checkedIn;
    private int vitalsTaken;

    public DailySummary() {}

    public DailySummary(String date) {
        this.date = date;
    }

    public String getDate() { return date; }
    public int getTotalAppointments() { return totalAppointments; }
    public int getCheckedIn() { return checkedIn; }
    public int getVitalsTaken() { return vitalsTaken; }

    public void setDate(String date) { this.date = date; }
    public void setTotalAppointments(int totalAppointments) { this.totalAppointments = totalAppointments; }
    public void setCheckedIn(int checkedIn) { this.checkedIn = checkedIn; }
    public void setVitalsTaken(int vitalsTaken) { this.vitalsTaken = vitalsTaken; }

    public void incrementAppointments() { totalAppointments++; }
    public void incrementCheckedIn() { checkedIn++; }
    public void incrementVitalsTaken() { vitalsTaken++; }
}