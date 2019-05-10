package com.deep3.medusLabs.repository;

import com.deep3.medusLabs.model.DeployedLab;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeployedLabRepository extends CrudRepository<DeployedLab, Long> {
}
