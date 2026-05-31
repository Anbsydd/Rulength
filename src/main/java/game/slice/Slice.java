package game.slice;

import data.LifeCycled;
import javafx.scene.control.Button;
public class Slice extends Button implements LifeCycled, TextSized {
    protected boolean loaded=false;
    public Slice() {
        super();
    }
    
    @Override
    public void load() {
        if (loaded) return;
        loaded=true;
    }
    
    @Override
    public void unload() {
        if (!loaded) return;
        loaded=false;
    }
    public void setSize(double size) {
        TextSized.super.setSize(size);
    }
}
