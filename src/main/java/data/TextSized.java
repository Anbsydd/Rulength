package data;
import javafx.scene.control.Labeled;

public interface TextSized{
    
    default void setSize(double size) {
        String s = this.getStyle();
        if(s.contains("-fx-font-size:")) s = s.replaceFirst("(-fx-font-size:)\\s*[^;]*", "$1 " + (int) size + ";");
        else s += "-fx-font-size:"+(int)size+";";
        ((Labeled)this).setStyle(s);
    }
    String getStyle();
}
