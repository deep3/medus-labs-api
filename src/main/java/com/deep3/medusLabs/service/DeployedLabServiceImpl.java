package com.deep3.medusLabs.service;

import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.aws.model.AWSAccountWrapper;
import com.deep3.medusLabs.aws.service.OrganisationsService;
import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.Lab;
import com.deep3.medusLabs.repository.DeployedLabRepository;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeployedLabServiceImpl implements DeployedLabService {

    private final OrganisationsService organisationsService;
    private DeployedLabRepository deployedLabRepository;

    @Autowired
    public DeployedLabServiceImpl(DeployedLabRepository deployedLabRepository, OrganisationsService organisationsService) {
        this.deployedLabRepository = deployedLabRepository;
        this.organisationsService = organisationsService;
    }

    /**
     * Find all Deployed Labs stored in the Data Source
     * @return A List of DeployedLabs
     */
    @Override
    public List<DeployedLab> findDeployedLabs() {
        return IteratorUtils.toList(deployedLabRepository.findAll().iterator())
                .stream()
                .filter(dep -> dep.getDeployedLabStatus() == DeployedLab.DeployedLabStatus.ACTIVE ||
                        dep.getDeployedLabStatus() == DeployedLab.DeployedLabStatus.DEPLOYING ||
                        dep.getDeployedLabStatus() == DeployedLab.DeployedLabStatus.FAILED ||
                        dep.getDeployedLabStatus() == DeployedLab.DeployedLabStatus.DELETING)
                .collect(Collectors.toList());
    }

    /**
     * Find all Deployed Labs that have been deleted (including those with a state of 'deleting')
     * @return A List of DeployedLabs
     */
    @Override
    public List<DeployedLab> findDeletedLabs() {
        return IteratorUtils.toList(deployedLabRepository.findAll().iterator())
                .stream()
                .filter(dep -> dep.getDeployedLabStatus() == DeployedLab.DeployedLabStatus.DELETED ||
                        dep.getDeployedLabStatus() == DeployedLab.DeployedLabStatus.DELETING)
                .collect(Collectors.toList());
    }

    /**
     * Save the DeployedLab to the local Data Store
     * @param lab The DeployedLab Object to save
     * @return A DeployedLab Object
     */
    @Override
    public DeployedLab saveDeployedLab(DeployedLab lab) {
        return deployedLabRepository.save(lab);
    }

    /**
     * Record the deployment of a Lab to the local data source
     * @param lab The labtype that has been deployed
     * @param accounts The accounts that have been used in the deployment
     */
    @Override
    public DeployedLab recordDeployingLab(Lab lab, List<AWSAccountWrapper> accounts) {
        List<String> accountIDs = accounts.stream().map(a -> a.getAccount().getId()).collect(Collectors.toList());
        DeployedLab deployed = new DeployedLab(lab, accountIDs);
        deployed.setDeployedLabStatus(DeployedLab.DeployedLabStatus.DEPLOYING);
        return deployedLabRepository.save(deployed);
    }

    /**
     * Get all of the AccountIDs associated with any of the currently Deployed Labs
     * @return A List of ID's represented as Strings
     */
    @Override
    public List<String> getDirtyAccountIDs() {
        List<String> dirtyAccounts = new ArrayList<>();

        for (DeployedLab lab : findDeployedLabs()) {
            dirtyAccounts.addAll(lab.getLabAccounts());
        }

        return dirtyAccounts;
    }

    /**
     * Get the number of 'clean' accounts (IE. Accounts that are not currently in use as part of a Deployed Lab)
     * @return An Integer value
     */
    @Override
    public int getNumberOfCleanAccounts() {
        return organisationsService.getValidAccounts().size() - getDirtyAccountIDs().size();
    }

    /**
     * Find a specific Deployed Lab by it's ID property.
     * @param deployedLabId The ID of the Deployed Lab to find
     * @return The resulting DeployedLab object
     * @throws ObjectNotFoundException
     */
    @Override
    public DeployedLab findDeployedLabById(Long deployedLabId) throws ObjectNotFoundException {
        return deployedLabRepository.findById(deployedLabId).orElseThrow(() -> new ObjectNotFoundException(DeployedLab.class, "id", deployedLabId.toString()));
    }

    /**
     * Delete a Deployed Lab by setting it's status to 'DELETED'
     * @param deployedLabId The ID of the Deployed Lab to Delete
     * @throws ObjectNotFoundException
     */
    @Override
    public void deleteDeployedLab(Long deployedLabId) throws ObjectNotFoundException {
        DeployedLab lab = deployedLabRepository.findById(deployedLabId).orElseThrow(() -> new ObjectNotFoundException(DeployedLab.class, "id", deployedLabId.toString()));
        lab.setDeployedLabStatus(DeployedLab.DeployedLabStatus.DELETED);
        saveDeployedLab(lab);
    }

    /**
     * Delete all DeployedLabs from the local Data Source
     */
    @Override
    public void deleteAll() { deployedLabRepository.deleteAll(); }

    /**
     * Check if the Deployment of a Lab has taken more than 30 minutes. This could indicate a failed deployment.
     * @param deployedLab The DeployedLab to check
     */
    public void labDeployedGreaterThan30Mins(DeployedLab deployedLab) {

        long difference = new Date().getTime() - deployedLab.getDateDeployed().getTime();

        // MilliSecond * minutes > 30 minutes
        long elapsedMinutes = 1000 * 60;

        if(difference / elapsedMinutes > 30) {
            deployedLab.setDeployedLabStatus(DeployedLab.DeployedLabStatus.FAILED);
        }
        deployedLabRepository.save(deployedLab);
    }
}
