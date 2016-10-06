package braque.internal.codegen;

import javax.lang.model.element.AnnotationMirror;

/**
 * Processes @Create annotations.
 * Created by mikesolomon on 11/09/16.
 */

public class CreateHandler extends AbstractRestHandler {

    public CreateHandler() {
        super(Utils.CREATE);
    }

    @Override
    public boolean isVisitable(AnnotationMirror annotationMirror) {
        return Utils.isCreate(annotationMirror);
    }

    @Override
    public String getRestInterface() {
        return "RESTCreate";
    }
}
