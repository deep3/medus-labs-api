package com.deep3.medusLabs.service;

import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.aws.model.AWSAccountWrapper;
import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.Lab;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DeployedLabService {

    DeployedLab findDeployedLabById(Long deployedLabId) throws ObjectNotFoundException;

    List<DeployedLab> findDeployedLabs();

    List<DeployedLab> findDeletedLabs();

    List<String> getDirtyAccountIDs();

    int getNumberOfCleanAccounts();

    DeployedLab saveDeployedLab(DeployedLab lab);

    DeployedLab recordDeployingLab(Lab lab, List<AWSAccountWrapper> accounts);

    void deleteDeployedLab(Long deployedLabId) throws ObjectNotFoundException;

    void deleteAll();

}
