package com.provadis.incidentmanagement.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.provadis.incidentmanagement.dto.CreateAlarmRequest;
import com.provadis.incidentmanagement.model.Alarm;
import com.provadis.incidentmanagement.model.AlarmSeverity;
import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.model.IncidentPriority;
import com.provadis.incidentmanagement.model.IncidentStatus;
import com.provadis.incidentmanagement.repository.AlarmRepository;
import com.provadis.incidentmanagement.repository.IncidentRepository;

@Service
public class AlarmService {

    private static final EnumSet<IncidentStatus> OPEN_STATUSES = EnumSet.of(
            IncidentStatus.OPEN,
            IncidentStatus.IN_PROGRESS
    );

    private final AlarmRepository alarmRepository;
    private final IncidentRepository incidentRepository;

    public AlarmService(AlarmRepository alarmRepository, IncidentRepository incidentRepository) {
        this.alarmRepository = alarmRepository;
        this.incidentRepository = incidentRepository;
    }

    public List<Alarm> getAll(String source) {
        String normalizedSource = normalize(source);
        if (normalizedSource == null) {
            return alarmRepository.findAllByOrderByIdDesc();
        }
        return alarmRepository.findAllBySourceOrderByCreatedAtDescIdDesc(normalizedSource);
    }

    @Transactional
    public Alarm create(CreateAlarmRequest request) {
        String source = requireText(request.source(), "source");
        String message = requireText(request.message(), "message");
        AlarmSeverity severity = parseSeverity(request.severity());
        LocalDateTime createdAt = LocalDateTime.now();

        Alarm alarm = new Alarm(source, message, severity, createdAt);
        Alarm savedAlarm = alarmRepository.save(alarm);

        Incident incident = incidentRepository
                .findFirstBySourceAndStatusInOrderByIdDesc(source, OPEN_STATUSES)
                .orElseGet(() -> new Incident(
                        "Incident for " + source,
                        message,
                        IncidentStatus.OPEN,
                        mapPriority(severity),
                        source
                ));

        incident.setDescription(message);
        incident.setPriority(maxPriority(incident.getPriority(), mapPriority(severity)));
        incidentRepository.save(incident);

        return savedAlarm;
    }

    @Transactional
    public void delete(Long id) {
        if (!alarmRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alarm not found");
        }
        alarmRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        alarmRepository.deleteAll();
    }

    private AlarmSeverity parseSeverity(String severity) {
        String normalized = normalize(severity);
        if (normalized == null) {
            return AlarmSeverity.MAJOR;
        }

        try {
            return AlarmSeverity.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return AlarmSeverity.MAJOR;
        }
    }

    private IncidentPriority mapPriority(AlarmSeverity severity) {
        return switch (severity) {
            case CRITICAL -> IncidentPriority.HIGH;
            case MAJOR -> IncidentPriority.MEDIUM;
            case MINOR -> IncidentPriority.LOW;
        };
    }

    private IncidentPriority maxPriority(IncidentPriority current, IncidentPriority incoming) {
        IncidentPriority base = current != null ? current : IncidentPriority.MEDIUM;
        return incoming.ordinal() > base.ordinal() ? incoming : base;
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
