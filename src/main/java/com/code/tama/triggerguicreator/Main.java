package com.code.tama.triggerguicreator;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import com.code.tama.triggerguicreator.*;

import com.google.gson.*;

public class Main extends JFrame {
    public final Map<String, BufferedImage> textureRegistry = new HashMap<>();

    private GuiCanvas canvas;
    private JPanel propertiesPanel;
    private GuiDefinition currentGui;
    private GuiElement selectedElement;
    private File lastDirectory;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Main().setVisible(true);
        });
    }

    public Main() {
        setTitle("TriggerAPI GUI Editor");
        setSize(1400, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        currentGui = new GuiDefinition();
        currentGui.width = 176;
        currentGui.height = 166;
        currentGui.elements = new ArrayList<>();

        initComponents();
        updatePropertiesPanel();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        addMenuItem(fileMenu, "New", e -> newGui());
        addMenuItem(fileMenu, "Open JSON", e -> openJson());
        addMenuItem(fileMenu, "Save JSON", e -> saveJson());

        JMenu textureMenu = new JMenu("Textures");
        addMenuItem(textureMenu, "Import Texture", e -> importTexture());
        menuBar.add(textureMenu);

        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Exit", e -> System.exit(0));
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        addToolButton(toolBar, "Button", () -> addElement("button"));
        addToolButton(toolBar, "Text", () -> addElement("text"));
        addToolButton(toolBar, "Image", () -> addElement("image"));
        addToolButton(toolBar, "Item", () -> addElement("item"));
        toolBar.addSeparator();
        addToolButton(toolBar, "Progress", () -> addElement("progress_bar"));
        addToolButton(toolBar, "Slider", () -> addElement("slider"));
        addToolButton(toolBar, "Entity", () -> addElement("entity"));
        toolBar.addSeparator();
        addToolButton(toolBar, "TextBox", () -> addElement("text_box"));
        addToolButton(toolBar, "Dropdown", () -> addElement("dropdown"));
        addToolButton(toolBar, "Switch", () -> addElement("switch"));
        addToolButton(toolBar, "Checkbox", () -> addElement("checkbox"));
        toolBar.addSeparator();
        addToolButton(toolBar, "Delete", this::deleteSelectedElement);

        toolBar.addSeparator();
        addToolButton(toolBar, "Zoom +", () -> canvas.zoomIn());
        addToolButton(toolBar, "Zoom -", () -> canvas.zoomOut());
        addToolButton(toolBar, "Reset", () -> canvas.resetZoom());

        add(toolBar, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(900);

        canvas = new GuiCanvas();
        splitPane.setLeftComponent(new JScrollPane(canvas));

        propertiesPanel = new JPanel();
        propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));
        propertiesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        splitPane.setRightComponent(new JScrollPane(propertiesPanel));

        add(splitPane, BorderLayout.CENTER);
    }

    private void importTexture() {
        JFileChooser chooser = new JFileChooser(lastDirectory);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        lastDirectory = chooser.getCurrentDirectory();

        String rl = JOptionPane.showInputDialog(
                this,
                "Input ResourceLocation of this image\n(e.g. triggerapi:example.png)",
                "Import Texture",
                JOptionPane.QUESTION_MESSAGE
        );

        if (rl == null || rl.isBlank()) return;

        try {
            BufferedImage img = javax.imageio.ImageIO.read(chooser.getSelectedFile());
            textureRegistry.put(rl.trim(), img);
            canvas.repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void addMenuItem(JMenu menu, String text, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        menu.add(item);
    }

    private void addToolButton(JToolBar bar, String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.addActionListener(e -> action.run());
        bar.add(btn);
    }

    private void updatePropertiesPanel() {
        propertiesPanel.removeAll();

        JPanel guiPanel = createPanel("GUI Properties");
        GridBagConstraints gbc = createGBC();

        addField(guiPanel, gbc, 0, "Title:", currentGui.title, t -> { currentGui.title = t; canvas.repaint(); });
        addField(guiPanel, gbc, 1, "Width:", String.valueOf(currentGui.width), t -> {
            try { currentGui.width = Integer.parseInt(t); canvas.updateCanvasSize(); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(guiPanel, gbc, 2, "Height:", String.valueOf(currentGui.height), t -> {
            try { currentGui.height = Integer.parseInt(t); canvas.updateCanvasSize(); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(guiPanel, gbc, 3, "Background:", currentGui.backgroundTexture, t -> { currentGui.backgroundTexture = t; canvas.repaint(); });

        propertiesPanel.add(guiPanel);
        propertiesPanel.add(Box.createVerticalStrut(10));

        if (selectedElement != null) {
            JPanel elemPanel = createPanel("Element - " + selectedElement.type);
            addElementProps(elemPanel);
            propertiesPanel.add(elemPanel);
        }

        propertiesPanel.revalidate();
        propertiesPanel.repaint();
    }

    private JPanel createPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder(title));
        return p;
    }

    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addElementProps(JPanel p) {
        GridBagConstraints gbc = createGBC();
        int row = 0;

        row = addCommon(p, gbc, row);
        row = switch (selectedElement.type) {
            case "button" -> addButton(p, gbc, row);
            case "text" -> addText(p, gbc, row);
            case "image" -> addImage(p, gbc, row);
            case "item" -> addItem(p, gbc, row);
            case "progress_bar" -> addProgress(p, gbc, row);
            case "slider" -> addSlider(p, gbc, row);
            case "entity" -> addEntity(p, gbc, row);
            case "text_box" -> addTextBox(p, gbc, row);
            case "dropdown" -> addDropdown(p, gbc, row);
            case "switch" -> addSwitch(p, gbc, row);
            case "checkbox" -> addCheckbox(p, gbc, row);
            default -> row;
        };
        addTooltip(p, gbc, row);
    }

    private int addCommon(JPanel p, GridBagConstraints gbc, int r) {
        addField(p, gbc, r++, "ID:", selectedElement.id, t -> { selectedElement.id = t; canvas.repaint(); });
        addField(p, gbc, r++, "X:", String.valueOf(selectedElement.x), t -> {
            try { selectedElement.x = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Y:", String.valueOf(selectedElement.y), t -> {
            try { selectedElement.y = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Width:", String.valueOf(selectedElement.width), t -> {
            try { selectedElement.width = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Height:", String.valueOf(selectedElement.height), t -> {
            try { selectedElement.height = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        return r;
    }

    private int addButton(JPanel p, GridBagConstraints gbc, int r) {
        addField(p, gbc, r++, "Texture:", selectedElement.texture, t -> { selectedElement.texture = t; canvas.repaint(); });
        addField(p, gbc, r++, "TX:", String.valueOf(selectedElement.textureX), t -> {
            try { selectedElement.textureX = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "TY:", String.valueOf(selectedElement.textureY), t -> {
            try { selectedElement.textureY = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "HoverY:", String.valueOf(selectedElement.hoverTextureY), t -> {
            try { selectedElement.hoverTextureY = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "ImgW:", String.valueOf(selectedElement.imageWidth), t -> {
            try { selectedElement.imageWidth = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "ImgH:", String.valueOf(selectedElement.imageHeight), t -> {
            try { selectedElement.imageHeight = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Script:", selectedElement.script, t -> selectedElement.script = t);
        return r;
    }

    private int addText(JPanel p, GridBagConstraints gbc, int r) {
        addField(p, gbc, r++, "Text:", selectedElement.text, t -> { selectedElement.text = t; canvas.repaint(); });
        addField(p, gbc, r++, "Color:", toHex6(selectedElement.color), t -> {
            try { selectedElement.color = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Scale:", String.valueOf(selectedElement.scale), t -> {
            try { selectedElement.scale = Float.parseFloat(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addCheck(p, gbc, r++, "Shadow:", selectedElement.shadow, b -> { selectedElement.shadow = b; canvas.repaint(); });
        return r;
    }

    private int addImage(JPanel p, GridBagConstraints gbc, int r) {
        addField(p, gbc, r++, "Texture:", selectedElement.texture, t -> { selectedElement.texture = t; canvas.repaint(); });
        addField(p, gbc, r++, "TX:", String.valueOf(selectedElement.textureX), t -> {
            try { selectedElement.textureX = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "TY:", String.valueOf(selectedElement.textureY), t -> {
            try { selectedElement.textureY = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "ImgW:", String.valueOf(selectedElement.imageWidth), t -> {
            try { selectedElement.imageWidth = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "ImgH:", String.valueOf(selectedElement.imageHeight), t -> {
            try { selectedElement.imageHeight = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        return r;
    }

    private int addItem(JPanel p, GridBagConstraints gbc, int r) {
        addField(p, gbc, r++, "Item ID:", selectedElement.item, t -> { selectedElement.item = t; canvas.repaint(); });
        return r;
    }

    private int addProgress(JPanel p, GridBagConstraints gbc, int r) {
        addField(p, gbc, r++, "Script:", selectedElement.progressScript, t -> selectedElement.progressScript = t);
        addField(p, gbc, r++, "Bar:", toHex6(selectedElement.barColor), t -> {
            try { selectedElement.barColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "BG:", toHex6(selectedElement.backgroundColor), t -> {
            try { selectedElement.backgroundColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Border:", toHex6(selectedElement.borderColor), t -> {
            try { selectedElement.borderColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addCheck(p, gbc, r++, "Show %:", selectedElement.showPercentage, b -> { selectedElement.showPercentage = b; canvas.repaint(); });
        addCheck(p, gbc, r++, "Vertical:", selectedElement.vertical, b -> { selectedElement.vertical = b; canvas.repaint(); });
        return r;
    }

    private int addSlider(JPanel p, GridBagConstraints gbc, int r) {
        addField(p, gbc, r++, "Min:", String.valueOf(selectedElement.minValue), t -> {
            try { selectedElement.minValue = Float.parseFloat(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Max:", String.valueOf(selectedElement.maxValue), t -> {
            try { selectedElement.maxValue = Float.parseFloat(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Default:", String.valueOf(selectedElement.defaultValue), t -> {
            try { selectedElement.defaultValue = Float.parseFloat(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Step:", String.valueOf(selectedElement.step), t -> {
            try { selectedElement.step = Float.parseFloat(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Color:", toHex6(selectedElement.sliderColor), t -> {
            try { selectedElement.sliderColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Handle:", toHex6(selectedElement.handleColor), t -> {
            try { selectedElement.handleColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addCheck(p, gbc, r++, "ShowVal:", selectedElement.showValue, b -> { selectedElement.showValue = b; canvas.repaint(); });
        addField(p, gbc, r++, "Script:", selectedElement.onChangeScript, t -> selectedElement.onChangeScript = t);
        return r;
    }

    private int addEntity(JPanel p, GridBagConstraints gbc, int r) {
        addField(p, gbc, r++, "Type:", selectedElement.entityType, t -> { selectedElement.entityType = t; canvas.repaint(); });
        addField(p, gbc, r++, "Scale:", String.valueOf(selectedElement.entityScale), t -> {
            try { selectedElement.entityScale = Float.parseFloat(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addCheck(p, gbc, r++, "Rotate:", selectedElement.rotateEntity, b -> { selectedElement.rotateEntity = b; canvas.repaint(); });
        addArea(p, gbc, r++, "NBT:", selectedElement.entityNbt, t -> selectedElement.entityNbt = t);
        return r;
    }

    private int addTextBox(JPanel p, GridBagConstraints gbc, int r) {
        addField(p, gbc, r++, "MaxLen:", String.valueOf(selectedElement.maxLength), t -> {
            try { selectedElement.maxLength = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Hint:", selectedElement.hintText, t -> { selectedElement.hintText = t; canvas.repaint(); });
        addField(p, gbc, r++, "TxtCol:", toHex6(selectedElement.textColor), t -> {
            try { selectedElement.textColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "HintCol:", toHex6(selectedElement.hintColor), t -> {
            try { selectedElement.hintColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addCheck(p, gbc, r++, "Multi:", selectedElement.multiline, b -> { selectedElement.multiline = b; canvas.repaint(); });
        addField(p, gbc, r++, "Script:", selectedElement.onTextChangeScript, t -> selectedElement.onTextChangeScript = t);
        return r;
    }

    private int addDropdown(JPanel p, GridBagConstraints gbc, int r) {
        addArea(p, gbc, r++, "Opts:", selectedElement.options != null ? String.join("\n", selectedElement.options) : "", t -> {
            selectedElement.options = t.isEmpty() ? null : Arrays.asList(t.split("\n"));
        });
        addField(p, gbc, r++, "DefIdx:", String.valueOf(selectedElement.defaultOption), t -> {
            try { selectedElement.defaultOption = Integer.parseInt(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Color:", toHex6(selectedElement.dropdownColor), t -> {
            try { selectedElement.dropdownColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "SelCol:", toHex6(selectedElement.selectedColor), t -> {
            try { selectedElement.selectedColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "TxtCol:", toHex6(selectedElement.textColor), t -> {
            try { selectedElement.textColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Script:", selectedElement.onSelectScript, t -> selectedElement.onSelectScript = t);
        return r;
    }

    private int addSwitch(JPanel p, GridBagConstraints gbc, int r) {
        addCheck(p, gbc, r++, "Default:", selectedElement.defaultState, b -> { selectedElement.defaultState = b; canvas.repaint(); });
        addField(p, gbc, r++, "OnCol:", toHex6(selectedElement.onColor), t -> {
            try { selectedElement.onColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "OffCol:", toHex6(selectedElement.offColor), t -> {
            try { selectedElement.offColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Script:", selectedElement.onToggleScript, t -> selectedElement.onToggleScript = t);
        return r;
    }

    private int addCheckbox(JPanel p, GridBagConstraints gbc, int r) {
        addCheck(p, gbc, r++, "Default:", selectedElement.defaultState, b -> { selectedElement.defaultState = b; canvas.repaint(); });
        addField(p, gbc, r++, "CheckTex:", selectedElement.checkedTexture, t -> { selectedElement.checkedTexture = t; canvas.repaint(); });
        addField(p, gbc, r++, "UnchkTex:", selectedElement.uncheckedTexture, t -> { selectedElement.uncheckedTexture = t; canvas.repaint(); });
        addField(p, gbc, r++, "ChkCol:", toHex6(selectedElement.checkColor), t -> {
            try { selectedElement.checkColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Label:", selectedElement.label, t -> { selectedElement.label = t; canvas.repaint(); });
        addField(p, gbc, r++, "LblCol:", toHex6(selectedElement.labelColor), t -> {
            try { selectedElement.labelColor = parseHex(t); canvas.repaint(); } catch (Exception ex) {}
        });
        addField(p, gbc, r++, "Script:", selectedElement.onToggleScript, t -> selectedElement.onToggleScript = t);
        return r;
    }

    private int addTooltip(JPanel p, GridBagConstraints gbc, int r) {
        addArea(p, gbc, r++, "Tooltip:", selectedElement.tooltip != null ? String.join("\n", selectedElement.tooltip) : "", t -> {
            selectedElement.tooltip = t.isEmpty() ? null : Arrays.asList(t.split("\n"));
        });
        return r;
    }

    private void addField(JPanel p, GridBagConstraints gbc, int row, String lbl, String val, java.util.function.Consumer<String> onChange) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        p.add(new JLabel(lbl), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField f = new JTextField(val != null ? val : "");
        f.addActionListener(e -> onChange.accept(f.getText()));
        p.add(f, gbc);
    }

    private void addCheck(JPanel p, GridBagConstraints gbc, int row, String lbl, boolean val, java.util.function.Consumer<Boolean> onChange) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        p.add(new JLabel(lbl), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JCheckBox cb = new JCheckBox("", val);
        cb.addActionListener(e -> onChange.accept(cb.isSelected()));
        p.add(cb, gbc);
    }

    private void addArea(JPanel p, GridBagConstraints gbc, int row, String lbl, String val, java.util.function.Consumer<String> onChange) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 2;
        p.add(new JLabel(lbl), gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        JTextArea ta = new JTextArea(val != null ? val : "", 4, 20);
        ta.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) { onChange.accept(ta.getText().trim()); }
        });
        p.add(new JScrollPane(ta), gbc);
    }

    private String toHex6(int color) {
        return String.valueOf(color & 0xFFFFFF);
    }

    private int parseHex(String s) {
        return Integer.parseInt(s);
    }

    private void addElement(String type) {
        GuiElement el = new GuiElement();
        el.type = type;
        el.id = type + "_" + (currentGui.elements.size() + 1);
        el.x = 10 + currentGui.elements.size() * 5;
        el.y = 10 + currentGui.elements.size() * 5;

        switch (type) {
            case "button" -> { el.width = 60; el.height = 20; }
            case "text" -> { el.width = 0; el.height = 0; el.text = "Text"; el.color = 4210752; el.scale = 1.0f; }
            case "image" -> { el.width = 32; el.height = 32; }
            case "item" -> { el.item = "minecraft:diamond"; }
            case "progress_bar" -> { el.width = 100; el.height = 20; el.barColor = 16711680; el.backgroundColor = 8421504; el.borderColor = 0; }
            case "slider" -> { el.width = 100; el.height = 20; el.minValue = 0; el.maxValue = 100; el.defaultValue = 50; el.step = 1; el.sliderColor = 11184810; el.handleColor = 9211020; el.showValue = true; }
            case "entity" -> { el.width = 50; el.height = 80; el.entityType = "minecraft:zombie"; el.entityScale = 30; el.rotateEntity = true; }
            case "text_box" -> { el.width = 120; el.height = 20; el.maxLength = 32; el.textColor = 16777215; el.hintColor = 8421504; }
            case "dropdown" -> { el.width = 100; el.height = 20; el.options = Arrays.asList("Option 1", "Option 2"); el.dropdownColor = 16777215; el.selectedColor = 65280; el.textColor = 0; }
            case "switch" -> { el.width = 40; el.height = 20; el.onColor = 65280; el.offColor = 16711680; }
            case "checkbox" -> { el.width = 15; el.height = 15; el.checkColor = 65280; el.label = "Checkbox"; el.labelColor = 4210752; }
        }
        currentGui.elements.add(el);
        canvas.repaint();
    }

    private void deleteSelectedElement() {
        if (selectedElement != null) {
            currentGui.elements.remove(selectedElement);
            selectedElement = null;
            updatePropertiesPanel();
            canvas.repaint();
        }
    }

    private void newGui() {
        currentGui = new GuiDefinition();
        currentGui.width = 176;
        currentGui.height = 166;
        currentGui.elements = new ArrayList<>();
        selectedElement = null;
        updatePropertiesPanel();
        canvas.updateCanvasSize();
        canvas.repaint();
    }

    private void openJson() {
        JFileChooser ch = new JFileChooser(lastDirectory);
        ch.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON", "json"));
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            lastDirectory = ch.getCurrentDirectory();
            try {
                String content = new String(java.nio.file.Files.readAllBytes(ch.getSelectedFile().toPath()));
                currentGui = new Gson().fromJson(content, GuiDefinition.class);
                if (currentGui.elements == null) currentGui.elements = new ArrayList<>();
                selectedElement = null;
                updatePropertiesPanel();
                canvas.updateCanvasSize();
                canvas.repaint();
                JOptionPane.showMessageDialog(this, "Loaded!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void saveJson() {
        JFileChooser ch = new JFileChooser(lastDirectory);
        ch.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON", "json"));
        if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            lastDirectory = ch.getCurrentDirectory();
            File file = ch.getSelectedFile();
            if (!file.getName().endsWith(".json")) file = new File(file + ".json");
            try {
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .registerTypeAdapter(GuiElement.class, new GuiElementSerializer())
                        .create();
                String json = gson.toJson(currentGui);
                java.nio.file.Files.write(file.toPath(), json.getBytes());
                JOptionPane.showMessageDialog(this, "Saved!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    class GuiCanvas extends JPanel {
        private Point dragStart;
        private GuiElement dragElement;
        private double zoom = 1.0;
        private static final double ZOOM_MIN = 0.25;
        private static final double ZOOM_MAX = 4.0;
        private static final double ZOOM_STEP = 0.1;


        public GuiCanvas() {

            setPreferredSize(new Dimension(currentGui.width + 100, currentGui.height + 100));
            setBackground(new Color(60, 63, 65));

            addMouseWheelListener(e -> {
                if (e.isControlDown()) {
                    if (e.getWheelRotation() < 0) zoomIn();
                    else zoomOut();
                    e.consume();
                }
            });

            MouseAdapter ma = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int mx = (int)(e.getX() / zoom);
                    int my = (int)(e.getY() / zoom);

                    int viewW = (int)(getWidth() / zoom);
                    int viewH = (int)(getHeight() / zoom);

                    int ox = (viewW - currentGui.width) / 2;
                    int oy = (viewH - currentGui.height) / 2;

                    int rx = mx - ox;
                    int ry = my - oy;

                    GuiElement clicked = null;
                    for (int i = currentGui.elements.size() - 1; i >= 0; i--) {
                        GuiElement el = currentGui.elements.get(i);
                        if (rx >= el.x && rx < el.x + el.width && ry >= el.y && ry < el.y + el.height) {
                            clicked = el;
                            break;
                        }
                    }

                    if (e.getButton() == MouseEvent.BUTTON1) {
                        selectedElement = clicked;
                        if (clicked != null) {
                            dragStart = new Point(rx - clicked.x, ry - clicked.y);
                            dragElement = clicked;
                        }
                        updatePropertiesPanel();
                        repaint();
                    }
                }

                public void mouseDragged(MouseEvent e) {
                    if (dragElement != null && dragStart != null) {

                        int mx = (int)(e.getX() / zoom);
                        int my = (int)(e.getY() / zoom);

                        int viewW = (int)(getWidth() / zoom);
                        int viewH = (int)(getHeight() / zoom);

                        int ox = (viewW - currentGui.width) / 2;
                        int oy = (viewH - currentGui.height) / 2;

                        int rx = mx - ox;
                        int ry = my - oy;

                        dragElement.x = Math.max(
                                0,
                                Math.min(currentGui.width - dragElement.width, rx - dragStart.x)
                        );

                        dragElement.y = Math.max(
                                0,
                                Math.min(currentGui.height - dragElement.height, ry - dragStart.y)
                        );

                        updatePropertiesPanel();
                        repaint();
                    }
                }


                public void mouseReleased(MouseEvent e) {
                    dragStart = null;
                    dragElement = null;
                }
            };

            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        public void zoomIn() {
            setZoom(zoom + ZOOM_STEP);
        }

        public void zoomOut() {
            setZoom(zoom - ZOOM_STEP);
        }

        public void resetZoom() {
            setZoom(1.0);
        }

        private void setZoom(double z) {
            zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, z));
            updateCanvasSize();
            repaint();
        }


        public void updateCanvasSize() {
            int w = (int)((currentGui.width + 100) * zoom);
            int h = (int)((currentGui.height + 100) * zoom);
            setPreferredSize(new Dimension(w, h));
            revalidate();
        }


        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.scale(zoom, zoom);

            int viewW = (int)(getWidth() / zoom);
            int viewH = (int)(getHeight() / zoom);

            int ox = (viewW - currentGui.width) / 2;
            int oy = (viewH - currentGui.height) / 2;


            if (currentGui.backgroundTexture != null) {
                BufferedImage bg = textureRegistry.get(currentGui.backgroundTexture);
                if (bg != null) {
                    g2.drawImage(bg, ox, oy, currentGui.width, currentGui.height, null);
                }
            }

            if (currentGui.backgroundTexture == null ||
                    !textureRegistry.containsKey(currentGui.backgroundTexture)) {

                g2.setColor(new Color(139, 139, 139));
                g2.fillRect(ox, oy, currentGui.width, currentGui.height);
            }
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(ox, oy, currentGui.width, currentGui.height);

            if (currentGui.title != null && !currentGui.title.isEmpty()) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString(currentGui.title, ox + 5, oy - 5);
            }

            for (GuiElement el : currentGui.elements) {
                drawElement(g2, el, ox, oy);
            }

            if (selectedElement != null) {
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(ox + selectedElement.x, oy + selectedElement.y, selectedElement.width, selectedElement.height);
            }
            g2.dispose();
        }

        private void drawTexture(
                Graphics2D g2,
                String texture,
                int x, int y,
                int w, int h,
                int tx, int ty,
                int imgW, int imgH
        ) {
            BufferedImage img = textureRegistry.get(texture);
            if (img == null) return;

            int sx1 = tx;
            int sy1 = ty;
            int sx2 = tx + w;
            int sy2 = ty + h;

            g2.drawImage(
                    img,
                    x, y, x + w, y + h,
                    sx1, sy1, sx2, sy2,
                    null
            );
        }


        private void drawElement(Graphics2D g2, GuiElement el, int ox, int oy) {
            int x = ox + el.x, y = oy + el.y;

            switch (el.type) {
                case "button" -> {
                    if (el.texture != null && textureRegistry.containsKey(el.texture)) {
                        drawTexture(
                                g2,
                                el.texture,
                                x, y,
                                el.width, el.height,
                                el.textureX, el.textureY,
                                el.imageWidth, el.imageHeight
                        );
                    } else {
                        g2.setColor(new Color(170, 170, 170));
                        g2.fillRect(x, y, el.width, el.height);
                        g2.setColor(Color.BLACK);
                        g2.drawRect(x, y, el.width, el.height);
                        g2.drawString("BTN", x + 5, y + el.height / 2 + 4);
                    }
                }

                case "text" -> {
                    if (el.text != null) {
                        g2.setColor(new Color(el.color));
                        g2.setFont(new Font("Arial", Font.PLAIN, (int)(12 * el.scale)));
                        g2.drawString(el.text, x, y + (int)(12 * el.scale));
                    }
                }
                case "image" -> {
                    if (el.texture != null && textureRegistry.containsKey(el.texture)) {
                        drawTexture(
                                g2,
                                el.texture,
                                x, y,
                                el.width, el.height,
                                el.textureX, el.textureY,
                                el.imageWidth, el.imageHeight
                        );
                    } else {
                        g2.setColor(new Color(200, 200, 255));
                        g2.fillRect(x, y, el.width, el.height);
                        g2.setColor(Color.BLUE);
                        g2.drawRect(x, y, el.width, el.height);
                        g2.drawString("IMG", x + 2, y + el.height / 2 + 3);
                    }
                }

                case "item" -> {
                    g2.setColor(new Color(255, 200, 200));
                    g2.fillRect(x, y, 16, 16);
                    g2.setColor(Color.RED);
                    g2.drawRect(x, y, 16, 16);
                    g2.setFont(new Font("Arial", Font.PLAIN, 8));
                    g2.drawString("I", x + 5, y + 11);
                }
                case "progress_bar" -> {
                    g2.setColor(new Color(el.backgroundColor));
                    g2.fillRect(x, y, el.width, el.height);
                    g2.setColor(new Color(el.barColor));
                    g2.fillRect(x, y, el.width / 2, el.height);
                    g2.setColor(new Color(el.borderColor));
                    g2.drawRect(x, y, el.width, el.height);
                }
                case "slider" -> {
                    g2.setColor(new Color(el.sliderColor));
                    g2.fillRect(x, y + el.height / 2 - 2, el.width, 4);
                    g2.setColor(new Color(el.handleColor));
                    g2.fillRect(x + el.width / 2 - 4, y, 8, el.height);
                }
                case "entity" -> {
                    g2.setColor(new Color(200, 255, 200));
                    g2.fillRect(x, y, el.width, el.height);
                    g2.setColor(Color.GREEN);
                    g2.drawRect(x, y, el.width, el.height);
                    g2.setFont(new Font("Arial", Font.PLAIN, 8));
                    g2.drawString("ENT", x + 2, y + el.height / 2 + 3);
                }
                case "text_box" -> {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(x, y, el.width, el.height);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(x, y, el.width, el.height);
                    g2.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2.drawString("TextBox", x + 2, y + el.height / 2 + 4);
                }
                case "dropdown" -> {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(x, y, el.width, el.height);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(x, y, el.width, el.height);
                    g2.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2.drawString("Dropdown", x + 2, y + el.height / 2 + 4);
                }
                case "switch" -> {
                    g2.setColor(new Color(el.offColor));
                    g2.fillRect(x, y, el.width, el.height);
                    g2.setColor(Color.WHITE);
                    g2.fillRect(x + 2, y + 2, el.width / 2 - 2, el.height - 4);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(x, y, el.width, el.height);
                }
                case "checkbox" -> {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(x, y, el.height, el.height);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(x, y, el.height, el.height);
                    if (el.label != null) {
                        g2.setFont(new Font("Arial", Font.PLAIN, 10));
                        g2.drawString(el.label, x + el.height + 4, y + el.height / 2 + 4);
                    }
                }
            }
        }
    }
}

