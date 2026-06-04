package util;


import event.StageSizeChange;
import javafx.scene.layout.Pane;

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
    public static void set(Pane pane, double width, double height, double widthMultiply, double heightMultiply) {
        pane.setMaxWidth(width*widthMultiply);
        pane.setMaxHeight(height*heightMultiply);
        pane.setMinWidth(width*widthMultiply);
        pane.setMinHeight(height*heightMultiply);
        pane.setPrefWidth(width*widthMultiply);
        pane.setPrefHeight(height*heightMultiply);
    }
    public static void set(Pane pane, double width, double height, double multiply) {
        set(pane, width, height, multiply, multiply);
    }
    public static void set(Pane pane, double width, double height) {
        set(pane, width, height, 1);
    }
}
