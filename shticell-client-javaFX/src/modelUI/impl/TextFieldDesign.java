package modelUI.impl;

import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TextFieldDesign {

    Color backgroundColor;
    String textStyle;
    Pos textAlignment;

    public TextFieldDesign() {}

    public TextFieldDesign(Color backgroundColor,String textStyle, Pos textAlignment) {
        this.backgroundColor = backgroundColor;
        this.textStyle = textStyle;
        this.textAlignment = textAlignment;
    }

    public String getTextStyle() {
        return textStyle;
    }

    public Pos getTextAlignment() {
        return textAlignment;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setPos(Pos pos) {
        this.textAlignment = pos;
    }

    public void setBackground(Color color) {
        this.backgroundColor = color;
    }

    public void setTextStyle(String style) {
        this.textStyle = style;
    }
}
