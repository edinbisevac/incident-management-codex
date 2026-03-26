package com.provadis.incidentmanagement.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.provadis.incidentmanagement.dto.CreateIncidentRequest;
import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.model.IncidentPriority;
import com.provadis.incidentmanagement.model.IncidentStatus;
import com.provadis.incidentmanagement.repository.IncidentRepository;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<Incident> getAll() {
        return incidentRepository.findAllByOrderByIdDesc();
    }

    public Incident getById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found"));
    }

    @Transactional
    public Incident create(CreateIncidentRequest request) {
        String title = requireText(request.title(), "title");
        String description = requireText(request.description(), "description");
        IncidentStatus status = request.status() != null ? request.status() : IncidentStatus.OPEN;
        IncidentPriority priority = request.priority() != null ? request.priority() : IncidentPriority.MEDIUM;
        String source = normalize(request.source());

        Incident incident = new Incident(title, description, status, priority, source);
        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident updateStatus(Long id, IncidentStatus status) {
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }

        Incident incident = getById(id);
        incident.setStatus(status);
        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident updatePriority(Long id, IncidentPriority priority) {
        if (priority == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "priority is required");
        }

        Incident incident = getById(id);
        incident.setPriority(priority);
        return incidentRepository.save(incident);
    }

    @Transactional
    public void delete(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found");
        }
        incidentRepository.deleteById(id);
    }

    private String requireText(String value, String fieldName) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
