package com.deep3.medusLabs.model;

import com.deep3.medusLabs.model.enums.LogLevel;

import javax.persistence.*;
import java.util.Date;

@Entity
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LogLevel logLevel;

    @Column(length = 500)
    private String message;

    private Date date;

    @ManyToOne
    @JoinColumn(name = "deployedlab_id")
    private DeployedLab lab;

    public LogEntry() {}

    public LogEntry(DeployedLab lab, LogLevel logLevel, String message) {
        setLab(lab);
        setLogLevel(logLevel);
        setMessage(message);
        setDate(new Date());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public DeployedLab getLab() {
        return lab;
    }

    public void setLab(DeployedLab lab) {
        this.lab = lab;
    }
}
