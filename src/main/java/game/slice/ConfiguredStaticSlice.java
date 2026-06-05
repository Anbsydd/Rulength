package game.slice;

import config.SliceConfig;
import javafx.scene.shape.Rectangle;

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
    private void applyConfig() {
        // 设置尺寸（允许超过屏幕限制）
        setPrefSize(config.width, config.height);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setMinSize(0, 0);
        // 设置地图坐标
        setMapX(config.mapX);
        setMapY(config.mapY);
        // 设置透明度
        setOpacity(config.opacity);
        // 设置clip圆角裁剪
        if (config.borderRadius > 0) {
            Rectangle clip = new Rectangle(config.width, config.height);
            clip.setArcWidth(config.borderRadius * 2);
            clip.setArcHeight(config.borderRadius * 2);
            setClip(clip);
        }
        // 构建CSS样式
        StringBuilder style = new StringBuilder();
        style.append("-fx-background-color: ").append(config.backgroundColor).append(";");
        style.append("-fx-border-color: ").append(config.borderColor).append(";");
        style.append("-fx-border-width: ").append((int) config.borderWidth).append(";");
        style.append("-fx-border-radius: ").append((int) config.borderRadius).append(";");
        style.append("-fx-background-radius: ").append((int) config.borderRadius).append(";");
        style.append("-fx-text-fill: ").append(config.textColor).append(";");
        style.append("-fx-alignment: center;");
        style.append("-fx-content-display: center;");
        setStyle(style.toString());
        // 设置名称（在setStyle之后，确保文本显示）
        setName(config.name);
    }

    /**
     * 获取关联的SliceConfig
     */
    public SliceConfig getConfig() {
        return config;
    }
}
