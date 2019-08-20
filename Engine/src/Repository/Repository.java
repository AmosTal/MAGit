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


import java.io.*;

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
        File Head = new File(path + "/.magit/branches/HEAD");
        Head.createNewFile();
    }

    public void createFiles() {
        createZippedFilesForMagitObjects();
        createFilesForBranches();
    }

    private void createFilesForBranches() {
        makeFileForBranch(headBranch, "HEAD");
        for (Branch branch : branches) {
            makeFileForBranch(branch, branch.getName());
        }

    }

    private void makeFileForBranch(Branch branch, String name) {
        try {
            PrintWriter out = new PrintWriter(this.path + "/.magit/branches/" + name);
            out.println(branch.getSha1());
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
        this.readBranches();//k
    }

    private void readBranches() {
        File folder = new File(this.path + "/.magit/branches");
        try {
            for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                FileReader fr = new FileReader(fileEntry);
                BufferedReader br = new BufferedReader(fr);
                if (!fileEntry.getName().equals("HEAD"))
                    this.branches.add(new Branch(br.readLine(), fileEntry.getName()));
                else
                    headBranch = new Branch(br.readLine(), "master");
                br.close();
                fr.close();
            }
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

        String sha1OfRoot = recursiveWcToObjectBuilder(this.path, true, username).getSha1();

        Commit commit;
        if (headBranch != null && headBranch.getSha1() != null)
            commit = new Commit(sha1OfRoot, headBranch.getSha1(), null, msg, username);//fix prev commit sha1
        else {
            commit = new Commit(sha1OfRoot, msg, username);
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
        String brReadLine;
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
                FileReader fr = new FileReader(new File(_path));
                BufferedReader br = new BufferedReader(fr);
                brReadLine = br.readLine();
                if (brReadLine == null)
                    brReadLine = "";
                obj = new Blob(brReadLine);
                br.close();
                fr.close();

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


    public void showCommitFiles() {
        {
            String rootFoldersha1 = ((Commit) objList.get(headBranch.getSha1())).getRootFolderSha1();
            recursivePrintAllCommitFiles(rootFoldersha1, path);
        }

    }

    private void recursivePrintAllCommitFiles(String folderSha1, String _path) {
        String newPath;
        for (Fof fof : ((Folder) objList.get(folderSha1)).getFofList()) {
            newPath = _path + "\\" + fof.getName();
            if (!fof.getIsBlob())
                recursivePrintAllCommitFiles(fof.getSha1(), newPath);
            ModuleTwo.printLine(newPath + fof.getInfo());
        }
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

    public void addNewBranch(String name) throws AlreadyExistingBranchException {
        if (branches.stream().filter(Branch -> Branch.getName().equals(name)).findFirst().orElse(null) == null) {
            Branch newBranch = new Branch(headBranch.getSha1(), name);
            branches.add(newBranch);
            makeFileForBranch(newBranch, newBranch.getName());
        } else
            throw new AlreadyExistingBranchException();
    }

    public boolean checkDeltaChanges() {
        Map<String, Fof> commitMap;
        if (headBranch.getSha1() != null)
            commitMap = getCommitMap((Commit) objList.get(headBranch.getSha1()));
        else
            commitMap = new HashMap<>();
        currDelta = new Delta(commitMap);
        recursiveWcToObjectBuilder(this.path, false, username);
        return currDelta.getIsChanged();
    }

    public void switchHead(String name) throws NoSuchBranchException {
        Branch branch = branches.stream().filter(Branch -> Branch.getName().equals(name)).findFirst().orElse(null);
        if (branch == null)
            throw new NoSuchBranchException();
        headBranch = branch;
        makeFileForBranch(headBranch, "HEAD");
        deleteWCfiles();
        deployCommit((Commit) objList.get(headBranch.getSha1()));
    }

    private void deployCommit(Commit commit) {
        if (commit != null)
            recursiveObjectToWCBuilder((Folder) objList.get(commit.getRootFolderSha1()), this.path);

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

    private void deleteWCfiles() {
        File file = new File(path);
        for (File fileEntry : Objects.requireNonNull(file.listFiles())) {
            if (!fileEntry.getName().equals(".magit")) {
                fileEntry.delete();
            }
        }
    }

    public void showRepoStatus() {

        ModuleTwo.printLine("The repository name is: " + name);
        ModuleTwo.printLine("Active user: " + username);
        if (!checkDeltaChanges()) {
            ModuleTwo.printLine("No changes were made since last commit");
        } else
            currDelta.showChanges();
    }

    public void showBranches() {
        for (Branch branch : branches) {
            ModuleTwo.printLine("Name: " + branch.getName() + "\nSha1: " + branch.getSha1() + "\nCommit message: " + ((Commit) objList.get(branch.getSha1())).getCommitPurposeMSG() + "\n----");
        }
        ModuleTwo.printLine(("HEAD BRANCH :\nName: " + headBranch.getName() + "\nSha1: " + headBranch.getSha1() + "\nCommit message: ") + ((Commit) objList.get(headBranch.getSha1())).getCommitPurposeMSG() + "\n----");
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
        repo.deployCommit((Commit) repo.objList.get(repo.headBranch.getSha1()));
        return repo;
    }

    private String recursiveSha1PrevCommitBuilder(MagitSingleCommit commit, XmlData xmlData) {
        Fof fof;
        MagitSingleCommit singleCommit;
        Commit newCommit;
        String rootfolderId = commit.getRootFolder().getId();
        String prevCommitSha1;
        if (commit.getPrecedingCommits().getPrecedingCommit().size() != 0) {
            PrecedingCommits.PrecedingCommit prevCommit = commit.getPrecedingCommits().getPrecedingCommit().get(0);
            singleCommit = xmlData.getCommitMap().get(prevCommit.getId());
            prevCommitSha1 = recursiveSha1PrevCommitBuilder(singleCommit, xmlData);
        } else
            prevCommitSha1 = null;
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
        for (Branch branch : branches) {
            if (branch.getName().equals(input)) {
                if (branch.getName().equals(headBranch.getName()))
                    throw new DeleteHeadBranchException();
                branches.remove(branch);
                File f = new File(path + "/.magit/branches/" + branch.getName());
                f.delete();
                found = true;
            }
        }
        if (!found)
            throw new NoSuchBranchException();
    }

    public void showBranchHistory() {
        Commit commit = ((Commit) objList.get(headBranch.getSha1()));
        while (commit != null) {
            ModuleTwo.printLine(commit.getInfo());
            if (commit.getPreviousCommitSha1() != null)
                commit = (Commit) objList.get(commit.getPreviousCommitSha1());
            else
                commit = null;

        }
    }

    public boolean hasCommitInHead() {
        return (headBranch.getSha1() != null);

    }
}


