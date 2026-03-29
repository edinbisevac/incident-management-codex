package com.provadis.incidentmanagement.incident.dto;

import com.provadis.incidentmanagement.incident.model.IncidentStatus;

public record UpdateIncidentStatusRequest(IncidentStatus status) {
}
