package dto.deserializer;

import com.google.gson.*;
import dto.CellDto;
import dto.CoordinateDto;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CellDtoDeserializer implements JsonDeserializer<CellDto> {

    private final Map<CoordinateDto, CellDto> referenceCache = new HashMap<>();

    @Override
    public CellDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        CoordinateDto coordinate = context.deserialize(json.getAsJsonObject().get("coordinate"), CoordinateDto.class);

        // Check if the cell with this coordinate is already deserialized
        if (referenceCache.containsKey(coordinate)) {
            return referenceCache.get(coordinate);
        }

        int version = json.getAsJsonObject().get("version").getAsInt();
        String updateBy = json.getAsJsonObject().get("updateBy").getAsString();
        String originalValue = json.getAsJsonObject().get("originalValue").getAsString();
        String effectiveValue = json.getAsJsonObject().get("effectiveValue").getAsString();
        Set<CellDto> influenceOn = new HashSet<>();
        Set<CellDto> influenceFrom = new HashSet<>();

        // Temporary cell for reference before deserializing influenceFrom
        CellDto cellDto = new CellDto(coordinate, version,updateBy, originalValue, effectiveValue, influenceOn, influenceFrom);
        referenceCache.put(coordinate, cellDto);  // Add the newly created cell to the cache

        // Deserialize influenceFrom, ensuring we don't create recursive loops
        json.getAsJsonObject().get("influenceFrom").getAsJsonArray()
                .forEach(jsonElement -> {
                    JsonObject asJsonObject = jsonElement.getAsJsonObject();

                    // Before deserializing, check if the referenced cell is already in the cache
                    CoordinateDto influencerCoordinate = context.deserialize(asJsonObject.get("coordinate"), CoordinateDto.class);
                    CellDto influencer;

                    if (referenceCache.containsKey(influencerCoordinate)) {
                        // Use cached version to avoid recursive deserialization
                        influencer = referenceCache.get(influencerCoordinate);
                    } else {
                        // Deserialize and add the influencer to cache
                        influencer = deserialize(asJsonObject, typeOfT, context);
                    }

                    // Add the influencer relationship
                    influenceFrom.add(influencer);
                    influencer.influenceOn().add(cellDto);
                });

        return cellDto;
    }
}
