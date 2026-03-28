package com.provadis.incidentmanagement.alarm.repository;

import com.provadis.incidentmanagement.alarm.model.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
}
