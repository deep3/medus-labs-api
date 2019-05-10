package com.deep3.medusLabs.repository;

import com.deep3.medusLabs.model.Lab;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabRepository extends CrudRepository<Lab, Long> {
    Lab findByName(String name);
}
