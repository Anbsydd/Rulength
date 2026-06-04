package data;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;
import post.ui.MapDraggedEvent;
import post.ui.MapScrolledEvent;

import static game.Game.bus;

public abstract class Slice extends Button implements LifeCycled, TextSized ,ShouldBeTranslated {
    protected boolean loaded=false;
    // 拖动相关字段
    protected double lastMouseX;
    protected double lastMouseY;
    protected double lastTraX;
    protected double lastTraY;;
    protected DoubleProperty mapX;
    protected DoubleProperty mapY;
    boolean isDragging = false;
    public boolean canBeDragged = true;
    StringProperty name = new SimpleStringProperty("");
    TranslateTransition t;
    ParallelTransition p;
    protected double[] size;
    abstract public double finalTraToMapX(double traX);
    abstract public double finalTraToMapY(double traY);
    abstract public double finalMapToTraX(double mapX);
    abstract public double finalMapToTraY(double mapY);
    public Slice() {
        super();
        mapX = new SimpleDoubleProperty(0);
        mapY = new SimpleDoubleProperty(0);
        t = new TranslateTransition(Duration.millis(100),this);
        p= new ParallelTransition(t);
        name.addListener((observable, oldValue, newValue) -> setText(newValue));
        mapX.addListener((observable, oldValue, newValue) -> {
            setTranslateX(finalMapToTraX(newValue.doubleValue()));
        });
        mapY.addListener((observable, oldValue, newValue) -> {
            setTranslateY(finalMapToTraY(newValue.doubleValue()));
        });
        bus.subscribe(MapDraggedEvent.class, e-> {
            p.stop();
            isDragging = false;
        });
        bus.subscribe(MapScrolledEvent.class, e-> {
            isDragging = false;
        });
    }
    
    public Slice(double X, double Y) {
        this();
        setLocation(X,Y);
    }
    
    @Override
    public void load() {
        if (loaded) return;
        addFilters();
        loaded=true;
    }
    protected void addFilters() {
        EventHandler<ScrollEvent> scrolled = e -> bus.publish(new post.camera.Scrolled(e));
        EventHandler<MouseEvent> pressed = e ->  {
            if (e.getButton() == MouseButton.PRIMARY  ) pressed(e);
            if (e.getButton() == MouseButton.SECONDARY) bus.publish(new post.camera.Pressed(e));
        };
        EventHandler<MouseEvent> dragged = e -> {
            if (e.getButton() == MouseButton.PRIMARY  ) dragged(e);
            if (e.getButton() == MouseButton.SECONDARY) bus.publish(new post.camera.Dragged(e));
        };
        EventHandler<MouseEvent> released = e -> {
            if (e.getButton() == MouseButton.PRIMARY  ) released();
            if (e.getButton() == MouseButton.SECONDARY) bus.publish(new post.camera.Released(e));
        };
//        addAndRegisterEventFilter(MouseEvent.MOUSE_ENTERED, enter);
//        addAndRegisterEventFilter(MouseEvent.MOUSE_EXITED, exit);
        addAndRegisterEventFilter(ScrollEvent.SCROLL,scrolled);
        addAndRegisterEventFilter(MouseEvent.MOUSE_PRESSED,pressed);
        addAndRegisterEventFilter(MouseEvent.MOUSE_DRAGGED,dragged);
        addAndRegisterEventFilter(MouseEvent.MOUSE_RELEASED,released);
    }
    
    protected void pressed(MouseEvent e) {
        isDragging = canBeDragged;
        recordXAY(e);
    }
    protected void recordXAY(MouseEvent e) {
        lastMouseX = e.getSceneX();
        lastMouseY = e.getSceneY();
        lastTraX = getTranslateX();
        lastTraY = getTranslateY();
    }
    protected void released(){
        isDragging = false;
    }
    abstract protected void dragged(MouseEvent e) ;
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
    
    public void setLocation(double x, double y) {
        setMapX(x);
        setMapY(y);
    }
    public ParallelTransition moveTo(double x,double y) {
        p.stop();
        t.setToX(x);
        t.setToY(y);
        p.play();
        return p;
    }
    
    public void setMapX(double mapX) {
        this.mapX.set(mapX);
    }
    
    public void setMapY(double mapY) {
        this.mapY.set(mapY);
    }
    public double getMapX() {
        return mapX.get();
    }
    public DoubleProperty mapXProperty() {
        return mapX;
    }
    public double getMapY() {
        return mapY.get();
    }
    public DoubleProperty mapYProperty() {
        return mapY;
    }
}
