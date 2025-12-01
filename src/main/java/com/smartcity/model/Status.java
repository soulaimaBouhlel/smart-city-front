package com.smartcity.model;

// Based on the EStatus in the UML diagram
public enum Status {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    // Assuming 'Active' is a valid status for general equipment
    ACTIVE 
}