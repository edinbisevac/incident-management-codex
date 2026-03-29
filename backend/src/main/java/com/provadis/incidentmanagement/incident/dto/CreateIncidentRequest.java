package com.provadis.incidentmanagement.incident.dto;

import com.provadis.incidentmanagement.incident.model.IncidentPriority;
import com.provadis.incidentmanagement.incident.model.IncidentStatus;

public record CreateIncidentRequest(
        String title,
        String description,
        String source,
        IncidentStatus status,
        IncidentPriority priority
) {
}
