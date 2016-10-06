package braque;

/**
 * Created by mikesolomon on 06/10/16.
 */
public interface Deserializer {
    <T extends braque.RESTEndpoint> java.util.List<T> deserialize(java.util.Map<String, Object> serialized, Class<T> type);
    <T extends braque.RESTEndpoint> java.util.List<T> deserialize(java.util.Map<String, Object> serialized, Class<T> type, java.util.Collection<String> remainingPaths);
}
