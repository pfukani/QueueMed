package com.queuemed.models;

public class QueueModel {
    public String name;
    public String time;
    public String status;
    public String patientId;

    public QueueModel() {} // Firebase requires empty constructor

    public QueueModel(String name, String time, String status, String patientId) {
        this.name = name;
        this.time = time;
        this.status = status;
        this.patientId = patientId;
    }
}
