package dto.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import dto.CoordinateDto;

import java.lang.reflect.Type;
import java.util.Map;

public class CoordinateMapSerializer implements JsonSerializer<Map<CoordinateDto, CoordinateDto>> {
    @Override
    public JsonElement serialize(Map<CoordinateDto, CoordinateDto> map, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        for (Map.Entry<CoordinateDto, CoordinateDto> entry : map.entrySet()) {
            // Convert the key (CoordinateDto) to a JSON string
            String key = context.serialize(entry.getKey()).toString();  // Serialize the key
            JsonElement value = context.serialize(entry.getValue());     // Serialize the value
            jsonObject.add(key, value);
        }
        return jsonObject;
    }
}
