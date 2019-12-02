package Merge;

public enum MergeCases {
Case1(true, true,true, true, true, true) {@Override public String takeOursOrTheirs() { return "ours"; }},
Case4(true, true, true, true, false, false){@Override public String takeOursOrTheirs() { return ""; }},
Case6(true, true,true, false, true, false){ @Override public String takeOursOrTheirs() { return "ours"; } },
Case7(true, true, true, false, false, true) {@Override public String takeOursOrTheirs() { return "theirs"; }},
Case8(true, true,true, false, false, false) {@Override public String takeOursOrTheirs() { return ""; }},
Case12(true, true, false, true, false, false)  {@Override public String takeOursOrTheirs() { return "ours"; }},
Case16(true, true, false, false, false, false){ @Override public String takeOursOrTheirs() { return ""; } },
Case23(true, false, true, false, false, true){@Override public String takeOursOrTheirs() { return "delete"; }},
Case24(true, false, true, false, false, false){@Override public String takeOursOrTheirs() { return ""; }},
Case32(true, false, false, false, false, false){@Override public String takeOursOrTheirs() { return "ours"; }},
Case38(false, true, true, false, true, false) {@Override public String takeOursOrTheirs() { return "delete"; }},
Case40(false, true, true, false, false, false){ @Override public String takeOursOrTheirs() { return ""; }},
Case47(false, true, false, false, false, true) {@Override public String takeOursOrTheirs() { return "theirs"; }},
Case48(false, true, false, false, false, false) {@Override public String takeOursOrTheirs() { return "theirs"; }},
Case56(false,false, true, false, false, false){ @Override public String takeOursOrTheirs() { return ""; } };




    private boolean existsInBase,  existsInTarget,  existsInAncestor,  baseEqualsTargetSha1,  targetEqualsAncestorSha1,  baseEqualsAncestorSha1;
MergeCases(boolean existsInBase, boolean existsInTarget, boolean existsInAncestor, boolean baseEqualsTargetSha1, boolean targetEqualsAncestorSha1, boolean baseEqualsAncestorSha1){
    this.existsInBase =existsInBase;
    this.existsInTarget = existsInTarget;
    this.existsInAncestor = existsInAncestor;
    this.baseEqualsTargetSha1 = baseEqualsTargetSha1;
    this.targetEqualsAncestorSha1 = targetEqualsAncestorSha1;
    this.baseEqualsAncestorSha1 = baseEqualsAncestorSha1;
}

    public boolean whichCaseIsIt(boolean existsInBase, boolean existsInTarget, boolean existsInAncestor, boolean baseEqualsTargetSha1, boolean targetEqualsAncestorSha1, boolean baseEqualsAncestorSha1){

        return (existsInBase == this.existsInBase&&
                existsInTarget == this.existsInTarget&&
                existsInAncestor == this.existsInAncestor&&
                baseEqualsTargetSha1 == this.baseEqualsTargetSha1&&
                targetEqualsAncestorSha1 == this.targetEqualsAncestorSha1&&
                baseEqualsAncestorSha1 == this.baseEqualsAncestorSha1);
    }
public abstract String takeOursOrTheirs();

}
