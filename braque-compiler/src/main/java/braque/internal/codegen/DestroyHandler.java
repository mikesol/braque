package braque.internal.codegen;

import javax.lang.model.element.AnnotationMirror;

/**
 * Handles @Destroy annotations.
 * Created by mikesolomon on 11/09/16.
 */

class DestroyHandler extends AbstractRestHandler {

    DestroyHandler() {
        super(Utils.DESTROY);
    }

    @Override
    public boolean isVisitable(AnnotationMirror annotationMirror) {
        return Utils.isDestroy(annotationMirror);
    }

    @Override
    public String getRestInterface() {
        return "RESTDestroy";
    }
}
