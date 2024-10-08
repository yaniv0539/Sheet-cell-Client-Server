package sheet.layout.api;

import sheet.layout.size.api.SizeGetters;

public interface LayoutGetters {
    SizeGetters getSize();
    int getRows();
    int getColumns();
}
