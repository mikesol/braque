package braque;

/**
 * Created by mikesolomon on 06/10/16.
 */
public interface Deserializer {
    <T extends braque.RESTOperation> java.util.List<T> deserialize(java.util.Map<String, Object> serialized, Class<T> type);
    <T extends braque.RESTOperation> java.util.List<T> deserialize(java.util.Map<String, Object> serialized, Class<T> type, java.util.Collection<String> remainingPaths);
}
