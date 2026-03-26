package com.provadis.incidentmanagement;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.provadis.incidentmanagement.dto.CreateAlarmRequest;
import com.provadis.incidentmanagement.model.Alarm;
import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.model.IncidentPriority;
import com.provadis.incidentmanagement.repository.AlarmRepository;
import com.provadis.incidentmanagement.repository.IncidentRepository;
import com.provadis.incidentmanagement.service.AlarmService;

@SpringBootTest
class AlarmServiceIntegrationTest {

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @BeforeEach
    void cleanUp() {
        alarmRepository.deleteAll();
        incidentRepository.deleteAll();
    }

    @Test
    void shouldCreateIncidentForFirstAlarmAndKeepHighestPriorityForSameSource() {
        alarmService.create(new CreateAlarmRequest("Router-01", "Link down", "CRITICAL"));
        alarmService.create(new CreateAlarmRequest("Router-01", "Recovered but unstable", "MINOR"));

        List<Incident> incidents = incidentRepository.findAll();
        List<Alarm> alarms = alarmRepository.findAll();

        assertThat(alarms).hasSize(2);
        assertThat(incidents).hasSize(1);
        assertThat(incidents.get(0).getSource()).isEqualTo("Router-01");
        assertThat(incidents.get(0).getDescription()).isEqualTo("Recovered but unstable");
        assertThat(incidents.get(0).getPriority()).isEqualTo(IncidentPriority.HIGH);
    }

    @Test
    void shouldCreateSeparateIncidentsForDifferentSources() {
        alarmService.create(new CreateAlarmRequest("Router-01", "Link down", "MAJOR"));
        alarmService.create(new CreateAlarmRequest("Switch-07", "Port saturation", "MINOR"));

        List<Incident> incidents = incidentRepository.findAll();

        assertThat(incidents).hasSize(2);
        assertThat(incidents)
                .extracting(Incident::getSource)
                .containsExactlyInAnyOrder("Router-01", "Switch-07");
    }
}
