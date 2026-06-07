package util;


import event.StageSizeChange;
import javafx.scene.layout.Region;

import static core.CoreAPI.bus;

public class PaneSizeManager {
    public static void add(Region region, double widthMultiply, double heightMultiply) {
        bus.subscribe(StageSizeChange.class, (StageSizeChange event) -> {
            region.setMaxWidth(event.width()*widthMultiply);
            region.setMaxHeight(event.height()*heightMultiply);
            region.setMinWidth(event.width()*widthMultiply);
            region.setMinHeight(event.height()*heightMultiply);
            region.setPrefWidth(event.width()*widthMultiply);
            region.setPrefHeight(event.height()*heightMultiply);
        });
    }
    public static void add(Region region, double multiply) {
        add(region, multiply, multiply);
    }
    public static void set(Region region, double width, double height, double widthMultiply, double heightMultiply) {
        region.setMaxWidth(width*widthMultiply);
        region.setMaxHeight(height*heightMultiply);
        region.setMinWidth(width*widthMultiply);
        region.setMinHeight(height*heightMultiply);
        region.setPrefWidth(width*widthMultiply);
        region.setPrefHeight(height*heightMultiply);
    }
    public static void set(Region region, double width, double height, double multiply) {
        set(region, width, height, multiply, multiply);
    }
    public static void set(Region region, double width, double height) {
        set(region, width, height, 1);
    }
}
