package game.window;

import game.slice.Player;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Move extends StackPane {
    private Player player;

    public Move() {
        super();
        initPlayer();
    }

    private void initPlayer() {
        // 创建Player实例
        player = new Player();
        player.name = "Player1";

        // 将玩家视图添加到Move层
        this.getChildren().add(player);
        player.load();
    }

    public Player getPlayer() {
        return player;
    }

}
