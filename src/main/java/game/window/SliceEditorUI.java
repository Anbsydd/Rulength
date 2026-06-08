package game.window;

import config.SliceConfig;
import config.SliceInjector;
import core.GameAPI;
import game.slice.ConfiguredMoveSlice;
import game.slice.Slice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.TextLan;

/**
 * Slice 编辑器 — 弹出窗口
 * <p>
 * 在设置界面中点击"编辑"按钮打开，支持：
 * - 选择定义、设置坐标/名称 → 生成新 Slice
 * - 列出当前所有 Slice，支持移除
 */
public class SliceEditorUI {

    private final Stage dialogStage;
    private final ObservableList<SliceItem> sliceList = FXCollections.observableArrayList();
    private final ComboBox<SliceConfig> defCombo;
    private final TextField nameField;
    private final TextField mapXField;
    private final TextField mapYField;
    private final CheckBox movedCheck;

    public SliceEditorUI() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle(TextLan.get("SliceEditor_Title"));
        dialogStage.setWidth(640);
        dialogStage.setHeight(520);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");

        // ========== 生成区域 ==========
        Label genTitle = new Label(TextLan.get("SliceEditor_GenerateTitle"));
        genTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        genTitle.setTextFill(Color.LIGHTCYAN);

        // 定义选择
        Label defLabel = new Label(TextLan.get("SliceEditor_SelectDef"));
        defLabel.setTextFill(Color.WHITE);
        defLabel.setMinWidth(70);

        defCombo = new ComboBox<>();
        defCombo.setPrefWidth(350);
        defCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SliceConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.ID + " (" + item.name + ")");
            }
        });
        defCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SliceConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.ID + " (" + item.name + ")");
            }
        });

        // 名称
        Label nameLabel = new Label(TextLan.get("SliceEditor_Name"));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setMinWidth(70);
        nameField = new TextField();
        nameField.setPrefWidth(150);

        // 地图 X
        Label mapXLabel = new Label(TextLan.get("SliceEditor_MapX"));
        mapXLabel.setTextFill(Color.WHITE);
        mapXField = new TextField("0");
        mapXField.setPrefWidth(100);

        // 地图 Y
        Label mapYLabel = new Label(TextLan.get("SliceEditor_MapY"));
        mapYLabel.setTextFill(Color.WHITE);
        mapYField = new TextField("0");
        mapYField.setPrefWidth(100);

        // moved
        movedCheck = new CheckBox(TextLan.get("SliceEditor_Moved"));
        movedCheck.setTextFill(Color.WHITE);

        // 选择定义时自动填充默认值
        defCombo.valueProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            if (nameField.getText().isBlank()) nameField.setText(sel.name);
            movedCheck.setSelected(sel.moved);
        });

        // 网格布局
        GridPane genGrid = new GridPane();
        genGrid.setHgap(10);
        genGrid.setVgap(8);
        genGrid.add(defLabel, 0, 0);
        genGrid.add(defCombo, 1, 0, 3, 1);
        genGrid.add(nameLabel, 0, 1);
        genGrid.add(nameField, 1, 1);
        genGrid.add(mapXLabel, 2, 1);
        genGrid.add(mapXField, 3, 1);
        genGrid.add(mapYLabel, 0, 2);
        genGrid.add(mapYField, 1, 2);
        genGrid.add(movedCheck, 2, 2, 2, 1);

        // 生成按钮
        HBox genBtnRow = new HBox(10);
        genBtnRow.setAlignment(Pos.CENTER_RIGHT);
        Button genBtn = new Button(TextLan.get("SliceEditor_Generate"));
        genBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        genBtn.setOnAction(e -> {
            SliceConfig sel = defCombo.getValue();
            if (sel == null) return;
            try {
                // 克隆定义并叠加用户输入
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
            } catch (NumberFormatException ex) {
                System.err.println("SliceEditor: 坐标格式错误");
            }
        });
        genBtnRow.getChildren().add(genBtn);

        VBox genSection = new VBox(10, genTitle, genGrid, genBtnRow);
        genSection.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 8; -fx-padding: 15;");

        // ========== 当前 Slice 列表 ==========
        Label listTitle = new Label(TextLan.get("SliceEditor_CurrentSlices"));
        listTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        listTitle.setTextFill(Color.WHITE);

        ListView<SliceItem> sliceListView = new ListView<>(sliceList);
        sliceListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SliceItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    Label info = new Label(item.id + " | " + item.name
                            + "  (" + String.format("%.0f", item.mapX) + ", " + String.format("%.0f", item.mapY) + ")"
                            + "  [" + (item.moved ? "移动" : "静态") + "]");
                    info.setTextFill(Color.WHITE);
                    info.setPrefWidth(380);
                    Button removeBtn = new Button(TextLan.get("SliceEditor_Remove"));
                    removeBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 12; -fx-cursor: hand;");
                    removeBtn.setOnAction(e2 -> {
                        GameAPI.removeSlice(item.slice);
                        sliceList.remove(item);
                    });
                    row.getChildren().addAll(info, removeBtn);
                    setGraphic(row);
                }
            }
        });

        VBox listSection = new VBox(10, listTitle, sliceListView);
        listSection.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 8; -fx-padding: 15;");
        VBox.setVgrow(sliceListView, Priority.ALWAYS);

        // ========== 关闭按钮 ==========
        Button closeBtn = new Button(TextLan.get("SliceEditor_Close"));
        closeBtn.setStyle("-fx-background-color: #888; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialogStage.close());
        HBox closeBar = new HBox(closeBtn);
        closeBar.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(genSection, listSection, closeBar);
        VBox.setVgrow(listSection, Priority.ALWAYS);

        dialogStage.setScene(new Scene(root));
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
