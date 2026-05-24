package util;


import javafx.scene.layout.Pane;
import post.ui.StageSizeChange;

import static game.Game.bus;

public class PaneSizeManager {
    public static void add(Pane pane, double widthMultiply, double heightMultiply) {
        bus.subscribe(StageSizeChange.class, (StageSizeChange event) -> {
            pane.setMaxWidth(event.width()*widthMultiply);
            pane.setMaxHeight(event.height()*heightMultiply);
            pane.setMinWidth(event.width()*widthMultiply);
            pane.setMinHeight(event.height()*heightMultiply);
            pane.setPrefWidth(event.width()*widthMultiply);
            pane.setPrefHeight(event.height()*heightMultiply);
        });
    }
    public static void add(Pane pane, double multiply) {
        add(pane, multiply, multiply);
    }
}
