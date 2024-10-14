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

        CellDto cellDto = new CellDto();
        cellDto.coordinate = coordinate;
        cellDto.version = json.getAsJsonObject().get("version").getAsInt();
        cellDto.originalValue = json.getAsJsonObject().get("originalValue").getAsString();
        cellDto.effectiveValue = json.getAsJsonObject().get("effectiveValue").getAsString();
        cellDto.influenceOn = new HashSet<>();
        cellDto.influenceFrom = new HashSet<>();

        //adding to reference;
        referenceCache.put(coordinate, cellDto);

        json.getAsJsonObject().get("influenceFrom").getAsJsonArray()
                .forEach(jsonElement -> {

                    JsonObject asJsonObject = jsonElement.getAsJsonObject();

                    CellDto influencer = deserialize(asJsonObject,typeOfT,context);
                    cellDto.influenceFrom.add(influencer);
                    influencer.influenceOn.add(cellDto);
                });

        return cellDto;
    }
}
