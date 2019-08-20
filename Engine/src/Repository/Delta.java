package Repository;


import EngineRunner.ModuleTwo;
import Objects.Folder.Fof;

import java.util.HashMap;
import java.util.Map;

class Delta {

    private Map<String, Fof> updatedFilesFofs; // <Path,Fof>
    private Map<String, Fof> newFilesFofs;
    private Map<String, Fof> deletedFilesFofs;
    private Map<String, Fof> commitMap;
    private boolean isChanged = false;


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


    void showChanges() {
        String linesToPrint;
        if (newFilesFofs.entrySet().size() != 0) {
            linesToPrint = "The following files and folders have been created:";
            showChangeInFiles(linesToPrint, newFilesFofs);
        }
        if (updatedFilesFofs.entrySet().size() != 0) {
            linesToPrint = "The following files and folder have been updated: ";
            showChangeInFiles(linesToPrint, updatedFilesFofs);
        }
        if (deletedFilesFofs.entrySet().size() != 0) {
            linesToPrint = "The following files and folders have been deleted: ";
            showChangeInFiles(linesToPrint, deletedFilesFofs);
        }
    }

    private void showChangeInFiles(String msg, Map<String, Fof> filesMap) {
        int i = 1;
        ModuleTwo.printLine(msg);
        for (Map.Entry<String, Fof> entry : filesMap.entrySet()) {
            ModuleTwo.printLine(i + ")\n" + "Path: " + entry.getKey() + entry.getValue().getInfo());
            i++;
        }
    }

    public boolean getIsChanged() {
        if (isChanged || !deletedFilesFofs.isEmpty())
            return true;
        return false;
    }

    public String getUsername(String fofpath) {
        Fof fof = commitMap.get(fofpath);
        if (fof == null)
            return null;
        else {
            return fof.getNameOfModifier();
        }
    }
}


