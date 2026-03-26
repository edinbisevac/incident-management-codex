package com.provadis.incidentmanagement.dto;

import com.provadis.incidentmanagement.model.IncidentStatus;

public record UpdateIncidentStatusRequest(IncidentStatus status) {
}
