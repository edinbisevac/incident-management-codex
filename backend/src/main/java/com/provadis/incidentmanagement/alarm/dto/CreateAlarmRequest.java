package com.provadis.incidentmanagement.alarm.dto;

import com.provadis.incidentmanagement.alarm.model.AlarmSeverity;
import java.time.LocalDateTime;

public record CreateAlarmRequest(
        String source,
        String message,
        AlarmSeverity severity,
        LocalDateTime createdAt
) {
}
