package sheet.layout.size.impl;

import sheet.layout.size.api.Size;

import java.io.Serializable;

public class SizeImpl implements Size, Serializable {
    private int width;
    private int height;

    private SizeImpl(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public static SizeImpl create(int width, int height) {
        return new SizeImpl(width, height);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void setWidth(int width) {
        if (!isValidWidth(width))
            throw new IllegalArgumentException(width + " is not a valid width, it must be a positive integer");
        this.width = width;
    }

    @Override
    public void setHeight(int height) {
        if (!isValidHeight(height))
            throw new IllegalArgumentException(height + " is not a valid width, it must be a positive number");
        this.height = height;
    }

    private static boolean isValidWidth(int width) {
        return width > 0;
    }

    private static boolean isValidHeight(int height) {
        return height > 0;
    }
}
