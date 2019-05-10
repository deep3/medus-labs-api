package com.deep3.medusLabs.controller;

import com.amazonaws.SdkClientException;
import com.deep3.medusLabs.aws.utilities.AWSMetadataUtils;
import com.deep3.medusLabs.model.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metadata")
public class InstanceMetadataController {

    @Autowired
    public InstanceMetadataController() {}

    /**
     * Get a given instance's ID
     * @return instance ID as a Response Entity object
     * @throws SdkClientException
     */
    @GetMapping(path = ("/instanceid"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getInstanceId() throws SdkClientException {

        APIResponse response;
    try {
            response = new APIResponse<>(HttpStatus.OK, AWSMetadataUtils.getInstanceId());
        } catch (SdkClientException e) {
            response = new APIResponse<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    /**
     * Get an instance role
     * @return instance role as a Response Entity Object
     * @throws SdkClientException
     */
    @GetMapping(path = ("/instancerole"), produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> getInstanceRole() throws SdkClientException {

        APIResponse response;
        try {
            response = new APIResponse<>(HttpStatus.OK, AWSMetadataUtils.getRoleArn());
        } catch (SdkClientException e) {
            response = new APIResponse<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}
