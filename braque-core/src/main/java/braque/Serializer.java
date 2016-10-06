package braque;

/**
 * Created by mikesolomon on 06/10/16.
 */
public interface Serializer {
    <T extends braque.RESTEndpoint> java.util.Map<String, Object> serialize(final T deserialized);
}
