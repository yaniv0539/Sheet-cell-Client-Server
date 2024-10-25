package dto.serializer;

import com.google.gson.*;
import dto.CellDto;

import java.lang.reflect.Type;

public class CellDtoSerializer implements JsonSerializer<CellDto> {
    @Override
    public JsonElement serialize(CellDto src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject cellOuter = new JsonObject();
        cellOuter.add("coordinate",context.serialize(src.coordinate()));
        cellOuter.addProperty("version",src.version());
        cellOuter.addProperty("originalValue",src.originalValue());
        cellOuter.addProperty("effectiveValue",src.effectiveValue());

        JsonArray array = new JsonArray();

        src.influenceFrom().forEach(cellDto -> array.add(serialize(cellDto, typeOfSrc, context)));
        cellOuter.add("influenceFrom", array);

        return cellOuter;
    }
}
