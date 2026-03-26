package com.provadis.incidentmanagement.dto;

public record CreateAlarmRequest(
        String source,
        String message,
        String severity
) {
}
