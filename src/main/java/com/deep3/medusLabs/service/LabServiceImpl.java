package com.deep3.medusLabs.service;

import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.model.Lab;
import com.deep3.medusLabs.repository.LabRepository;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabServiceImpl implements LabService {

    private LabRepository labRepository;

    @Autowired
    public LabServiceImpl(LabRepository labRepository) {
        this.labRepository = labRepository;
    }

    /**
     * Find a specific Lab by it's ID property.
     * @param id The ID of the Lab to find
     * @return The resulting Lab object
     * @throws ObjectNotFoundException
     */
    @Override
    public Lab findOne(Long id) throws ObjectNotFoundException {
        try {
            return labRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException(Lab.class, "id", id.toString()));
        } catch (Exception ex) {
            throw new ObjectNotFoundException(Lab.class, "id", id.toString());
        }
    }

    /**
     * Find all Deployed Labs stored in the Data Source
     * @return A List of DeployedLabs
     */
    @Override
    public List<Lab> findAll() {
        return IteratorUtils.toList(labRepository.findAll().iterator());
    }

    /**
     * Find a specific Lab by it's NAME property.
     * @param name The Name of the Lab to find
     * @return The resulting Lab object
     * @throws ObjectNotFoundException
     */
    @Override
    public Lab findByName(String name) throws ObjectNotFoundException {

        try {
            return labRepository.findByName(name);
        } catch (Exception ex) {
            throw new ObjectNotFoundException(Lab.class, "Name", name);
        }
    }

    /**
     * Save the Lab to the local Data Store
     * @param lab The Lab Object to save
     * @return A Lab Object
     */
    @Override
    public Lab save(Lab lab) {
        return labRepository.save(lab);
    }

    /**
     * Delete all Labs from the local Data Source
     */
    @Override
    public void deleteAll() {
        labRepository.deleteAll();
    }

    /**
     * Update an existing Lab in the local data store
     * @param labToUpdate The Lab To update
     * @param updateLab The Updated Lab
     * @return A Lab Object
     */
    @Override
    public Lab updateLab(Lab labToUpdate, Lab updateLab) {
        labToUpdate.setPrivateParameters(updateLab.getPrivateParameters());
        labToUpdate.setName(updateLab.getName());
        labToUpdate.setDescription(updateLab.getDescription());
        labToUpdate.setTemplateUrl(updateLab.getTemplateUrl());
        return labToUpdate;
    }
}