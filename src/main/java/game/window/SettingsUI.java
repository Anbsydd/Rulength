package game.window;

import config.*;
import core.GameAPI;
import game.ui.GameButton;
import game.ui.UITheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import util.ImageManager;
import util.TextLan;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全屏设置界面 — 深色工业规则风
 * <p>
 * 按ESC键打开/关闭，以blank.png为背景，
 * 所有config文件中的配置都可以在此更改。
 * 修改时立即生效，保存按钮写入JSON文件，
 * 取消按钮重新从JSON文件加载，还原按钮从defaultConfig恢复。
 * <p>
 * 面板层次（依据 UI设计.md）：
 *   外框（铜色边框）
 *   ↓
 *   内阴影
 *   ↓
 *   深色主体（rgba(10,15,20,0.92)）
 *   ↓
 *   微发光边缘（电力黄光晕）
 */
public class SettingsUI extends StackPane {

    // 配置文件路径常量
    private static final String CONFIG_DIR = "assets/config/";
    private static final String DEFAULT_CONFIG_DIR = "assets/defaultConfig/";
    private static final String README_PATH = CONFIG_DIR + "readme.json";
    private static final String SETTINGS_CONFIG_PATH = CONFIG_DIR + "settingsConfig.json";

    // 配置文件名列表
    private static final String[] CONFIG_FILES = {
            "gameConfig.json",
            "stageConfig.json",
            "cameraConfig.json",
            "miniMapConfig.json",
            "settingsConfig.json"
    };

    // 配置类映射
    private static final Map<String, Class<?>> CONFIG_CLASSES = new LinkedHashMap<>();

    static {
        CONFIG_CLASSES.put("gameConfig.json", GameConfig.class);
        CONFIG_CLASSES.put("stageConfig.json", StageConfig.class);
        CONFIG_CLASSES.put("cameraConfig.json", CameraConfig.class);
        CONFIG_CLASSES.put("miniMapConfig.json", MiniMapConfig.class);
        CONFIG_CLASSES.put("settingsConfig.json", SettingsConfig.class);
    }

    // 设置界面自身配置
    private SettingsConfig settingsConfig;
    private StackPane overlay;
    private SliceEditorUI sliceEditor;

    // 当前生效的配置对象（用于取消时恢复）
    private final Map<String, Object> savedConfigs = new LinkedHashMap<>();

    // 设置界面中的编辑值映射：configFileName -> fieldName -> property
    private final Map<String, Map<String, Object>> editValues = new LinkedHashMap<>();

    // 字段说明映射
    private final Map<String, Map<String, String>> fieldDescriptions = new LinkedHashMap<>();

    // 是否可见
    private boolean settingsVisible = false;

    // 主内容区域
    private ScrollPane scrollPane;
    private VBox configContainer;

    // 脏标记：是否有修改
    private boolean dirty = false;

    public SettingsUI() {
        loadSettingsConfig();
        loadReadme();
        loadSavedConfigs();
        buildUI();
        setupEscHandler();
        sliceEditor = new SliceEditorUI();
    }

    /**
     * 加载设置界面自身配置
     */
    private void loadSettingsConfig() {
        try {
            settingsConfig = ConfigLoader.loadConfig(SETTINGS_CONFIG_PATH, SettingsConfig.class);
        } catch (Exception e) {
            settingsConfig = new SettingsConfig();
            settingsConfig.opacity = 0.5;
            e.printStackTrace();
        }
    }

    /**
     * 加载readme.json中的字段说明
     */
    private void loadReadme() {
        try {
            String json = new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(README_PATH)));
            Map<String, Map<String, String>> readme = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {});
            fieldDescriptions.putAll(readme);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载当前配置文件到savedConfigs（用于取消时恢复）
     */
    private void loadSavedConfigs() {
        savedConfigs.clear();
        for (String fileName : CONFIG_FILES) {
            try {
                Class<?> clazz = CONFIG_CLASSES.get(fileName);
                Object config = ConfigLoader.loadConfig(CONFIG_DIR + fileName, clazz);
                savedConfigs.put(fileName, config);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 构建UI — 蒸汽朋克工业风格
     */
    private void buildUI() {
        // 背景（blank.png 作为最底层）
        ImageView bgView = new ImageView(ImageManager.load("uiImages/backgrounds/blank.png"));
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);
        bgView.fitWidthProperty().bind(widthProperty());
        bgView.fitHeightProperty().bind(heightProperty());
        getChildren().add(bgView);

        // 深色遮罩（带铜色边框感）
        overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(6, 9, 13, 0.85);"
                + "-fx-border-color: #0B0F14;"
                + "-fx-border-width: 3;");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);

        // 整体透明度
        setOpacity(settingsConfig.opacity);

        // ===== 主面板 =====
        VBox mainPanel = new VBox(15);
        mainPanel.setAlignment(Pos.CENTER);
        mainPanel.setMaxWidth(760);
        mainPanel.setMaxHeight(Double.MAX_VALUE);
        mainPanel.setStyle(UITheme.panelCornerStyle());

        // ---- 标题栏 ----
        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(8, 16, 8, 16));
        // 左侧装饰线
        Label titleDecor = new Label("▌");
        titleDecor.setFont(UITheme.fontBold(24));
        titleDecor.setTextFill(Color.web(UITheme.COPPER_MID));
        // 标题
        Label titleLabel = new Label(TextLan.get("SettingsUI_Title"));
        titleLabel.setFont(UITheme.fontBold(24));
        titleLabel.setTextFill(UITheme.TEXT_TITLE);
        // 右侧发光点
        Label titleGlow = new Label("◆");
        titleGlow.setFont(UITheme.fontBold(10));
        titleGlow.setTextFill(Color.web(UITheme.GLOW_POWER));
        HBox titleSpacer = new HBox();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        titleBar.getChildren().addAll(titleDecor, titleLabel, titleSpacer, titleGlow);
        // 标题下发光分割线
        Region titleLine = new Region();
        titleLine.setPrefHeight(1.5);
        titleLine.setStyle("-fx-background-color: linear-gradient(to right, "
                + UITheme.GLOW_POWER + ", transparent);");

        // ---- 配置内容区域（可滚动） ----
        configContainer = new VBox(16);
        configContainer.setPadding(new Insets(8, 12, 8, 12));

        for (String fileName : CONFIG_FILES) {
            VBox section = createConfigSection(fileName);
            configContainer.getChildren().add(section);
        }

        scrollPane = new ScrollPane(configContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(Double.MAX_VALUE);
        scrollPane.setStyle(UITheme.scrollPaneStyle());
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // ---- 按钮栏 ----
        HBox buttonBar = createButtonBar();

        mainPanel.getChildren().addAll(titleBar, titleLine, scrollPane, buttonBar);
        overlay.getChildren().add(mainPanel);

        // 初始不可见
        setVisible(false);
        setPickOnBounds(false);
    }

    /**
     * 创建一个配置文件的分组 — 铜色金属面板
     */
    private VBox createConfigSection(String fileName) {
        VBox section = new VBox(8);
        section.setStyle(UITheme.panelStyle());

        // 分组标题
        Label sectionTitle = new Label(getDisplayName(fileName));
        sectionTitle.setFont(UITheme.fontBold(16));
        sectionTitle.setTextFill(UITheme.TEXT_PRIMARY);

        // 标题下细线
        Region sectionLine = new Region();
        sectionLine.setPrefHeight(1);
        sectionLine.setStyle("-fx-background-color: " + UITheme.COPPER_DARK + ";");

        section.getChildren().addAll(sectionTitle, sectionLine);

        // 字段编辑器
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        Class<?> clazz = CONFIG_CLASSES.get(fileName);
        Object config = savedConfigs.get(fileName);
        Map<String, String> descriptions = fieldDescriptions.getOrDefault(fileName, Collections.emptyMap());

        if (config != null && clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(config);
                    String desc = descriptions.getOrDefault(field.getName(), "");
                    Node editor = createFieldEditor(fileName, field.getName(), value, desc, field.getType());
                    section.getChildren().add(editor);
                    fieldMap.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        editValues.put(fileName, fieldMap);
        return section;
    }

    /**
     * 创建字段编辑器 — 工业风格输入控件
     */
    private Node createFieldEditor(String fileName, String fieldName, Object value, String description, Class<?> type) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        // 字段名标签
        Label nameLabel = new Label(fieldName);
        nameLabel.setFont(UITheme.font(13));
        nameLabel.setTextFill(UITheme.TEXT_LIGHT);
        nameLabel.setMinWidth(140);
        nameLabel.setMaxWidth(140);

        // 说明标签
        Label descLabel = new Label(description);
        descLabel.setFont(UITheme.font(11));
        descLabel.setTextFill(UITheme.TEXT_DIM);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(260);

        Control input;
        if (type == int.class) {
            Spinner<Integer> spinner = new Spinner<>(Integer.MIN_VALUE, Integer.MAX_VALUE, (Integer) value);
            spinner.setEditable(true);
            spinner.setPrefWidth(110);
            spinner.getEditor().setStyle(UITheme.fieldStyle());
            spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                editValues.get(fileName).put(fieldName, newVal);
                applyConfig(fileName);
            });
            input = spinner;
        } else if (type == double.class) {
            TextField textField = new TextField(String.valueOf(value));
            textField.setPrefWidth(110);
            textField.setStyle(UITheme.fieldStyle());
            textField.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    double d = Double.parseDouble(newVal);
                    editValues.get(fileName).put(fieldName, d);
                    applyConfig(fileName);
                } catch (NumberFormatException ignored) {}
            });
            input = textField;
        } else if (type == boolean.class) {
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected((Boolean) value);
            checkBox.setTextFill(UITheme.TEXT_LIGHT);
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                editValues.get(fileName).put(fieldName, newVal);
                applyConfig(fileName);
            });
            input = checkBox;
        } else {
            TextField textField = new TextField(value != null ? value.toString() : "");
            textField.setPrefWidth(180);
            textField.setStyle(UITheme.fieldStyle());
            textField.textProperty().addListener((obs, oldVal, newVal) -> {
                editValues.get(fileName).put(fieldName, newVal);
                applyConfig(fileName);
            });
            input = textField;
        }

        row.getChildren().addAll(nameLabel, input, descLabel);
        return row;
    }

    /**
     * 创建按钮栏 — 工业控制台风格
     */
    private HBox createButtonBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(8, 16, 12, 16));

        // 分割装饰
        Region barLine = new Region();
        barLine.setPrefHeight(1);
        barLine.setStyle("-fx-background-color: linear-gradient(to right, transparent, "
                + UITheme.COPPER_MID + ", transparent);");
        barLine.setMaxWidth(Double.MAX_VALUE);

        GameButton saveBtn = GameButton.success(TextLan.get("SettingsUI_Save"));
        saveBtn.setOnAction(e -> saveConfigs());

        GameButton cancelBtn = GameButton.danger(TextLan.get("SettingsUI_Cancel"));
        cancelBtn.setOnAction(e -> cancelConfigs());

        GameButton resetBtn = GameButton.secondary(TextLan.get("SettingsUI_ResetDefault"));
        resetBtn.setOnAction(e -> resetToDefaults());

        GameButton editBtn = GameButton.tech(TextLan.get("SettingsUI_Edit"));
        editBtn.setOnAction(e -> sliceEditor.show());

        // 右端科技装饰
        Label barGlow = new Label("◥");
        barGlow.setFont(UITheme.fontTech(12));
        barGlow.setTextFill(Color.web(UITheme.GLOW_TECH));
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(barGlow, spacer, saveBtn, cancelBtn, resetBtn, editBtn);
        return bar;
    }

    /**
     * 设置ESC键处理
     */
    private void setupEscHandler() {
        core.CoreAPI.stageRef.getJavafxStage().getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                toggleVisibility();
                e.consume();
            }
        });
    }

    /**
     * 切换可见性
     */
    public void toggleVisibility() {
        settingsVisible = !settingsVisible;
        if (settingsVisible) {
            refreshEditors();
        }
        setVisible(settingsVisible);
        setPickOnBounds(settingsVisible);
    }

    /**
     * 刷新编辑器内容（从当前JSON文件重新加载）
     */
    private void refreshEditors() {
        loadSavedConfigs();
        editValues.clear();
        configContainer.getChildren().clear();
        for (String fileName : CONFIG_FILES) {
            VBox section = createConfigSection(fileName);
            configContainer.getChildren().add(section);
        }
        dirty = false;
    }

    /**
     * 立即应用配置到游戏
     */
    private void applyConfig(String fileName) {
        dirty = true;
        try {
            Class<?> clazz = CONFIG_CLASSES.get(fileName);
            Object config = clazz.getDeclaredConstructor().newInstance();

            Map<String, Object> fieldMap = editValues.get(fileName);
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = fieldMap.get(field.getName());
                if (value != null) {
                    if (field.getType() == int.class) {
                        field.setInt(config, ((Number) value).intValue());
                    } else if (field.getType() == double.class) {
                        field.setDouble(config, ((Number) value).doubleValue());
                    } else if (field.getType() == boolean.class) {
                        field.setBoolean(config, Boolean.parseBoolean(value.toString()));
                    } else {
                        field.set(config, value);
                    }
                }
            }

            applyToGame(fileName, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将配置应用到游戏组件
     */
    private void applyToGame(String fileName, Object config) {
        switch (fileName) {
            case "cameraConfig.json" -> GameAPI.applyCameraConfig((CameraConfig) config);
            case "miniMapConfig.json" -> GameAPI.applyMiniMapConfig((MiniMapConfig) config);
            case "gameConfig.json" -> {}
            case "stageConfig.json" -> GameAPI.applyStageConfig((StageConfig) config);
            case "settingsConfig.json" -> applySettingsConfig((SettingsConfig) config);
        }
    }

    /**
     * 保存按钮：将当前编辑值写入JSON文件
     */
    private void saveConfigs() {
        try {
            for (String fileName : CONFIG_FILES) {
                Class<?> clazz = CONFIG_CLASSES.get(fileName);
                Object config = clazz.getDeclaredConstructor().newInstance();

                Map<String, Object> fieldMap = editValues.get(fileName);
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = fieldMap.get(field.getName());
                    if (value != null) {
                        if (field.getType() == int.class) {
                            field.setInt(config, ((Number) value).intValue());
                        } else if (field.getType() == double.class) {
                            field.setDouble(config, ((Number) value).doubleValue());
                        } else if (field.getType() == boolean.class) {
                            field.setBoolean(config, Boolean.parseBoolean(value.toString()));
                        } else {
                            field.set(config, value);
                        }
                    }
                }

                ConfigWriter.writeConfig(CONFIG_DIR + fileName, config);
            }
            dirty = false;
            loadSavedConfigs();
            closeSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消按钮：从JSON文件重新加载配置到游戏
     */
    private void cancelConfigs() {
        try {
            for (String fileName : CONFIG_FILES) {
                Class<?> clazz = CONFIG_CLASSES.get(fileName);
                Object config = ConfigLoader.loadConfig(CONFIG_DIR + fileName, clazz);
                applyToGame(fileName, config);
            }
            dirty = false;
            closeSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 还原默认配置按钮：将defaultConfig复制到config，并应用到游戏
     */
    private void resetToDefaults() {
        try {
            ConfigWriter.copyAllDefaultsToConfig();

            loadSavedConfigs();
            for (String fileName : CONFIG_FILES) {
                Object config = savedConfigs.get(fileName);
                applyToGame(fileName, config);
            }

            editValues.clear();
            configContainer.getChildren().clear();
            for (String fileName : CONFIG_FILES) {
                VBox section = createConfigSection(fileName);
                configContainer.getChildren().add(section);
            }

            dirty = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 应用设置界面自身配置（运行时热更新）
     */
    private void applySettingsConfig(SettingsConfig config) {
        this.settingsConfig = config;
        setOpacity(config.opacity);
    }

    /**
     * 关闭设置界面
     */
    private void closeSettings() {
        settingsVisible = false;
        setVisible(false);
        setPickOnBounds(false);
    }

    /**
     * 获取配置文件显示名
     */
    private String getDisplayName(String fileName) {
        return switch (fileName) {
            case "gameConfig.json" -> TextLan.get("SettingsUI_GameConfig");
            case "stageConfig.json" -> TextLan.get("SettingsUI_StageConfig");
            case "cameraConfig.json" -> TextLan.get("SettingsUI_CameraConfig");
            case "miniMapConfig.json" -> TextLan.get("SettingsUI_MiniMapConfig");
            case "settingsConfig.json" -> TextLan.get("SettingsUI_SettingsConfig");
            default -> fileName;
        };
    }
}
