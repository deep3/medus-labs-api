package com.deep3.medusLabs.model;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(length=1000)
    private String name;

    @Column(length=1000)
    private String description;

    @NotEmpty
    private String templateUrl;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="privateParameters_id")
    private List<PrivateParameter> privateParameters;

    @ElementCollection
    @Column(name="region")
    private List<String> regions;

    private String status;

    private Date created;

    private Date lastDeployed;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="deployedLab_id")
    private List<DeployedLab> deployedLabs;

    public Lab(){
        this.privateParameters = new ArrayList<>();
    }

    public Lab(String name, String templateUrl) {
        this.setName(name);
        this.setTemplateUrl(templateUrl);
        this.privateParameters = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastDeployed() {
        return lastDeployed;
    }

    public void setLastDeployed(Date lastDeployed) {
        this.lastDeployed = lastDeployed;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateUrl() {
        return templateUrl;
    }

    public void setTemplateUrl(String templateUrl) {
        this.templateUrl = templateUrl;
    }

    public List<PrivateParameter> getPrivateParameters() {
        return privateParameters;
    }

    public void setPrivateParameters(List<PrivateParameter> privateParameters) {
        this.privateParameters = privateParameters;
    }

    public void addPrivateParameter(PrivateParameter parameter) {
        privateParameters.add(parameter);
    }

    public List<Parameter> getCloudFormationPrivateParameters() {
        return privateParameters.stream().map(PrivateParameter::getAsParameter).collect(Collectors.toList());
    }

    public List<ObjectNode> getCloudFormationPublicParameters() {
        return new ArrayList<>();
    }

    public List<String> getRegions() {
        return regions;
    }

    public void setRegions(List<String> regions) {
        this.regions = regions;
    }
}
