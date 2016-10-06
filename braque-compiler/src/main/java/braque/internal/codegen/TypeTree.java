package braque.internal.codegen;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * A structure that helps tracing objects' inheritence.
 * Created by mikesolomon on 13/09/16.
 */

class TypeTree {
    TypeTree mSuper = null;
    final List<TypeTree> mSubs = new ArrayList<>();
    final TypeElement mBase;
    TypeTree(final TypeElement base) {
        mBase = base;
    }
}
