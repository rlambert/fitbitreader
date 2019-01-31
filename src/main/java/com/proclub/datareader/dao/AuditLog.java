package com.proclub.datareader.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name="AuditLog", schema="dbo")
public class AuditLog {

    public enum Activity {
        RefreshedCredentials,
        RefreshError,
        ActivityLevelRead,
        WeightRead,
        SleepRead,
        StepsRead,
        Error,
        ReauthEmail,
        RunSummary
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "Id")
    private long id;

    @Column(name = "DateTime")
    private LocalDateTime dateTime;

    @Column(name = "UserId")
    private String fkUserGuid;
    public void setFkUserGuid(String uid) {
        fkUserGuid = uid.toUpperCase();
    }

    @Column(name = "Activity")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String activity;

    @Column(name = "Details")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String details;

    public AuditLog() {}

    public AuditLog(String fkUserGuid, LocalDateTime dateTime, Activity activity, String details) {
        this.fkUserGuid = fkUserGuid;
        this.dateTime = dateTime.truncatedTo(ChronoUnit.SECONDS);
        this.activity = activity.name();
        this.details = details;
    }
}
