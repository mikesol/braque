package braque.internal.codegen;

import javax.lang.model.element.AnnotationMirror;

/**
 * Handles @Show annotations
 * Created by mikesolomon on 11/09/16.
 */

class ShowHandler extends AbstractRestHandler {

    ShowHandler() {
        super(Utils.SHOW);
    }

    @Override
    public boolean isVisitable(AnnotationMirror annotationMirror) {
        return Utils.isShow(annotationMirror);
    }

    @Override
    public String getRestInterface() {
        return "RESTShow";
    }
}
