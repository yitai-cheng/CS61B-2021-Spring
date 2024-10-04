package gitlet;

import java.io.File;
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
    public static final File COMMIT_GRAPH = join(GITLET_DIR, "commit-graph");
    private static String HEAD = "";
    private static TreeMap<String, String> branchHeadMap = new TreeMap<>();
    private static TreeMap<String, String> staged4AdditionMap = new TreeMap<>();
    private static TreeMap<String, String> staged4RemovalMap = new TreeMap<>();
    private static CommitGraph commitGraph;

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
        writeObject(STAGING_ADDITION, staged4AdditionMap);
        writeObject(STAGING_REMOVAL, staged4RemovalMap);
        // create initial commit
        Commit initialCommit = new Commit("initial commit", new Date(0));
        HEAD = "master";
        writeContents(HEAD_FILE, HEAD);
        String sha1Id = sha1(serialize(initialCommit));
        branchHeadMap.put(HEAD, sha1Id);
        writeObject(BRANCH_FILE, branchHeadMap);
        writeObject(join(COMMIT_DIR, sha1Id), initialCommit);
        commitGraph = new CommitGraph(sha1Id);
        writeObject(COMMIT_GRAPH, commitGraph);
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
        staged4RemovalMap = readObject(STAGING_REMOVAL, TreeMap.class);
        if (staged4RemovalMap.containsKey(fileName)) {
            staged4RemovalMap.remove(fileName);
            writeObject(STAGING_REMOVAL, staged4RemovalMap);
        }
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
        if (staged4AdditionMap.isEmpty() && staged4RemovalMap.isEmpty()) {
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
        Commit newCommit = new Commit(message, new Date(), nameToBlobMapping, currentCommitId, currentCommit);
        commitHelper(newCommit);
        commitGraph = readObject(COMMIT_GRAPH, CommitGraph.class);
        commitGraph.addVertex(sha1(serialize(newCommit)));
        commitGraph.addEdge(sha1(serialize(newCommit)), currentCommitId);
        writeObject(COMMIT_GRAPH, commitGraph);
    }


    private static void commitHelper(Commit newCommit) {

        // clean the staging area
        staged4AdditionMap.clear();
        staged4RemovalMap.clear();
        writeObject(STAGING_ADDITION, staged4AdditionMap);
        writeObject(STAGING_REMOVAL, staged4RemovalMap);

        // save the new commit
        String newCommitId = sha1(serialize(newCommit));
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
        while (currentCommitId != null) {
            Commit currentCommit = readObject(join(COMMIT_DIR, currentCommitId), Commit.class);
            System.out.println("===");
            System.out.println("commit " + currentCommitId);
            if (currentCommit.getSecondParentCommitId() != null) {
                System.out.println("Merge: " + currentCommit.getParentCommitId().substring(0, 7) + " " + currentCommit.getSecondParentCommitId().substring(0, 7));
            }
            Formatter formatter = new Formatter(Locale.US);
            Date currentTimeStamp = currentCommit.getTimestamp();
            String formattedTimeStamp = String.valueOf(formatter.format("%ta %tb %te %tT %tY %tz", currentTimeStamp, currentTimeStamp, currentTimeStamp, currentTimeStamp, currentTimeStamp, currentTimeStamp));
            System.out.println("Date: " + formattedTimeStamp);
            formatter.close();
            System.out.println(currentCommit.getMessage());
            System.out.println();
            currentCommitId = currentCommit.getParentCommitId();
        }
    }

    public static void logAllCommits() {
        List<String> commitIds = plainFilenamesIn(COMMIT_DIR);
        for (String commitId : commitIds) {
            Commit currentCommit = readObject(join(COMMIT_DIR, commitId), Commit.class);
            System.out.println("===");
            System.out.println("commit " + commitId);
            if (currentCommit.getSecondParentCommitId() != null) {
                System.out.println("Merge: "
                        + currentCommit.getParentCommitId().substring(0, 8)
                        + " "
                        + currentCommit.getSecondParentCommitId().substring(0, 8));
            }
            Formatter formatter = new Formatter(Locale.US);
            Date currentTimeStamp = currentCommit.getTimestamp();
            String formattedTimeStamp = String.valueOf(
                    formatter.format("%ta %tb %te %tT %tY %tz",
                    currentTimeStamp, currentTimeStamp, currentTimeStamp,
                            currentTimeStamp, currentTimeStamp, currentTimeStamp));
            System.out.println("Date: " + formattedTimeStamp);
            formatter.close();
            System.out.println(currentCommit.getMessage());
            System.out.println();
        }
    }

    public static void find(String message) {
        List<String> commitIds = plainFilenamesIn(COMMIT_DIR);
        boolean commitExist = false;
        for (String commitId : commitIds) {
            Commit currentCommit = readObject(join(COMMIT_DIR, commitId), Commit.class);
            if (currentCommit.getMessage().equals(message)) {
                commitExist = true;
                System.out.println(commitId);
            }
        }
        if (!commitExist) {
            System.out.println("Found no commit with that message.");
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
            if (!filesInWorkingDir.contains(stagedFileName)) {
                System.out.println(stagedFileName + " (deleted)");
            } else if (!sha1(readContentsAsString(join(CWD, stagedFileName))).equals(staged4AdditionMap.get(stagedFileName))) {
                System.out.println(stagedFileName + " (modified)");
            }
        }
        for (String trackedFile : nameToBlobMapping.keySet()) {
            if (filesInWorkingDir.contains(trackedFile) && !staged4AdditionMap.containsKey(trackedFile)
                    && !sha1(readContentsAsString(join(CWD, trackedFile))).equals(nameToBlobMapping.get(trackedFile))) {
                System.out.println(trackedFile + " (modified)");
            } else if (!filesInWorkingDir.contains(trackedFile) && !staged4RemovalMap.containsKey(trackedFile)) {
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
        writeContents(join(CWD, fileName), readContentsAsString(join(BLOB_DIR, nameToBlobMapping.get(fileName))));
    }

    public static void checkoutBranch(String branchName) {
        branchHeadMap = readObject(BRANCH_FILE, TreeMap.class);
        HEAD = readContentsAsString(HEAD_FILE);
        if (!branchHeadMap.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else if (branchName.equals(HEAD)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        String commitId = branchHeadMap.get(branchName);
        checkoutByCommitId(commitId);
        // set the new HEAD to the branch
        HEAD = branchName;
        writeContents(HEAD_FILE, HEAD);
    }

    public static void createBranch(String branchName) {
        branchHeadMap = readObject(BRANCH_FILE, TreeMap.class);
        if (branchHeadMap.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        HEAD = readContentsAsString(HEAD_FILE);
        branchHeadMap.put(branchName, branchHeadMap.get(HEAD));
        writeObject(BRANCH_FILE, branchHeadMap);
    }

    public static void removeBranch(String branchName) {
        branchHeadMap = readObject(BRANCH_FILE, TreeMap.class);
        if (!branchHeadMap.containsKey(branchName)) {
            System.out.println("A branch with that name does not exists.");
            System.exit(0);
        }
        HEAD = readContentsAsString(HEAD_FILE);
        if (branchName.equals(HEAD)) {
            System.out.println("Cannot remove the current branch");
            System.exit(0);
        }
        branchHeadMap.remove(branchName);
        writeObject(BRANCH_FILE, branchHeadMap);
    }

    public static void reset(String commitId) {
        if (!join(COMMIT_DIR, commitId).isFile()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        checkoutByCommitId(commitId);
        HEAD = readContentsAsString(HEAD_FILE);
        branchHeadMap = readObject(BRANCH_FILE, TreeMap.class);
        branchHeadMap.put(HEAD, commitId);
        writeObject(BRANCH_FILE, branchHeadMap);
    }

    private static void checkoutByCommitId(String commitId) {
        Commit commit = readObject(join(COMMIT_DIR, commitId), Commit.class);
        Map<String, String> checkedOutNameToBlobMapping = commit.getNameToBlobMapping();
        Map<String, String> currentNameToBlobMapping = getCurrentCommitBlob();
        List<String> filesInWorkingDir = plainFilenamesIn(CWD);
        staged4AdditionMap = readObject(STAGING_ADDITION, TreeMap.class);
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
        staged4AdditionMap.clear();
        staged4RemovalMap.clear();
        writeObject(STAGING_ADDITION, staged4AdditionMap);
        writeObject(STAGING_REMOVAL, staged4RemovalMap);
    }

    public static void merge(String branchName) {
        staged4AdditionMap = readObject(STAGING_ADDITION, TreeMap.class);
        staged4RemovalMap = readObject(STAGING_REMOVAL, TreeMap.class);
        //if there are staged additions or removals present
        if (!staged4AdditionMap.isEmpty() || !staged4RemovalMap.isEmpty()) {
            System.out.println("You have uncommitted changes");
            System.exit(0);
        }
        branchHeadMap = readObject(BRANCH_FILE, TreeMap.class);
        HEAD = readContentsAsString(HEAD_FILE);
        //if the branch with the given name does not exist
        if (!branchHeadMap.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        // attempting to merge a branch with itself
        else if (branchName.equals(HEAD)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        //if merge will not change the commit
        String currentCommitId = (String) readObject(BRANCH_FILE, TreeMap.class).get(HEAD);
        Commit currentCommit = readObject(join(COMMIT_DIR, currentCommitId), Commit.class);
        Map<String, String> currentNameToBlobMapping = currentCommit.getNameToBlobMapping();
        String tobeMergedCommitId = (String) readObject(BRANCH_FILE, TreeMap.class).get(branchName);
        Commit tobeMergedCommit = readObject(join(COMMIT_DIR, tobeMergedCommitId), Commit.class);
        Map<String, String> tobeMergedNameToBlobMapping = tobeMergedCommit.getNameToBlobMapping();
        if (currentNameToBlobMapping.equals(tobeMergedNameToBlobMapping)) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        // If an untracked file in the current commit would be overwritten or deleted by the merge
        List<String> filesInWorkingDir = plainFilenamesIn(CWD);
        for (String fileName : filesInWorkingDir) {
            if (!staged4AdditionMap.containsKey(fileName) && !currentNameToBlobMapping.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        String latestCommonAncestorCommitId = "";
        commitGraph = readObject(COMMIT_GRAPH, CommitGraph.class);

        Set<Integer> bfsPath = new HashSet<>();
        Queue<Integer> fringe =
                new LinkedList<>();
        fringe.offer(commitGraph.getVertexId(currentCommitId));
        while (!fringe.isEmpty()) {
            int v = fringe.poll();
            bfsPath.add(v);
            for (int w : commitGraph.getAdj(v)) {
                fringe.offer(w);
            }
        }

        Queue<Integer> secondFringe =
                new LinkedList<>();
        secondFringe.offer(commitGraph.getVertexId(tobeMergedCommitId));
        while (!secondFringe.isEmpty()) {
            int v = secondFringe.poll();
            if (bfsPath.contains(v)) {
                latestCommonAncestorCommitId = commitGraph.getCommitId(v);
                break;
            }
            for (int w : commitGraph.getAdj(v)) {
                secondFringe.offer(w);
            }
        }


        tobeMergedCommitId = (String) readObject(BRANCH_FILE, TreeMap.class).get(branchName);
        currentCommitId = (String) readObject(BRANCH_FILE, TreeMap.class).get(HEAD);
        if (latestCommonAncestorCommitId.equals(tobeMergedCommitId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        } else if (latestCommonAncestorCommitId.equals(currentCommitId)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        Commit latestCommonAncestorCommit = readObject(join(COMMIT_DIR, latestCommonAncestorCommitId), Commit.class);
        Map<String, String> latestCommonAncestorNameToBlobMapping = latestCommonAncestorCommit.getNameToBlobMapping();
        Set<String> fileNames = new HashSet<>();
        fileNames.addAll(currentNameToBlobMapping.keySet());
        fileNames.addAll(tobeMergedNameToBlobMapping.keySet());
        fileNames.addAll(latestCommonAncestorNameToBlobMapping.keySet());
        boolean isMergeConflict = false;
        for (String fileName : fileNames) {
            if (latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && currentNameToBlobMapping.containsKey(fileName)
                    && tobeMergedNameToBlobMapping.containsKey(fileName)
                    && !latestCommonAncestorNameToBlobMapping.get(fileName).equals(tobeMergedNameToBlobMapping.get(fileName))
                    && latestCommonAncestorNameToBlobMapping.get(fileName).equals(currentNameToBlobMapping.get(fileName))) {
                checkoutFile(tobeMergedCommitId, fileName);
                add(fileName);
            } else if (latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && currentNameToBlobMapping.containsKey(fileName)
                    && tobeMergedNameToBlobMapping.containsKey(fileName)
                    && latestCommonAncestorNameToBlobMapping.get(fileName).equals(tobeMergedNameToBlobMapping.get(fileName))
                    && !latestCommonAncestorNameToBlobMapping.get(fileName).equals(currentNameToBlobMapping.get(fileName))) {
                continue;
            } else if (latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && currentNameToBlobMapping.containsKey(fileName)
                    && tobeMergedNameToBlobMapping.containsKey(fileName)
                    && !latestCommonAncestorNameToBlobMapping.get(fileName).equals(currentNameToBlobMapping.get(fileName))
                    && currentNameToBlobMapping.get(fileName).equals(tobeMergedNameToBlobMapping.get(fileName))
            ) {
                continue;
            } else if (latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && !currentNameToBlobMapping.containsKey(fileName)
                    && !tobeMergedNameToBlobMapping.containsKey(fileName)) {
                continue;
            } else if (!latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && currentNameToBlobMapping.containsKey(fileName)
                    && !tobeMergedNameToBlobMapping.containsKey(fileName)) {
                continue;
            } else if (!latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && !currentNameToBlobMapping.containsKey(fileName)
                    && tobeMergedNameToBlobMapping.containsKey(fileName)) {
                checkoutFile(tobeMergedCommitId, fileName);
                add(fileName);
            } else if (latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && latestCommonAncestorNameToBlobMapping.get(fileName).equals(currentNameToBlobMapping.get(fileName))
                    && !tobeMergedNameToBlobMapping.containsKey(fileName)) {
                remove(fileName);
            } else if (latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && latestCommonAncestorNameToBlobMapping.get(fileName).equals(tobeMergedNameToBlobMapping.get(fileName))
                    && !currentNameToBlobMapping.containsKey(fileName)) {
                continue;
            } else if (latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && currentNameToBlobMapping.containsKey(fileName)
                    && tobeMergedNameToBlobMapping.containsKey(fileName)
                    && !latestCommonAncestorNameToBlobMapping.get(fileName).equals(currentNameToBlobMapping.get(fileName))
                    && !currentNameToBlobMapping.get(fileName).equals(tobeMergedNameToBlobMapping.get(fileName))
                    || latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && !currentNameToBlobMapping.containsKey(fileName)
                    && tobeMergedNameToBlobMapping.containsKey(fileName)
                    && !latestCommonAncestorNameToBlobMapping.get(fileName).equals(tobeMergedNameToBlobMapping.get(fileName))
                    || latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && currentNameToBlobMapping.containsKey(fileName)
                    && !tobeMergedNameToBlobMapping.containsKey(fileName)
                    && !latestCommonAncestorNameToBlobMapping.get(fileName).equals(currentNameToBlobMapping.get(fileName))
                    || !latestCommonAncestorNameToBlobMapping.containsKey(fileName)
                    && currentNameToBlobMapping.containsKey(fileName)
                    && tobeMergedNameToBlobMapping.containsKey(fileName)
                    && !currentNameToBlobMapping.get(fileName).equals(tobeMergedNameToBlobMapping.get(fileName))
            ) {
                isMergeConflict = true;
                String currentFileContent = "";
                String tobeMergedFileContent = "";
                if (currentNameToBlobMapping.containsKey(fileName)) {
                    currentFileContent = readContentsAsString(join(BLOB_DIR, currentNameToBlobMapping.get(fileName)));
                }
                if (tobeMergedNameToBlobMapping.containsKey(fileName)) {
                    tobeMergedFileContent = readContentsAsString(join(BLOB_DIR, tobeMergedNameToBlobMapping.get(fileName)));
                }
                writeContents(join(CWD, fileName), "<<<<<<< HEAD\n"
                        + currentFileContent
                        + "=======\n"
                        + tobeMergedFileContent
                        + ">>>>>>>\n");
                add(fileName);
            }
        }
        staged4AdditionMap = readObject(STAGING_ADDITION, TreeMap.class);
        staged4RemovalMap = readObject(STAGING_REMOVAL, TreeMap.class);
        for (Map.Entry<String, String> entry : staged4AdditionMap.entrySet()) {
            currentNameToBlobMapping.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : staged4RemovalMap.entrySet()) {
            currentNameToBlobMapping.remove(entry.getKey());
        }
        Commit newCommit = new Commit(
                "Merged " + branchName + " into " + HEAD + ".",
                new Date(), currentNameToBlobMapping, currentCommitId, tobeMergedCommitId);
        commitHelper(newCommit);
        if (isMergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        commitGraph.addVertex(sha1(serialize(newCommit)));
        commitGraph.addEdge(sha1(serialize(newCommit)), currentCommitId);
        commitGraph.addEdge(sha1(serialize(newCommit)), tobeMergedCommitId);
        writeObject(COMMIT_GRAPH, commitGraph);

    }
}
