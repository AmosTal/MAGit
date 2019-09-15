package Merge;

import java.util.Arrays;
import java.util.Optional;

public class MergeCase {
    public static Optional<MergeCases> caseIs(boolean existsInBase, boolean existsInTarget, boolean existsInAncestor, boolean baseEqualsTargetSha1, boolean targetEqualsAncestorSha1, boolean baseEqualsAncestorSha1){
        return Arrays.stream(MergeCases.values()).filter(mc->mc.whichCaseIsIt(existsInBase,  existsInTarget,  existsInAncestor,  baseEqualsTargetSha1,  targetEqualsAncestorSha1,  baseEqualsAncestorSha1)).findFirst();
    }
}
