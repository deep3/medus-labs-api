package com.deep3.medusLabs.model;

import java.util.List;

public class DeployLabResponse {

    private DeployedLab deployedLab;
    private List<Student> studentList;

    public DeployLabResponse(DeployedLab deployedLab, List<Student> studentList) {
        this.deployedLab = deployedLab;
        this.studentList = studentList;
    }

    public DeployLabResponse() {}

    public DeployedLab getDeployedLab() {
        return deployedLab;
    }

    public void setDeployedLab(DeployedLab deployedLab) {
        this.deployedLab = deployedLab;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }
}
