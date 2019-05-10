package com.deep3.medusLabs.controller;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.deep3.medusLabs.aws.exceptions.NoAvailableServiceComponent;
import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.aws.model.AWSAccountWrapper;
import com.deep3.medusLabs.aws.service.AccountWranglerService;
import com.deep3.medusLabs.aws.service.OrganisationsService;
import com.deep3.medusLabs.model.*;
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.utilities.StackTrackingUtils;
import com.deep3.medusLabs.security.service.TokenAuthenticationService;
import com.deep3.medusLabs.service.DeployedLabLogService;
import com.deep3.medusLabs.service.DeployedLabService;
import com.deep3.medusLabs.service.LabServiceImpl;
import com.deep3.medusLabs.utilities.JsonObjectUtils;
import com.deep3.medusLabs.utilities.PasswordUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/aws/labs")
public class AWSLabController {

    private AccountWranglerService wranglerService;
    private LabServiceImpl labService;
    private DeployedLabService deployedLabService;
    private static final Logger LOG = LoggerFactory.getLogger(AWSLabController.class);
    private DeployedLabLogService deployedLabLogService;
    private OrganisationsService organisationsService;
    private TokenAuthenticationService tokenAuthenticationService;

    @Autowired
    public AWSLabController(AccountWranglerService wranglerService,
                            LabServiceImpl labService,
                            DeployedLabService deployedLabService,
                            DeployedLabLogService deployedLabLogService,
                            OrganisationsService organisationsService,
                            TokenAuthenticationService tokenAuthenticationService) {

        this.wranglerService = wranglerService;
        this.labService = labService;
        this.deployedLabService = deployedLabService;
        this.deployedLabLogService = deployedLabLogService;
        this.organisationsService = organisationsService;
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    /**
     * Get all available Labs from S3 bucket
     * @return - A Response Entity containing an APIResponse
     */
    @GetMapping(path = ("/"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getAvailableLabsFromS3() {
        APIResponse response = new APIResponse<>(HttpStatus.OK, labService.findAll());
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Gets The CloudFormation Public Parameters For the provided service
     * @param serviceType - The name of the Service this Lab relates too
     * @return - A Response Entity containing an APIResponse
     * @throws ObjectNotFoundException
     */
    @GetMapping(path = ("/param"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getKnownParams(@RequestParam(value = "service", required = false) String serviceType) throws ObjectNotFoundException {
        APIResponse response = new APIResponse<>(HttpStatus.OK, labService.findByName(serviceType).getCloudFormationPublicParameters());
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Deploys A lab into the AWS environment
     * @return - A Response Entity containing an APIResponse
     */
    @RequestMapping(value = "/deploy", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = "application/json")
    public APIResponse<DeployLabResponse> deploy(@RequestBody Map<String, Object> payload) throws ObjectNotFoundException {
        List<Parameter> parameters = new ArrayList<>();

        Lab lab = labService.findByName(payload.get("name").toString());
        Long numberOfUsers = Long.parseLong(payload.get("amount").toString());
        String region = payload.get("region").toString();

        // Get the required number of clean accounts
        Map<AWSAccountWrapper, Student> cleanAccounts =
                wranglerService.getAccounts().stream().filter(a -> !deployedLabService.getDirtyAccountIDs().contains(a.getAccount().getId()))
                        .limit(numberOfUsers)
                        .collect(Collectors.toMap(
                                a -> a,
                                a -> new Student("STUDENT" + UUID.randomUUID().toString().substring(0, 6),
                                        PasswordUtils.generatePassword(),
                                        a.getAccount().getId())
                                )
                        );

        DeployedLab deployingLab = deployedLabService.recordDeployingLab(lab, new ArrayList<>(cleanAccounts.keySet()));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {

                StackTrackingUtils.setNumOfUsers(Math.toIntExact(numberOfUsers));
                deployedLabLogService.createLog(deployingLab, LogLevel.INFO,
                        "Lab creation started by user: '" + tokenAuthenticationService.getUsername() + "' from account : '" + organisationsService.getAccountName() + "'." );

                for (Map.Entry<AWSAccountWrapper, Student> entry : cleanAccounts.entrySet()) {
                    AWSAccountWrapper wrapper = entry.getKey();
                    Student student = entry.getValue();
                    deployedLabLogService.createLog(deployingLab, LogLevel.INFO,
                            "Creating username and passwords.");

                    parameters.add(new Parameter().withParameterKey("UserName").withParameterValue(student.getUsername()));
                    parameters.add(new Parameter().withParameterKey("Password").withParameterValue(student.getPassword()));
                    parameters.addAll(JsonObjectUtils.getParametersFromJson((Map) payload.get("params")));
                    parameters.addAll(lab.getCloudFormationPrivateParameters());

                    // Deploy lab in MemberOrganisation
                    try {
                        wranglerService.deployCloudFormationStack(
                                Regions.fromName(region), lab.getName() + UUID.randomUUID().toString().substring(0, 6), wrapper, deployingLab, parameters
                        );
                    } catch (NoAvailableServiceComponent e) {
                        String errorMessage = "Failed to deploy lab for user [ " + student.getUsername() +
                                " ] - Could not access the service component for region [ " + region +
                                " ] in account [ " + wrapper.getAccount().getId() + " ]";
                        LOG.error(errorMessage, e);
                        deployingLab.setDeployedLabStatus(DeployedLab.DeployedLabStatus.FAILED);
                        deployedLabLogService.createLog(deployingLab, LogLevel.ERROR, errorMessage);
                    }
                    parameters.clear();
                }
            } catch (Exception e) {
                LOG.error("Thread creating lab failed! {}", e);
                deployingLab.setDeployedLabStatus(DeployedLab.DeployedLabStatus.FAILED);
                deployedLabService.saveDeployedLab(deployingLab);
                deployedLabLogService.createLog(deployingLab, LogLevel.ERROR,
                        "Lab creation failed during thread execution.");
            }
        });
        return new APIResponse<>(HttpStatus.ACCEPTED, new DeployLabResponse(deployingLab, new ArrayList<>(cleanAccounts.values())));
    }

    /**
     * Undeploys A Lab, including cleaning any accounts associated with the Lab
     * @param deployedLabId - The DeployedLab ID
     * @return - A Response Entity containing an APIResponse
     * @throws ObjectNotFoundException
     */
    @DeleteMapping(path = ("/undeploy"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> undeploy(@RequestParam(value = "deployedLabId") Long deployedLabId) throws ObjectNotFoundException {
        APIResponse response;
        DeployedLab deployedLab = deployedLabService.findDeployedLabById(deployedLabId);

        deployedLabLogService.createLog(deployedLab, LogLevel.INFO,
                "Undeployment started with Lab ID: '" + deployedLab.getId() + "' by User: '" + tokenAuthenticationService.getUsername() + "' from Teacher account : '" + organisationsService.getAccountName() + "'.");

        if (deployedLab.getDeployedLabStatus() == DeployedLab.DeployedLabStatus.ACTIVE || deployedLab.getDeployedLabStatus() == DeployedLab.DeployedLabStatus.FAILED) {
            deployedLab.setDeployedLabStatus(DeployedLab.DeployedLabStatus.DELETING);
            deployedLabService.saveDeployedLab(deployedLab);
            /*
             * We could create multiple threads for the forEach loop on each account
             * however the AWS API has throttling and you would risk hitting that throttle
             */
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    List<AWSAccountWrapper> deployedLabAccounts = wranglerService.getAccounts().stream()
                            .filter(account -> deployedLab.getLabAccounts().contains(account.getAccount().getId()))
                            .collect(Collectors.toList());

                    for(AWSAccountWrapper account: deployedLabAccounts)
                    {
                        deployedLabLogService.createLog(deployedLabId, LogLevel.INFO, "Deleting content of account: %s", account.getAccount().getId());
                        account.wipeAccount(deployedLab.getId());
                    }

                    deployedLabService.deleteDeployedLab(deployedLabId);
                    deployedLabLogService.createLog(deployedLab, LogLevel.SUCCESS,
                            "Undeployment of Lab with ID: '" + deployedLab.getId() + "' completed successfully.");

                } catch (Exception e) {
                    LOG.error("Thread deleting lab failed! {}", e);
                    deployedLab.setDeployedLabStatus(DeployedLab.DeployedLabStatus.FAILED);
                    deployedLabService.saveDeployedLab(deployedLab);
                    deployedLabLogService.createLog(deployedLab, LogLevel.ERROR,
                            "Undeployment of Lab with ID '" + deployedLab.getId() + "' failed during thread execution.");
                }
            });

            response = new APIResponse(HttpStatus.ACCEPTED);
        } else {
            deployedLabLogService.createLog(deployedLab, LogLevel.ERROR,
                    "Lab with ID: '" + deployedLabId + "' is not in ACTIVE or FAILED state.");
            response = new APIResponse(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}
