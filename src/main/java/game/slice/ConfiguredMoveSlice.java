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
        // 设置样式：带边框和背景色，便于可视化测试
        setStyle("-fx-background-color: rgba(0,150,255,0.6); -fx-border-color: blue; -fx-border-width: 2; -fx-text-fill: white;");
    }
    
    
    /**
     * 获取关联的SliceConfig
     */
    public SliceConfig getConfig() {
        return config;
    }
}
