package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository.
 * All gitlet operations including init, add, rm, etc..
 *
 * @author Yitai Cheng
 */
public class Repository {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    public static final File BRANCH_FILE = join(GITLET_DIR, "branches");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File STAGING_DIR = join(GITLET_DIR, "staging-area");
    public static final File STAGING_ADDITION = join(STAGING_DIR, "staged4addition");
    public static final File STAGING_REMOVAL = join(STAGING_DIR, "staged4removal");
    public static String currentBranch = null;
    public static String HEAD = "";
    public static TreeMap<String, String> branchHeadMap = new TreeMap<>();
    public static TreeMap<String, String> staged4AdditionMap = new TreeMap<>();
    public static TreeMap<String, String> staged4RemovalMap = new TreeMap<>();

    /* TODO: fill in the rest of this class. */
    private static Map<String, String> getCurrentCommitBlob() {
        HEAD = readContentsAsString(HEAD_FILE);
        String currentCommitId = (String) readObject(BRANCH_FILE, TreeMap.class).get(HEAD);
        Commit currentCommit = readObject(join(COMMIT_DIR, currentCommitId), Commit.class);
        return currentCommit.getNameToBlobMapping();
    }

    public static void init() {
        if (!GITLET_DIR.mkdir()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        STAGING_DIR.mkdir();
        // create initial commit
        currentBranch = "master";
        Commit initialCommit = new Commit("A Gitlet version-control system already exists in the current directory", new Date(0));
        HEAD = currentBranch;
        writeContents(HEAD_FILE, HEAD);

        String sha1Id = sha1(initialCommit);
        branchHeadMap.put(currentBranch, sha1Id);
        writeObject(BRANCH_FILE, branchHeadMap);
        writeObject(join(COMMIT_DIR, sha1Id), initialCommit);
    }

    public static void add(String fileName) {
        if (!join(CWD, fileName).isFile()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String fileContent = readContentsAsString(join(CWD, fileName));
        String hashValueOfFileContent = sha1(fileContent);
        Map<String, String> nameToBlobMapping = getCurrentCommitBlob();

        staged4AdditionMap = readObject(STAGING_ADDITION, TreeMap.class);

        if (staged4AdditionMap.containsKey(fileName)) {
            if (hashValueOfFileContent.equals(nameToBlobMapping.get(fileName))) {
                staged4AdditionMap.remove(fileName);
            } else {
                writeContents(join(BLOB_DIR, hashValueOfFileContent), fileContent);
                staged4AdditionMap.put(fileName, hashValueOfFileContent);
            }

        } else {
            if (hashValueOfFileContent.equals(nameToBlobMapping.get(fileName))) {
                return;
            }
            writeContents(join(BLOB_DIR, hashValueOfFileContent), fileContent);
            staged4AdditionMap.put(fileName, hashValueOfFileContent);
        }
        writeObject(STAGING_ADDITION, staged4AdditionMap);
    }

    public static void commit(String message) {
        staged4AdditionMap = readObject(STAGING_ADDITION, TreeMap.class);
        staged4RemovalMap = readObject(STAGING_REMOVAL, TreeMap.class);
        if (staged4AdditionMap.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        HEAD = readContentsAsString(HEAD_FILE);
        String currentCommitId = (String) readObject(BRANCH_FILE, TreeMap.class).get(HEAD);
        Commit currentCommit = readObject(join(COMMIT_DIR, currentCommitId), Commit.class);
        Map<String, String> nameToBlobMapping = currentCommit.getNameToBlobMapping();

        // update the current commit
        for (Map.Entry<String, String> entry : staged4AdditionMap.entrySet()) {
            nameToBlobMapping.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : staged4RemovalMap.entrySet()) {
            nameToBlobMapping.remove(entry.getKey());
        }
        // clean the staging area
        staged4AdditionMap.clear();
        staged4RemovalMap.clear();
        writeObject(STAGING_ADDITION, staged4AdditionMap);
        writeObject(STAGING_REMOVAL, staged4RemovalMap);

        // save the new commit
        Commit newCommit = new Commit(message, new Date(), nameToBlobMapping, currentCommitId, currentCommit);
        String newCommitId = sha1(newCommit);
        writeObject(join(COMMIT_DIR, newCommitId), newCommit);

        // let the head pointer points to the new commit
        branchHeadMap = readObject(BRANCH_FILE, TreeMap.class);
        branchHeadMap.put(HEAD, newCommitId);
        writeObject(BRANCH_FILE, branchHeadMap);
    }

    public static void remove(String fileName) {
        staged4AdditionMap = readObject(STAGING_ADDITION, TreeMap.class);
        Map<String, String> nameToBlobMapping = getCurrentCommitBlob();
        if (!staged4AdditionMap.containsKey(fileName) && !nameToBlobMapping.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (staged4AdditionMap.containsKey(fileName)) {
            staged4AdditionMap.remove(fileName);
            writeObject(STAGING_ADDITION, staged4AdditionMap);
        }
        if (nameToBlobMapping.containsKey(fileName)) {
            staged4RemovalMap = readObject(STAGING_REMOVAL, TreeMap.class);
            staged4RemovalMap.put(fileName, nameToBlobMapping.get(fileName));
            writeObject(STAGING_REMOVAL, staged4RemovalMap);
            join(CWD, fileName).delete();
        }
    }

    public static void log() {
        HEAD = readContentsAsString(HEAD_FILE);
        String currentCommitId = (String) readObject(BRANCH_FILE, TreeMap.class).get(HEAD);
        Commit currentCommit = readObject(join(COMMIT_DIR, currentCommitId), Commit.class);
        while (currentCommit != null) {
            System.out.println("===");
            System.out.println("commit " + currentCommitId);
            if (currentCommit.getSecondParentCommit() != null) {
                System.out.println("Merge: " + currentCommit.getParentCommitId().substring(0, 8) + " " + currentCommit.getSecondParentCommitId().substring(0, 8));
            }
            Formatter formatter = new Formatter();
            Date currentTimeStamp = currentCommit.getTimestamp();
            String formattedTimeStamp = String.valueOf(formatter.format("%ta %tb %td %tT %tY %tz", currentTimeStamp, currentTimeStamp, currentTimeStamp, currentTimeStamp, currentTimeStamp, currentTimeStamp));
            System.out.println("Date: " + formattedTimeStamp);
            formatter.close();
            System.out.println(currentCommit.getMessage());
            System.out.println();
//            currentCommit = currentCommit.getParentCommit();
            currentCommitId = currentCommit.getParentCommitId();
            currentCommit = readObject(join(COMMIT_DIR, currentCommitId), Commit.class);
        }
    }

    public static void logAllCommits() {
        List<String> commitIds = plainFilenamesIn(COMMIT_DIR);
        for (String commitId : commitIds) {
            Commit currentCommit = readObject(join(COMMIT_DIR, commitId), Commit.class);
            System.out.println("===");
            System.out.println("commit " + commitId);
            if (!currentCommit.getSecondParentCommitId().isEmpty()) {
                System.out.println("Merge: " + currentCommit.getParentCommitId().substring(0, 8) + " " + currentCommit.getSecondParentCommitId().substring(0, 8));
            }
            Formatter formatter = new Formatter();
            Date currentTimeStamp = currentCommit.getTimestamp();
            String formattedTimeStamp = String.valueOf(formatter.format("%ta %tb %td %tT %tY %tz", currentTimeStamp, currentTimeStamp, currentTimeStamp, currentTimeStamp, currentTimeStamp, currentTimeStamp));
            System.out.println("Date: " + formattedTimeStamp);
            formatter.close();
            System.out.println(currentCommit.getMessage());
            System.out.println();
        }
    }

    public static void find(String message) {
        List<String> commitIds = plainFilenamesIn(COMMIT_DIR);
        for (String commitId : commitIds) {
            Commit currentCommit = readObject(join(COMMIT_DIR, commitId), Commit.class);
            if (currentCommit.getMessage().equals(message)) {
                System.out.println(commitId);
            }
        }
    }

    public static void status() {
        System.out.println("=== Branches ===");
        HEAD = readContentsAsString(HEAD_FILE);
        branchHeadMap = readObject(BRANCH_FILE, TreeMap.class);
        for (String branchName : branchHeadMap.keySet()) {
            if (branchName.equals(HEAD)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);

            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        staged4AdditionMap = readObject(STAGING_ADDITION, TreeMap.class);
        for (String stagedFileName : staged4AdditionMap.keySet()) {
            System.out.println(stagedFileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        staged4RemovalMap = readObject(STAGING_REMOVAL, TreeMap.class);
        for (String stagedFileName : staged4RemovalMap.keySet()) {
            System.out.println(stagedFileName);
        }
        System.out.println();

        List<String> filesInWorkingDir = plainFilenamesIn(CWD);
        Map<String, String> nameToBlobMapping = getCurrentCommitBlob();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String stagedFileName : staged4AdditionMap.keySet()) {
            if (!sha1(readContentsAsString(join(CWD, stagedFileName))).equals(staged4AdditionMap.get(stagedFileName))) {
                System.out.println(stagedFileName + " (modified)");
            } else if (!filesInWorkingDir.contains(stagedFileName)) {
                System.out.println(stagedFileName + " (deleted)");
            }
        }
        for (String trackedFile : nameToBlobMapping.keySet()) {
            if (!staged4AdditionMap.containsKey(trackedFile) && !sha1(readContentsAsString(join(CWD, trackedFile))).equals(nameToBlobMapping.get(trackedFile))) {
                System.out.println(trackedFile + " (modified)");
            } else if (!staged4RemovalMap.containsKey(trackedFile) && !filesInWorkingDir.contains(trackedFile)) {
                System.out.println(trackedFile + " (deleted)");
            }
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (String fileName : filesInWorkingDir) {
            if (!staged4AdditionMap.containsKey(fileName) && !nameToBlobMapping.containsKey(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    public static void checkoutFile(String fileName) {
        HEAD = readContentsAsString(HEAD_FILE);
        String currentCommitId = (String) readObject(BRANCH_FILE, TreeMap.class).get(HEAD);
        checkoutFile(currentCommitId, fileName);
    }

    public static void checkoutFile(String commitId, String fileName) {
        if (!join(COMMIT_DIR, commitId).isFile()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = readObject(join(COMMIT_DIR, commitId), Commit.class);
        Map<String, String> nameToBlobMapping = commit.getNameToBlobMapping();
        if (!nameToBlobMapping.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        writeContents(join(CWD, fileName), readContentsAsString(join(CWD, nameToBlobMapping.get(fileName))));
    }

    public static void checkoutBranch(String branchName) {
        branchHeadMap = readObject(BRANCH_FILE, TreeMap.class);
        HEAD = readContentsAsString(HEAD_FILE);
        Map<String, String> currentNameToBlobMapping = getCurrentCommitBlob();
        if (!branchHeadMap.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else if (branchHeadMap.get(branchName).equals(HEAD)) {
            System.out.println("No neeed to checkout the current branch.");
            System.exit(0);
        }
        String commitId = branchHeadMap.get(branchName);
        Commit commit = readObject(join(COMMIT_DIR, commitId), Commit.class);
        Map<String, String> checkedOutNameToBlobMapping = commit.getNameToBlobMapping();
        List<String> filesInWorkingDir = plainFilenamesIn(CWD);
        for (String fileName : filesInWorkingDir) {
            if (!staged4AdditionMap.containsKey(fileName) && !currentNameToBlobMapping.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        // Takes all files in the commit at the head of the given branch,
        // and puts them in the working directory,
        // overwriting the versions of the files that are already there if they exist.
        for (Map.Entry<String, String> entries : checkedOutNameToBlobMapping.entrySet()) {
            writeContents(join(CWD, entries.getKey()), readContentsAsString(join(BLOB_DIR, entries.getValue())));
        }
        // Any files that are tracked in the current branch but are not present in the checked-out branch are deleted
        for (String fileName : filesInWorkingDir) {
            if (!checkedOutNameToBlobMapping.containsKey(fileName)) {
                join(CWD, fileName).delete();
            }
        }
        // clear the staging area
        writeObject(STAGING_ADDITION, staged4AdditionMap);
        writeObject(STAGING_REMOVAL, staged4RemovalMap);

        // set the new HEAD to the branch
        HEAD = branchName;
        writeContents(HEAD_FILE, HEAD);

    }

}
