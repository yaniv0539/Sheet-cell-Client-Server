package component.main.center.dashboard.model;

public class SheetTableLine {
    private String userName;
    private String sheetName;
    private String layout;
    private String permission; //maybe enum ??

    public SheetTableLine(String userName, String sheetName, String layout, String permission) {
        this.userName = userName;
        this.sheetName = sheetName;
        this.layout = layout;
        this.permission = permission;
    }

    public String getUserName() {
        return userName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getLayout() {
        return layout;
    }

    public String getPermission() {
        return permission;
    }
}
