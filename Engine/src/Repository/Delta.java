package Repository;


import Objects.Folder.Fof;

import java.util.HashMap;
import java.util.Map;

class Delta {


    private Map<String, Fof> updatedFilesFofs; // <Path,Fof>
    private Map<String, Fof> newFilesFofs;
    private Map<String, Fof> deletedFilesFofs;
    private Map<String, Fof> commitMap;
    private boolean isChanged = false;

    Map<String, Fof> getCommitMap() {
        return commitMap;
    }
    Map<String, Fof> getUpdatedFilesFofs() {
        return updatedFilesFofs;
    }
    Map<String, Fof> getNewFilesFofs() {
        return newFilesFofs;
    }

    Map<String, Fof> getDeletedFilesFofs() {
        return deletedFilesFofs;
    }



    Delta(Map<String, Fof> _currentCommitMap) {

        updatedFilesFofs = new HashMap<>();
        newFilesFofs = new HashMap<>();
        deletedFilesFofs = new HashMap<>(_currentCommitMap);
        commitMap = _currentCommitMap;
    }


    void isObjectChanged(Fof fof, String objPath) {
        if (!deletedFilesFofs.containsKey(objPath)) {
            newFilesFofs.put(objPath, fof);
            isChanged = true;
        } else {
            if (!((deletedFilesFofs.get(objPath)).getSha1().equals(fof.getSha1()))) {
                updatedFilesFofs.put(objPath, fof);
                isChanged = true;
            }
            deletedFilesFofs.remove(objPath);

        }
    }


     String showChanges() {
        String linesToPrint = "";
        if (newFilesFofs.entrySet().size() != 0) {
            linesToPrint = linesToPrint.concat("The following files and folders have been created:\n");
            linesToPrint = showChangeInFiles(linesToPrint, newFilesFofs);
        }
        if (updatedFilesFofs.entrySet().size() != 0) {
            linesToPrint = linesToPrint.concat("The following files and folder have been updated:\n");
            linesToPrint = showChangeInFiles(linesToPrint, updatedFilesFofs);
        }
        if (deletedFilesFofs.entrySet().size() != 0) {
            linesToPrint = linesToPrint.concat("The following files and folders have been deleted:\n");
            linesToPrint = showChangeInFiles(linesToPrint, deletedFilesFofs);
        }
        return linesToPrint;
    }

    private String showChangeInFiles(String msg, Map<String, Fof> filesMap) {
        int i = 1;
        for (Map.Entry<String, Fof> entry : filesMap.entrySet()) {
            msg = msg.concat(i + ")" + " Path: " + entry.getKey() + entry.getValue().getInfo())+"\n";
            i++;
        }
        return msg;
    }

    boolean getIsChanged() {
        return isChanged || !deletedFilesFofs.isEmpty();
    }

    String getUsername(String fofpath) {
        Fof fof = commitMap.get(fofpath);
        if (fof == null)
            return null;
        else {
            return fof.getNameOfModifier();
        }
    }
}


