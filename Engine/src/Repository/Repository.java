package Repository;

import EngineRunner.ModuleTwo;
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


import java.io.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import java.util.zip.*;

public class Repository {
    private Map<String, MagitObject> objList; //<sha1,object>
    private ArrayList<Branch> branches;
    private Branch headBranch = null;
    private String path;
    private String name;
    private static String username = "default";
    private Delta currDelta;

    public Repository(String _path, Map<String, MagitObject> _objList, ArrayList<Branch> _branches) {
        path = _path;
        objList = _objList;
        branches = _branches;
        name = new File(_path).getName();
    }
    public String getPath() {
        return path;
    }
    public String getHeadBranchName()
    {
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
        File Head = new File(path + "/.magit/branches/HEAD");
        Head.createNewFile();
    }

    public void createFiles() {
        createZippedFilesForMagitObjects();
        createFilesForBranches();
    }

    private void createFilesForBranches() {
        makeFileForBranch(headBranch.getName(),"HEAD");
        makeFileForBranch(headBranch.getSha1(),headBranch.getName());
        for (Branch branch : branches) {
            makeFileForBranch(branch.getSha1(), branch.getName());
        }

    }
    public ArrayList<Branch> getBranches(){
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
        String path2;
        for (Map.Entry<String, MagitObject> entry : objList.entrySet()) {

            try {
                path2 = this.path + "/.magit/objects/" + entry.getKey();
                File file = new File(path2);
                if (!file.exists()) {
                    FileOutputStream fos = new FileOutputStream(path2);
                    GZIPOutputStream gos = new GZIPOutputStream(fos);
                    ObjectOutputStream oos = new ObjectOutputStream(gos);
                    oos.writeObject(entry.getValue());
                    oos.flush();
                    oos.close();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void readRepoFiles() {
        this.readMagitObjects();
        this.readBranches();
    }

    private void readBranches() {
        File folder = new File(this.path + "/.magit/branches");
        String nameOfHead ="";
        try {
            for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                FileReader fr = new FileReader(fileEntry);
                BufferedReader br = new BufferedReader(fr);
                if (fileEntry.getName().equals("HEAD")){
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

    public void newCommit(String msg) throws AlreadyExistingBranchException {

        String sha1OfRoot = recursiveWcToObjectBuilder(this.path, true, username).getSha1();

        Commit commit;
        if (headBranch != null && headBranch.getSha1() != null)
            commit = new Commit(sha1OfRoot, headBranch.getSha1(), null, msg, username);
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
            recursiveMapBuilder(commit.getRootFolderSha1(), res, this.path);
        }
        return res;
    }


    private Fof recursiveWcToObjectBuilder(String _path, boolean isCommit, String modifier) {
        ArrayList<Fof> fofLst = new ArrayList<>();
        File file = new File(_path);
        MagitObject obj = null;
        Fof fof;
        String newModifier;
        String _fofpath;
        String content;
        if (file.isDirectory()) {
            for (File fileEntry : Objects.requireNonNull(file.listFiles())) {
                _fofpath = _path + "/" + fileEntry.getName();
                if (!fileEntry.getName().equals(".magit")) {
                    newModifier = currDelta.getUsername(_fofpath);
                    if (newModifier == null) {
                        newModifier = username;
                    }
                    fof = recursiveWcToObjectBuilder(_fofpath, isCommit, newModifier);
                    if (fof != null)
                        fofLst.add(fof);
                }
            }
            if (fofLst.size() == 0) {
                new File(_path).delete();
                return null;
            }
            obj = new Folder(fofLst);
        } else {
            try {
                content = new String ( Files.readAllBytes( Paths.get(_path) ) );
                obj = new Blob(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        assert obj != null;
        fof = new Fof(obj.getSha1(), file.getName(), !file.isDirectory(), modifier, new DateAndTime(file.lastModified()));
        if (isCommit)
            objList.put(obj.getSha1(), obj);
        else if (!_path.equals(path))
            currDelta.isObjectChanged(fof, _path);
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

    public void addNewBranch(String name,Commit commit) throws AlreadyExistingBranchException {
        Branch branch;
        if (branches.stream().filter(Branch -> Branch.getName().equals(name)).findFirst().orElse(null) == null
                && !headBranch.getName().equals(name)) {
            if (commit != null){
                branch = new Branch(commit.getSha1(), "master");
                headBranch = branch;
                makeFileForBranch(branch.getName(),"HEAD");            //update the head file
            }
            else
                branch = new Branch(headBranch.getSha1(), name);
            branches.add(branch);
            makeFileForBranch(branch.getSha1(), branch.getName());
        } else
            throw new AlreadyExistingBranchException();
    }

    public boolean checkDeltaChanges() {
        Map<String, Fof> commitMap = new HashMap<>();
        if (headBranch != null) {
            if (headBranch.getSha1() != null)
                commitMap = getCommitMap((Commit) objList.get(headBranch.getSha1()));
        }
        currDelta = new Delta(commitMap);
        recursiveWcToObjectBuilder(this.path, false, username);
        return currDelta.getIsChanged();
    }


    public void switchHead(String name) throws NoSuchBranchException, IOException {
        Branch branch = branches.stream().filter(Branch -> Branch.getName().equals(name)).findFirst().orElse(null);
        if (branch == null)
            throw new NoSuchBranchException();
        branches.add(headBranch);
        branches.remove(branch);
        headBranch = branch;
        makeFileForBranch(headBranch.getName(), "HEAD");
        deleteWCfiles(this.path);
        deployCommit((Commit) objList.get(headBranch.getSha1()),this.path);
    }

    public void deployCommit(Commit commit , String pathOfCommit) {
        if (commit != null)
            recursiveObjectToWCBuilder((Folder) objList.get(commit.getRootFolderSha1()), pathOfCommit);

    }

    private void recursiveObjectToWCBuilder(Folder _folder, String _path) {
        String newPath;
        for (Fof fof : _folder.getFofList()) {
            newPath = _path + "/" + fof.getName();
            if (!fof.getIsBlob()) {
                new File(newPath).mkdir();
                recursiveObjectToWCBuilder((Folder) objList.get(fof.getSha1()), newPath);

            } else
                try {
                    PrintWriter out = new PrintWriter(newPath);
                    out.write(objList.get(fof.getSha1()).getContent());
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
    }

    public void deleteWCfiles(String _path) throws IOException { //Delete is not working properly. FIX DIS!
        File file = new File(_path);
        for (File fileEntry : Objects.requireNonNull(file.listFiles())) {
            if (fileEntry.isDirectory() && !fileEntry.getName().equals(".magit")){
                FileUtils.cleanDirectory(fileEntry);
                fileEntry.delete();
        }
            else if (fileEntry.isFile())
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

    public static Repository makeRepoFromXmlRepo(XmlData xmldata) {
        MagitRepository mr = xmldata.getMagitRepository();
        MagitSingleCommit singleCommit;
        String commitSha1;
        boolean isHead;
        Repository repo = new Repository(mr.getLocation(), new HashMap<String, MagitObject>(), new ArrayList<Branch>());
        for (MagitSingleBranch mgbrnach : mr.getMagitBranches().getMagitSingleBranch()) {
            isHead = mr.getMagitBranches().getHead().equals(mgbrnach.getName());
            if (mgbrnach.getPointedCommit() != null) {

                if (isHead && mgbrnach.getPointedCommit().getId().equals("")) {
                    repo.headBranch = new Branch("", "master");
                    return repo;
                }
                singleCommit = xmldata.getCommitMap().get(mgbrnach.getPointedCommit().getId());
                commitSha1 = repo.recursiveSha1PrevCommitBuilder(singleCommit, xmldata);
                if (isHead)
                    repo.headBranch = new Branch(commitSha1, mgbrnach.getName());
                else
                    repo.branches.add(new Branch(commitSha1, mgbrnach.getName()));
            }
        }
        repo.deployCommit((Commit) repo.objList.get(repo.headBranch.getSha1()),repo.getPath());
        return repo;
    }

    private String recursiveSha1PrevCommitBuilder(MagitSingleCommit commit, XmlData xmlData) {
        Fof fof;
        MagitSingleCommit singleCommit;
        Commit newCommit;
        String rootfolderId = commit.getRootFolder().getId();
        String prevCommitSha1 = null;
        if (commit.getPrecedingCommits() != null){
            if(commit.getPrecedingCommits().getPrecedingCommit().size() != 0){
                PrecedingCommits.PrecedingCommit prevCommit = commit.getPrecedingCommits().getPrecedingCommit().get(0);
                singleCommit = xmlData.getCommitMap().get(prevCommit.getId());
                prevCommitSha1 = recursiveSha1PrevCommitBuilder(singleCommit, xmlData);
            }
        }
        fof = recursiveXmlCommitBuilder(xmlData, rootfolderId, false);
        newCommit = new Commit(fof.getSha1(), prevCommitSha1, null, commit.getMessage(), commit.getAuthor(), new DateAndTime(commit.getDateOfCreation()));
        objList.put(newCommit.getSha1(), newCommit);
        return newCommit.getSha1();
    }

    private Fof recursiveXmlCommitBuilder(XmlData xmlData, String ID, boolean isBlob) {
        ArrayList<Fof> foflst = new ArrayList<>();
        String name;
        String username;
        String lastmodifed;
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
            lastmodifed = xmlData.getFolderMap().get(ID).getLastUpdateDate();
        } else {
            content = xmlData.getBlobMap().get(ID).getContent();
            obj = new Blob(content);
            name = xmlData.getBlobMap().get(ID).getName();
            username = xmlData.getBlobMap().get(ID).getLastUpdater();
            lastmodifed = xmlData.getBlobMap().get(ID).getLastUpdateDate();
        }
        objList.put(obj.getSha1(), obj);
        return new Fof(obj.getSha1(), name, isBlob, username, new DateAndTime(lastmodifed));

    }

    public void deleteThisBranch(String input) throws DeleteHeadBranchException, NoSuchBranchException {
        boolean found = false;
        Branch br =null;
        if (input.equals(headBranch.getName()))
            throw new DeleteHeadBranchException();
        for (Branch branch : branches) {
            if (branch.getName().equals(input)) {
                br=branch;
                File f = new File(path + "/.magit/branches/" + branch.getName());
                f.delete();
                found = true;
            }
        }
        if (!found)
            throw new NoSuchBranchException();
        branches.remove(br);
    }

    public List<Commit> getBranchCommits(Branch branch){
        List<Commit> res = new ArrayList<>();
        Commit commit = ((Commit) objList.get(branch.getSha1()));
        while (commit != null) {
            res.add(commit);
            if (commit.getPreviousCommitSha1() != null){
                commit = (Commit) objList.get(commit.getPreviousCommitSha1());
            }
            else
                commit = null;
        }
        return res;
    }

    public boolean hasCommitInHead() {
        if (headBranch == null)
            return false;
        return (headBranch.getSha1() != null);
    }

    public void resetBranch(Commit commit) throws IOException {
        headBranch.UpdateSha1(commit.getSha1());
        makeFileForBranch(headBranch.getSha1(),headBranch.getName());
        deleteWCfiles(this.path);
        deployCommit(commit,this.path);
    }

    public ArrayList<Commit> getCommits() {
        ArrayList<Commit> commitLst = new ArrayList<>();
        for(Map.Entry<String,MagitObject> entry:objList.entrySet()){
            if(entry.getValue() instanceof Commit){
                commitLst.add((Commit)entry.getValue());
            }
        }
        Collections.sort(commitLst);
        return commitLst;
    }

    public Branch getHeadBranch() {
        return headBranch;
    }
}


