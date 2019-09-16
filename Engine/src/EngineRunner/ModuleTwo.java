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
import puk.team.course.magit.ancestor.finder.AncestorFinder;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

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

    public static boolean executeCommit(String msg) throws NoActiveRepositoryException, CommitCannotExecutException{
        checkIfActiveRepoExists();
        if (activeRepo.checkDeltaChanges()) {
            activeRepo.newCommit(msg);
            activeRepo.createFiles();
            return true;
        } else
            throw new CommitCannotExecutException();
    }

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

    public static String changesBetweenCommitsToString(String sha1) throws IOException {
        return activeRepo.deltaChangesBetweenCommitsToString(sha1);
    }
    public static void deleteBranch(String input) throws NoActiveRepositoryException, DeleteHeadBranchException, NoSuchBranchException {
        checkIfActiveRepoExists();
        activeRepo.deleteThisBranch(input);
    }

    private static void checkIfActiveRepoExists() throws NoActiveRepositoryException {
        if (activeRepo == null)
            throw new NoActiveRepositoryException();
    }

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

    public static List<Commit> getActiveReposBranchCommits(Branch branch) { return activeRepo.getBranchCommits(branch);
    }

    public static ArrayList<Branch> getActiveReposBranches() {
        return activeRepo.getBranches();
    }

    public static void merge(Branch branch,String msg) throws IOException, CannotMergeException {

        activeRepo.mergeCommits(branch,msg);
    }

    public static String isPointedCommit(Commit commit) {
        for(Branch branch:activeRepo.getBranches())
        {
            if(branch.getSha1().equals(commit.getSha1()))
                return branch.getName();
        }
        if(activeRepo.getHeadBranch().getSha1().equals(commit.getSha1()))
            return activeRepo.getHeadBranch().getName();
        return "";
    }
}
