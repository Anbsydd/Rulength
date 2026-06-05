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

    public ConfiguredMoveSlice(SliceConfig config) {
        super(0, 0);
        this.config = config;
        applyConfig();
    }

    /**
     * 应用SliceConfig中的配置到当前Slice
     */
    private void applyConfig() {
        // 设置名称
        setName(config.name);
        setSize(config.width, config.height);
        bus.subscribe(StageSizeChange.class,e->{
            setSize(config.width*e.multiX(), config.height*e.multiY());
        });
        // 设置透明度
        setOpacity(config.opacity);
        // 设置clip圆角裁剪
        if (config.borderRadius > 0) {
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(config.width, config.height);
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
        setStyle(style.toString());
    }
    
    
    /**
     * 获取关联的SliceConfig
     */
    public SliceConfig getConfig() {
        return config;
    }
}
