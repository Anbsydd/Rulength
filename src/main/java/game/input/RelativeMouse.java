package game.input;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

import java.awt.*;

/**
 * 相对鼠标模式工具类
 * <p>
 * 拖拽期间隐藏鼠标，由 Camera 的 AnimationTimer 每帧将 OS 光标归位到窗口中心，
 * 使 MouseEvent 产生持续的增量偏移，实现无边界拖拽。
 * <p>
 * delta 计算采用 OS 屏幕坐标（java.awt.MouseInfo），绕过 JavaFX 事件坐标的亚像素问题。
 * Robot.mouseMove 设置整数屏幕坐标，JavaFX 反算 sceneX/Y 时会产生 0.3~0.5px 误差，
 * 如果用 JavaFX 事件坐标算 delta，每次 warpBack 后第一帧就会引入这个噪声，肉眼可见地抖动。
 * MouseInfo 直接读取 OS 鼠标位置（像素级整数），不存在亚像素问题。
 */
public class RelativeMouse {

    private static boolean active = false;
    private static Scene scene;
    private static Robot robot;
    /** 窗口中心的场景坐标缓存，供 exit 定位使用 */
    private static double centerSceneX;
    private static double centerSceneY;
    /** 窗口中心的 OS 屏幕坐标（像素级整数，用于 MouseInfo 差值基准） */
    private static double centerScreenX;
    private static double centerScreenY;
    /** MouseInfo 上一次读取的 OS 屏幕坐标 */
    private static double lastScreenX;
    private static double lastScreenY;
    /** 按下时的鼠标场景坐标（供exit定位使用） */
    private static double pressSceneX;
    private static double pressSceneY;
    /** 按下时target在场景坐标系中的原点（供exit定位使用） */
    private static double pressTargetSceneX;
    private static double pressTargetSceneY;
    /** 被拖拽的目标节点（Camera拖拽时为null），exit时用于定位鼠标 */
    private static Node targetNode;
    /** 屏幕坐标 → 场景坐标 的转换比例（场景像素/屏幕像素） */
    private static double screenToSceneScale = 1.0;
    /** 鼠标灵敏度倍率 */
    private static double sensitivity = 1.0;

    /**
     * 设置鼠标灵敏度
     * @param s 1.0=正常，>1.0加速，<1.0减速（建议范围0.05~5.0）
     */
    public static void setSensitivity(double s) {
        sensitivity = Math.max(0.05, s);
    }

    /**
     * 进入相对鼠标模式
     *
     * @param s        JavaFX场景
     * @param target   被拖拽的Node（拖拽slice传this，Camera传null），exit时用于定位鼠标
     * @param pressX   按下时的鼠标场景X
     * @param pressY   按下时的鼠标场景Y
     */
    public static void enter(Scene s, Node target, double pressX, double pressY) {
        if (active) return;
        scene = s;
        centerSceneX = scene.getWidth() / 2.0;
        centerSceneY = scene.getHeight() / 2.0;
        if (centerSceneX <= 0 || centerSceneY <= 0) return;

        // 计算屏幕中心到场景中心的转换比例
        Point2D rootCenter = scene.getRoot().localToScreen(centerSceneX, centerSceneY);
        if (rootCenter == null) return;
        centerScreenX = rootCenter.getX();
        centerScreenY = rootCenter.getY();

        pressSceneX = pressX;
        pressSceneY = pressY;
        targetNode = target;

        if (target != null) {
            Point2D targetScene = target.localToScene(0, 0);
            pressTargetSceneX = targetScene.getX();
            pressTargetSceneY = targetScene.getY();
        } else {
            pressTargetSceneX = Double.NaN;
            pressTargetSceneY = Double.NaN;
        }

        if (robot == null) {
            try {
                robot = new Robot();
            } catch (AWTException e) {
                System.err.println("RelativeMouse: Robot初始化失败: " + e.getMessage());
                return;
            }
        }

        // 计算屏幕→场景的转换比例
        // 取场景中+0.5px的点，看屏幕坐标变化多少
        Point2D off1 = scene.getRoot().localToScreen(0, 0);
        Point2D off2 = scene.getRoot().localToScreen(1, 0);
        if (off1 != null && off2 != null && off2.getX() > off1.getX()) {
            screenToSceneScale = 1.0 / (off2.getX() - off1.getX());
        } else {
            screenToSceneScale = 1.0;
        }

        // 初始赋值：lastScreen = 当前OS鼠标位置
        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi != null) {
            lastScreenX = pi.getLocation().x;
            lastScreenY = pi.getLocation().y;
        } else {
            lastScreenX = centerScreenX;
            lastScreenY = centerScreenY;
        }

        scene.setCursor(Cursor.NONE);
        active = true;
    }

    /**
     * 获取当前帧的场景坐标X增量（基于 MouseInfo OS 屏幕坐标差值）
     */
    public static double pollDeltaX(MouseEvent event) {
        if (!active) return 0;
        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi == null) return 0;
        double curX = pi.getLocation().x;
        double raw = curX - lastScreenX;
        lastScreenX = curX;
        return raw * screenToSceneScale * sensitivity;
    }

    /**
     * 获取当前帧的场景坐标Y增量（基于 MouseInfo OS 屏幕坐标差值）
     */
    public static double pollDeltaY(MouseEvent event) {
        if (!active) return 0;
        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi == null) return 0;
        double curY = pi.getLocation().y;
        double raw = curY - lastScreenY;
        lastScreenY = curY;
        return raw * screenToSceneScale * sensitivity;
    }

    /**
     * 将OS光标归位到窗口中心，重置 OS 屏幕坐标基准。
     * <p>
     * 由 Camera 的 AnimationTimer 每帧末尾调用，不要在 dragged 中直接调用。
     */
    public static void warpBack() {
        if (!active || robot == null || scene == null) return;
        centerSceneX = scene.getWidth() / 2.0;
        centerSceneY = scene.getHeight() / 2.0;
        Point2D screenPt = scene.getRoot().localToScreen(centerSceneX, centerSceneY);
        if (screenPt == null) return;
        centerScreenX = screenPt.getX();
        centerScreenY = screenPt.getY();
        robot.mouseMove((int) centerScreenX, (int) centerScreenY);
        // warp后将OS基准重置到中心
        lastScreenX = centerScreenX;
        lastScreenY = centerScreenY;
    }

    /**
     * 退出相对鼠标模式：显示光标，将鼠标放到正确位置
     * <p>
     * 有target（Slice拖拽）时：鼠标放到"相对target保持按下时偏移"的位置
     * 无target（Camera拖拽）时：鼠标保持在窗口中心
     */
    public static void exit(Scene s) {
        if (!active) return;
        active = false;
        s.setCursor(Cursor.DEFAULT);

        // 有target时计算exit位置：鼠标应放在 当前target场景位置 + 按下时的偏移
        if (targetNode != null && robot != null && !Double.isNaN(pressTargetSceneX)) {
            Point2D curTargetScene = targetNode.localToScene(0, 0);
            if (curTargetScene != null) {
                double endSceneX = curTargetScene.getX() + (pressSceneX - pressTargetSceneX);
                double endSceneY = curTargetScene.getY() + (pressSceneY - pressTargetSceneY);
                Point2D screenPt = s.getRoot().localToScreen(endSceneX, endSceneY);
                if (screenPt != null) {
                    robot.mouseMove((int) screenPt.getX(), (int) screenPt.getY());
                }
            }
        }

        targetNode = null;
        scene = null;
    }

    /** 当前是否处于相对鼠标模式 */
    public static boolean isActive() {
        return active;
    }
}
