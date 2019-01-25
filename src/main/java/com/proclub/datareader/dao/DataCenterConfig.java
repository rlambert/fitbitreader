package com.proclub.datareader.dao;

import com.proclub.datareader.model.security.OAuthCredentials;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
//@IdClass(DataCenterConfig.Pkey.class)
@Table(name="DataCenterConfig", schema="dbo")
@Data
public class DataCenterConfig {

    private static Logger _logger = LoggerFactory.getLogger(DataCenterConfig.class);

    public enum PartnerStatus
    {
        /*
         * This is the data in the SimpleTrack.Status column
         *
         * relationship between the user and the partner
         * the state will change as they sign-in, experience data fetch issues or opt-out of the partner
         */

        Null (-1),
        SignUp (0),     // new user for this partner
        Active (1),     // have successfully authed
        AuthErr (2),    // auth was lost, user must re-auth
        RefreshErr (3), // partner did not respond or threw error during transmission
        DataErr(4),     // partner sent invalid tracking data (failed LSO validation)
        OptOut (5);     // user has opted out of further automatic refreshes but can still view past data

        public short status;

        private PartnerStatus(int status) {
            this.status = (short) status;
        }
    }

    public enum DataStatus
    {
        /*
         * This is the SimpleTrack.DataStatus column
         *
         * used to control any running processes for a user for the same partner
         * ensures no two web pages or processes are running on the same user at the same time
         */

        Null(-1),
        New(0),         // ready for next data operation
        Update(1),      // in the middle of the auth process
        Refresh(2);      // in the middle of the data refresh process

        public short status;

        private DataStatus(int status) {
            this.status = (short) status;
        }

    }

    /*
    @Data
    public static class Pkey implements Serializable {
        private UUID fkUserGuid;
        private int sourceSystem;

        public Pkey() {}

        public Pkey(UUID fkUserGuid, int sourceSystem) {
            this.fkUserGuid = fkUserGuid;
            this.sourceSystem = sourceSystem;
        }
    }
    */

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="fkUserGuid")
    private UUID fkUserGuid;

    @Column(name="SourceSystem")
    private int sourceSystem;

    @Column(name="LastChecked")
    private LocalDateTime lastChecked;

    @Column(name="PanelDisplay")
    private int panelDisplay;

    @Column(name="Status")
    private int status;

    @Column(name="StatusText")
    private String statusText;

    @Column(name="DataStatus")
    private int dataStatus;

    @Column(name="Credentials")
    private String credentials;

    @Column(name="Modified")
    private LocalDateTime modified;

    @Transient
    private OAuthCredentials oAuthCredentials;

    /**
     * getter will create an OAuthCredentials instance from
     * the database column data (JSON) when called.
     * @return OAuthCredentials
     * @throws IOException
    */

    public OAuthCredentials getOAuth() throws IOException {
        if (oAuthCredentials == null) {
            oAuthCredentials = OAuthCredentials.create(credentials);
        }
        return oAuthCredentials;
    }

    /*
        fkUserGuid uniqueidentifier NOT NULL,
    SourceSystem int NOT NULL,
    LastChecked datetime NOT NULL,
    PanelDisplay int NOT NULL,
    Status int NOT NULL,
    StatusText varchar(150) NULL,
    DataStatus int NOT NULL,
    Credentials varchar(700) NULL,
    Modified datetime NOT NULL
     */

    public DataCenterConfig() {}

    /**
     *
     * @param fkUserGuid
     * @param sourceSystem
     * @param lastChecked
     * @param panelDisplay
     * @param status
     * @param statusText
     * @param dataStatus
     * @param credentials
     * @param modified
     */
    public DataCenterConfig(UUID fkUserGuid, int sourceSystem, LocalDateTime lastChecked, int panelDisplay, int status,
                            String statusText, int dataStatus, String credentials, LocalDateTime modified) {
        this.fkUserGuid = fkUserGuid;
        this.sourceSystem = sourceSystem;
        this.lastChecked = lastChecked.truncatedTo(ChronoUnit.SECONDS);
        this.panelDisplay = panelDisplay;
        this.status = status;
        this.statusText = statusText;
        this.dataStatus = dataStatus;
        this.credentials = credentials;
        this.modified = modified.truncatedTo(ChronoUnit.SECONDS);
    }
}
