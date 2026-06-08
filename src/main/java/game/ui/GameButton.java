package game.ui;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * 工业风格按钮 — 替代默认 JavaFX Button
 * <p>
 * 根据 UI设计.md 规范：
 * - 默认：深铜色、微渐变、边缘高光
 * - Hover：微发光、边框变亮
 * - Pressed：内陷感
 */
public class GameButton extends Button {

    private static final DropShadow GLOW_SHADOW = new DropShadow(6, Color.rgb(231, 193, 90, 0.25));

    /**
     * @param text 按钮文字
     * @param accentColor 发光强调色（如 #E7C15A 电力黄 / #4CC9F0 科技蓝）
     * @param bgBase 背景基色（如 #7A5A32 深铜色）
     */
    public GameButton(String text, String accentColor, String bgBase) {
        super(text);
        setCursor(Cursor.HAND);
        applyDefaultStyle(accentColor, bgBase);

        setOnMouseEntered(e -> {
            setStyle(buildStyle(accentColor, bgBase, true));
            setEffect(GLOW_SHADOW);
        });
        setOnMouseExited(e -> {
            applyDefaultStyle(accentColor, bgBase);
            setEffect(null);
        });
        setOnMousePressed(e -> {
            setStyle(buildStyle(accentColor, bgBase, false)
                    + "-fx-translate-y: 1px;"
                    + "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.5), 4, 0, 0, 2);");
        });
        setOnMouseReleased(e -> {
            applyDefaultStyle(accentColor, bgBase);
            if (isHover()) setEffect(GLOW_SHADOW);
        });
    }

    private void applyDefaultStyle(String accentColor, String bgBase) {
        setStyle(buildStyle(accentColor, bgBase, false));
    }

    private static String buildStyle(String accentColor, String bgBase, boolean hover) {
        String bg;
        String border;
        if (hover) {
            bg = "linear-gradient(to bottom, derive(" + bgBase + ", 30%), " + bgBase + ")";
            border = "derive(" + accentColor + ", 20%)";
        } else {
            bg = "linear-gradient(to bottom, derive(" + bgBase + ", 15%), " + bgBase + ")";
            border = "derive(" + bgBase + ", -20%)";
        }
        return "-fx-background-color: " + bg + ";"
                + "-fx-background-radius: 4;"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: 1.5;"
                + "-fx-border-radius: 4;"
                + "-fx-text-fill: " + (hover ? "white" : "#E6E6E6") + ";"
                + "-fx-font-family: 'Microsoft YaHei';"
                + "-fx-font-size: 13px;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 6 16 6 16;";
    }

    /** 快捷工厂：电力黄按钮 */
    public static GameButton power(String text) {
        return new GameButton(text, UITheme.GLOW_POWER, UITheme.COPPER_DARK);
    }

    /** 快捷工厂：科技蓝按钮 */
    public static GameButton tech(String text) {
        return new GameButton(text, UITheme.GLOW_TECH, UITheme.COPPER_DARK);
    }

    /** 快捷工厂：危险红按钮 */
    public static GameButton danger(String text) {
        return new GameButton(text, UITheme.GLOW_DANGER, "#8B2E36");
    }

    /** 快捷工厂：正常绿按钮 */
    public static GameButton success(String text) {
        return new GameButton(text, UITheme.GLOW_NORMAL, "#3D6B2E");
    }

    /** 快捷工厂：灰白次要按钮 */
    public static GameButton secondary(String text) {
        return new GameButton(text, "#888888", "#444444");
    }
}
