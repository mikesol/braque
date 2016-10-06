package braque.internal.codegen;

import braque.Operation;
import braque.PropertyManyness;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to store information about properties, used to generate the Fanner.
 * Created by mikesolomon on 20/09/16.
 */

public class PropertyInfo {
    List<Pair<String, Operation>> paths = new ArrayList<>();
    PropertyManyness propertyManyness;
    String propertyName;
    boolean isUID;
}
