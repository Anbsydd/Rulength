package game.ui;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * UI 主题常量 — Rulength 深色工业规则风
 * <p>
 * 配色方案依据 UI设计.md：
 * - 主背景：深蓝黑色 #0B0F14 / #11161D / #161C24
 * - 金属边框：铜色 #7A5A32 / #A97C46 / #D7B072
 * - 发光：电力黄 #E7C15A / 科技蓝 #4CC9F0 / 危险红 #E63946 / 正常绿 #70E000
 */
public final class UITheme {

    private UITheme() {}

    // ==================== 颜色 ====================

    // -- 主背景 --
    public static final Color BG_DEEP = Color.rgb(11, 15, 20);        // #0B0F14
    public static final Color BG_DARK = Color.rgb(17, 22, 29);        // #11161D
    public static final Color BG_MID  = Color.rgb(22, 28, 36);        // #161C24
    public static final Color BG_PANEL = Color.rgb(10, 15, 20, 0.92); // panel 背景

    // -- 铜金属边框 --
    public static final String COPPER_DARK  = "#7A5A32";
    public static final String COPPER_MID   = "#A97C46";
    public static final String COPPER_LIGHT = "#D7B072";

    // -- 发光色 --
    public static final String GLOW_POWER    = "#E7C15A";  // 电力黄
    public static final String GLOW_TECH     = "#4CC9F0";  // 科技蓝
    public static final String GLOW_DANGER   = "#E63946";  // 危险红
    public static final String GLOW_NORMAL   = "#70E000";  // 正常绿

    // -- 文字 --
    public static final Color TEXT_PRIMARY = Color.rgb(215, 176, 114);  // 铜色主字
    public static final Color TEXT_SECONDARY = Color.rgb(169, 124, 70); // 铜色次字
    public static final Color TEXT_LIGHT = Color.rgb(230, 230, 230);    // 亮白
    public static final Color TEXT_DIM = Color.rgb(140, 140, 140);      // 灰色
    public static final Color TEXT_TITLE = Color.rgb(231, 193, 90);     // 电力黄标题

    // ==================== 字体 ====================

    /** UI 字体（思源黑体 / HarmonyOS Sans 回退到 Microsoft YaHei） */
    public static Font font(double size) {
        return Font.font("Microsoft YaHei", size);
    }

    public static Font fontBold(double size) {
        return Font.font("Microsoft YaHei", FontWeight.BOLD, size);
    }

    /** 技术数字字体（Orbitron / Exo 2 回退到 Consolas） */
    public static Font fontTech(double size) {
        return Font.font("Consolas", size);
    }

    // ==================== 面板样式字符串 ====================

    /** 深色面板背景 + 铜色边框 + 微发光边缘 */
    public static String panelStyle() {
        return "-fx-background-color: rgba(10,15,20,0.92);"
                + "-fx-border-color: " + COPPER_MID + ";"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 4;"
                + "-fx-background-radius: 4;"
                + "-fx-effect: dropshadow(gaussian, rgba(231,193,90,0.15), 8, 0, 0, 0);";
    }

    /** 切角面板（替代纯圆角矩形） */
    public static String panelCornerStyle() {
        return "-fx-background-color: rgba(10,15,20,0.92);"
                + "-fx-border-color: " + COPPER_DARK + " " + COPPER_LIGHT + " " + COPPER_LIGHT + " " + COPPER_DARK + ";"
                + "-fx-border-width: 2;"
                + "-fx-effect: dropshadow(gaussian, rgba(231,193,90,0.1), 6, 0, 0, 0);";
    }

    /** 输入框/下拉框样式 */
    public static String fieldStyle() {
        return "-fx-background-color: #0B0F14;"
                + "-fx-border-color: " + COPPER_DARK + ";"
                + "-fx-border-width: 1.5;"
                + "-fx-text-fill: #E6E6E6;"
                + "-fx-prompt-text-fill: #666666;"
                + "-fx-background-radius: 3;"
                + "-fx-border-radius: 3;"
                + "-fx-font-size: 13px;";
    }

    /** 滚动条样式（暗色铜边） */
    public static String scrollPaneStyle() {
        return "-fx-background: transparent;"
                + "-fx-background-color: transparent;"
                + "-fx-border-color: " + COPPER_DARK + ";"
                + "-fx-border-width: 1;";
    }
}
