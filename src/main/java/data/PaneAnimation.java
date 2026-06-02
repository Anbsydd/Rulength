package data;

import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class PaneAnimation {
    TranslateTransition t;
    ScaleTransition s;
    ParallelTransition p;
    public PaneAnimation(Node node){
        t=new TranslateTransition(Duration.millis(100),node);;
        s=new ScaleTransition(Duration.millis(100),node);
        p=new ParallelTransition(t,s);
    }
    
    public ParallelTransition moveTo(double x, double y,double scale) {
        p.stop();
        t.setToX(x);
        t.setToY(y);
        s.setToX(scale);
        s.setToY(scale);
        p.play();
        return p;
    }
    public ParallelTransition moveTo(double scale) {
        p.stop();
        s.setToX(scale);
        s.setToY(scale);
        p.play();
        return p;
    }
    public ParallelTransition moveTo(double x, double y) {
        p.stop();
        t.setToX(x);
        t.setToY(y);
        p.play();
        return p;
    }
    public ParallelTransition stop() {
        p.stop();
        return p;
    }
}
