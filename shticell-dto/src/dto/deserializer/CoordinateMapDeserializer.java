package dto.deserializer;

import com.google.gson.*;
import dto.CoordinateDto;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CoordinateMapDeserializer implements JsonDeserializer<Map<CoordinateDto, CoordinateDto>> {
    @Override
    public Map<CoordinateDto, CoordinateDto> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Map<CoordinateDto, CoordinateDto> map = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            // Deserialize the key and value
            CoordinateDto key = context.deserialize(JsonParser.parseString(entry.getKey()), CoordinateDto.class);
            CoordinateDto value = context.deserialize(entry.getValue(), CoordinateDto.class);
            map.put(key, value);
        }

        return map;
    }
}
