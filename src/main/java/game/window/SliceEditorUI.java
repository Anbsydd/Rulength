package game.window;

import config.SliceConfig;
import config.SliceInjector;
import core.GameAPI;
import game.slice.ConfiguredMoveSlice;
import game.slice.Slice;
import game.ui.GameButton;
import game.ui.UITheme;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.TextLan;

/**
 * Slice 编辑器 — 蒸汽朋克风格弹出窗口
 * <p>
 * 在设置界面中点击"编辑"按钮打开，支持：
 * - 选择定义、设置坐标/名称 → 生成新 Slice
 * - 列出当前所有 Slice，支持移除
 * <p>
 * 面板层次（依据 UI设计.md）：
 *   外框（铜色边框）
 *   ↓
 *   深色主体（rgba(10,15,20,0.92)）
 *   ↓
 *   微发光边缘（科技蓝光晕）
 */
public class SliceEditorUI {

    private final Stage dialogStage;
    private final ObservableList<SliceItem> sliceList = FXCollections.observableArrayList();
    private final ComboBox<SliceConfig> defCombo;
    private final TextField nameField;
    private final TextField mapXField;
    private final TextField mapYField;
    private final CheckBox movedCheck;
    private final Label statusLabel;

    public SliceEditorUI() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("■ " + TextLan.get("SliceEditor_Title"));
        dialogStage.setWidth(680);
        dialogStage.setHeight(580);

        // ===== 根容器：深色面板 + 铜色边框 =====
        VBox root = new VBox(0);
        root.setPadding(new Insets(16));
        root.setStyle(UITheme.panelCornerStyle());

        // ===== 标题栏 =====
        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(4, 8, 8, 8));
        Label titleDecor = new Label("▌");
        titleDecor.setFont(UITheme.fontBold(22));
        titleDecor.setTextFill(Color.web(UITheme.COPPER_MID));
        Label titleLabel = new Label(TextLan.get("SliceEditor_Title"));
        titleLabel.setFont(UITheme.fontBold(20));
        titleLabel.setTextFill(UITheme.TEXT_TITLE);
        Label titleTech = new Label("◈");
        titleTech.setFont(UITheme.fontTech(12));
        titleTech.setTextFill(Color.web(UITheme.GLOW_TECH));
        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        titleBar.getChildren().addAll(titleDecor, titleLabel, titleSpacer, titleTech);

        // 标题分割线
        Region titleLine = new Region();
        titleLine.setPrefHeight(1.5);
        titleLine.setStyle("-fx-background-color: linear-gradient(to right, "
                + UITheme.GLOW_TECH + ", transparent);");
        titleLine.setPadding(new Insets(0, 0, 4, 0));

        // ========== 生成区域 ==========
        VBox genSection = new VBox(10);
        genSection.setStyle(UITheme.panelStyle());
        genSection.setPadding(new Insets(12));

        Label genTitle = new Label(TextLan.get("SliceEditor_GenerateTitle"));
        genTitle.setFont(UITheme.fontBold(15));
        genTitle.setTextFill(UITheme.TEXT_PRIMARY);

        // 定义选择
        Label defLabel = new Label(TextLan.get("SliceEditor_SelectDef"));
        defLabel.setTextFill(UITheme.TEXT_LIGHT);
        defLabel.setMinWidth(70);

        defCombo = new ComboBox<>();
        defCombo.setPrefWidth(400);
        defCombo.setStyle(UITheme.fieldStyle());
        defCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SliceConfig item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(UITheme.fieldStyle());
                setText(empty || item == null ? null : item.ID + " (" + item.name + ")");
                setTextFill(UITheme.TEXT_LIGHT);
            }
        });
        defCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SliceConfig item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(UITheme.fieldStyle());
                setText(empty || item == null ? null : "▸ " + item.ID + " (" + item.name + ")");
                setTextFill(UITheme.TEXT_LIGHT);
            }
        });

        // 名称
        Label nameLabel = new Label(TextLan.get("SliceEditor_Name"));
        nameLabel.setTextFill(UITheme.TEXT_LIGHT);
        nameLabel.setMinWidth(70);
        nameField = new TextField();
        nameField.setPrefWidth(140);
        nameField.setStyle(UITheme.fieldStyle());

        // 地图 X / Y
        Label mapXLabel = new Label(TextLan.get("SliceEditor_MapX"));
        mapXLabel.setTextFill(UITheme.TEXT_LIGHT);
        mapXField = new TextField("0");
        mapXField.setPrefWidth(90);
        mapXField.setStyle(UITheme.fieldStyle());

        Label mapYLabel = new Label(TextLan.get("SliceEditor_MapY"));
        mapYLabel.setTextFill(UITheme.TEXT_LIGHT);
        mapYField = new TextField("0");
        mapYField.setPrefWidth(90);
        mapYField.setStyle(UITheme.fieldStyle());

        // moved
        movedCheck = new CheckBox(TextLan.get("SliceEditor_Moved"));
        movedCheck.setTextFill(UITheme.TEXT_LIGHT);

        // 选择定义时自动填充
        defCombo.valueProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            if (nameField.getText().isBlank()) nameField.setText(sel.name);
            movedCheck.setSelected(sel.moved);
        });

        // 生成网格
        GridPane genGrid = new GridPane();
        genGrid.setHgap(10);
        genGrid.setVgap(8);
        genGrid.add(defLabel, 0, 0);
        genGrid.add(defCombo, 1, 0, 5, 1);
        genGrid.add(nameLabel, 0, 1);
        genGrid.add(nameField, 1, 1);
        genGrid.add(mapXLabel, 2, 1);
        genGrid.add(mapXField, 3, 1);
        genGrid.add(mapYLabel, 4, 1);
        genGrid.add(mapYField, 5, 1);
        genGrid.add(movedCheck, 1, 2, 3, 1);

        // 生成按钮行
        HBox genBtnRow = new HBox(10);
        genBtnRow.setAlignment(Pos.CENTER_RIGHT);
        GameButton genBtn = GameButton.power(TextLan.get("SliceEditor_Generate"));
        genBtn.setOnAction(e -> handleGenerate());
        genBtnRow.getChildren().add(genBtn);

        genSection.getChildren().addAll(genTitle, genGrid, genBtnRow);

        // ========== 当前 Slice 列表 ==========
        VBox listSection = new VBox(8);
        listSection.setStyle(UITheme.panelStyle());
        listSection.setPadding(new Insets(12));
        VBox.setVgrow(listSection, Priority.ALWAYS);

        HBox listTitleBar = new HBox(10);
        listTitleBar.setAlignment(Pos.CENTER_LEFT);
        Label listDecor = new Label("▸");
        listDecor.setFont(UITheme.fontBold(10));
        listDecor.setTextFill(Color.web(UITheme.COPPER_MID));
        Label listTitle = new Label(TextLan.get("SliceEditor_CurrentSlices"));
        listTitle.setFont(UITheme.fontBold(15));
        listTitle.setTextFill(UITheme.TEXT_PRIMARY);
        // 计数标签
        Label countLabel = new Label();
        countLabel.setFont(UITheme.fontTech(12));
        countLabel.setTextFill(Color.web(UITheme.GLOW_TECH));
        // 监听列表变化更新计数
        sliceList.addListener((javafx.collections.ListChangeListener<SliceItem>) c -> {
            countLabel.setText("[" + sliceList.size() + "]");
        });
        listTitleBar.getChildren().addAll(listDecor, listTitle, countLabel);

        ListView<SliceItem> sliceListView = new ListView<>(sliceList);
        sliceListView.setStyle("-fx-background-color: #0B0F14;"
                + "-fx-border-color: " + UITheme.COPPER_DARK + ";"
                + "-fx-border-width: 1.5;"
                + "-fx-background-radius: 3;"
                + "-fx-border-radius: 3;");
        sliceListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SliceItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle("-fx-background-color: transparent;"
                            + "-fx-border-color: transparent transparent " + UITheme.COPPER_DARK + " transparent;"
                            + "-fx-border-width: 0 0 0.5 0;");
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(4, 4, 4, 4));

                    // ID 标签
                    Label idLabel = new Label(item.id);
                    idLabel.setFont(UITheme.fontTech(12));
                    idLabel.setTextFill(Color.web(UITheme.GLOW_TECH));
                    idLabel.setMinWidth(80);
                    idLabel.setMaxWidth(80);

                    // 名称
                    Label nameLabel2 = new Label(item.name);
                    nameLabel2.setFont(UITheme.font(13));
                    nameLabel2.setTextFill(UITheme.TEXT_LIGHT);
                    nameLabel2.setMinWidth(100);
                    nameLabel2.setMaxWidth(100);

                    // 坐标
                    Label coordLabel = new Label(String.format("(%.0f, %.0f)", item.mapX, item.mapY));
                    coordLabel.setFont(UITheme.font(12));
                    coordLabel.setTextFill(UITheme.TEXT_DIM);
                    coordLabel.setMinWidth(90);
                    coordLabel.setMaxWidth(90);

                    // 类型
                    Label typeLabel = new Label(item.moved ? "⚡移动" : "▣静态");
                    typeLabel.setFont(UITheme.font(12));
                    typeLabel.setTextFill(item.moved
                            ? Color.web(UITheme.GLOW_POWER)
                            : Color.web(UITheme.COPPER_MID));

                    // 删除按钮
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    GameButton removeBtn = GameButton.danger(TextLan.get("SliceEditor_Remove"));
                    removeBtn.setOnAction(e2 -> {
                        GameAPI.removeSlice(item.slice);
                        sliceList.remove(item);
                    });

                    row.getChildren().addAll(idLabel, nameLabel2, coordLabel, typeLabel, spacer, removeBtn);
                    setGraphic(row);
                }
            }
        });
        VBox.setVgrow(sliceListView, Priority.ALWAYS);

        listSection.getChildren().addAll(listTitleBar, sliceListView);

        // ========== 状态栏 ==========
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(8, 4, 0, 4));
        statusBar.setAlignment(Pos.CENTER_RIGHT);
        statusLabel = new Label("");
        statusLabel.setFont(UITheme.fontTech(11));
        statusLabel.setTextFill(Color.web(UITheme.GLOW_TECH));

        GameButton closeBtn = GameButton.secondary(TextLan.get("SliceEditor_Close"));
        closeBtn.setOnAction(e -> dialogStage.close());
        statusBar.getChildren().addAll(statusLabel, closeBtn);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        root.getChildren().addAll(titleBar, titleLine, genSection, listSection, statusBar);
        VBox.setVgrow(listSection, Priority.ALWAYS);

        dialogStage.setScene(new Scene(root));
    }

    /** 处理生成 */
    private void handleGenerate() {
        SliceConfig sel = defCombo.getValue();
        if (sel == null) {
            statusLabel.setText(TextLan.get("SliceEditor_NoDefSelected"));
            statusLabel.setTextFill(Color.web(UITheme.GLOW_DANGER));
            return;
        }
        try {
            SliceConfig cfg = new SliceConfig();
            cfg.ID = sel.ID;
            cfg.name = nameField.getText().isBlank() ? sel.name : nameField.getText();
            cfg.moved = movedCheck.isSelected();
            cfg.mapX = Double.parseDouble(mapXField.getText());
            cfg.mapY = Double.parseDouble(mapYField.getText());
            cfg.opacity = sel.opacity;
            cfg.text = sel.text;
            cfg.attributes.putAll(sel.attributes);
            cfg.methods.putAll(sel.methods);
            cfg.event.putAll(sel.event);

            Slice slice = GameAPI.spawnSlice(cfg);
            sliceList.add(new SliceItem(cfg.ID, cfg.name, cfg.mapX, cfg.mapY, cfg.moved, slice));
            statusLabel.setText(TextLan.get("SliceEditor_Generated") + " " + cfg.name);
            statusLabel.setTextFill(Color.web(UITheme.GLOW_NORMAL));
        } catch (NumberFormatException ex) {
            statusLabel.setText(TextLan.get("SliceEditor_InvalidCoord"));
            statusLabel.setTextFill(Color.web(UITheme.GLOW_DANGER));
        }
    }

    /** 打开编辑器 */
    public void show() {
        // 刷新定义列表
        try {
            java.util.List<SliceConfig> defs = SliceInjector.getAll();
            if (defs.isEmpty()) {
                SliceInjector.loadAll();
                defs = SliceInjector.getAll();
            }
            defCombo.setItems(FXCollections.observableArrayList(defs));
            if (!defs.isEmpty()) defCombo.getSelectionModel().select(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 重置输入字段
        nameField.clear();
        mapXField.setText("0");
        mapYField.setText("0");
        movedCheck.setSelected(false);
        statusLabel.setText("");

        // 加载当前所有 Slice 到列表
        sliceList.clear();
        for (Slice s : GameAPI.getAllSlices()) {
            boolean moved = s instanceof ConfiguredMoveSlice;
            String id = (s instanceof game.slice.ConfiguredMoveSlice ms)
                    ? ms.getConfig().ID
                    : ((game.slice.ConfiguredStaticSlice) s).getConfig().ID;
            sliceList.add(new SliceItem(id, s.getName(), s.getMapX(), s.getMapY(), moved, s));
        }

        dialogStage.show();
    }

    /** 列表项 */
    private record SliceItem(String id, String name, double mapX, double mapY, boolean moved, Slice slice) {}
}
