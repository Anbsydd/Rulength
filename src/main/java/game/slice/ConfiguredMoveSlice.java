package game.slice;

import config.SliceConfig;
import event.StageSizeChange;

import static game.Game.bus;

/**
 * 基于SliceConfig配置创建的可移动Slice
 * moved=true时使用，跟随地图缩放移动
 */
public class ConfiguredMoveSlice extends MoveSlice {

    private final SliceConfig config;
    private final SliceConfig.TextConfig text;
    /** 圆角裁剪矩形，用于clip */
    private javafx.scene.shape.Rectangle clip;

    public ConfiguredMoveSlice(SliceConfig config) {
        super(0, 0);
        this.config = config;
        this.text = config.text;
        applyConfig();
    }

    /**
     * 应用SliceConfig中的配置到当前Slice
     */
    private void applyConfig() {
        // 设置名称
        setName(config.name);
        // 设置地图坐标
        setMapX(config.mapX);
        setMapY(config.mapY);
        // 设置文本层尺寸
        setSize(text.width, text.height);
        // 设置clip圆角裁剪
        if (text.borderRadius > 0) {
            clip = new javafx.scene.shape.Rectangle(text.width, text.height);
            clip.setArcWidth(text.borderRadius * 2);
            clip.setArcHeight(text.borderRadius * 2);
            setClip(clip);
        }
        // 应用初始样式
        applyStyle(1, 1);
        // 监听窗口大小变化，同步更新尺寸、裁剪区域和样式
        bus.subscribe(StageSizeChange.class, e -> {
            double newWidth = text.width * e.multiX();
            double newHeight = text.height * e.multiY();
            setSize(newWidth, newHeight);
            setSize(text.fontSize * Math.min(e.multiX(), e.multiY()));
            // 同步更新clip裁剪矩形尺寸
            if (clip != null) {
                clip.setWidth(newWidth);
                clip.setHeight(newHeight);
                double scaledRadius = text.borderRadius * Math.min(e.multiX(), e.multiY());
                clip.setArcWidth(scaledRadius * 2);
                clip.setArcHeight(scaledRadius * 2);
            }
            // 同步更新样式（边框、字体等随缩放变化）
            applyStyle(e.multiX(), e.multiY());
        });
        // 设置自动换行
        setWrapText(text.wrapText);
        // 设置整体透明度（外层opacity）
        setOpacity(config.opacity);
    }

    /**
     * 根据缩放比例应用CSS样式
     * 文本层样式由text配置控制，外层opacity由config.opacity控制
     */
    private void applyStyle(double multiX, double multiY) {
        double scale = Math.min(multiX, multiY);
        StringBuilder style = new StringBuilder();
        style.append("-fx-background-color: ").append(text.backgroundColor).append(";");
        style.append("-fx-border-color: ").append(text.borderColor).append(";");
        // 边框宽度至少保留1px，避免缩小时边框消失
        style.append("-fx-border-width: ").append(Math.max(1, (int) Math.round(text.borderWidth * scale))).append(";");
        style.append("-fx-border-radius: ").append(Math.max(0, (int) Math.round(text.borderRadius * scale))).append(";");
        style.append("-fx-background-radius: ").append(Math.max(0, (int) Math.round(text.borderRadius * scale))).append(";");
        style.append("-fx-text-fill: ").append(text.textColor).append(";");
        style.append("-fx-padding: ").append(Math.max(0, (int) Math.round(text.insertTop * scale))).append(" ").append(Math.max(0, (int) Math.round(text.insertRight * scale))).append(" ").append(Math.max(0, (int) Math.round(text.insertBottom * scale))).append(" ").append(Math.max(0, (int) Math.round(text.insertLeft * scale))).append(";");
        style.append("-fx-font-size: ").append(Math.max(1, (int) Math.round(text.fontSize * scale))).append("px;");
        // 取消按钮默认的聚焦光环和按下发光效果
        style.append("-fx-focus-color: transparent;");
        style.append("-fx-faint-focus-color: transparent;");
        style.append("-fx-highlight-fill: transparent;");
        setStyle(style.toString());
    }

    /**
     * 获取关联的SliceConfig
     */
    public SliceConfig getConfig() {
        return config;
    }
}
