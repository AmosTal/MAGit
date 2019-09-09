package EngineRunner;

import ControlPackage.Controller;
import Objects.Branch.AlreadyExistingBranchException;
import Objects.Branch.Branch;
import Objects.Commit.Commit;
import Objects.Commit.CommitCannotExecutException;
import Repository.*;

import XML.XmlData;
import XML.XmlNotValidException;
import org.apache.commons.io.FileUtils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public static void InitializeRepo(String path) throws IOException {
        Repository repo = new Repository(path, new HashMap<>(), new ArrayList<>());
        repo.createEmptyRepo();
        activeRepo = repo;
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
            FileUtils.deleteDirectory(new File(reader.getMagitRepository().getLocation()));
            new File(reader.getMagitRepository().getLocation()).mkdir();
            activeRepo = Repository.makeRepoFromXmlRepo(reader);
            activeRepo.createEmptyRepo();
            activeRepo.createFiles();
        } else {
            SwitchRepo(pathFromXml);
        }
    }

    public static boolean executeCommit(String msg) throws NoActiveRepositoryException, CommitCannotExecutException, AlreadyExistingBranchException {
        checkIfActiveRepoExists();
        if (activeRepo.checkDeltaChanges()) {
            activeRepo.newCommit(msg);
            activeRepo.createFiles();
            return true;
        } else
            throw new CommitCannotExecutException();
    }
//    public static void showAllCommitFiles() {
//        try {
//            checkIfActiveRepoExists();
//            checkIfHeadBranchHasCommit();
//            activeRepo.showCommitFiles();
//        } catch (NoActiveRepositoryException | NoCommitInActiveBranch e) {
//            e.printStackTrace();
//        }
//    }
    public static void makeNewBranch(String name) throws NoActiveRepositoryException, AlreadyExistingBranchException {


        checkIfActiveRepoExists();
        activeRepo.addNewBranch(name, null);
    }

    public static boolean checkChanges() throws NoActiveRepositoryException {

        checkIfActiveRepoExists();
        return activeRepo.checkDeltaChanges();
    }

    public static void checkout(String name) throws NoActiveRepositoryException, NoSuchBranchException, IOException {

        checkIfActiveRepoExists();
        activeRepo.switchHead(name);

    }

    public static String showStatus() {
        return activeRepo.showRepoStatus();
    }

//    public static void showAllBranches() {
//        try {
//            checkIfActiveRepoExists();
//            checkIfHeadBranchHasCommit();
//            activeRepo.showBranches();
//        } catch (NoActiveRepositoryException | NoCommitInActiveBranch e) {
//            e.printStackTrace();
//        }
//
//    }
    public static void deleteBranch(String input) throws NoActiveRepositoryException, DeleteHeadBranchException, NoSuchBranchException {
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

//    public static void showActiveBranchHistory() {
//        try {
//            checkIfActiveRepoExists();
//            checkIfHeadBranchHasCommit();
//            activeRepo.showBranchHistory();
//        } catch (NoActiveRepositoryException | NoCommitInActiveBranch e) {
//            e.printStackTrace();
//        }
//    }
    public static void resetActiveRepoHeadBranch(Commit commit) throws IOException {
        activeRepo.resetBranch(commit);
    }

    public static String getActiveRepoPath() {
        return activeRepo.getPath();
    }

    public static String getActiveRepoName() {
        return activeRepo.getName();
    }

    public static Repository getActiveRepo() {
        return activeRepo;
    }

    public static String getActiveBranchName() {
        return activeRepo.getHeadBranchName();
    }

    public static List<Commit> getActiveReposBranchCommits(Branch branch) {
        return activeRepo.getBranchCommits(branch);
    }

    public static ArrayList<Branch> getActiveReposBranches() {
        return activeRepo.getBranches();
    }
}
