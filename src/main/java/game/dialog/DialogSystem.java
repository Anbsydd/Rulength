package game.dialog;

import com.fasterxml.jackson.databind.ObjectMapper;
import event.StageSizeChange;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import util.TextLan;

import java.io.File;
import java.util.List;

import static core.CoreAPI.bus;

/**
 * 对话系统
 * <p>
 * 全屏覆盖层，底部固定黑色半透明蒙版，
 * 蒙版内显示说话者头像（拍摄自游戏中对应的 Slice）、名称和对话文本。
 * 文本超出蒙版高度时可通过滚轮滚动查看。
 * 点击任意位置推进到下一句，最后一句点击后关闭。
 */
public class DialogSystem extends StackPane {

    // ============ 布局常量 ============
    private static final double BOTTOM_RATIO = 0.4;
    private static final String OVERLAY_STYLE = "-fx-background-color: rgba(0,0,0,0.6);";
    private static final String AVATAR_FRAME_STYLE ="";

    private static final double AVATAR_SIZE = 120;
    private static final double AVATAR_LEFT = 40;
    private static final double AVATAR_TOP = 30;
    private static final double AVATAR_TEXT_GAP = 20;
    private static final double SPEAKER_FONT = 18;
    private static final double TEXT_FONT = 24;
    private static final double NAME_TEXT_GAP = 10;
    private static final double TEXT_RIGHT = 40;
    private static final double SCROLLBAR_WIDTH = 8;

    // ============ UI 组件 ============
    private final Pane overlay;              // Pane 支持子元素绝对定位
    private final StackPane avatarFrame;
    private final ImageView avatarView;
    private final Label speakerLabel;
    private final ScrollPane scrollPane;
    private final Label dialogTextLabel;

    // ============ 状态 ============
    private List<DialogEntry> dialogs;
    private int currentIndex = -1;
    private double vw = 1280;
    private double vh = 720;

    // ============ 内部配置类 ============

    public static class DialogConfig {
        public List<DialogEntry> dialogs;
    }

    public static class DialogEntry {
        public String id;
        public String speakerRef;
        public String text;
    }

    // ==================== 构造 ====================

    public DialogSystem() {
        setPickOnBounds(true);
        setVisible(false);
        

        // --- 底部黑色蒙版：Pane 固定在底部，高度 40% ---
        overlay = new Pane();
        overlay.setStyle(OVERLAY_STYLE);
        overlay.setMaxHeight(Region.USE_PREF_SIZE);
        setAlignment(this, Pos.BOTTOM_CENTER);
        getChildren().add(overlay);

        // 头像外框
        avatarFrame = new StackPane();
        avatarFrame.setStyle(AVATAR_FRAME_STYLE);
        overlay.getChildren().add(avatarFrame);

        avatarView = new ImageView();
        avatarView.setPreserveRatio(true);
        avatarFrame.getChildren().add(avatarView);

        // 说话者名称
        speakerLabel = new Label();
        speakerLabel.setTextFill(Color.WHITE);
        speakerLabel.setFont(Font.font("SimSun", SPEAKER_FONT));
        overlay.getChildren().add(speakerLabel);

        // 文本滚动容器
        scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background: transparent; -fx-background-color: transparent;" +
                "-fx-border-color: transparent; -fx-padding: 0;"
        );
        overlay.getChildren().add(scrollPane);

        dialogTextLabel = new Label();
        dialogTextLabel.setTextFill(Color.WHITE);
        dialogTextLabel.setWrapText(true);
        dialogTextLabel.setTextAlignment(TextAlignment.LEFT);
        scrollPane.setContent(dialogTextLabel);

        // 窗口大小变化
        bus.subscribe(StageSizeChange.class, e -> {
            vw = e.width();
            vh = e.height();
            if (isVisible()) reposition();
        });

        // 鼠标点击推进
        addEventFilter(MouseEvent.MOUSE_CLICKED, e -> nextDialog());
    }

    // ==================== 公开方法 ====================

    public void showDialog(String jsonPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DialogConfig config = mapper.readValue(new File(jsonPath), DialogConfig.class);
            if (config.dialogs == null || config.dialogs.isEmpty()) {
                System.err.println("DialogSystem: 对话文件 " + jsonPath + " 为空");
                return;
            }
            this.dialogs = config.dialogs;
            currentIndex = -1;
            setVisible(true);
            toFront();
            nextDialog();
        } catch (Exception e) {
            System.err.println("DialogSystem: 加载对话文件 " + jsonPath + " 失败");
            e.printStackTrace();
        }
    }

    public void nextDialog() {
        currentIndex++;
        if (dialogs == null || currentIndex >= dialogs.size()) {
            hide();
            return;
        }
        DialogEntry entry = dialogs.get(currentIndex);
        updateAvatar(entry.speakerRef);
        speakerLabel.setText(entry.speakerRef);
        dialogTextLabel.setText(TextLan.get(entry.text));
        scrollPane.setVvalue(0);
        if (isVisible()) reposition();
    }

    public void hide() {
        dialogs = null;
        currentIndex = -1;
        setVisible(false);
    }

    // ==================== 私有方法 ====================

    private void updateAvatar(String speakerRef) {
        // 通过 name 查找默认配置（不是 ID，dialog 中 speakerRef 是显示名）
        config.SliceConfig def = null;
        for (config.SliceConfig d : config.SliceInjector.getAll()) {
            if (speakerRef.equals(d.name)) {
                def = d;
                break;
            }
        }
        if (def == null) {
            avatarFrame.setVisible(false);
            return;
        }
        avatarFrame.setVisible(true);

        // 创建一个临时 Button，手动应用样式后快照
        javafx.scene.control.Button temp = new javafx.scene.control.Button(def.name);
        double w = def.text.width;
        double h = def.text.height;
        temp.setPrefSize(w, h);
        temp.setMaxSize(w, h);
        temp.setMinSize(w, h);

        StringBuilder style = new StringBuilder();
        style.append("-fx-background-color: ").append(def.text.backgroundColor).append(";");
        style.append("-fx-border-color: ").append(def.text.borderColor).append(";");
        style.append("-fx-border-width: ").append(Math.max(1, (int) Math.round(def.text.borderWidth))).append(";");
        style.append("-fx-border-radius: ").append(Math.max(0, (int) Math.round(def.text.borderRadius))).append(";");
        style.append("-fx-background-radius: ").append(Math.max(0, (int) Math.round(def.text.borderRadius))).append(";");
        style.append("-fx-text-fill: ").append(def.text.textColor).append(";");
        style.append("-fx-padding: ").append(Math.max(0, (int) Math.round(def.text.insertTop))).append(" ")
                .append(Math.max(0, (int) Math.round(def.text.insertRight))).append(" ")
                .append(Math.max(0, (int) Math.round(def.text.insertBottom))).append(" ")
                .append(Math.max(0, (int) Math.round(def.text.insertLeft))).append(";");
        style.append("-fx-font-size: ").append(Math.max(1, (int) Math.round(def.text.fontSize))).append("px;");
        style.append("-fx-focus-color: transparent;");
        style.append("-fx-faint-focus-color: transparent;");
        style.append("-fx-highlight-fill: transparent;");
        temp.setStyle(style.toString());
        temp.setWrapText(def.text.wrapText);
        temp.setOpacity(def.opacity);

        // 临时加入场景 → 快照 → 移除
        overlay.getChildren().add(temp);
        try {
            temp.applyCss();
            temp.layout();
            SnapshotParameters sp = new SnapshotParameters();
            sp.setFill(Color.TRANSPARENT);
            WritableImage snapshot = temp.snapshot(sp, null);
            avatarView.setImage(snapshot);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            overlay.getChildren().remove(temp);
        }
    }

    private void reposition() {
        double overlayH = vh * BOTTOM_RATIO;
        double overlayW = vw;

        overlay.setPrefSize(overlayW, overlayH);

        // 头像
        double avatarSize = Math.min(AVATAR_SIZE, overlayH * 0.45);
        avatarFrame.setLayoutX(AVATAR_LEFT);
        avatarFrame.setLayoutY(AVATAR_TOP);
        avatarFrame.setPrefSize(avatarSize, avatarSize);
        avatarFrame.setMaxSize(avatarSize, avatarSize);
        avatarView.setFitWidth(avatarSize);
        avatarView.setFitHeight(avatarSize);

        // 说话者名称：头像右侧
        double labelX = AVATAR_LEFT + avatarSize + AVATAR_TEXT_GAP;
        double labelY = AVATAR_TOP + 5;
        speakerLabel.setLayoutX(labelX);
        speakerLabel.setLayoutY(labelY);

        // 文本滚动区域
        double scrollX = labelX;
        double scrollY = labelY + SPEAKER_FONT + NAME_TEXT_GAP;
        double scrollWidth = overlayW - scrollX - TEXT_RIGHT;
        double scrollHeight = overlayH - scrollY - 10;
        double textFont = Math.min(TEXT_FONT, overlayH * 0.06);

        scrollPane.setLayoutX(scrollX);
        scrollPane.setLayoutY(scrollY);
        scrollPane.setPrefSize(scrollWidth, scrollHeight);
        scrollPane.setMaxSize(scrollWidth, scrollHeight);

        dialogTextLabel.setPrefWidth(scrollWidth - SCROLLBAR_WIDTH - 4);
        dialogTextLabel.setFont(Font.font("SimSun", textFont));
    }
}
