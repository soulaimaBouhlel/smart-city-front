package com.smartcity.dto;

import lombok.Data;

@Data
public class ReportIssueRequest {
    private String citizenId;
    private String equipmentId;
    private String description;
    private String interventionType;
}
