package Merge;

import java.util.Arrays;
import java.util.Optional;

public class MergeCase {
    String baseContent,targetContent,ancestorContent;
    Optional<MergeCases> mergecases;
    public String getBaseContent() {
        return baseContent;
    }

    public String getTargetContent() {
        return targetContent;
    }

    public String getAncestorContent() {
        return ancestorContent;
    }


    public MergeCase(Optional<MergeCases> mergecases,String...args)
    {
        this.mergecases=mergecases;
        baseContent=args[0];
        targetContent=args[1];
        ancestorContent=args[2];
    }


    public static Optional<MergeCases> caseIs(boolean existsInBase, boolean existsInTarget, boolean existsInAncestor, boolean baseEqualsTargetSha1, boolean targetEqualsAncestorSha1, boolean baseEqualsAncestorSha1){

        return Arrays.stream(MergeCases.values()).filter(mc->mc.whichCaseIsIt(existsInBase,  existsInTarget,  existsInAncestor,  baseEqualsTargetSha1,  targetEqualsAncestorSha1, baseEqualsAncestorSha1)).findFirst();
    }
}
