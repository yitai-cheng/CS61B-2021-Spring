package gitlet;

import gitlet.Repository;

import static gitlet.Repository.GITLET_DIR;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Yitai Cheng
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validateArgs(args, 1);
                Repository.init();
                break;
            case "add":
                validateArgs(args, 2);
                validateGitletInitialization();
                String fileName = args[1];
                Repository.add(fileName);
                break;
            case "commit":
                validateArgs(args, 2);
                validateGitletInitialization();
                String message = args[1];
                if (message.isBlank()) {
                    System.out.println("Please enter a commit message");
                    System.exit(0);
                }
                Repository.commit(message);
                break;
            case "rm":
                validateArgs(args, 2);
                validateGitletInitialization();
                Repository.remove(args[1]);
                break;
            case "log":
                validateArgs(args, 1);
                validateGitletInitialization();
                Repository.log();
                break;
            case "global-log":
                validateArgs(args, 1);
                validateGitletInitialization();
                Repository.logAllCommits();
                break;
            case "find":
                validateArgs(args, 2);
                validateGitletInitialization();
                Repository.find(args[1]);
                break;
            case "status":
                validateArgs(args, 1);
                validateGitletInitialization();
                Repository.status();
                break;
            case "checkout":
                validateGitletInitialization();
                if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else if (args.length == 3 && args[1].equals("--")) {
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    Repository.checkoutFile(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "branch":
                validateArgs(args, 2);
                validateGitletInitialization();
                Repository.createBranch(args[1]);
                break;
            case "rm-branch":
                validateArgs(args, 2);
                validateGitletInitialization();
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                validateArgs(args, 2);
                validateGitletInitialization();
                Repository.reset(args[1]);
                break;
            case "merge":
                validateArgs(args, 2);
                validateGitletInitialization();
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    public static void validateArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void validateGitletInitialization() {
        if (!GITLET_DIR.isDirectory()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
