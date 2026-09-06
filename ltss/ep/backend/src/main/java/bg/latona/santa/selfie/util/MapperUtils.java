package bg.latona.santa.selfie.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MapperUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private <T> String serializeToJSON(T type) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(type);
        log.info("Successfully serialized object of type {} to JSON", type.getClass().getSimpleName());

        return json;
    }

    public <T> T deserializeJson(String json, Class<T> type) throws JsonProcessingException {
        T result = objectMapper.readValue(json, type);
        log.info("Successfully deserialized JSON to object of type {}", type.getSimpleName());

        return result;
    }

    public <T> T deserializeJson(String json, JavaType javaType)
            throws JsonProcessingException {

        T result = objectMapper.readValue(json, javaType);
        log.info("Successfully deserialized JSON to type {}", javaType);
        return result;
    }

    public <S, T> T map(S source, Class<T> targetType) {
        T result = objectMapper.convertValue(source, targetType);
        log.info("Successfully mapped object of type {} to {}", source.getClass().getSimpleName(), targetType.getSimpleName());

        return result;
    }

    public <T> JavaType createListType(Class<T> elementType) {
        return objectMapper.getTypeFactory()
                .constructCollectionType(List.class, elementType);
    }
}
