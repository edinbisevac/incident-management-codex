package com.provadis.incidentmanagement.dto;

import com.provadis.incidentmanagement.model.IncidentPriority;
import com.provadis.incidentmanagement.model.IncidentStatus;

public record CreateIncidentRequest(
        String title,
        String description,
        IncidentStatus status,
        IncidentPriority priority,
        String source
) {
}
