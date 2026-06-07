package game.slice;

import event.MapTransformEvent;
import event.StageSizeChange;
import event.input.MouseDragged;
import event.input.MousePressed;
import event.input.MouseReleased;
import event.input.MouseScrolled;
import game.Game;
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

import java.awt.*;

import static core.CoreAPI.bus;

public abstract class Slice extends Button implements LifeCycled, TextSized, Coordinatable {
    protected boolean loaded = false;

    // 拖动相关字段
    protected double lastMouseX;
    protected double lastMouseY;
    protected double lastTraX;
    protected double lastTraY;
    /** 当前拖拽事件的鼠标场景坐标，供Obstruct等外部约束使用 */
    protected double currentDragSceneX;
    protected double currentDragSceneY;
    /** 当前拖拽事件的鼠标屏幕坐标，用于Robot移动系统光标 */
    protected double currentDragScreenX;
    protected double currentDragScreenY;
    /** 阻止Robot.mouseMove循环触发dragged的递归守卫 */
    private boolean cursorCorrecting = false;
    /** java.awt.Robot 实例，用于同步操作系统光标位置 */
    private Robot robot;
    protected DoubleProperty mapX;
    protected DoubleProperty mapY;
    boolean isDragging = false;
    public boolean canBeDragged = true;
    StringProperty name = new SimpleStringProperty("");

    public Slice() {
        super();
        // 取消按钮默认的焦点光环和按下发光效果
        setFocusTraversable(false);
        // 初始化 AWT Robot
        try { robot = new Robot(); } catch (AWTException ignored) {}
        mapX = new SimpleDoubleProperty(0);
        mapY = new SimpleDoubleProperty(0);
        name.addListener((observable, oldValue, newValue) -> setText(newValue));
        mapX.addListener((observable, oldValue, newValue) -> {
            setTranslateX(finalMapToTraX(newValue.doubleValue()));
        });
        mapY.addListener((observable, oldValue, newValue) -> {
            setTranslateY(finalMapToTraY(newValue.doubleValue()));
        });
        bus.subscribe(MapTransformEvent.class, e -> {
            reloadTra();
        });
        bus.subscribe(StageSizeChange.class, e -> {
//            setSize();
        });
    }

    public Slice(double X, double Y) {
        this();
        setLocation(X, Y);
    }

    @Override
    public void load() {
        if (loaded) return;
        addFilters();
        loaded = true;
    }

    protected void addFilters() {
        EventHandler<ScrollEvent> scrolled = e -> bus.publish(new MouseScrolled(e));
        EventHandler<MouseEvent> pressed = e -> {
            if (e.getButton() == MouseButton.PRIMARY) pressed(e);
            if (e.getButton() == MouseButton.SECONDARY) bus.publish(new MousePressed(e));
        };
        EventHandler<MouseEvent> dragged = e -> {
            if (e.getButton() == MouseButton.PRIMARY) dragged(e);
            if (e.getButton() == MouseButton.SECONDARY) bus.publish(new MouseDragged(e));
        };
        EventHandler<MouseEvent> released = e -> {
            if (e.getButton() == MouseButton.PRIMARY) released();
            if (e.getButton() == MouseButton.SECONDARY) bus.publish(new MouseReleased(e));
        };
        addAndRegisterEventFilter(ScrollEvent.SCROLL, scrolled);
        addAndRegisterEventFilter(MouseEvent.MOUSE_PRESSED, pressed);
        addAndRegisterEventFilter(MouseEvent.MOUSE_DRAGGED, dragged);
        addAndRegisterEventFilter(MouseEvent.MOUSE_RELEASED, released);
    }

    protected void pressed(MouseEvent e) {
        isDragging = canBeDragged;
        currentDragScreenX = e.getScreenX();
        currentDragScreenY = e.getScreenY();
        recordXAY(e.getSceneX(), e.getSceneY());
    }

    protected void recordXAY(double MouseSceneX, double MouseSceneY) {
        lastMouseX = MouseSceneX;
        lastMouseY = MouseSceneY;
        lastTraX = getTranslateX();
        lastTraY = getTranslateY();
    }

    protected void released() {
        isDragging = false;
    }

    /**
     * 根据当前 mapX/mapY 和坐标转换函数，重新计算并设置 translateX/Y
     */
    protected void reloadTra() {
        setTranslateX(finalMapToTraX(mapX.doubleValue()));
        setTranslateY(finalMapToTraY(mapY.doubleValue()));
    }

    protected void dragged(MouseEvent e) {
        if (!isDragging) return;
        currentDragSceneX = e.getSceneX();
        currentDragSceneY = e.getSceneY();
        currentDragScreenX = e.getScreenX();
        currentDragScreenY = e.getScreenY();

        // 递归守卫：跳过由Robot.mouseMove触发的drag事件
        if (cursorCorrecting) {
            cursorCorrecting = false;
            // 只同步translate锚点（适应可能的位置微小变化）
            lastTraX = getTranslateX();
            lastTraY = getTranslateY();
            // 保持 lastMouseX/Y 不变（已在主路径设为了修正后的场景坐标）
            return;
        }

        // 保存本次拖拽锚点（修正光标时需要）
        double oldTraX = lastTraX;
        double oldTraY = lastTraY;
        double oldMouseX = lastMouseX;
        double oldMouseY = lastMouseY;

        double intendedTraX = (currentDragSceneX - oldMouseX) / isMoveSlice() + oldTraX;
        double intendedTraY = (currentDragSceneY - oldMouseY) / isMoveSlice() + oldTraY;

        setMapX(finalTraToMapX(intendedTraX));
        setMapY(finalTraToMapY(intendedTraY));
        // 移动后检测碰撞（可能会通过clampToBoundary修改位置）
        Game.checkCollisions(this);

        double actualTraX = getTranslateX();
        double actualTraY = getTranslateY();
        double deltaTraX = actualTraX - intendedTraX;
        double deltaTraY = actualTraY - intendedTraY;
        boolean wasClamped = (Math.abs(deltaTraX) >= 0.5 || Math.abs(deltaTraY) >= 0.5);

        if (wasClamped) {
            // —— 被阻挡了 ——
            // 分轴处理：被阻挡的轴"吃掉"鼠标位移，可滑动的轴正常前进
            lastTraX = actualTraX;
            lastTraY = actualTraY;

            if (Math.abs(deltaTraX) >= 0.5) {
                // X轴被阻挡，不更新鼠标锚点 → 下一帧X轴位移被吃光
                lastMouseX = oldMouseX;
            } else {
                // X轴可滑动，正常更新鼠标锚点
                lastMouseX = (actualTraX - oldTraX) * isMoveSlice() + oldMouseX;
            }
            if (Math.abs(deltaTraY) >= 0.5) {
                // Y轴被阻挡，不更新鼠标锚点
                lastMouseY = oldMouseY;
            } else {
                // Y轴可滑动，正常更新
                lastMouseY = (actualTraY - oldTraY) * isMoveSlice() + oldMouseY;
            }

            // 把系统光标拉回到修正后的位置
            if (robot != null) {
                double targetScreenX = currentDragScreenX + (lastMouseX - currentDragSceneX);
                double targetScreenY = currentDragScreenY + (lastMouseY - currentDragSceneY);
                currentDragSceneX = lastMouseX;
                currentDragSceneY = lastMouseY;
                cursorCorrecting = true;
                robot.mouseMove((int) Math.round(targetScreenX), (int) Math.round(targetScreenY));
            }
        } else {
            // —— 没有阻挡 ——
            syncFullDragAnchor();
        }
    }
    abstract public double isMoveSlice();
    @Override
    public void unload() {
        if (!loaded) return;
        loaded = false;
    }

    public void setSize(double size) {
        TextSized.super.setSize(size);
    }
    
    protected void setSize(double width, double height) {
        // 设置尺寸（允许超过屏幕限制）
        setPrefSize(width, height);
        setMaxSize(width, height);
        setMinSize(width, height);
    }
    public <T extends Event> void addAndRegisterEventFilter(EventType<T> var1, EventHandler<? super T> var2) {
        addEventFilter(var1, var2);
        register(() -> removeEventFilter(var1, var2));
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getName() {
        return name.get();
    }

    public void setLocation(double x, double y) {
        setMapX(x);
        setMapY(y);
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

    /**
     * 在被外部强制移动后同步拖拽和鼠标锚点，防止下一次dragged()把位置拉回
     */
    public void syncDragAnchor() {
        lastTraX = getTranslateX();
        lastTraY = getTranslateY();
    }

    /**
     * 被外部强制移动后同步所有拖拽锚点（包括鼠标场景坐标），
     * 保证鼠标始终拖拽着slice的固定相对位置
     */
    public void syncFullDragAnchor() {
        lastTraX = getTranslateX();
        lastTraY = getTranslateY();
        lastMouseX = currentDragSceneX;
        lastMouseY = currentDragSceneY;
    }

    /** 获取本次拖拽开始前（或上次clamp后）的translate锚点X */
    public double getLastTranslateX() { return lastTraX; }
    /** 获取本次拖拽开始前（或上次clamp后）的translate锚点Y */
    public double getLastTranslateY() { return lastTraY; }
}