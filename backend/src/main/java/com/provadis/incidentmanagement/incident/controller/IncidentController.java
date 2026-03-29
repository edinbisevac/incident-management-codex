package com.provadis.incidentmanagement.incident.controller;

import com.provadis.incidentmanagement.incident.dto.CreateIncidentRequest;
import com.provadis.incidentmanagement.incident.dto.UpdateIncidentPriorityRequest;
import com.provadis.incidentmanagement.incident.dto.UpdateIncidentStatusRequest;
import com.provadis.incidentmanagement.incident.model.Incident;
import com.provadis.incidentmanagement.incident.service.IncidentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public List<Incident> getAllIncidents() {
        return incidentService.getAllIncidents();
    }

    @GetMapping("/{id}")
    public Incident getIncidentById(@PathVariable Long id) {
        return incidentService.getIncidentById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Incident createIncident(@RequestBody CreateIncidentRequest request) {
        return incidentService.createIncident(request);
    }

    @PatchMapping("/{id}/status")
    public Incident updateIncidentStatus(
            @PathVariable Long id,
            @RequestBody UpdateIncidentStatusRequest request
    ) {
        return incidentService.updateStatus(id, request);
    }

    @PatchMapping("/{id}/priority")
    public Incident updateIncidentPriority(
            @PathVariable Long id,
            @RequestBody UpdateIncidentPriorityRequest request
    ) {
        return incidentService.updatePriority(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
    }
}
