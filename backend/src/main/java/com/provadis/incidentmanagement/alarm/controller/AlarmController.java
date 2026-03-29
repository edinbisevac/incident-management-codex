package com.provadis.incidentmanagement.alarm.controller;

import com.provadis.incidentmanagement.alarm.dto.CreateAlarmRequest;
import com.provadis.incidentmanagement.alarm.model.Alarm;
import com.provadis.incidentmanagement.alarm.service.AlarmService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Alarm createAlarm(@RequestBody CreateAlarmRequest request) {
        return alarmService.createAlarm(request);
    }

    @GetMapping
    public List<Alarm> getAlarms(@RequestParam(required = false) String source) {
        return alarmService.getAlarms(source);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlarm(@PathVariable Long id) {
        alarmService.deleteAlarm(id);
    }
}
