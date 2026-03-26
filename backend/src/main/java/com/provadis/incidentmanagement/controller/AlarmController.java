package com.provadis.incidentmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.provadis.incidentmanagement.dto.CreateAlarmRequest;
import com.provadis.incidentmanagement.model.Alarm;
import com.provadis.incidentmanagement.service.AlarmService;

@RestController
@RequestMapping("/api/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Alarm create(@RequestBody CreateAlarmRequest request) {
        return alarmService.create(request);
    }

    @GetMapping
    public List<Alarm> getAll(@RequestParam(required = false) String source) {
        return alarmService.getAll(source);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        alarmService.delete(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll() {
        alarmService.deleteAll();
    }
}
