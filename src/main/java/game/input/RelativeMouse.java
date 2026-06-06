package game.input;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

import java.awt.*;

/**
 * 相对鼠标模式工具类
 * <p>
 * 拖拽期间隐藏鼠标，每帧通过Robot将OS光标归位到窗口中心，
 * 使MouseEvent产生持续的中心偏移量，实现无边界拖拽。
 * <p>
 * 用法：
 * pressed()  → {@link #enter(Scene)}
 * dragged()  → pollDeltaX/Y(...) → 应用位移 → {@link #warpBack()}
 * released() → {@link #exit(Scene)}
 */
public class RelativeMouse {

    private static boolean active = false;
    private static Scene scene;
    private static Robot robot;
    /** 窗口中心的场景坐标X */
    private static double centerSceneX;
    /** 窗口中心的场景坐标Y */
    private static double centerSceneY;

    /**
     * 进入相对鼠标模式：
     * 保存窗口中心坐标，隐藏光标，初始化Robot，将OS光标归位到窗口中心
     */
    public static void enter(Scene s) {
        if (active) return;
        scene = s;
        centerSceneX = scene.getWidth() / 2.0;
        centerSceneY = scene.getHeight() / 2.0;

        if (centerSceneX <= 0 || centerSceneY <= 0) return;

        if (robot == null) {
            try {
                robot = new Robot();
            } catch (AWTException e) {
                System.err.println("RelativeMouse: Robot初始化失败: " + e.getMessage());
                return;
            }
        }

        // 第一次归位：将OS光标拉到窗口中心（后续每帧由warpBack保持）
        Point2D screenPt = scene.getRoot().localToScreen(centerSceneX, centerSceneY);
        if (screenPt == null) return;
        robot.mouseMove((int) screenPt.getX(), (int) screenPt.getY());

        scene.setCursor(Cursor.NONE);
        active = true;
    }

    /**
     * 获取当前帧的场景坐标X增量（事件场景X - 窗口中心X）
     */
    public static double pollDeltaX(MouseEvent event) {
        if (!active) return 0;
        return event.getSceneX() - centerSceneX;
    }

    /**
     * 获取当前帧的场景坐标Y增量（事件场景Y - 窗口中心Y）
     */
    public static double pollDeltaY(MouseEvent event) {
        if (!active) return 0;
        return event.getSceneY() - centerSceneY;
    }

    /**
     * 将OS光标归位到窗口中心。
     * 每帧dragged处理完增量位移后调用一次。
     */
    public static void warpBack() {
        if (!active || robot == null || scene == null) return;
        Point2D screenPt = scene.getRoot().localToScreen(centerSceneX, centerSceneY);
        if (screenPt == null) return;
        robot.mouseMove((int) screenPt.getX(), (int) screenPt.getY());
    }

    /**
     * 退出相对鼠标模式：显示光标，清理状态
     */
    public static void exit(Scene s) {
        if (!active) return;
        active = false;
        s.setCursor(Cursor.DEFAULT);
    }

    /** 当前是否处于相对鼠标模式 */
    public static boolean isActive() {
        return active;
    }
}
