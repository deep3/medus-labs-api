package com.deep3.medusLabs.configuration;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.deep3.medusLabs.aws.components.EnabledRegions;
import com.deep3.medusLabs.aws.service.S3Service;
import com.deep3.medusLabs.model.Lab;
import com.deep3.medusLabs.model.PrivateParameter;
import com.deep3.medusLabs.service.LabService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class LabConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(LabConfiguration.class);

    private CloudFormationScriptManager cloudFormationScriptManager;
    private LabService labService;
    private S3Service s3Service;

    @Autowired
    public LabConfiguration(CloudFormationScriptManager cloudFormationScriptManager, LabService labService, S3Service s3Service) {
        this.cloudFormationScriptManager = cloudFormationScriptManager;
        this.labService = labService;
        this.s3Service = s3Service;
        populateLabData();
    }

    /**
     * Populates the database with metadata about available labs. Parses the available cloudFormation scripts to ensure
     * only available labs are present in the database
     */
    private void populateLabData() {
        List<String> labs = s3Service.listObjects(cloudFormationScriptManager.getBucketName(), "Labs");

        for (String labKey : labs) {
            try {
                Lab lab = new Lab(FilenameUtils.getBaseName(labKey), cloudFormationScriptManager.getTemplateUrl(FilenameUtils.getBaseName(labKey)));

                S3Object s3Object = s3Service.getObject(cloudFormationScriptManager.getBucketName(), labKey);
                S3ObjectInputStream inputStream = s3Object.getObjectContent();

                JsonNode labNode = new ObjectMapper().readTree(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));

                JsonNode descNode = labNode.get("Description");

                if (descNode != null) {
                    lab.setDescription((descNode.asText()));
                }

                // Load lab Metadata
                JsonNode metaNode = labNode.get("Metadata");
                List<String> enabledRegions = EnabledRegions.getEnabledRegionsAsString();
                if (metaNode != null) {
                    // Load lab region data
                    JsonNode regionsNode = metaNode.get("SupportedRegions");
                    if (regionsNode != null) {
                        List<String> specifiedRegions = new ArrayList<>();
                        for (JsonNode region : regionsNode) {
                            // Validate specified region string fits the expected region format and is contained within the enabled regions
                            if (enabledRegions.contains(region.asText())) {
                                specifiedRegions.add(region.asText());
                            } else {
                                LOG.warn("The region [ " + region.asText() + " ] is not registered as an enabled region or is written in an incorrect format");
                            }
                        }
                        lab.setRegions(specifiedRegions);
                    }
                }
                // Default value for regions if no regions can be gathered from the script
                if (lab.getRegions() == null || lab.getRegions().size() == 0) {
                    lab.setRegions(enabledRegions);
                }

                JsonNode jsonNode = labNode.get("Parameters");

                // Collects CF URLs for nested stacks
                if (jsonNode != null) {
                    Iterator<String> parameterNames = jsonNode.fieldNames();

                    String parameterName;
                    String parameterValue;
                    String desc = null;
                    String type = null;
                    while (parameterNames.hasNext()) {

                        parameterName = parameterNames.next();
                        List<JsonNode> parameters = jsonNode.findValues(parameterName);

                        for(JsonNode param: parameters){
                            parameterValue = String.valueOf(param);

                            if(parameterValue.contains("Description")){
                                JsonNode innerNode = param.get("Description");
                                desc = innerNode.asText();
                            }
                            if(parameterValue.contains("Type")){
                                JsonNode innerNode = param.get("Type");
                                type = innerNode.asText();
                            }
                        }

                        if (cloudFormationScriptManager.getTemplateUrl(parameterName) != null) {
                            lab.addPrivateParameter(new PrivateParameter(parameterName, cloudFormationScriptManager.getTemplateUrl(parameterName), type, desc));
                        }
                        desc = null;
                        type =  null;
                    }
                }

                try {
                    Lab existingLab = labService.findByName(lab.getName());

                    if (existingLab == null){
                        labService.save(lab); // create new lab
                    }
                    else{
                        lab.setId(existingLab.getId());
                        labService.save(lab); // update existing lab
                    }
                }
                catch (Exception e)
                {
                    LOG.error("Failed to update Lab " + FilenameUtils.getName(labKey) + " in the database");
                }
            } catch (IOException e) {
                LOG.error("Failed to save Lab " + FilenameUtils.getName(labKey) + " into the database");
            }
        }
    }
}
