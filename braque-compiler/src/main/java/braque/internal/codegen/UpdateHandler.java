package braque.internal.codegen;

import javax.lang.model.element.AnnotationMirror;

/**
 * Handles @Update annotation
 * Created by mikesolomon on 11/09/16.
 */

class UpdateHandler extends AbstractRestHandler {

    UpdateHandler() {
        super(Utils.UPDATE);
    }

    @Override
    public boolean isVisitable(AnnotationMirror annotationMirror) {
        return Utils.isUpdate(annotationMirror);
    }

    @Override
    public String getRestInterface() {
        return "RESTUpdate";
    }
}
