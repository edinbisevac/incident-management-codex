package com.provadis.incidentmanagement.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.provadis.incidentmanagement.model.Incident;
import com.provadis.incidentmanagement.model.IncidentStatus;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findAllByOrderByIdDesc();

    Optional<Incident> findFirstBySourceAndStatusInOrderByIdDesc(String source, Collection<IncidentStatus> statuses);
}
