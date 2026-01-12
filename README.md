<!DOCTYPE html>

<html lang="en">
<head>
<meta charset="UTF-8">
<title>TriggerAPI GUI Editor Documentation</title>
</head>
<body style="font-family: sans-serif; line-height: 1.6; color: #333; max-width: 900px; margin: 0 auto; padding: 20px;">

```
<header style="border-bottom: 2px solid #4A90E2; padding-bottom: 10px; margin-bottom: 20px;">
    <h1 style="color: #4A90E2; margin-bottom: 5px;">TriggerAPI GUI Editor</h1>
    <p style="font-style: italic; color: #666;">A visual design tool for creating and exporting Minecraft-compatible custom GUI JSON definitions.</p>
</header>

<section>
    <h2 style="color: #2C3E50;">Overview</h2>
    <p>The <strong>TriggerAPI GUI Editor</strong> is a Java Swing-based application designed to help developers visually arrange interface elements. It generates a structured JSON output that defines elements such as buttons, progress bars, and entity displays, specifically tailored for the TriggerAPI framework.</p>
</section>

<section>
    <h2 style="color: #2C3E50;">Core Components</h2>
    <table style="width: 100%; border-collapse: collapse; margin: 20px 0;">
        <thead>
            <tr style="background-color: #f2f2f2;">
                <th style="border: 1px solid #ddd; padding: 12px; text-align: left;">File</th>
                <th style="border: 1px solid #ddd; padding: 12px; text-align: left;">Function</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td style="border: 1px solid #ddd; padding: 12px;"><code>Main.java</code></td>
                <td style="border: 1px solid #ddd; padding: 12px;">The application entry point. Handles the UI layout, rendering canvas, and event logic for dragging/dropping elements.</td>
            </tr>
            <tr>
                <td style="border: 1px solid #ddd; padding: 12px;"><code>GuiDefinition.java</code></td>
                <td style="border: 1px solid #ddd; padding: 12px;">The top-level data structure representing the GUI's global properties (title, dimensions, background).</td>
            </tr>
            <tr>
                <td style="border: 1px solid #ddd; padding: 12px;"><code>GuiElement.java</code></td>
                <td style="border: 1px solid #ddd; padding: 12px;">A versatile class containing all possible attributes for GUI components (Buttons, Sliders, Text Boxes, etc.).</td>
            </tr>
            <tr>
                <td style="border: 1px solid #ddd; padding: 12px;"><code>GuiElementSerializer.java</code></td>
                <td style="border: 1px solid #ddd; padding: 12px;">A custom GSON serializer that ensures only relevant fields are exported to the JSON file based on the element type.</td>
            </tr>
        </tbody>
    </table>
</section>

<section>
    <h2 style="color: #2C3E50;">Features</h2>
    <ul style="list-style-type: square;">
        <li><strong>Visual Canvas:</strong> Drag-and-drop elements within a defined workspace.</li>
        <li><strong>Zoom Controls:</strong> Support for zooming in/out (0.25x to 4.0x) using the toolbar or <code>Ctrl + Mouse Wheel</code>.</li>
        <li><strong>Texture Mapping:</strong> Import custom PNGs and assign resource locations to preview buttons and images.</li>
        <li><strong>Property Inspector:</strong> A dedicated side panel to modify IDs, coordinates, colors (using integer codes), and scripts.</li>
        <li><strong>Clean Export:</strong> Generates "pretty-printed" JSON with minimal bloat.</li>
    </ul>
</section>

<section>
    <h2 style="color: #2C3E50;">Supported Elements</h2>
    <p>The editor supports various Minecraft-centric components:</p>
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
        <div style="background: #f9f9f9; padding: 10px; border-left: 4px solid #4A90E2;">
            <strong>Standard Widgets:</strong>
            <ul>
                <li>Buttons (with hover textures)</li>
                <li>Text (with scale and shadow)</li>
                <li>Images</li>
                <li>Item Slots (for Minecraft items)</li>
            </ul>
        </div>
        <div style="background: #f9f9f9; padding: 10px; border-left: 4px solid #4A90E2;">
            <strong>Advanced Widgets:</strong>
            <ul>
                <li>Entity Display (with NBT support)</li>
                <li>Progress Bars (Vertical/Horizontal)</li>
                <li>Sliders and Dropdowns</li>
                <li>Checkboxes and Switches</li>
            </ul>
        </div>
    </div>
</section>

<section>
    <h2 style="color: #2C3E50;">Usage Guide</h2>
    <ol>
        <li><strong>Creating Elements:</strong> Click any component type in the top toolbar to place it on the canvas.</li>
        <li><strong>Selection:</strong> Click an element on the canvas to select it. It will be highlighted with a yellow border.</li>
        <li><strong>Editing:</strong> Use the right-hand panel to change values. Press <em>Enter</em> in a text field to apply changes to the canvas.</li>
        <li><strong>Textures:</strong> Use <code>Textures -> Import Texture</code>. Assign it a name like <code>modid:textures/gui/bg.png</code>. You can then use this name in the "Texture" or "Background" fields of your elements.</li>
        <li><strong>Saving:</strong> Go to <code>File -> Save JSON</code> to export your work.</li>
    </ol>
</section>

<footer style="margin-top: 40px; padding-top: 10px; border-top: 1px solid #ddd; font-size: 0.9em; color: #888;">
    <p>Developed for TriggerAPI GUI Creation. Requires Java 17+ and the Google GSON library.</p>
</footer>

```

</body>
</html>
