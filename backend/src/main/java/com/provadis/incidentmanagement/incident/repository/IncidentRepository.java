package com.provadis.incidentmanagement.incident.repository;

import com.provadis.incidentmanagement.incident.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
}
