package Repository;

import Merge.MergeCase;
import Merge.MergeCases;
import Objects.Api.MagitObject;
import Objects.Blob.Blob;
import Objects.Branch.AlreadyExistingBranchException;
import Objects.Branch.Branch;
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
    private HashMap<String,MergeCase> conflictMap=new HashMap<>();
    private String latestMergedBranchSha1=null;



    public Repository(String _path, Map<String, MagitObject> _objList, ArrayList<Branch> _branches) {
        path = _path;
        objList = _objList;
        branches = _branches;
        name = new File(_path).getName();
    }

    public String getPath() {
        return path;
    }
    public HashMap<String,MergeCase> getConflictMap() {
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

    public void Clone(String _path) throws IOException {
        String name = this.name;
        File srcDir = new File(this.path);
        File destDir = new File(_path);
        FileUtils.copyDirectory(srcDir, destDir);
        srcDir = new File(_path+"/.magit/branches");
        destDir = new File(_path+"/.magit/branches/"+name);
        FileUtils.copyDirectory(srcDir, destDir);
        new File(_path+"/.magit/branches/remote branches/"+name).delete();
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

    public void createFiles() {
        createZippedFilesForMagitObjects();
        createFilesForBranches();
    }

    private Commit getCommitBySha1(String sha1) {
        return (Commit) objList.get(sha1);
    }

    private void createFilesForBranches() {
        makeFileForBranch(headBranch.getName(), "HEAD");
        makeFileForBranch(headBranch.getSha1(), headBranch.getName());
        for (Branch branch : branches) {
            makeFileForBranch(branch.getSha1(), branch.getName());
        }

    }

    public ArrayList<Branch> getBranches() {
        return branches;
    }

    private void makeFileForBranch(String content, String name) {
        try {
            PrintWriter out = new PrintWriter(this.path + "/.magit/branches/" + name);
            out.println(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createZippedFilesForMagitObjects() {
        for (Map.Entry<String, MagitObject> entry : objList.entrySet())
            createSingleZippedFileForMagitObject(entry.getKey(), entry.getValue());

    }

    private void createSingleZippedFileForMagitObject(String sha1, MagitObject obj) {
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readRepoFiles() {
        this.readMagitObjects();
        this.readBranches();
    }

    private void readBranches() {
        File folder = new File(this.path + "/.magit/branches");
        String nameOfHead = "";
        try {
            for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if(fileEntry.isDirectory())
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMagitObjects() {
        File folder = new File(this.path + "/.magit/objects");
        MagitObject obj;
        try {
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
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void newCommit(String msg) {

        String sha1OfRoot;
        sha1OfRoot = Objects.requireNonNull(recursiveWcToObjectBuilder(this.path,"", true, username,currDelta)).getSha1();

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

    private Fof recursiveWcToObjectBuilder(String location,String _path, boolean isCommit, String modifier,Delta delta) {
        ArrayList<Fof> fofLst = new ArrayList<>();
        File file = new File(location+_path);
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
                    fof = recursiveWcToObjectBuilder(location,_fofpath, isCommit, newModifier,delta);
                    if (fof != null)
                        fofLst.add(fof);
                }
            }
            if (fofLst.size() == 0) {
                new File(location+_path).delete();
                return null;
            }
            obj = new Folder(fofLst);
        } else {
            try {
                content = new String(Files.readAllBytes(Paths.get(location+_path)));
                obj = new Blob(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public void addNewBranch(String name, Commit commit) throws AlreadyExistingBranchException {
        Branch branch;
        if (branches.stream().filter(Branch -> Branch.getName().equals(name)).findFirst().orElse(null) == null
                && !headBranch.getName().equals(name)) {
            if (commit != null) {
                branch = new Branch(commit.getSha1(), "master");
                headBranch = branch;
                makeFileForBranch(branch.getName(), "HEAD");            //update the head file
            } else
                branch = new Branch(headBranch.getSha1(), name);
            branches.add(branch);
            makeFileForBranch(branch.getSha1(), branch.getName());
        } else
            throw new AlreadyExistingBranchException();
    }

    public boolean checkDeltaChanges() {
        Map<String, Fof> commitMap = new HashMap<>();
        if (headBranch != null) {
            if (!headBranch.getSha1().equals(""))
                commitMap = getCommitMap((Commit) objList.get(headBranch.getSha1()));
        }
        currDelta = new Delta(commitMap);
        recursiveWcToObjectBuilder(this.path,"", false, username,currDelta);
        return currDelta.getIsChanged();
    }

    private Delta deltaChangesBetweenCommits(String sha1) {
        Map<String, Fof> commitMap = getCommitMap((Commit) objList.get(sha1));
        Delta delta = new Delta(commitMap);
        recursiveWcToObjectBuilder(this.path+"/.magit/merge files","", false, username,delta);
        return delta;
    }

    public String deltaChangesBetweenCommitsToString(String sha1){
        String commitPath = path + "/.magit/Commit files/";
        Map<String, Fof> commitMap = getCommitMap((Commit) objList.get(sha1));
        Delta delta = new Delta(commitMap);
        recursiveWcToObjectBuilder(commitPath,"", false, username,delta);
        return delta.showChanges();
    }

    public void switchHead(String name) throws NoSuchBranchException, IOException {
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

    public String showRepoStatus() {
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
                else
                    repo.branches.add(new Branch(commitSha1, mgBranch.getName()));
            }
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
        String sha1OfRoot = Objects.requireNonNull(recursiveWcToObjectBuilder(path + "/.magit/merge files/","", true, username,currDelta)).getSha1();
        Folder commitFolder=(Folder)objList.get(sha1OfRoot);
        Commit commit = new Commit(commitFolder.getSha1(), headBranch.getSha1(), latestMergedBranchSha1, msg, username);
        recursiveObjectToWCBuilder(commitFolder,this.path);
        objList.put(commit.getSha1(), commit);
        headBranch.UpdateSha1(commit.getSha1());
        makeFileForBranch(headBranch.getSha1(), headBranch.getName());
        createZippedFilesForMagitObjects();
    }
    public boolean mergeCommits(Branch branch) throws IOException, CannotMergeException {
        latestMergedBranchSha1=branch.getSha1();
        String pathMerge = path + "/.magit/merge files/";
        new File(pathMerge).mkdir();

        String sha1OfAncestor = findAncestor(branch.getSha1(), headBranch.getSha1());
        if(!(sha1OfAncestor.equals(branch.getSha1())||sha1OfAncestor.equals(headBranch.getSha1()))) {
            Folder branchFolder = (Folder) objList.get(((Commit) objList.get(branch.getSha1())).getRootFolderSha1());
            Folder headFolder = (Folder) objList.get(((Commit) objList.get(headBranch.getSha1())).getRootFolderSha1());

            deleteWCFiles(pathMerge);
            recursiveObjectToWCBuilder(headFolder, pathMerge);
            Delta headBranchDelta = deltaChangesBetweenCommits(sha1OfAncestor);
            deleteWCFiles(pathMerge);
            recursiveObjectToWCBuilder(branchFolder, pathMerge);
            Delta branchDelta = deltaChangesBetweenCommits(sha1OfAncestor);
            deleteWCFiles(pathMerge);


            conflictMap=mergeConflicts(headBranchDelta, branchDelta);
        }
        else {
            if (sha1OfAncestor.equals(headBranch.getSha1())) {
                headBranch.UpdateSha1(branch.getSha1());
                makeFileForBranch(headBranch.getSha1(), headBranch.getName());
            } else
                return false;
        }
        return true;
    }

    private HashMap<String,MergeCase>  mergeConflicts(Delta headDelta, Delta branchDelta) {

        boolean existsInTarget=false;
        HashMap<String,MergeCase> mergeMap=new HashMap<>();
        String baseContent=null,ancestorContent=null,targetContent=null;
        for(Map.Entry<String,Fof> entry:headDelta.getCommitMap().entrySet())
        {
            if(entry.getValue().getIsBlob())
                mergeMap.put(entry.getKey(),sixBooleanGoneWild(headDelta,branchDelta,entry));
            else
            {
                MergeCase mc = new MergeCase(MergeCase.caseIs(true, true, true,
                        true, true, true),true,null, null, null);
                mergeMap.put(entry.getKey(), mc);
            }
        }
        for(Map.Entry<String,Fof> entry:headDelta.getNewFilesFofs().entrySet())
        {
            if(entry.getValue().getIsBlob()) {
                baseContent = objList.get(entry.getValue().getSha1()).getContent();
                if (branchDelta.getNewFilesFofs().get(entry.getKey()) != null) {
                    targetContent = objList.get(branchDelta.getNewFilesFofs().get(entry.getKey()).getSha1()).getContent();
                    MergeCase mc = new MergeCase(MergeCase.caseIs(true, true, false,
                            false, false, false), false, baseContent, targetContent, null);
                    mergeMap.put(entry.getKey(), mc);
                    branchDelta.getNewFilesFofs().remove(entry.getKey());
                } else {
                    MergeCase mc = new MergeCase(MergeCase.caseIs(true, false, false,
                            false, false, false), false, baseContent, targetContent, null);
                    mergeMap.put(entry.getKey(), mc);
                }
                targetContent = null;
            }
            else
            {
                MergeCase mc = new MergeCase(MergeCase.caseIs(true, true, true,
                        true, true, true),true,null, null, null);
                mergeMap.put(entry.getKey(), mc);
            }
        }
        for(Map.Entry<String,Fof> entry:branchDelta.getNewFilesFofs().entrySet())
        {
            if(entry.getValue().getIsBlob()) {
                targetContent = objList.get(branchDelta.getNewFilesFofs().get(entry.getKey()).getSha1()).getContent();
                MergeCase mc = new MergeCase(MergeCase.caseIs(false, true, false,
                        false, false, false),false, null, targetContent, null);
                mergeMap.put(entry.getKey(), mc);
            }
            else
            {
                MergeCase mc = new MergeCase(MergeCase.caseIs(true, true, true,
                        true, true, true),true,null, null, null);
                mergeMap.put(entry.getKey(), mc);
            }
        }
        return mergeMap;
    }


    private Function<String, CommitRepresentative> CommitRepresentativeMapper = this::getCommitBySha1;

    private MergeCase sixBooleanGoneWild(Delta headDelta, Delta branchDelta,Map.Entry<String,Fof> entry)
    {
        boolean  baseEqualsTargetSha1,  targetEqualsAncestorSha1,  baseEqualsAncestorSha1, existsInAncestor = true, existsInBase, existsInTarget;
        String baseSha1="noBase";
        String targetSha1="noTarget";
        String contentAncestor=objList.get(branchDelta.getCommitMap().get(entry.getKey()).getSha1()).getContent();
        String contentBase=null,contentTarget=null;
        if(branchDelta.getDeletedFilesFofs().get(entry.getKey())==null) {
            existsInTarget = true;
            if (branchDelta.getUpdatedFilesFofs().get(entry.getKey()) != null) {
                contentTarget = objList.get(branchDelta.getUpdatedFilesFofs().get(entry.getKey()).getSha1()).getContent();
                targetSha1 = branchDelta.getUpdatedFilesFofs().get(entry.getKey()).getSha1();
                targetEqualsAncestorSha1 = false;
            } else {
                targetEqualsAncestorSha1 = true;
                contentTarget=contentAncestor;
            }
        }
        else {
            targetEqualsAncestorSha1=false;
            existsInTarget = false;
        }
        if(headDelta.getDeletedFilesFofs().get(entry.getKey())==null)
        {
            existsInBase=true;
            if(headDelta.getUpdatedFilesFofs().get(entry.getKey())!=null)
            {
                contentBase=objList.get(headDelta.getUpdatedFilesFofs().get(entry.getKey()).getSha1()).getContent();
                baseSha1=headDelta.getUpdatedFilesFofs().get(entry.getKey()).getSha1();
                baseEqualsAncestorSha1=false;
            }
            else {
                baseEqualsAncestorSha1 = true;
                contentBase=contentAncestor;
            }
        }
        else {
            baseEqualsAncestorSha1=false;
            existsInBase = false;
        }
        baseEqualsTargetSha1= (targetEqualsAncestorSha1 && baseEqualsAncestorSha1) || (baseSha1.equals(targetSha1));
        if(existsInBase && existsInTarget && !(baseEqualsAncestorSha1 && targetEqualsAncestorSha1)) {
            baseEqualsTargetSha1 = false;
            targetEqualsAncestorSha1 = false;
            baseEqualsAncestorSha1 = false;
        }
        Optional<MergeCases> res=MergeCase.caseIs(existsInBase,existsInTarget, true,
            baseEqualsTargetSha1,targetEqualsAncestorSha1,baseEqualsAncestorSha1);

        return  new MergeCase(res,false,contentBase,contentTarget,contentAncestor);

    }
    private String findAncestor(String sha1_1, String sha1_2) {
        AncestorFinder finder = new AncestorFinder(CommitRepresentativeMapper);
        return finder.traceAncestor(sha1_1, sha1_2);
    }


    public boolean isConflictsEmpty() {
        return conflictMap.isEmpty();
    }
}


