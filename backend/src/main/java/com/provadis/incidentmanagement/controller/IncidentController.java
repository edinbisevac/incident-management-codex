package com.provadis.incidentmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.provadis.incidentmanagement.dto.CreateIncidentRequest;
import com.provadis.incidentmanagement.dto.UpdateIncidentPriorityRequest;
import com.provadis.incidentmanagement.dto.UpdateIncidentStatusRequest;
import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.service.IncidentService;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public List<Incident> getAll() {
        return incidentService.getAll();
    }

    @GetMapping("/{id}")
    public Incident getById(@PathVariable Long id) {
        return incidentService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Incident create(@RequestBody CreateIncidentRequest request) {
        return incidentService.create(request);
    }

    @PatchMapping("/{id}/status")
    public Incident updateStatus(@PathVariable Long id, @RequestBody UpdateIncidentStatusRequest request) {
        return incidentService.updateStatus(id, request.status());
    }

    @PatchMapping("/{id}/priority")
    public Incident updatePriority(@PathVariable Long id, @RequestBody UpdateIncidentPriorityRequest request) {
        return incidentService.updatePriority(id, request.priority());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        incidentService.delete(id);
    }
}
