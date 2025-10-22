package com.queuemed.models;

/**
 * User model represents both staff and patient users.
 * It will be sent to and received from the backend API.
 */
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String contact;
    private String role; // "patient" or "staff"

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String firstName, String lastName, String email, String contact, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contact = contact;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getContact() { return contact; }
    public String getRole() { return role; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setContact(String contact) { this.contact = contact; }
    public void setRole(String role) { this.role = role; }
}
