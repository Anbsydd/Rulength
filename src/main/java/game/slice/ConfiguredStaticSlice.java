package game.slice;

import config.SliceConfig;
import event.StageSizeChange;

import static game.Game.bus;

/**
 * 基于SliceConfig配置创建的静态Slice
 * moved=false时使用，不跟随地图缩放移动
 */
public class ConfiguredStaticSlice extends StaticSlice {

    private final SliceConfig config;

    public ConfiguredStaticSlice(SliceConfig config) {
        super(0, 0);
        this.config = config;
        applyConfig();
    }

    /**
     * 应用SliceConfig中的配置到当前Slice
     */
    private javafx.scene.shape.Rectangle clip;

    private void applyConfig() {
        // 设置名称
        setName(config.name);
        // 设置地图坐标
        setMapX(config.mapX);
        setMapY(config.mapY);
        setSize(config.width, config.height);
        // 设置clip圆角裁剪
        if (config.borderRadius > 0) {
            clip = new javafx.scene.shape.Rectangle(config.width, config.height);
            clip.setArcWidth(config.borderRadius * 2);
            clip.setArcHeight(config.borderRadius * 2);
            setClip(clip);
        }
        // 应用初始样式
        applyStyle(1, 1);
        // 监听窗口大小变化，同步更新尺寸、裁剪区域和样式
        bus.subscribe(StageSizeChange.class, e -> {
            double newWidth = config.width * e.multiX();
            double newHeight = config.height * e.multiY();
            setSize(newWidth, newHeight);
            setSize(config.fontSize * Math.min(e.multiX(), e.multiY()));
            // 同步更新clip裁剪矩形尺寸
            if (clip != null) {
                clip.setWidth(newWidth);
                clip.setHeight(newHeight);
                double scaledRadius = config.borderRadius * Math.min(e.multiX(), e.multiY());
                clip.setArcWidth(scaledRadius * 2);
                clip.setArcHeight(scaledRadius * 2);
            }
            // 同步更新样式（边框、字体等随缩放变化）
            applyStyle(e.multiX(), e.multiY());
        });
        // 设置自动换行
        setWrapText(config.wrapText);
        // 设置透明度
        setOpacity(config.opacity);
    }

    /**
     * 根据缩放比例应用CSS样式
     */
    private void applyStyle(double multiX, double multiY) {
        double scale = Math.min(multiX, multiY);
        StringBuilder style = new StringBuilder();
        style.append("-fx-background-color: ").append(config.backgroundColor).append(";");
        style.append("-fx-border-color: ").append(config.borderColor).append(";");
        // 边框宽度至少保留1px，避免缩小时边框消失
        style.append("-fx-border-width: ").append(Math.max(1, (int) Math.round(config.borderWidth * scale))).append(";");
        style.append("-fx-border-radius: ").append(Math.max(0, (int) Math.round(config.borderRadius * scale))).append(";");
        style.append("-fx-background-radius: ").append(Math.max(0, (int) Math.round(config.borderRadius * scale))).append(";");
        style.append("-fx-text-fill: ").append(config.textColor).append(";");
        style.append("-fx-padding: ").append(Math.max(0, (int) Math.round(config.insertTop * scale))).append(" ").append(Math.max(0, (int) Math.round(config.insertRight * scale))).append(" ").append(Math.max(0, (int) Math.round(config.insertBottom * scale))).append(" ").append(Math.max(0, (int) Math.round(config.insertLeft * scale))).append(";");
        style.append("-fx-font-size: ").append(Math.max(1, (int) Math.round(config.fontSize * scale))).append("px;");
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
