package Merge;

public enum MergeCases {
    FileCreatedInBase(true,false,false,false,false,false),
    FileCreatedInTarget(false,true,false,false,false,false);

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
        return (existsInBase || existsInTarget || existsInAncestor) &&
                !baseEqualsTargetSha1 && !targetEqualsAncestorSha1 && !baseEqualsAncestorSha1;
    }


}