package com.provadis.incidentmanagement.alarm.service;

import com.provadis.incidentmanagement.alarm.dto.CreateAlarmRequest;
import com.provadis.incidentmanagement.alarm.model.Alarm;
import com.provadis.incidentmanagement.alarm.model.AlarmSeverity;
import com.provadis.incidentmanagement.alarm.repository.AlarmRepository;
import com.provadis.incidentmanagement.incident.model.Incident;
import com.provadis.incidentmanagement.incident.model.IncidentPriority;
import com.provadis.incidentmanagement.incident.model.IncidentStatus;
import com.provadis.incidentmanagement.incident.repository.IncidentRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private static final Sort ALARM_SORT = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
    );

    private final AlarmRepository alarmRepository;
    private final IncidentRepository incidentRepository;

    public List<Alarm> getAlarms(String source) {
        if (isBlank(source)) {
            return alarmRepository.findAll(ALARM_SORT);
        }

        return alarmRepository.findBySourceIgnoreCase(source.trim(), ALARM_SORT);
    }

    @Transactional
    public Alarm createAlarm(CreateAlarmRequest request) {
        validateCreateRequest(request);

        Alarm alarm = Alarm.builder()
                .source(request.source().trim())
                .message(request.message().trim())
                .severity(request.severity())
                .createdAt(request.createdAt() != null ? request.createdAt() : LocalDateTime.now())
                .build();

        Alarm savedAlarm = alarmRepository.save(alarm);
        upsertIncidentForAlarm(savedAlarm);

        return savedAlarm;
    }

    public void deleteAlarm(Long id) {
        Alarm alarm = alarmRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alarm not found"));

        alarmRepository.delete(alarm);
    }

    private void upsertIncidentForAlarm(Alarm alarm) {
        IncidentPriority mappedPriority = mapSeverityToPriority(alarm.getSeverity());

        Incident incident = incidentRepository.findFirstBySourceIgnoreCaseAndStatusNotOrderByIdAsc(
                        alarm.getSource(),
                        IncidentStatus.RESOLVED
                )
                .orElseGet(() -> createIncidentForAlarm(alarm, mappedPriority));

        incident.setPriority(getHigherPriority(incident.getPriority(), mappedPriority));
        incidentRepository.save(incident);
    }

    private Incident createIncidentForAlarm(Alarm alarm, IncidentPriority priority) {
        return Incident.builder()
                .title("Incident from " + alarm.getSource())
                .description("Generated from alarm: " + alarm.getMessage())
                .status(IncidentStatus.OPEN)
                .priority(priority)
                .source(alarm.getSource())
                .build();
    }

    private IncidentPriority mapSeverityToPriority(AlarmSeverity severity) {
        return switch (severity) {
            case CRITICAL -> IncidentPriority.HIGH;
            case MAJOR -> IncidentPriority.MEDIUM;
            case MINOR -> IncidentPriority.LOW;
        };
    }

    private IncidentPriority getHigherPriority(IncidentPriority current, IncidentPriority candidate) {
        if (current == null) {
            return candidate;
        }

        return current.ordinal() >= candidate.ordinal() ? current : candidate;
    }

    private void validateCreateRequest(CreateAlarmRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (isBlank(request.source())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source is required");
        }
        if (isBlank(request.message())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is required");
        }
        if (request.severity() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Severity is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
