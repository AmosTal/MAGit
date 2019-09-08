package Objects.Folder;

import Objects.Api.MagitObject;

import java.util.ArrayList;
import java.util.Collections;

public class Folder extends MagitObject {
    private ArrayList<Fof> FofList = null;


    public Folder(ArrayList<Fof> _FofList){
        super(buildContent(_FofList));
        FofList = _FofList;
    }

    public ArrayList<Fof> getFofList(){
        return FofList;
    }
    private static String buildContent (ArrayList<Fof> arr){
        StringBuilder content = new StringBuilder();
        Collections.sort(arr);
        for (Fof fof:arr) {
            content.append(fof.getContent());
        }
        return content.toString();
    }
}
