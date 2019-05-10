package com.deep3.medusLabs.controller;

import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.model.APIResponse;
import com.deep3.medusLabs.model.Lab;
import com.deep3.medusLabs.service.DeployedLabLogService;
import com.deep3.medusLabs.service.DeployedLabService;
import com.deep3.medusLabs.service.LabServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/labs")
public class LabController {

    private LabServiceImpl labService;
    private DeployedLabService deployedLabService;
    private DeployedLabLogService deployedLabLogService;

    @Autowired
    public LabController(LabServiceImpl labService, DeployedLabService deployedLabService,
                         DeployedLabLogService deployedLabLogService) {
        this.labService = labService;
        this.deployedLabService = deployedLabService;
        this.deployedLabLogService = deployedLabLogService;
    }

    /**
     * Get All labs from the local data source
     * @return - A Response Entity containing an APIResponse
     */
    @GetMapping(path = ("/"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getAllLabs() {
        APIResponse response = new APIResponse<>(HttpStatus.OK, labService.findAll());
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /***
     * Get the number of clean accounts available
     * @return - A Response Entity containing an APIResponse
     */
    @GetMapping(path = ("/accounts/clean"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getNumberOfCleanAccounts() {
        APIResponse response = new APIResponse<>(HttpStatus.OK, deployedLabService.getNumberOfCleanAccounts());
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /***
     * Get deployed Labs from the local application data store
     * @return - A Response Entity containing an APIResponse
     */
    @GetMapping(path = ("/deployed"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getAllDeployedLabs() {
        APIResponse response = new APIResponse<>(HttpStatus.OK, deployedLabService.findDeployedLabs());
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /***
     * Get deleted Labs from the local application data store
     * @return - A Response Entity containing an APIResponse
     */
    @GetMapping(path = ("/deleted"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getAllDeletedLabs() {
        APIResponse response = new APIResponse<>(HttpStatus.OK, deployedLabService.findDeletedLabs());
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Get A lab from the local data source by querying it's ID
     * @return - A Response Entity containing an APIResponse
     */
    @GetMapping(path = ("/{id}"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getLab(@PathVariable("id") long id) throws ObjectNotFoundException {
        APIResponse response = new APIResponse<>(HttpStatus.OK, labService.findOne(id));
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Get deployed lab logs from the local data source by querying using the deployed lab ID
     * @return - A Response Entity containing an APIResponse
     */
    @GetMapping(path = ("deployed/logs/{id}"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getLogsForDeployedLab(@PathVariable("id") long id) {
        APIResponse response = new APIResponse<>(HttpStatus.OK, deployedLabLogService.getLogsByLab(id));
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Create a new lab in the local data store
     * @return - A Response Entity containing an APIResponse
     */
    @PostMapping(path = ("/"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> createLab(@RequestBody Lab lab) {
        APIResponse response = new APIResponse<>(HttpStatus.CREATED, labService.save(lab));
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Update a lab in the local data store
     * @return - A Response Entity containing an APIResponse
     */
    @PutMapping(path = ("/"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> updateLab(@RequestBody Lab lab) throws ObjectNotFoundException {
        Lab existingLab = lab;
        existingLab.setId(labService.findByName(lab.getName()).getId());
        APIResponse response = new APIResponse<>(HttpStatus.CREATED, labService.save(existingLab));
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}