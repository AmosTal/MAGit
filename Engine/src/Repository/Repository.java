package Repository;

import EngineRunner.ModuleTwo;
import Merge.MergeCase;
import Merge.MergeCases;
import Objects.Api.MagitObject;
import Objects.Blob.Blob;
import Objects.Branch.*;
import Objects.Commit.Commit;
import Objects.Date.DateAndTime;
import Objects.Folder.Fof;
import Objects.Folder.Folder;
import XML.XmlData;
import XMLpackage.*;
import org.apache.commons.io.FileUtils;
import puk.team.course.magit.ancestor.finder.AncestorFinder;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;


import java.io.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import java.util.function.Function;
import java.util.zip.*;

public class Repository {
    private Map<String, MagitObject> objList; //<sha1,object>
    private ArrayList<Branch> branches;
    private Branch headBranch = null;
    private String path;
    private String name;
    private static String username = "default";
    private Delta currDelta;
    private HashMap<String, MergeCase> conflictMap = new HashMap<>();
    private String latestMergedBranchSha1 = null;
    private String remoteRepoPath = "";
    private String remoteRepoName;

    public Repository(String _path, Map<String, MagitObject> _objList, ArrayList<Branch> _branches) {
        path = _path;
        objList = _objList;
        branches = _branches;
        name = new File(_path).getName();
    }
    public Repository(String _path, Map<String, MagitObject> _objList, ArrayList<Branch> _branches,String remotePath,String remoteName) {
        path = _path;
        objList = _objList;
        branches = _branches;
        name = new File(_path).getName();
        remoteRepoPath = remotePath;
        remoteRepoName = remoteName;
    }


    public String getPath() {
        return path;
    }

    public HashMap<String, MergeCase> getConflictMap() {
        return conflictMap;
    }

    public String getHeadBranchName() {
        return headBranch.getName();
    }

    public String getName() {
        return name;
    }

    public static void updateUsername(String name) {
        username = name;
    }

    public void createEmptyRepo() throws IOException {
        new File(path).mkdir();
        new File(path + "/.magit").mkdir();
        new File(path + "/.magit/branches").mkdir();
        new File(path + "/.magit/objects").mkdir();
        new File(path + "/.magit/Commit files").mkdir();
        new File(path + "/.magit/merge files").mkdir();
        File Head = new File(path + "/.magit/branches/HEAD");
        Head.createNewFile();
    }

    public void createFiles() throws IOException {
        createZippedFilesForMagitObjects();
        createFilesForBranches();
    }

    private Commit getCommitBySha1(String sha1) {
        return (Commit) objList.get(sha1);
    }

    private void createFilesForBranches() throws FileNotFoundException {
        ArrayList<Branch> arr = new ArrayList<>(branches);
        makeFileForBranch(headBranch.getName(), "HEAD");
        makeFileForBranch(headBranch.getSha1(), headBranch.getName());
        for (Branch branch : branches) {
            makeFileForBranch(branch.getSha1(), branch.getName());
            if(branch.getName().contains("/")||branch.getName().contains("\\"))
                arr.remove(branch);
        }
        branches = arr;

    }

    public ArrayList<Branch> getBranches() {
        return branches;
    }

    private void makeFileForBranch(String content, String name) throws FileNotFoundException {
            PrintWriter out = new PrintWriter(this.path + "/.magit/branches/" + name);
            out.println(content);
            out.close();
    }

    private void createZippedFilesForMagitObjects() throws IOException {
        for (Map.Entry<String, MagitObject> entry : objList.entrySet())
            createSingleZippedFileForMagitObject(entry.getKey(), entry.getValue());

    }

    private void createSingleZippedFileForMagitObject(String sha1, MagitObject obj) throws IOException {
            File file = new File(this.path + "/.magit/objects/" + sha1);
            if (!file.exists()) {
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                GZIPOutputStream gos = new GZIPOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(gos);
                oos.writeObject(obj);
                oos.flush();
                oos.close();
                fos.close();
            }
    }

    public void readRepoFiles() throws IOException, ClassNotFoundException {
        this.readMagitObjects();
        this.readBranches();
    }

    private void readBranches() throws IOException {
        File folder = new File(this.path + "/.magit/branches");
        String nameOfHead = "";
            for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (fileEntry.isDirectory())
                    continue;
                FileReader fr = new FileReader(fileEntry);
                BufferedReader br = new BufferedReader(fr);
                if (fileEntry.getName().equals("HEAD")) {
                    nameOfHead = br.readLine();
                    br.close();
                    fr.close();
                    continue;
                }
                Branch branch = new Branch(br.readLine(), fileEntry.getName());
                this.branches.add(branch);
                br.close();
                fr.close();
            }
            String finalNameOfHead = nameOfHead;
            headBranch = branches.stream().filter(Branch -> Branch.getName().equals(finalNameOfHead)).findFirst().orElse(null);
            branches.remove(headBranch);
    }

    private void readMagitObjects() throws IOException, ClassNotFoundException {
        File folder = new File(this.path + "/.magit/objects");
        MagitObject obj;
            for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                FileInputStream fin = new FileInputStream(fileEntry.getPath());
                GZIPInputStream gis = new GZIPInputStream(fin);
                ObjectInputStream ois = new ObjectInputStream(gis);
                obj = (MagitObject) ois.readObject();
                objList.put(obj.getSha1(), obj);
                ois.close();
                gis.close();
                fin.close();
            }
    }

    public void newCommit(String msg) throws IOException {

        String sha1OfRoot;
        sha1OfRoot = Objects.requireNonNull(recursiveWcToObjectBuilder(this.path, "", true, username, currDelta)).getSha1();

        Commit commit;
        if (headBranch != null && !headBranch.getSha1().equals(""))
            commit = new Commit(sha1OfRoot, headBranch.getSha1(), "", msg, username);
        else {
            commit = new Commit(sha1OfRoot, msg, username); //first commit
            headBranch = new Branch(commit.getSha1(), "master");
        }
        headBranch.UpdateSha1(commit.getSha1());
        objList.put(commit.getSha1(), commit);
    }

    private Map<String, Fof> getCommitMap(Commit commit) {
        Map<String, Fof> res = new HashMap<>();
        if (commit != null) {
            recursiveMapBuilder(commit.getRootFolderSha1(), res, "");
        }
        return res;
    }

    private Fof recursiveWcToObjectBuilder(String location, String _path, boolean isCommit, String modifier, Delta delta) throws IOException {
        ArrayList<Fof> fofLst = new ArrayList<>();
        File file = new File(location + _path);
        MagitObject obj = null;
        Fof fof;
        String newModifier;
        String _fofpath;
        String content;
        if (file.isDirectory()) {
            for (File fileEntry : Objects.requireNonNull(file.listFiles())) {
                _fofpath = _path + "/" + fileEntry.getName();
                if (!fileEntry.getName().equals(".magit")) {
                    newModifier = delta.getUsername(_fofpath);
                    if (newModifier == null) {
                        newModifier = username;
                    }
                    fof = recursiveWcToObjectBuilder(location, _fofpath, isCommit, newModifier, delta);
                    if (fof != null)
                        fofLst.add(fof);
                }
            }
            if (fofLst.size() == 0) {
                new File(location + _path).delete();
                return null;
            }
            obj = new Folder(fofLst);
        } else {
                content = new String(Files.readAllBytes(Paths.get(location + _path)));
                obj = new Blob(content);
        }
        assert obj != null;
        fof = new Fof(obj.getSha1(), file.getName(), !file.isDirectory(), modifier, new DateAndTime(file.lastModified()));
        if (isCommit)
            objList.put(obj.getSha1(), obj);
        else if (!(_path).equals(""))
            delta.isObjectChanged(fof, _path);
        return fof;
    }

    private void recursiveMapBuilder(String folderSha1, Map<String, Fof> map, String _path) {
        if (objList.get(folderSha1) instanceof Folder) {
            for (Fof fof : ((Folder) objList.get(folderSha1)).getFofList()) {
                if (!fof.getIsBlob())
                    recursiveMapBuilder(fof.getSha1(), map, _path + "/" + fof.getName());
                map.put(_path + "/" + fof.getName(), fof);
            }
        }
    }

    public void addNewBranch(String name,String sha1) throws AlreadyExistingBranchException, NoCommitHasBeenMadeException, BranchNoNameException, FileNotFoundException {
        Branch branch;
        if(name.equals(""))
        {
            throw new BranchNoNameException();
        }
        if(headBranch==null)
            throw new NoCommitHasBeenMadeException();
        if (branches.stream().filter(Branch -> Branch.getName().equals(name)).findFirst().orElse(null) == null
                && !headBranch.getName().equals(name)) {
            branch = new Branch(sha1, name);
            branches.add(branch);
            makeFileForBranch(branch.getSha1(), branch.getName());
        } else
            throw new AlreadyExistingBranchException();
    }

    public boolean checkDeltaChanges() throws IOException {
        Map<String, Fof> commitMap = new HashMap<>();
        if (headBranch != null) {
            if (!headBranch.getSha1().equals(""))
                commitMap = getCommitMap((Commit) objList.get(headBranch.getSha1()));
        }
        currDelta = new Delta(commitMap);
        recursiveWcToObjectBuilder(this.path, "", false, username, currDelta);
        return currDelta.getIsChanged();
    }

    private Delta deltaChangesBetweenCommits(String sha1) throws IOException {
        Map<String, Fof> commitMap = getCommitMap((Commit) objList.get(sha1));
        Delta delta = new Delta(commitMap);
        recursiveWcToObjectBuilder(this.path + "/.magit/merge files", "", false, username, delta);
        return delta;
    }

    public String deltaChangesBetweenCommitsToString(String sha1) throws IOException {
        String commitPath = path + "/.magit/Commit files/";
        Map<String, Fof> commitMap = getCommitMap((Commit) objList.get(sha1));
        Delta delta = new Delta(commitMap);
        recursiveWcToObjectBuilder(commitPath, "", false, username, delta);
        return delta.showChanges();
    }

    public void switchHead(String name) throws NoSuchBranchException, IOException,CheckOutHeadException {
        if(headBranch.getName().equals(name))
            throw new CheckOutHeadException();
        Branch branch = branches.stream().filter(Branch -> Branch.getName().equals(name)).findFirst().orElse(null);
        if (branch == null)
            throw new NoSuchBranchException();
        branches.add(headBranch);
        branches.remove(branch);
        headBranch = branch;
        makeFileForBranch(headBranch.getName(), "HEAD");
        deleteWCFiles(this.path);
        deployCommit((Commit) objList.get(headBranch.getSha1()), this.path);
    }

    public void deployCommit(Commit commit, String pathOfCommit) throws IOException {
        if (commit != null)
            recursiveObjectToWCBuilder((Folder) objList.get(commit.getRootFolderSha1()), pathOfCommit);

    }

    private void recursiveObjectToWCBuilder(Folder _folder, String _path) throws IOException {
        String newPath;
        for (Fof fof : _folder.getFofList()) {
            newPath = _path + "/" + fof.getName();
            if (!fof.getIsBlob()) {
                new File(newPath).mkdir();
                recursiveObjectToWCBuilder((Folder) objList.get(fof.getSha1()), newPath);

            } else {
                PrintWriter out = new PrintWriter(newPath);
                out.write(objList.get(fof.getSha1()).getContent());
                out.close();
            }
        }
    }

    public static void deleteWCFiles(String _path) throws IOException {
        File file = new File(_path);
        for (File fileEntry : Objects.requireNonNull(file.listFiles())) {
            if (fileEntry.isDirectory() && !fileEntry.getName().equals(".magit")) {
                FileUtils.cleanDirectory(fileEntry);
                fileEntry.delete();
            } else if (fileEntry.isFile())
                fileEntry.delete();
        }

    }

    public String showRepoStatus() throws IOException {
        String noChanges = "No changes were made since last commit";
        if (!checkDeltaChanges()) {
            return noChanges;
        } else
            return currDelta.showChanges();
    }


    public static Repository makeRepoFromXmlRepo(XmlData xmldata) throws IOException {
        MagitRepository mr = xmldata.getMagitRepository();
        MagitSingleCommit singleCommit;
        String commitSha1;
        boolean isHead;
        Repository repo = new Repository(mr.getLocation(), new HashMap<>(), new ArrayList<>());
        for (MagitSingleBranch mgBranch : mr.getMagitBranches().getMagitSingleBranch()) {
            isHead = mr.getMagitBranches().getHead().equals(mgBranch.getName());
            if (mgBranch.getPointedCommit() != null) {
                if (isHead && mgBranch.getPointedCommit().getId().equals("")) {
                    repo.headBranch = new Branch("", "master");
                    return repo;
                }
                singleCommit = xmldata.getCommitMap().get(mgBranch.getPointedCommit().getId());
                commitSha1 = repo.recursiveSha1PrevCommitBuilder(singleCommit, xmldata);
                if (isHead)
                    repo.headBranch = new Branch(commitSha1, mgBranch.getName());
                else {
                        repo.branches.add(new Branch(commitSha1, mgBranch.getName()));
                }
            }

        }
        if(xmldata.hasRemote()) {
            repo.remoteRepoName = xmldata.getRemoteName();
            repo.remoteRepoPath = xmldata.getRemotePath();
        }
        repo.deployCommit((Commit) repo.objList.get(repo.headBranch.getSha1()), repo.getPath());
        return repo;
    }

    private String recursiveSha1PrevCommitBuilder(MagitSingleCommit commit, XmlData xmlData) {
        Fof fof;
        MagitSingleCommit singleCommit;
        Commit newCommit;
        String rootfolderId = commit.getRootFolder().getId();
        String prevCommitSha1 = "";
        if (commit.getPrecedingCommits() != null) {
            if (commit.getPrecedingCommits().getPrecedingCommit().size() != 0) {
                PrecedingCommits.PrecedingCommit prevCommit = commit.getPrecedingCommits().getPrecedingCommit().get(0);
                singleCommit = xmlData.getCommitMap().get(prevCommit.getId());
                prevCommitSha1 = recursiveSha1PrevCommitBuilder(singleCommit, xmlData);
            }
        }
        fof = recursiveXmlCommitBuilder(xmlData, rootfolderId, false);
        newCommit = new Commit(fof.getSha1(), prevCommitSha1, "", commit.getMessage(), commit.getAuthor(), new DateAndTime(commit.getDateOfCreation()));
        objList.put(newCommit.getSha1(), newCommit);
        return newCommit.getSha1();
    }

    private Fof recursiveXmlCommitBuilder(XmlData xmlData, String ID, boolean isBlob) {
        ArrayList<Fof> foflst = new ArrayList<>();
        String name;
        String username;
        String lastModified;
        MagitObject obj;
        boolean isFolder;
        String content;
        if (!isBlob) {
            for (Item item : xmlData.getFolderMap().get(ID).getItems().getItem()) {
                isFolder = !item.getType().equals("blob");
                foflst.add(recursiveXmlCommitBuilder(xmlData, item.getId(), !isFolder));
            }
            obj = new Folder(foflst);
            name = xmlData.getFolderMap().get(ID).getName();
            username = xmlData.getFolderMap().get(ID).getLastUpdater();
            lastModified = xmlData.getFolderMap().get(ID).getLastUpdateDate();
        } else {
            content = xmlData.getBlobMap().get(ID).getContent();
            obj = new Blob(content);
            name = xmlData.getBlobMap().get(ID).getName();
            username = xmlData.getBlobMap().get(ID).getLastUpdater();
            lastModified = xmlData.getBlobMap().get(ID).getLastUpdateDate();
        }
        objList.put(obj.getSha1(), obj);
        return new Fof(obj.getSha1(), name, isBlob, username, new DateAndTime(lastModified));

    }

    public void deleteThisBranch(String input) throws DeleteHeadBranchException, NoSuchBranchException {
        boolean found = false;
        Branch br = null;
        if (input.equals(headBranch.getName()))
            throw new DeleteHeadBranchException();
        for (Branch branch : branches) {
            if (branch.getName().equals(input)) {
                br = branch;
                File f = new File(path + "/.magit/branches/" + branch.getName());
                f.delete();
                found = true;
            }
        }
        if (!found)
            throw new NoSuchBranchException();
        branches.remove(br);
    }

    public List<Commit> getBranchCommits(Branch branch) {
        List<Commit> res = new ArrayList<>();
        Commit commit = ((Commit) objList.get(branch.getSha1()));
        while (commit != null) {
            res.add(commit);
            if (!commit.getPreviousCommitSha1().equals("")) {
                commit = (Commit) objList.get(commit.getPreviousCommitSha1());
            } else
                commit = null;
        }
        return res;
    }


    public void resetBranch(Commit commit) throws IOException {
        headBranch.UpdateSha1(commit.getSha1());
        makeFileForBranch(headBranch.getSha1(), headBranch.getName());
        deleteWCFiles(this.path);
        deployCommit(commit, this.path);
    }

    public ArrayList<Commit> getCommits() {
        ArrayList<Commit> commitLst = new ArrayList<>();
        for (Map.Entry<String, MagitObject> entry : objList.entrySet()) {
            if (entry.getValue() instanceof Commit) {
                commitLst.add((Commit) entry.getValue());
            }
        }
        Collections.sort(commitLst);
        return commitLst;
    }

    public Branch getHeadBranch() {
        return headBranch;
    }

    public String getPreviousCommitSha1(String sha1) {
        return ((Commit) objList.get(sha1)).getPreviousCommitSha1();
    }

    public void buildCommitForMerge(String msg) throws IOException {
        deleteWCFiles(this.path);
        String sha1OfRoot = Objects.requireNonNull(recursiveWcToObjectBuilder(path + "/.magit/merge files/", "", true, username, currDelta)).getSha1();
        Folder commitFolder = (Folder) objList.get(sha1OfRoot);
        Commit commit = new Commit(commitFolder.getSha1(), headBranch.getSha1(), latestMergedBranchSha1, msg, username);
        recursiveObjectToWCBuilder(commitFolder, this.path);
        objList.put(commit.getSha1(), commit);
        headBranch.UpdateSha1(commit.getSha1());
        makeFileForBranch(headBranch.getSha1(), headBranch.getName());
        createZippedFilesForMagitObjects();
    }

    public boolean mergeCommits(String branchSha1) throws IOException {
        latestMergedBranchSha1 = branchSha1;
        String pathMerge = path + "/.magit/merge files/";
        new File(pathMerge).mkdir();

        String sha1OfAncestor = findAncestor(branchSha1, headBranch.getSha1());
        if (!(sha1OfAncestor.equals(branchSha1) || sha1OfAncestor.equals(headBranch.getSha1()))) {
            Folder branchFolder = (Folder) objList.get(((Commit) objList.get(branchSha1)).getRootFolderSha1());
            Folder headFolder = (Folder) objList.get(((Commit) objList.get(headBranch.getSha1())).getRootFolderSha1());

            deleteWCFiles(pathMerge);
            recursiveObjectToWCBuilder(headFolder, pathMerge);
            Delta headBranchDelta = deltaChangesBetweenCommits(sha1OfAncestor);
            deleteWCFiles(pathMerge);
            recursiveObjectToWCBuilder(branchFolder, pathMerge);
            Delta branchDelta = deltaChangesBetweenCommits(sha1OfAncestor);
            deleteWCFiles(pathMerge);


            conflictMap = mergeConflicts(headBranchDelta, branchDelta);
        } else {
            if (sha1OfAncestor.equals(headBranch.getSha1())) {
                headBranch.UpdateSha1(branchSha1);
                makeFileForBranch(headBranch.getSha1(), headBranch.getName());
                deleteWCFiles(this.path);
                deployCommit((Commit)objList.get(branchSha1),this.path);
                return false;
            } else {
                return false;
            }
        }
        return true;
    }

    private HashMap<String, MergeCase> mergeConflicts(Delta headDelta, Delta branchDelta) {

        boolean existsInTarget = false;
        HashMap<String, MergeCase> mergeMap = new HashMap<>();
        String baseContent , targetContent ;
        for (Map.Entry<String, Fof> entry : headDelta.getCommitMap().entrySet()) {
            if (entry.getValue().getIsBlob())
                mergeMap.put(entry.getKey(), sixBooleanGoneWild(headDelta, branchDelta, entry));
            else {
                MergeCase mc = new MergeCase(MergeCase.caseIs(true, true, true,
                        true, true, true), true, null, null, null);
                mergeMap.put(entry.getKey(), mc);
            }
        }
        for (Map.Entry<String, Fof> entry : headDelta.getNewFilesFofs().entrySet()) {
            if (entry.getValue().getIsBlob()) {
                baseContent = objList.get(entry.getValue().getSha1()).getContent();
                if (branchDelta.getNewFilesFofs().get(entry.getKey()) != null) {
                    targetContent = objList.get(branchDelta.getNewFilesFofs().get(entry.getKey()).getSha1()).getContent();
                    MergeCase mc = new MergeCase(MergeCase.caseIs(true, true, false,
                            targetContent.equals(baseContent), false, false), false, baseContent, targetContent, null);
                    mergeMap.put(entry.getKey(), mc);
                    branchDelta.getNewFilesFofs().remove(entry.getKey());
                } else {
                    MergeCase mc = new MergeCase(MergeCase.caseIs(true, false, false,
                            false, false, false), false, baseContent, null, null);
                    mergeMap.put(entry.getKey(), mc);
                }
            } else {
                MergeCase mc = new MergeCase(MergeCase.caseIs(true, true, true,
                        true, true, true), true, null, null, null);
                mergeMap.put(entry.getKey(), mc);
            }
        }
        for (Map.Entry<String, Fof> entry : branchDelta.getNewFilesFofs().entrySet()) {
            if (entry.getValue().getIsBlob()) {
                targetContent = objList.get(branchDelta.getNewFilesFofs().get(entry.getKey()).getSha1()).getContent();
                MergeCase mc = new MergeCase(MergeCase.caseIs(false, true, false,
                        false, false, false), false, null, targetContent, null);
                mergeMap.put(entry.getKey(), mc);
            } else {
                MergeCase mc = new MergeCase(MergeCase.caseIs(true, true, true,
                        true, true, true), true, null, null, null);
                mergeMap.put(entry.getKey(), mc);
            }
        }
        return mergeMap;
    }


    private Function<String, CommitRepresentative> CommitRepresentativeMapper = this::getCommitBySha1;

    private MergeCase sixBooleanGoneWild(Delta headDelta, Delta branchDelta, Map.Entry<String, Fof> entry) {
        boolean baseEqualsTargetSha1, targetEqualsAncestorSha1, baseEqualsAncestorSha1, existsInAncestor = true, existsInBase, existsInTarget;
        String baseSha1 = "noBase";
        String targetSha1 = "noTarget";
        String contentAncestor = objList.get(branchDelta.getCommitMap().get(entry.getKey()).getSha1()).getContent();
        String contentBase = null, contentTarget = null;
        if (branchDelta.getDeletedFilesFofs().get(entry.getKey()) == null) {
            existsInTarget = true;
            if (branchDelta.getUpdatedFilesFofs().get(entry.getKey()) != null) {
                contentTarget = objList.get(branchDelta.getUpdatedFilesFofs().get(entry.getKey()).getSha1()).getContent();
                targetSha1 = branchDelta.getUpdatedFilesFofs().get(entry.getKey()).getSha1();
                targetEqualsAncestorSha1 = false;
            } else {
                targetEqualsAncestorSha1 = true;
                contentTarget = contentAncestor;
            }
        } else {
            targetEqualsAncestorSha1 = false;
            existsInTarget = false;
        }
        if (headDelta.getDeletedFilesFofs().get(entry.getKey()) == null) {
            existsInBase = true;
            if (headDelta.getUpdatedFilesFofs().get(entry.getKey()) != null) {
                contentBase = objList.get(headDelta.getUpdatedFilesFofs().get(entry.getKey()).getSha1()).getContent();
                baseSha1 = headDelta.getUpdatedFilesFofs().get(entry.getKey()).getSha1();
                baseEqualsAncestorSha1 = false;
            } else {
                baseEqualsAncestorSha1 = true;
                contentBase = contentAncestor;
            }
        } else {
            baseEqualsAncestorSha1 = false;
            existsInBase = false;
        }
        baseEqualsTargetSha1 = (targetEqualsAncestorSha1 && baseEqualsAncestorSha1) || (baseSha1.equals(targetSha1));
        Optional<MergeCases> res = MergeCase.caseIs(existsInBase, existsInTarget, true,
                baseEqualsTargetSha1, targetEqualsAncestorSha1, baseEqualsAncestorSha1);

        return new MergeCase(res, false, contentBase, contentTarget, contentAncestor);

    }

    private String findAncestor(String sha1_1, String sha1_2) {
        AncestorFinder finder = new AncestorFinder(CommitRepresentativeMapper);
        return finder.traceAncestor(sha1_1, sha1_2);
    }

    private void recursiveFolderBuilder(String pathMerge,HashMap<String, MergeCase> folderLst)
    {

        HashMap<String, MergeCase> newFolderLst=new HashMap<>();
        for(Map.Entry<String, MergeCase> entry:folderLst.entrySet())
            if(entry.getValue().getIsFolder())
                if((!new File(pathMerge+entry.getKey()).mkdir())&&!(new File(pathMerge+entry.getKey()).isDirectory()))
                {
                    newFolderLst.put(entry.getKey(),entry.getValue());
                }
        if(!newFolderLst.isEmpty())
            recursiveFolderBuilder(pathMerge,newFolderLst);
    }
    public HashMap<String, MergeCase> getConflictHashMap() throws FileNotFoundException {

         HashMap<String, MergeCase> resMap = new HashMap<>();
         String pathMerge = ModuleTwo.getActiveRepoPath() + "/.magit/merge files/";
         new File(pathMerge).mkdir();
         recursiveFolderBuilder(pathMerge, conflictMap);
            for (Map.Entry<String, MergeCase> entry : conflictMap.entrySet()) {
                if (!entry.getValue().getIsFolder()) {
                    if (entry.getValue().getMergecases().get().takeOursOrTheirs().equals(""))
                        resMap.put(entry.getKey(), entry.getValue());
                    else {
                        if (entry.getValue().getMergecases().get().takeOursOrTheirs().equals("ours")) {
                            PrintWriter out = new PrintWriter(pathMerge + entry.getKey());
                            out.write(entry.getValue().getBaseContent());
                            out.close();
                        }
                        if (entry.getValue().getMergecases().get().takeOursOrTheirs().equals("theirs")) {
                            {
                                PrintWriter out = new PrintWriter(pathMerge + entry.getKey());
                                out.write(entry.getValue().getTargetContent());
                                out.close();
                            }
                        }
                    }
                }
            }
        HashMap<String, MergeCase> map = new HashMap<>(resMap);
        for(Map.Entry<String, MergeCase> entry:resMap.entrySet())
            if(entry.getValue().getMergecases().get().takeOursOrTheirs().equals("delete"))
                map.remove(entry.getKey());
        resMap=map;
        return resMap;
    }

    public void Clone(String _path) throws IOException {
        String name = this.name;
        File srcDir = new File(this.path);
        File destDir = new File(_path);
        FileUtils.copyDirectory(srcDir, destDir);
        srcDir = new File(_path + "/.magit/branches");
        destDir = new File(_path + "/.magit/branches/" + name);
        FileUtils.copyDirectory(srcDir, destDir);
        new File(_path + "/.magit/branches/remote branches/" + name).delete();
    }

    public void fetch() throws IOException {
        File theirObjects = new File(remoteRepoPath + "/.magit/objects");
        File myObjects = new File(this.path + "/.magit/objects");
        FileUtils.copyDirectory(theirObjects, myObjects);
        File branches = new File(remoteRepoPath + "/.magit/branches");
        for (File fileEntry : Objects.requireNonNull(branches.listFiles())) {
            if (fileEntry.isFile()) {
                File fileToDelete = new File(this.path + "/.magit/branches/" + fileEntry.getName());
                fileToDelete.delete();
                FileUtils.copyFile(fileEntry, fileToDelete);
            }
        }
    }

    public void makeRemoteRepositoryFiles(String pathOfOldRepo) throws IOException{
        makeRemoteRepositoryFile(pathOfOldRepo);
        makeRemoteRepositoryNameFile(pathOfOldRepo);
    }
    private void makeRemoteRepositoryFile(String pathOfOldRepo) throws IOException {
        File pathOfRepoFile = new File(this.path+"/.magit/remoteRepositoryPath.txt");
        if(pathOfRepoFile.createNewFile()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(pathOfRepoFile));
            writer.write(pathOfOldRepo);
            writer.close();
        }
    }

    public void updateRemoteRepoPath() throws IOException {
        File remoteRepoPathFile = new File(path+"/.magit/remoteRepositoryPath.txt");
        if(remoteRepoPathFile.isFile()){
            BufferedReader r = new BufferedReader(new FileReader(remoteRepoPathFile));
            remoteRepoPath = r.readLine();
            r.close();
        }
    }
    public void updateRemoteRepoName() throws IOException {
        File remoteRepoPathFile = new File(path+"/.magit/remoteRepositoryName.txt");
        if(remoteRepoPathFile.isFile()){
            BufferedReader r = new BufferedReader(new FileReader(remoteRepoPathFile));
            remoteRepoName = r.readLine();
            r.close();
        }
    }

    public boolean isHeadBranchRTB() throws IOException { //checks if rb with name of head branch exists.
        String headName;
        File headFile = new File(path+"/.magit/branches/HEAD");
        BufferedReader r = new BufferedReader(new FileReader(headFile));
        headName = r.readLine();
        r.close();
        String _path = this.path+"/.magit/branches";
        File branches = new File(_path);
        File remoteBranchesFolder = null;
        for(File file : Objects.requireNonNull(branches.listFiles())){
            if(file.isDirectory())
                remoteBranchesFolder = file;
        }
        assert remoteBranchesFolder != null;
        return (new File(_path+"/"+remoteBranchesFolder.getName()+"/"+headName).exists());
    }


    public boolean isCommitInObjList(String sha1) throws NoCommitInObjList {
        if(objList.get(sha1)!=null)
            return true;
        throw new NoCommitInObjList();
    }
    public String getRemoteRepositoryPath() {
        return remoteRepoPath;
    }

    public void updateCommits(String remotePath,ArrayList<String> arr) throws IOException { // help for push
        File commitFileToCopy;
        File myObjects;
        for(String commitSha1ToCopy:arr){
            commitFileToCopy = new File(remotePath + "/.magit/objects/"+commitSha1ToCopy);
            myObjects = new File(path + "/.magit/objects/"+commitSha1ToCopy);
            FileUtils.copyFile(commitFileToCopy,myObjects);
        }
    }

    public ArrayList<String> getWantedSha1sForPush() throws IOException {
        ArrayList<String> arr = new ArrayList<>();
        String sha1ToAdd = headBranch.getSha1();
        File headFile = new File(path+"/.magit/branches/HEAD");
        BufferedReader r = new BufferedReader(new FileReader(headFile));
        String headName = r.readLine();
        r.close();
        File fileOfRemoteHeadBranch = new File(this.path + "/.magit/branches/" + remoteRepoName + "/" + headName);
        BufferedReader br = new BufferedReader(new FileReader(fileOfRemoteHeadBranch));
        String sha1OfAncestor = br.readLine();
        br.close();
        while (!sha1ToAdd.equals(sha1OfAncestor)) {
            arr.add(sha1ToAdd);
            String comitSha1RootFolder = ((Commit) objList.get(sha1ToAdd)).getRootFolderSha1();
            Folder rootFolder = (Folder) objList.get(comitSha1RootFolder);
            recursiveSha1Adder(rootFolder, arr);
            sha1ToAdd = ((Commit) objList.get(sha1ToAdd)).getPreviousCommitSha1();
        }
        return arr;
    }

    private void recursiveSha1Adder(Folder folder,ArrayList<String> arr){
        for (Fof fof : folder.getFofList()) {
            if (!fof.getIsBlob()) {
                arr.add(fof.getSha1());
                recursiveSha1Adder((Folder) objList.get(fof.getSha1()), arr);
            } else
                arr.add(fof.getSha1());
        }
    }

    public void updateHeadBranch(String remotePath) throws IOException {
        File myHeadBranch = new File(path + "/.magit/branches/"+headBranch.getName());
        myHeadBranch.delete();
        File remoteHeadBranch = new File(remotePath + "/.magit/branches/"+headBranch.getName());
        FileUtils.copyFile(remoteHeadBranch,myHeadBranch);
    }

    private void makeRemoteRepositoryNameFile(String pathOfOldRepo) throws IOException {
        File pathOfRepoFile = new File(this.path+"/.magit/remoteRepositoryName.txt");
        File repoNameFile = new File(pathOfOldRepo);
        if(pathOfRepoFile.createNewFile()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(pathOfRepoFile));
            writer.write(repoNameFile.getName());
            writer.close();
        }
    }

    public boolean lrIsPushed() throws IOException {
        File rtbFile = new File(this.path+"/.magit/branches/"+headBranch.getName());
        File rbFile = new File(this.path+"/.magit/branches/"+this.remoteRepoName+"/"+headBranch.getName());
        BufferedReader r = new BufferedReader(new FileReader(rtbFile));
        String rtbCommitSha1 = r.readLine();
        r = new BufferedReader(new FileReader(rbFile));
        String rbCommitSha1=r.readLine();
        r.close();
        return rtbCommitSha1.equals(rbCommitSha1);
    }

    public void updateRB() throws IOException {
        File rtbFile = new File(this.path+"/.magit/branches/"+headBranch.getName());
        File rbFile = new File(this.path+"/.magit/branches/"+this.remoteRepoName+"/"+headBranch.getName());
        rbFile.delete();
        FileUtils.copyFile(rtbFile,rbFile);
    }

    public ArrayList<String> getWantedSha1sForPull(String sha1OfAncestor){
        ArrayList<String> arr = new ArrayList<>();
        String sha1ToAdd = headBranch.getSha1();
        while (!sha1ToAdd.equals(sha1OfAncestor)) {
            arr.add(sha1ToAdd);
            String comitSha1RootFolder = ((Commit) objList.get(sha1ToAdd)).getRootFolderSha1();
            Folder rootFolder = (Folder) objList.get(comitSha1RootFolder);
            recursiveSha1Adder(rootFolder, arr);
            sha1ToAdd = ((Commit) objList.get(sha1ToAdd)).getPreviousCommitSha1();
        }
        return arr;
    }

    public void updateCommitsForPull(String myPath, ArrayList<String> arr) throws IOException {
        File commitFileToCopy;
        File myObjects;
        for(String commitSha1ToCopy:arr){
            commitFileToCopy = new File(path + "/.magit/objects/"+commitSha1ToCopy);
            myObjects = new File(myPath + "/.magit/objects/"+commitSha1ToCopy);
            FileUtils.copyFile(commitFileToCopy,myObjects);
        }
    }

    public void updateHeadBranchForPull(String myPath) throws IOException {
        File myHeadBranch = new File(myPath + "/.magit/branches/"+headBranch.getName());
        myHeadBranch.delete();
        File remoteHeadBranch = new File(path + "/.magit/branches/"+headBranch.getName());
        FileUtils.copyFile(remoteHeadBranch,myHeadBranch);
    }

    public void makeRemoteFiles() throws IOException {
        if(remoteRepoName!=null){
            File pathOfRepoFile = new File(this.path+"/.magit/remoteRepositoryName.txt");
            pathOfRepoFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(pathOfRepoFile));
            writer.write(remoteRepoName);
            writer.close();
            File pathOfRepoPathFile = new File(this.path+"/.magit/remoteRepositoryPath.txt");
            pathOfRepoPathFile.createNewFile();
            BufferedWriter r = new BufferedWriter(new FileWriter(pathOfRepoPathFile));
            r.write(remoteRepoPath);
            r.close();
            new File(this.path+"/.magit/branches/"+this.remoteRepoName+"/").mkdir();
        }
    }
}



