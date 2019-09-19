package EngineRunner;


import ControlPackage.Controller;
import Objects.Branch.AlreadyExistingBranchException;
import Objects.Branch.Branch;
import Objects.Branch.BranchNoNameException;
import Objects.Branch.NoCommitHasBeenMadeException;
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

    public static void makeRemoteRepositoryFiles(String path) throws IOException {
        activeRepo.makeRemoteRepositoryFiles(path);
    }
    public static void SwitchRepo(String path) throws NoSuchRepoException, IOException {
        Path p = Paths.get(path + "/.magit");
        if (Files.isDirectory(p)) {
            Repository repo = new Repository(path, new HashMap<>(), new ArrayList<>());
            repo.readRepoFiles();
            repo.updateRemoteRepoPath();
            repo.updateRemoteRepoName();
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

    public static void makeNewBranch(String name,String sha1) throws NoActiveRepositoryException, AlreadyExistingBranchException, NoCommitHasBeenMadeException, BranchNoNameException {


        checkIfActiveRepoExists();
        activeRepo.addNewBranch(name,sha1);
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

    public static String changesBetweenCommitsToString(String sha1) {
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

    public static boolean merge(String branchSha1) throws IOException, CannotMergeException {

        return activeRepo.mergeCommits(branchSha1);
    }

    public static String isPointedCommitBranchList(Commit commit)
    {
        String res="";
        for(Branch branch:activeRepo.getBranches())
        {
            if(branch.getSha1().equals(commit.getSha1())) {
                if(res.equals(""))
                    res = branch.getName();
                else
                    res = res + ", "+ branch.getName() ;
            }
        }
        if(activeRepo.getHeadBranch().getSha1().equals(commit.getSha1())) {
            if (res.equals(""))
                res = activeRepo.getHeadBranch().getName();
            else
                res = res + ", "+ activeRepo.getHeadBranch().getName() ;
        }
        return res;
    }
    public static String isPointedCommit(String commitSha1) {
        for(Branch branch:activeRepo.getBranches())
        {
            if(branch.getSha1().equals(commitSha1))
                return branch.getName();
        }
        if(activeRepo.getHeadBranch().getSha1().equals(commitSha1))
            return activeRepo.getHeadBranch().getName();
        return "";
    }

    public static boolean activeRepoHeadHasRtbOfRb() throws IOException {
        return getActiveRepo().headHasRtb();
    }



    public static void push() throws IOException, NoSuchRepoException {

        if(activeRepo.isHeadBranchRTB()){
            ArrayList<String> arr =activeRepo.getWantedSha1s();
            String activeRepoPath = getActiveRepoPath();
            SwitchRepo(activeRepo.getRemoteRepositoryPath());
            activeRepo.updateCommits(activeRepoPath,arr);
            activeRepo.updateHeadBranch(activeRepoPath);
            SwitchRepo(activeRepoPath);
        }
    }
}
