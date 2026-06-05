package game.slice;

import config.SliceConfig;

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
        // 设置名称
        setName(config.name);
        // 设置尺寸
        setPrefSize(config.width, config.height);
        // 设置样式：带边框和背景色，便于可视化测试
        setStyle("-fx-background-color: rgba(255,150,0,0.6); -fx-border-color: orange; -fx-border-width: 2; -fx-text-fill: white;");
    }

    /**
     * 获取关联的SliceConfig
     */
    public SliceConfig getConfig() {
        return config;
    }
}
