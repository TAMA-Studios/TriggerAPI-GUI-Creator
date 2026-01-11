package com.code.tama.triggerguicreator;

import java.util.List;

public class GuiElement {
    public String type, id;
    // Common
    public int x, y, width, height;
    public List<String> tooltip;

    // Button/Image
    public String texture;
    public int textureX, textureY, hoverTextureY;
    public int imageWidth = 256, imageHeight = 256;
    public String script;

    // Text
    public String text;
    public int color = 4210752;
    public float scale = 1.0f;
    public boolean shadow;

    // Item
    public String item;

    // Progress Bar
    public String progressScript;
    public int barColor = 16711680, backgroundColor = 8421504, borderColor = 0;
    public boolean showPercentage, vertical;

    // Slider
    public float minValue, maxValue = 100, defaultValue = 50, step = 1;
    public int sliderColor = 11184810, handleColor = 9211020;
    public boolean showValue = true;
    public String onChangeScript;

    // Entity
    public String entityType, entityNbt;
    public float entityScale = 30;
    public boolean rotateEntity = true;

    // Text Box
    public int maxLength = 32;
    public String hintText;
    public int textColor = 16777215, hintColor = 8421504;
    public boolean multiline;
    public String onTextChangeScript;

    // Dropdown
    public List<String> options;
    public int defaultOption;
    public int dropdownColor = 16777215, selectedColor = 65280;
    public String onSelectScript;

    // Switch
    public boolean defaultState;
    public int onColor = 65280, offColor = 16711680;
    public String onToggleScript;

    // Checkbox
    public String checkedTexture, uncheckedTexture;
    public int checkColor = 65280;
    public String label;
    public int labelColor = 4210752;
}
