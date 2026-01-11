package com.code.tama.triggerguicreator;

import java.util.ArrayList;
import java.util.List;

// Data classes
class GuiDefinition {
    public String type = "custom";
    public String title;
    public int width = 176;
    public int height = 166;
    public String backgroundTexture;
    public List<GuiElement> elements = new ArrayList<>();
}
