package game;

import config.ConfigLoader;
import config.GameConfig;
import game.window.Stage;
import javafx.application.Application;

public class App extends Application {

    private static final String GAME_CONFIG_PATH = "assets/config/gameConfig.json";

    public static void main(String[] args) {
        // 在 JavaFX 初始化前读取渲染配置，设置 Prism 系统属性
        try {
            GameConfig config = ConfigLoader.loadConfig(GAME_CONFIG_PATH, GameConfig.class);
            // 设置渲染帧率上限（Prism 刷新率）
            System.setProperty("prism.refreshRate", String.valueOf(config.maxFrameRate));
            // 设置垂直同步
            System.setProperty("prism.vsync", String.valueOf(config.vSync));
            System.out.println("App: 帧率上限=" + config.maxFrameRate + "Hz, 垂直同步=" + config.vSync);
        } catch (Exception e) {
            // 配置读取失败时使用默认值
            System.err.println("App: 无法读取渲染配置，使用默认值 (60Hz, vSync=true): " + e.getMessage());
            System.setProperty("prism.refreshRate", "60");
            System.setProperty("prism.vsync", "true");
        }
        launch(args);
    }

    @Override
    public void start(javafx.stage.Stage javafxStage) throws Exception {
        Stage stage = new Stage(javafxStage);
    }
}
