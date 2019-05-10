package com.deep3.medusLabs.controller;

import com.amazonaws.services.organizations.model.Account;
import com.deep3.medusLabs.aws.exceptions.BadRequestException;
import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;
import com.deep3.medusLabs.aws.model.AWSAccountWrapper;
import com.deep3.medusLabs.aws.service.AccountWranglerService;
import com.deep3.medusLabs.aws.service.OrganisationsService;
import com.deep3.medusLabs.model.APIResponse;
import com.deep3.medusLabs.model.CreateStudentAccountRequest;
import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.service.DeployedLabService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/organisations")
public class MemberOrganisationController {

    private final OrganisationsService organisationsService;
    private AccountWranglerService accountWranglerService;
    private DeployedLabService deployedLabService;

    private static final Logger LOG = LoggerFactory.getLogger(MemberOrganisationController.class);

    @Autowired
    public MemberOrganisationController(OrganisationsService organisationsService, AccountWranglerService accountWranglerService, DeployedLabService deployedLabService) {
        this.organisationsService = organisationsService;
        this.accountWranglerService = accountWranglerService;
        this.deployedLabService = deployedLabService;
    }

    /**
     * Get all Accounts from the local data source
     * @return - A Response Entity containing an APIResponse
     */
    @GetMapping(path = ("/"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse<Account>> getAllAccounts(@RequestParam(value = "type", required = false) String accountTypes) throws BadRequestException {
        List<Account> results;

        if(accountTypes == null){
            results = organisationsService.getAllAccounts();
        } else if (accountTypes.equalsIgnoreCase("invalid")) {
            results = organisationsService.getInvalidAccounts();
        } else if (accountTypes.equalsIgnoreCase("valid")) {
            results = organisationsService.getValidAccounts();
        } else {
            throw new BadRequestException("The value " + accountTypes + " is not supported for parameter 'type'");
        }
        return new ResponseEntity<>(new APIResponse<>(HttpStatus.OK,results),HttpStatus.OK);
    }

    /**
     * Get the Root Account ID For the Organisation configured by the local AWS CLI
     * @return - A Response Entity containing an APIResponse
     */
    @GetMapping(path = "/rootaccountid",produces = MediaType.APPLICATION_JSON_VALUE)
    public APIResponse<String> getAccountId(){
        return new APIResponse<String>(HttpStatus.OK,organisationsService.getAccountId());
    }

    /**
     * Creates new Student Accounts
     * @param request - A valid CreateStudentAccountRequest object, containing the number of accounts required
     * @return - A Response Entity containing an APIResponse
     */
    @PostMapping(path = "/create")
    public ResponseEntity<APIResponse<ArrayList<String>>> createNewAccounts(@RequestBody @Valid CreateStudentAccountRequest request) {
        return new ResponseEntity<APIResponse<ArrayList<String>>>(new APIResponse(HttpStatus.OK, organisationsService.createAccounts(request.getAccounts(), request.getEmail())), HttpStatus.OK);
    }

    /**
     * 'Nukes' all AWS accounts. Should be noted this does not delete them
     * @return - A response entity containing both the HTTP response code and a variable confirming the number of accounts deleted.
     */
    @DeleteMapping(path = "/nuke")
    public APIResponse<String> deleteAllData () {
        List<DeployedLab> labs = deployedLabService.findDeployedLabs();
        ArrayList<AWSAccountWrapper> accs = accountWranglerService.getAccounts();
        labs.forEach(lab ->{
            try {
                long labID = lab.getId();
                deployedLabService.deleteDeployedLab(labID);
                LOG.info("Deleting Lab with id : {}",labID);
            } catch (ObjectNotFoundException e) {
                e.printStackTrace();
            }
        });
        Long labToNukeID = labs.get(0).getId();
        int numAccs = accs.size();
        String response = String.valueOf(numAccs);
        return new APIResponse<>(accountWranglerService.wipeAllAccounts(labToNukeID), response);
    }
}
