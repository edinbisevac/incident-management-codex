package com.provadis.incidentmanagement.incident.repository;

import com.provadis.incidentmanagement.incident.model.Incident;
import com.provadis.incidentmanagement.incident.model.IncidentStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findFirstBySourceIgnoreCaseAndStatusNotOrderByIdAsc(String source, IncidentStatus status);
}
