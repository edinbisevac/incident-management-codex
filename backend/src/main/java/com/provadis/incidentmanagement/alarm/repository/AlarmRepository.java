package com.provadis.incidentmanagement.alarm.repository;

import com.provadis.incidentmanagement.alarm.model.Alarm;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    List<Alarm> findBySourceIgnoreCase(String source, Sort sort);
}
