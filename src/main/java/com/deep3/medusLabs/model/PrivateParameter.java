package com.deep3.medusLabs.model;

import com.amazonaws.services.cloudformation.model.Parameter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Replacement for com.amazonaws.services.cloudformation.model.Parameter to work with Hibernate
 */
@Entity
public class PrivateParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paramName;
    private String paramType;
    private String paramDescription;
    private String paramValue;

    public PrivateParameter() {}

    public PrivateParameter(String paramName, String paramValue, String paramType,String paramDescription) {
        this.paramName = paramName;
        this.paramValue = paramValue;
        this.paramType = paramType;
        this.paramDescription = paramDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getParamDescription() {
        return paramDescription;
    }

    public void setParamDescription(String paramDescription) {
        this.paramDescription = paramDescription;
    }

    public Parameter getAsParameter() {
        return new Parameter().withParameterKey(paramName).withParameterValue(paramValue);
    }
}
