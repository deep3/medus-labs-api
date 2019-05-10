package com.deep3.medusLabs.repository;

import com.deep3.medusLabs.model.LogEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeployedLabLogRepository extends CrudRepository<LogEntry, Long> {
}
