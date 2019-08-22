package EngineRunner;

import ControlPackage.Controller;
import Objects.Branch.AlreadyExistingBranchException;
import Objects.Commit.CommitCannotExecutException;
import Repository.*;
import UIRunner.ModuleOne;
import XML.XmlData;
import XML.XmlNotValidException;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class ModuleTwo {

    private static Repository activeRepo = null;

    public static void updateUsername(String name) {
        Repository.updateUsername(name);
    }

    public static void SwitchRepo(String path) throws NoSuchRepoException {
        Path p = Paths.get(path + "/.magit");
        if (Files.isDirectory(p)) {
            Repository repo = new Repository(path, new HashMap<>(), new ArrayList<>());
            repo.readRepoFiles();
            activeRepo = repo;
        } else
            throw new NoSuchRepoException();
    }

    public static void InitializeRepo(String path)//create empty repo (not from xml)
    {
        try {
            Repository repo = new Repository(path, new HashMap<>(), new ArrayList<>());
            repo.createEmptyRepo();
            activeRepo = repo;
        } catch (IOException e) {
            e.getMessage();
        }
    }

    public static void loadRepo(String path) throws XmlNotValidException, IOException, NoSuchRepoException {


        XmlData reader = new XmlData(path);
        String pathFromXml = reader.getMagitRepository().getLocation();
        Path p = Paths.get(pathFromXml + "/.magit");
        boolean deleteRepo = true;
        if (Files.isDirectory(p)) {
            deleteRepo = Controller.deleteOrNot();
        }
        if (deleteRepo) {
            activeRepo = Repository.makeRepoFromXmlRepo(reader);
            activeRepo.createEmptyRepo();
            activeRepo.createFiles();
        } else {
            SwitchRepo(pathFromXml);
        }
    }

    public static boolean executeCommit(String msg) throws NoActiveRepositoryException, CommitCannotExecutException {
        checkIfActiveRepoExists();
        if (activeRepo.checkDeltaChanges()) {
            activeRepo.newCommit(msg);
            activeRepo.createFiles();
            return true;
        } else
            throw new CommitCannotExecutException();
    }

    public static void printLine(String strToPrint) {
        ModuleOne.PrintString(strToPrint);
    }


    public static void showAllCommitFiles() {
        try {
            checkIfActiveRepoExists();
            checkIfHeadBranchHasCommit();
            activeRepo.showCommitFiles();
        } catch (NoActiveRepositoryException | NoCommitInActiveBranch e) {
            e.printStackTrace();
        }
    }

    public static void makeNewBranch(String name) throws NoActiveRepositoryException, AlreadyExistingBranchException {


            checkIfActiveRepoExists();
            activeRepo.addNewBranch(name);
    }

    public static void changesChecker() {
        try {
            if (checkChanges()) {
                ModuleOne.switchHead();
            }

        } catch (NoActiveRepositoryException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkAnswer() {
        ModuleOne.PrintString("\ny for yes \nn for no\n");
        String answer = ModuleOne.getString();
        while (true) {
            switch (answer) {
                case "y":
                case "yes":
                case "Y":
                case "Yes":
                    return true;
                case "n":
                case "no":
                case "N":
                case "No":
                    return false;
                default:
                    ModuleOne.PrintString("try again\n");
                    answer = ModuleOne.getString();
                    break;
            }
        }
    }

    public static boolean checkChanges() throws NoActiveRepositoryException {

        checkIfActiveRepoExists();
        if (activeRepo.checkDeltaChanges()) {
            ModuleOne.PrintString("You have changes without commiting them. Would you like to commit them?");
            return checkAnswer();
        } else
            return false;

    }

    public static void checkout(String name) throws NoActiveRepositoryException, NoSuchBranchException {

            checkIfActiveRepoExists();
            activeRepo.switchHead(name);

    }

    public static void showStatus() {
        try {
            checkIfActiveRepoExists();
            activeRepo.showRepoStatus();
        } catch (NoActiveRepositoryException e) {
            e.printStackTrace();
        }
    }

    public static void showAllBranches() {
        try {
            checkIfActiveRepoExists();
            checkIfHeadBranchHasCommit();
            activeRepo.showBranches();
        } catch (NoActiveRepositoryException | NoCommitInActiveBranch e) {
            e.printStackTrace();
        }

    }

    public static void deleteBranch(String input) throws NoActiveRepositoryException,DeleteHeadBranchException, NoSuchBranchException{
            checkIfActiveRepoExists();
            activeRepo.deleteThisBranch(input);
    }

    private static void checkIfActiveRepoExists() throws NoActiveRepositoryException {
        if (activeRepo == null)
            throw new NoActiveRepositoryException();
    }

    private static void checkIfHeadBranchHasCommit() throws NoCommitInActiveBranch {
        if (!activeRepo.hasCommitInHead())
            throw new NoCommitInActiveBranch();
    }

    public static void showActiveBranchHistory() {
        try {
            checkIfActiveRepoExists();
            checkIfHeadBranchHasCommit();
            activeRepo.showBranchHistory();
        } catch (NoActiveRepositoryException | NoCommitInActiveBranch e) {
            e.printStackTrace();
        }
    }

    public static String getActiveRepoPath(){
        return activeRepo.getPath();
    }
    public static String getActiveRepoName()
    {
        return activeRepo.getName();
    }
    public static String getActiveBranchName()
    {
        return activeRepo.getHeadBranchName();
    }

}
