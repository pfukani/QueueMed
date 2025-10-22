package com.queuemed.models;

public class PatientQueueItem {
    private String id;
    private String name;
    private String status; // must match what we use in adapter

    // Empty constructor for Firebase
    public PatientQueueItem() {}

    public PatientQueueItem(String id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }  // <-- needed for QueueAdapter
    public void setStatus(String status) { this.status = status; }
}
