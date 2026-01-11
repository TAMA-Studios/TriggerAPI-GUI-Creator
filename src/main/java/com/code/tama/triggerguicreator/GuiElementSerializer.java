package com.code.tama.triggerguicreator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

// Custom serializer to only include relevant fields for each element type
class GuiElementSerializer implements JsonSerializer<GuiElement> {
    public JsonElement serialize(GuiElement el, java.lang.reflect.Type type, JsonSerializationContext ctx) {
        JsonObject obj = new JsonObject();
        // Common fields
        obj.addProperty("type", el.type);
        obj.addProperty("id", el.id);
        if (el.x != 0) obj.addProperty("x", el.x);
        if (el.y != 0) obj.addProperty("y", el.y);
        if (el.width != 0) obj.addProperty("width", el.width);
        if (el.height != 0) obj.addProperty("height", el.height);
        if (el.tooltip != null) obj.add("tooltip", ctx.serialize(el.tooltip));

        // Type-specific fields
        switch (el.type) {
            case "button" -> {
                addIfNotNull(obj, "texture", el.texture);
                if (el.textureX != 0) obj.addProperty("texture_x", el.textureX);
                if (el.textureY != 0) obj.addProperty("texture_y", el.textureY);
                if (el.hoverTextureY != 0) obj.addProperty("hover_texture_y", el.hoverTextureY);
                if (el.imageWidth != 256) obj.addProperty("image_width", el.imageWidth);
                if (el.imageHeight != 256) obj.addProperty("image_height", el.imageHeight);
                addIfNotNull(obj, "script", el.script);
            }
            case "text" -> {
                addIfNotNull(obj, "text", el.text);
                if (el.color != 4210752) obj.addProperty("color", el.color);
                if (el.scale != 1.0f) obj.addProperty("scale", el.scale);
                if (el.shadow) obj.addProperty("shadow", el.shadow);
            }
            case "image" -> {
                addIfNotNull(obj, "texture", el.texture);
                if (el.textureX != 0) obj.addProperty("texture_x", el.textureX);
                if (el.textureY != 0) obj.addProperty("texture_y", el.textureY);
                if (el.imageWidth != 256) obj.addProperty("image_width", el.imageWidth);
                if (el.imageHeight != 256) obj.addProperty("image_height", el.imageHeight);
            }
            case "item" -> {
                addIfNotNull(obj, "item", el.item);
            }
            case "progress_bar" -> {
                addIfNotNull(obj, "progress_script", el.progressScript);
                if (el.barColor != 16711680) obj.addProperty("bar_color", el.barColor);
                if (el.backgroundColor != 8421504) obj.addProperty("background_color", el.backgroundColor);
                if (el.borderColor != 0) obj.addProperty("border_color", el.borderColor);
                if (el.showPercentage) obj.addProperty("show_percentage", el.showPercentage);
                if (el.vertical) obj.addProperty("vertical", el.vertical);
            }
            case "slider" -> {
                if (el.minValue != 0) obj.addProperty("min_value", el.minValue);
                if (el.maxValue != 100) obj.addProperty("max_value", el.maxValue);
                if (el.defaultValue != 50) obj.addProperty("default_value", el.defaultValue);
                if (el.step != 1) obj.addProperty("step", el.step);
                if (el.sliderColor != 11184810) obj.addProperty("slider_color", el.sliderColor);
                if (el.handleColor != 9211020) obj.addProperty("handle_color", el.handleColor);
                if (!el.showValue) obj.addProperty("show_value", el.showValue);
                addIfNotNull(obj, "on_change_script", el.onChangeScript);
            }
            case "entity" -> {
                addIfNotNull(obj, "entity_type", el.entityType);
                if (el.entityScale != 30) obj.addProperty("entity_scale", el.entityScale);
                if (!el.rotateEntity) obj.addProperty("rotate_entity", el.rotateEntity);
                addIfNotNull(obj, "entity_nbt", el.entityNbt);
            }
            case "text_box" -> {
                if (el.maxLength != 32) obj.addProperty("max_length", el.maxLength);
                addIfNotNull(obj, "hint_text", el.hintText);
                if (el.textColor != 16777215) obj.addProperty("text_color", el.textColor);
                if (el.hintColor != 8421504) obj.addProperty("hint_color", el.hintColor);
                if (el.multiline) obj.addProperty("multiline", el.multiline);
                addIfNotNull(obj, "on_text_change_script", el.onTextChangeScript);
            }
            case "dropdown" -> {
                if (el.options != null) obj.add("options", ctx.serialize(el.options));
                if (el.defaultOption != 0) obj.addProperty("default_option", el.defaultOption);
                if (el.dropdownColor != 16777215) obj.addProperty("dropdown_color", el.dropdownColor);
                if (el.selectedColor != 65280) obj.addProperty("selected_color", el.selectedColor);
                if (el.textColor != 0) obj.addProperty("text_color", el.textColor);
                addIfNotNull(obj, "on_select_script", el.onSelectScript);
            }
            case "switch" -> {
                if (el.defaultState) obj.addProperty("default_state", el.defaultState);
                if (el.onColor != 65280) obj.addProperty("on_color", el.onColor);
                if (el.offColor != 16711680) obj.addProperty("off_color", el.offColor);
                addIfNotNull(obj, "on_toggle_script", el.onToggleScript);
            }
            case "checkbox" -> {
                if (el.defaultState) obj.addProperty("default_state", el.defaultState);
                addIfNotNull(obj, "checked_texture", el.checkedTexture);
                addIfNotNull(obj, "unchecked_texture", el.uncheckedTexture);
                if (el.checkColor != 65280) obj.addProperty("check_color", el.checkColor);
                addIfNotNull(obj, "label", el.label);
                if (el.labelColor != 4210752) obj.addProperty("label_color", el.labelColor);
                addIfNotNull(obj, "on_toggle_script", el.onToggleScript);
            }
        }

        return obj;
    }

    private void addIfNotNull(JsonObject obj, String key, String value) {
        if (value != null && !value.isEmpty()) {
            obj.addProperty(key, value);
        }
    }
}
