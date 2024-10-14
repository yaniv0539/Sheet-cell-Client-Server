package dto;

import engine.jaxb.generated.STLLayout;
import sheet.api.SheetGetters;
import sheet.cell.api.CellGetters;
import sheet.coordinate.api.Coordinate;

import java.util.*;

public class SheetDto {
    public String name;
    public LayoutDto layout;
    public int version;
    public Map<String, CellDto> activeCells;
    public Set<RangeDto> ranges;

    public SheetDto(SheetGetters sheet) {
        this.name = sheet.getName();
        this.version = sheet.getVersion();
        this.layout = new LayoutDto(sheet.getLayout());
        this.activeCells = new HashMap<>();
        this.ranges = new HashSet<>();

        sheet.getActiveCells().forEach((coordinate, cell) -> activeCells.put(coordinate.toString(), new CellDto(cell)));
        activeCells.values().forEach(CellDto::setInfluenceOn);
        sheet.getRanges().forEach(range-> ranges.add(new RangeDto(range)));
    }

    public Map<String, CellDto> getActiveCells() {
        return Collections.unmodifiableMap(activeCells);
    }

    public LayoutDto getLayout() {
        return layout;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public Set<RangeDto> getRanges() {
        return ranges;
    }
    //    public static void main(String[] args) {
//        //test
//        Engine  e = EngineImpl.create();
//        String file = "C:/Users/itayr/OneDrive/Desktop/second year/java/shticell_client_server/Engine/src/engine/jaxb/resources/grades.xml";
//        e.readXMLInitFile(file);
//        SheetDto dto = new SheetDto(e.getSheetStatus());
//        System.out.println(dto.name);
//        System.out.println(dto.layout.rows);
//        System.out.println(dto.layout.columns);
//
//        dto.activeCells.forEach((coordinate, cell) -> {
//            System.out.println(coordinate +" ="+ cell.effectiveValue);
//            cell.influenceFrom.forEach(cellDto -> System.out.println(cellDto.coordinate));
//            cell.influenceOn.forEach(cellDto -> System.out.println(cellDto.coordinate));
//        });
//        dto.ranges.forEach(range -> System.out.println(range.name));
//
//    }
}
