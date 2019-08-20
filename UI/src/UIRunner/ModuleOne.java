package UIRunner;

import EngineRunner.ModuleTwo;
import Repository.NoSuchRepoException;

import java.util.Scanner;

public class ModuleOne {
    public static void RunConsole() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Please choose an option:\n" +
                    "1.Update user name \n" +
                    "2.Load repository from xml \n" +
                    "3.Switch repository \n" +
                    "4.Show all the commit files with historical information \n" +
                    "5.Show Status of files\n" +
                    "6.Make commit\n" +
                    "7.Show all branches \n" +
                    "8.Make a new branch\n" +
                    "9.Delete branch\n" +
                    "10.Choose new head branch (checkout)\n" +
                    "11.Show active branch history\n" +
                    "12.Make a new repository \n" +
                    "13.Exit \n");
            String input = scanner.nextLine();
            switch (input) {
                case "1": {
                    System.out.println("Please enter the user name: ");
                    input = scanner.nextLine();
                    ModuleTwo.updateUsername(input);
                    break;
                }
                case "2": {
                    System.out.println("Please enter the path: ");
                    input = scanner.nextLine();
                   // ModuleTwo.loadRepo(input);
                    break;
                }
                case "3": {
                    System.out.println("Please enter the repository path: ");
                    input = scanner.nextLine();
                    try {
                        ModuleTwo.SwitchRepo(input);
                    } catch (NoSuchRepoException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "4": {
                    System.out.println("The current commit files are: ");
                    ModuleTwo.showAllCommitFiles();
                    break;
                }
                case "5": {
                    ModuleTwo.showStatus();
                    break;
                }
                case "6": {
                    ///makeCommit(scanner);

                    break;
                }
                case "7": {
                    ModuleTwo.showAllBranches();
                    break;
                }
                case "8": {
                    System.out.println("Please enter the name of the branch");
                    input = scanner.nextLine();
                    ModuleTwo.makeNewBranch(input);
                    break;
                }
                case "9": {
                    System.out.println("Please enter the name of the branch");
                    input = scanner.nextLine();
                    ModuleTwo.deleteBranch(input);
                    break;
                }
                case "10": {
                    ModuleTwo.changesChecker();
                }
                break;
                case "11": {
                    ModuleTwo.showActiveBranchHistory();
                    break;
                }
                case "12": {
                    System.out.println("Please enter the new repository path: ");
                    input = scanner.nextLine();
                    ModuleTwo.InitializeRepo(input);
                    break;
                }

                case "13":
                    System.exit(0);
            }
        }
    }

    public static void PrintString(String string) {
        System.out.println(string);
    }

    public static String getString() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

//    private static void makeCommit(Scanner scanner) {
//        String input;
//        System.out.println("Please enter the commit purpose");
//        input = scanner.nextLine();
//        ModuleTwo.executeCommit(input);
//    }

    public static void switchHead() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the name of the branch");
        String input;
        input = scanner.nextLine();
        ModuleTwo.checkout(input);
    }
}

