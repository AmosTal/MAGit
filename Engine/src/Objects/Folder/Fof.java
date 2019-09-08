package Objects.Folder;

import Objects.Date.DateAndTime;

import java.io.Serializable;


public class Fof implements Serializable,Comparable {
    private String sha1;
    private String name;
    private Boolean isBlob;
    private String nameOfModifier;
    private DateAndTime dateAndTime;

    public Fof(String _sha1, String _name, Boolean _isBlob, String _nameOfModifier, DateAndTime _dateAndTime) {
        sha1 = _sha1;
        name = _name;
        isBlob = _isBlob;
        nameOfModifier = _nameOfModifier;
        dateAndTime = _dateAndTime;
    }

    public Boolean getIsBlob() {
        return isBlob;
    }

    public String getContent() {
        return sha1 + name + isBlob + nameOfModifier;
    }

    public String getInfo() {
        return "\nType :" + (isBlob ? "Blob" : "Folder") + "\nSha1 :" + sha1 +
                "\nName Of Modifier :" + nameOfModifier + "\nDate :" + dateAndTime.getDate();

    }

    public String getSha1() {
        return sha1;
    }

    public String getName() {
        return name;
    }

    public String getNameOfModifier() {
        return nameOfModifier;
    }

    @Override
    public int compareTo(Object fof) {
        return ((Fof)fof).sha1.compareTo(sha1);
    }
}
