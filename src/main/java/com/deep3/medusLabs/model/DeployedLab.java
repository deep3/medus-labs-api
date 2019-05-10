package com.deep3.medusLabs.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class DeployedLab {

    private DeployedLabStatus deployedLabStatus;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="lab_id")
    private Lab lab;

    private Date deployed;

    public DeployedLab()
    {
        setDeployed(new Date());
        deployedLabStatus = DeployedLabStatus.ACTIVE;
    }

    private Date undeployed;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<String> labAccounts;

    public DeployedLab(Lab lab, List<String> accounts)
    {
        setDeployed(new Date());
        setLab(lab);
        setLabAccounts(accounts);
        deployedLabStatus = DeployedLabStatus.ACTIVE;
    }

    public DeployedLabStatus getDeployedLabStatus() {
        return deployedLabStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public Date getDeployed() {
        return deployed;
    }

    public void setDeployed(Date deployed) {
        this.deployed = deployed;
    }

    public Date getUndeployed() {
        return undeployed;
    }

    public void setUndeployed(Date undeployed) {
        this.undeployed = undeployed;
    }

    public List<String> getLabAccounts() {
        return labAccounts;
    }

    public void setLabAccounts(List<String> labAccounts) {
        this.labAccounts = labAccounts;
    }

    public void setDeployedLabStatus(DeployedLabStatus deployedLabStatus) {
        this.deployedLabStatus = deployedLabStatus;
    }

    public Date getDateDeployed() {
        return deployed;
    }
    public void setDateDeployed(Date deployed) {
        this.deployed = deployed;
    }

    public enum DeployedLabStatus {
        DEPLOYING, ACTIVE, DELETING, FAILED, DELETED;
    }
}
