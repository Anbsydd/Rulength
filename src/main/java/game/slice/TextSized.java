package game.slice;

import javafx.scene.control.Labeled;

/**
 * 可设置字体大小的接口，适用于 Labeled 控件（Button, Label 等）
 */
public interface TextSized {

    default void setSize(double size) {
        String s = this.getStyle();
        if (s.contains("-fx-font-size:")) s = s.replaceFirst("(-fx-font-size:)\s*[^;]*", "$1 " + (int) size + ";");
        else s += "-fx-font-size:" + (int) size + ";";
        ((Labeled) this).setStyle(s);
    }

    String getStyle();
}