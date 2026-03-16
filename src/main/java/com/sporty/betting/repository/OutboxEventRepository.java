package com.sporty.betting.repository;

import com.sporty.betting.model.OutboxEvent;
import com.sporty.betting.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    List<OutboxEvent> findByStatus(OutboxStatus status);
}
