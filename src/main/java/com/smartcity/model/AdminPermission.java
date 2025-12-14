package com.smartcity.model;

public enum AdminPermission {
    // Scheduler Admin permissions
    VIEW_ALL_INTERVENTIONS,
    ASSIGN_TECHNICIANS,
    OVERRIDE_PRIORITY,
    MANAGE_TECHNICIAN_SCHEDULE,
    
    // HR Admin permissions
    CREATE_ACCOUNTS,
    DELETE_ACCOUNTS,
    UPDATE_SYSTEM_CONFIG,
    MANAGE_EQUIPMENT_DATA,
    VIEW_REPORTS,
    
    // Shared permissions
    VIEW_DASHBOARD,
    EXPORT_DATA,
    SEND_NOTIFICATIONS
}