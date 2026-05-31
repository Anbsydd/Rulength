package data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.util.Scanner;

public abstract class Slice extends Button implements LifeCycled, TextSized {
    protected boolean loaded=false;
    
    StringProperty name = new SimpleStringProperty("");
    public Slice() {
        super();
        name.addListener((observable, oldValue, newValue) -> setText(newValue));
        
    }
    
    @Override
    public void load() {
        if (loaded) return;
        addFilters();
        loaded=true;
    }
    protected void addFilters() {
        EventHandler<ScrollEvent> scrolled = e -> game.Game.bus.publish(new post.camera.Scrolled(e));
        EventHandler<MouseEvent> pressed = e ->   game.Game.bus.publish(new post.camera.Pressed(e));
        EventHandler<MouseEvent> dragged = e -> game.Game.bus.publish(new post.camera.Dragged(e));
        EventHandler<MouseEvent> released = e -> game.Game.bus.publish(new post.camera.Released(e));
//        addAndRegisterEventFilter(MouseEvent.MOUSE_ENTERED, enter);
//        addAndRegisterEventFilter(MouseEvent.MOUSE_EXITED, exit);
        addAndRegisterEventFilter(ScrollEvent.SCROLL,scrolled);
        addAndRegisterEventFilter(MouseEvent.MOUSE_PRESSED,pressed);
        addAndRegisterEventFilter(MouseEvent.MOUSE_DRAGGED,dragged);
        addAndRegisterEventFilter(MouseEvent.MOUSE_RELEASED,released);
    }
    @Override
    public void unload() {
        if (!loaded) return;
        loaded=false;
    }
    public void setSize(double size) {
        TextSized.super.setSize(size);
    }
    public  <T extends Event> void addAndRegisterEventFilter(EventType<T> var1, EventHandler<? super T> var2){
        addEventFilter(var1,var2);
        register(() -> removeEventFilter(var1,var2));
    }
    public void setName(String name) {
        this.name.set(name);
    }
    
}
