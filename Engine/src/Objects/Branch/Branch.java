package Objects.Branch;


public class Branch {
    private String Sha1OfCommit;
    private String nameOfBranch;

    public Branch(String sha1, String _nameOfBranch) {
        Sha1OfCommit = sha1;
        nameOfBranch = _nameOfBranch;
    }

    public String getName() {
        return nameOfBranch;
    }

    public String getSha1() {
        return Sha1OfCommit;
    }

    public void UpdateSha1(String sha1) {
        Sha1OfCommit = sha1;
    }
}
