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
//                validateArgs();
                Repository.remove(args[1]);
                break;
            case "log":
                validateArgs(args, 1);
                validateGitletInitialization();
                Repository.log();
                break;
            case "global-log":
//                validateArgs();
                Repository.logAllCommits();
                break;
            case "find":
//                validateArgs();
                Repository.find(args[1]);
                break;
            case "status":
//                validateArgs();
                Repository.status();
                break;
            case "checkout":
                validateGitletInitialization();
                if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else if (args.length == 3) {
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4) {
                    Repository.checkoutFile(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
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
