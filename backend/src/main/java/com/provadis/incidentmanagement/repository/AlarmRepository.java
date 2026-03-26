package com.provadis.incidentmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.provadis.incidentmanagement.model.Alarm;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    List<Alarm> findAllByOrderByIdDesc();

    List<Alarm> findAllBySourceOrderByCreatedAtDescIdDesc(String source);
}
