package XML;

import XMLpackage.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

import java.util.HashMap;

import java.util.Map;

public class XmlData {
    private MagitRepository magitRepository;
    private MagitBranches magitBranches;
    private MagitBlobs magitBlobs;
    private MagitFolders magitFolders;
    private MagitCommits magitCommits;
    private String repositoryPath;

    private Map<String, MagitBlob> blobMap = new HashMap<>();
    private Map<String, MagitSingleFolder> folderMap = new HashMap<>();
    private Map<String, MagitSingleCommit> commitMap = new HashMap<>();

    public Map<String, MagitSingleFolder> getFolderMap() {
        return folderMap;
    }

    public Map<String, MagitSingleCommit> getCommitMap() {
        return commitMap;
    }

    public Map<String, MagitBlob> getBlobMap() {
        return blobMap;
    }

    public MagitRepository getMagitRepository() {
        return magitRepository;
    }


    public XmlData(String XmlPath) throws XmlNotValidException {
        File file = new File(XmlPath);
        try {
            checkIfXmlExists(file);
            JAXBContext jaxbContext = JAXBContext.newInstance(MagitRepository.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            magitRepository = (MagitRepository) jaxbUnmarshaller.unmarshal(file);
            magitBranches = magitRepository.getMagitBranches();
            magitCommits = magitRepository.getMagitCommits();
            magitFolders = magitRepository.getMagitFolders();
            magitBlobs = magitRepository.getMagitBlobs();
            repositoryPath = magitRepository.getLocation();
            makeMaps();
            checkIfXmlIsValid();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }


    private void makeMaps() throws XmlNotValidException {
        for (MagitSingleCommit commit : magitRepository.getMagitCommits().getMagitSingleCommit()) {
            if (commitMap.get(commit.getId()) == null)
                commitMap.put(commit.getId(), commit);
            else
                throw new XmlNotValidException("xml not valid:2 Commits with the same ID");
        }
        for (MagitSingleFolder folder : magitRepository.getMagitFolders().getMagitSingleFolder()) {
            if (folderMap.get(folder.getId()) == null)
                folderMap.put(folder.getId(), folder);
            else
                throw new XmlNotValidException("xml not valid:2 Folders with the same ID");
        }
        for (MagitBlob blob : magitRepository.getMagitBlobs().getMagitBlob()) {
            if (blobMap.get(blob.getId()) == null)
                blobMap.put(blob.getId(), blob);
            else
                throw new XmlNotValidException("xml not valid:2 Blobs with the same ID");
        }
    }

    private void checkIfXmlIsValid() throws XmlNotValidException {
        for (MagitSingleFolder folder : folderMap.values()) {
            for (Item item : folder.getItems().getItem()) {
                if (item.getType().equals("blob")) {
                    if (blobMap.get(item.getId()) == null)
                        throw new XmlNotValidException("xml not valid:Folder points to item(Blob) id that doesn't exist");
                } else {
                    if (folderMap.get(item.getId()) == null)
                        throw new XmlNotValidException("xml not valid:Folder points to item(Folder) id that doesn't exist");
                    if (item.getId().equals(folder.getId()))
                        throw new XmlNotValidException("xml not valid:Folder points to itself");
                }
            }
            for (MagitSingleCommit commit : commitMap.values()) {
                if (folderMap.get(commit.getRootFolder().getId()) == null)
                    throw new XmlNotValidException("xml not valid:Commit points to folder id that doesn't exist");
                if (!folderMap.get(commit.getRootFolder().getId()).isIsRoot())
                    throw new XmlNotValidException("xml not valid:Commit points to a non root folder");
            }
            boolean validHead = false;
            for (MagitSingleBranch branch : magitBranches.getMagitSingleBranch()) {
                if (commitMap.get(branch.getPointedCommit().getId()) == null)
                    throw new XmlNotValidException("xml not valid:Branch points to commit id that doesn't exist");
                if (magitBranches.getHead().equals(branch.getName()))
                    validHead = true;
            }
            if (!validHead)
                throw new XmlNotValidException("xml not valid:Head doesn't point to an existing branch");
        }
    }

    private void checkIfXmlExists(File file) throws XmlNotValidException {
        String[] str = file.getName().split("\\.");
        int len = str.length;
        if (!file.exists() || !str[len - 1].equals("xml"))
            throw new XmlNotValidException("xml file wasn't found");
    }
}
