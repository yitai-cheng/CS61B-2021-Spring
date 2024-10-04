package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a gitlet commit object.
 *
 * @author Yitai Cheng
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String message;
    /**
     * The timestamp of this Commit.
     */
    private Date timestamp;
    private Map<String, String> nameToBlobMapping;
    private String parentCommitId;
    private String secondParentCommitId;
    private transient Commit parentCommit;
    private transient Commit secondParentCommit;


    public Commit(String message, Date timestamp) {
        this.message = message;
        this.timestamp = timestamp;
        this.nameToBlobMapping = new HashMap<>();
    }

    public Commit(String message, Date timestamp, Map<String, String> nameToBlobMapping,
                  String parentCommitId, Commit parentCommit) {
        this.message = message;
        this.timestamp = timestamp;
        this.nameToBlobMapping = nameToBlobMapping;
        this.parentCommitId = parentCommitId;
        this.parentCommit = parentCommit;
    }

    public Commit(String message, Date timestamp, Map<String, String> nameToBlobMapping,
                  String parentCommitId, String secondParentCommitId) {
        this.message = message;
        this.timestamp = timestamp;
        this.nameToBlobMapping = nameToBlobMapping;
        this.parentCommitId = parentCommitId;
        this.secondParentCommitId = secondParentCommitId;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getNameToBlobMapping() {
        return nameToBlobMapping;
    }

    public String getParentCommitId() {
        return parentCommitId;
    }

    public String getSecondParentCommitId() {
        return secondParentCommitId;
    }

}
