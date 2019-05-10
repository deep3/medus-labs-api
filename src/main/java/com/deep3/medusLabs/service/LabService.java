package com.deep3.medusLabs.service;

import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.model.Lab;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LabService {

    Lab findOne(Long id) throws ObjectNotFoundException;

    List<Lab> findAll();

    Lab findByName(String Name) throws ObjectNotFoundException;;

    Lab save(Lab lab);

    void deleteAll();

    Lab updateLab(Lab existingLab, Lab updateLab);
}
