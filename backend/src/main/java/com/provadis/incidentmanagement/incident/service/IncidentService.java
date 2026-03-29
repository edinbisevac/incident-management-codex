package com.provadis.incidentmanagement.incident.service;

import com.provadis.incidentmanagement.incident.dto.CreateIncidentRequest;
import com.provadis.incidentmanagement.incident.dto.UpdateIncidentPriorityRequest;
import com.provadis.incidentmanagement.incident.dto.UpdateIncidentStatusRequest;
import com.provadis.incidentmanagement.incident.model.Incident;
import com.provadis.incidentmanagement.incident.model.IncidentPriority;
import com.provadis.incidentmanagement.incident.model.IncidentStatus;
import com.provadis.incidentmanagement.incident.repository.IncidentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Incident getIncidentById(Long id) {
        return findIncidentOrThrow(id);
    }

    public Incident createIncident(CreateIncidentRequest request) {
        validateCreateRequest(request);

        Incident incident = Incident.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .source(request.source().trim())
                .status(request.status() != null ? request.status() : IncidentStatus.OPEN)
                .priority(request.priority() != null ? request.priority() : IncidentPriority.MEDIUM)
                .build();

        return incidentRepository.save(incident);
    }

    public Incident updateStatus(Long id, UpdateIncidentStatusRequest request) {
        if (request == null || request.status() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        Incident incident = findIncidentOrThrow(id);
        incident.setStatus(request.status());
        return incidentRepository.save(incident);
    }

    public Incident updatePriority(Long id, UpdateIncidentPriorityRequest request) {
        if (request == null || request.priority() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Priority is required");
        }

        Incident incident = findIncidentOrThrow(id);
        incident.setPriority(request.priority());
        return incidentRepository.save(incident);
    }

    public void deleteIncident(Long id) {
        Incident incident = findIncidentOrThrow(id);
        incidentRepository.delete(incident);
    }

    private Incident findIncidentOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));
    }

    private void validateCreateRequest(CreateIncidentRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (isBlank(request.title())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (isBlank(request.description())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
        }
        if (isBlank(request.source())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
