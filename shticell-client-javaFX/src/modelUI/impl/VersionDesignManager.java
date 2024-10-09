package modelUI.impl;

import app.AppController;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import sheet.coordinate.api.Coordinate;

import java.util.HashMap;
import java.util.Map;

public class VersionDesignManager {

    int numberOfVersions;
    AppController mainController;
    Map<Integer,Map<Integer,TextFieldDesign>> cellsDesigns;
    Map<Integer,Map<Integer,Integer>> rowsLayouts;
    Map<Integer,Map<Integer,Integer>> columnsLayouts;

    public void clear() {
        cellsDesigns.clear();
        rowsLayouts.clear();
        columnsLayouts.clear();
        numberOfVersions = 1;
        cellsDesigns = new HashMap<>();
        rowsLayouts = new HashMap<>();
        columnsLayouts = new HashMap<>();

        cellsDesigns.put(numberOfVersions,new HashMap<>());
        rowsLayouts.put(numberOfVersions,new HashMap<>());
        columnsLayouts.put(numberOfVersions,new HashMap<>());
    }

    public class VersionDesign {

        Map<Integer,TextFieldDesign> cellDesignsVersion;
        Map<Integer,Integer> rowsLayoutVersion;
        Map<Integer,Integer> columnsLayoutVersion;

        public VersionDesign(int Version) {
            cellDesignsVersion = cellsDesigns.get(Version);
            rowsLayoutVersion = rowsLayouts.get(Version);
            columnsLayoutVersion = columnsLayouts.get(Version);
        }

        public Map<Integer, Integer> getColumnsLayoutVersion() {
            return columnsLayoutVersion;
        }

        public Map<Integer, TextFieldDesign> getCellDesignsVersion() {
            return cellDesignsVersion;
        }

        public Map<Integer, Integer> getRowsLayoutVersion() {
            return rowsLayoutVersion;
        }
    }

    public VersionDesignManager() {
        numberOfVersions = 1;
        cellsDesigns = new HashMap<>();
        rowsLayouts = new HashMap<>();
        columnsLayouts = new HashMap<>();

        cellsDesigns.put(numberOfVersions,new HashMap<>());
        rowsLayouts.put(numberOfVersions,new HashMap<>());
        columnsLayouts.put(numberOfVersions,new HashMap<>());
    }
    public void saveVersionDesign(GridPane CurrGridPane) {
        setCellsDesign(CurrGridPane);
        setRowsLayouts(CurrGridPane);
        setColumnsLayouts(CurrGridPane);
    }

    public void addVersion(){
        numberOfVersions++;
        cellsDesigns.put(numberOfVersions, new HashMap<>(cellsDesigns.get(numberOfVersions-1)));
        rowsLayouts.put(numberOfVersions, new HashMap<>(rowsLayouts.get(numberOfVersions-1)));
        columnsLayouts.put(numberOfVersions, new HashMap<>(columnsLayouts.get(numberOfVersions-1)));
    }


    private void setColumnsLayouts(GridPane gridPane) {
        Map<Integer,Integer> columnToWidth = new HashMap<>(columnsLayouts.get(numberOfVersions));
        for (int i = 0; i < gridPane.getColumnConstraints().size(); i++) {
            columnToWidth.put(i,(int) gridPane.getColumnConstraints().get(i).getPrefWidth());
        }
        columnsLayouts.put(numberOfVersions,columnToWidth);
    }

    private void setRowsLayouts(GridPane gridPane) {
        Map<Integer,Integer> rowToHeight = new HashMap<>(rowsLayouts.get(numberOfVersions));

        for (int i = 0; i < gridPane.getRowConstraints().size(); i++) {
            //rowToHeight.put(i,(int) gridPane.getRowConstraints().get(i).getPrefHeight());
            rowToHeight.put(i,(int) gridPane.getRowConstraints().get(i).getPrefHeight());
        }
        rowsLayouts.put(numberOfVersions,rowToHeight);
    }

    private void setCellsDesign(GridPane gridPane) {

        Map<Integer,TextFieldDesign> IndexNodeToCellDesign = new HashMap<>(cellsDesigns.get(numberOfVersions));
        for (int i = 0; i < gridPane.getChildren().size(); i++) {
            if (gridPane.getChildren().get(i) instanceof TextField tf) {
                //IndexNodeToCellDesign.put(i,new TextFieldDesign(mainController.getBackground(tf),tf.getStyle(),tf.getAlignment()));
                IndexNodeToCellDesign.put(i,new TextFieldDesign(mainController.getBackground(tf),tf.getStyle(),tf.getAlignment()));
            }
        }
        cellsDesigns.put(numberOfVersions,IndexNodeToCellDesign);
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
    }

    public VersionDesign getVersionDesign(int version){
        return new VersionDesign(version);
    }
//    public static <K, V> Map<K, V> deepCopy(Map<K, V> originalMap) {
//        // Create a new map and populate it with copies of the original map's entries
//        Map<K, V> copiedMap = new HashMap<>();
//        for (Map.Entry<K, V> entry : originalMap.entrySet()) {
//            // Assuming K and V are immutable, we can copy the references.
//            // If K or V is mutable, you would need a custom deep copy logic here.
//            copiedMap.put(entry.getKey(), entry.getValue());
//        }
//        return copiedMap;
//    }

}
