package game;

import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Camera extends StackPane {
    private StackPane root;
    public Camera(StackPane root) {
        this.root = root;
    }
    public static double scale = 1;
    private double minScale = 1;
    private double maxScale = 2000000000;
    private double traX = 0;
    private double traY = 0;
    private final double zoomFactor = 1.4;
    ParallelTransition pt;
    /**视角的控制与事件发送
     * 监听鼠标
     */
    // 右键按下时的鼠标屏幕坐标
    private double dragStartMouseX,dragStartMouseY;
    // 右键按下时的地图初始偏移（相对于屏幕中心）
    private double oldTraX,oldTraY;
    public void pressed(MouseEvent e) {
        if (!e.isSecondaryButtonDown()) return;
        pt.stop();
        dragStartMouseX = e.getScreenX();
        dragStartMouseY = e.getScreenY();
        oldTraX = getTranslateX();
        oldTraY = getTranslateY();
    }
    public Move dragged(MouseEvent e) {
        if (!e.isSecondaryButtonDown()) return null;
        pt.stop();
        double deltaX = e.getScreenX() - dragStartMouseX;
        double deltaY = e.getScreenY() - dragStartMouseY;
        double newTraX = oldTraX + deltaX;
        double newTraY = oldTraY + deltaY;
        newTraX = clampMapTraX(newTraX);
        newTraY = clampMapTraY(newTraY);
        if (newTraX != oldTraX || newTraY != oldTraY) {
            return new Move(scale,newTraX,newTraY,1);
        }
        return null;
    }
    public record Move(double scale, double traX, double traY, double ms){}
    private double clampMapTraX(double traX) {
        double mapHalfWidth =  root.getWidth() * scale / 2.0;
        double screenCenterX = root.getWidth() / 2.0;
        double minTraX = screenCenterX - mapHalfWidth;
        double maxTraX = mapHalfWidth - screenCenterX;
        return Math.max(minTraX, Math.min(maxTraX, traX));
    }
    private double clampMapTraY(double traY) {
        double mapHalfHeight = root.getHeight() * scale / 2.0;
        double screenCenterY = root.getHeight() / 2.0;
        double minTraY = screenCenterY - mapHalfHeight;
        
        double maxTraY = mapHalfHeight - screenCenterY;
        return Math.max(minTraY, Math.min(maxTraY, traY));
    }
    public Scaling scrolled(ScrollEvent event){
        double changeScale = checkScalingRequirements(event);
        if(scale != changeScale) return new Scaling(scale, changeScale/scale);
        return null;
    }
    public record Scaling(double oldScale, double realScale){}
    private double checkScalingRequirements(ScrollEvent event) {
        double changeScale = scale;
        changeScale = event.getDeltaY() > 0? changeScale * zoomFactor:changeScale /zoomFactor;
        changeScale = Math.max(minScale, Math.min(changeScale, maxScale));
        return changeScale;
    }
    private Move executeScale(double oldScale, double needToScale) {
        if (needToScale ==1 ) return null;
        double[] newTra = checkTranslationForScaling(needToScale);
        scale = oldScale*needToScale;
        return new Move(scale,newTra[0], newTra[1],300);
    }
    private double[] checkTranslationForScaling(double needToScale) {
        //根据中心位置缩放屏幕，将map放大缩小。
        //缩放中心相对于屏幕中心的位置（eTraX，eTraY），
        // map中心相对于屏幕中心的位置（oldTraX，oldTraY）即将放大或缩小的倍数realScale，
        // 已经缩放的倍数scale，屏幕大小（defaultWidth，defaultHeight）
        // 计算缩放完毕后map中心相对于屏幕的位置（newTraX，newTraY）。
        // map必须完全覆盖屏幕，即map的最左边不能在屏幕最左边的右边，其他边以此类推。
        double x = checkTranslationXForScaling(0, needToScale);
        double y = checkTranslationYForScaling(0, needToScale);
        return new double[]{x,y};
    }
    private double checkTranslationXForScaling(double centerX,double needToScale){
        double oldTraX = getTranslateX();
        double newTraX = oldTraX + (centerX - oldTraX) * (1 - needToScale);
        double minTraX = (root.getWidth() / 2.0) * (1 - scale*needToScale);
        double maxTraX = (root.getWidth() / 2.0) * (scale*needToScale - 1);
        return Math.max(minTraX, Math.min(maxTraX, newTraX));
    }
    private double checkTranslationYForScaling(double centerY,double needToScale){
        double oldTraY = getTranslateY();
        double newTraY = oldTraY + (centerY - oldTraY) * (1 - needToScale);
        double minTraY = (root.getHeight() / 2.0) * (1 - scale*needToScale);
        double maxTraY = (root.getHeight() / 2.0) * (scale*needToScale - 1);
        return Math.max(minTraY, Math.min(maxTraY, newTraY));
    }
    private void move(double scale, double newTraX, double newTraY, int timeMillis){
        stopTransition();
        this.traX = newTraX;
        this.traY = newTraY;
        ScaleTransition st = new ScaleTransition(Duration.millis(timeMillis), this);
        st.setToX(scale);
        st.setToY(scale);
        TranslateTransition tt = new TranslateTransition(Duration.millis(timeMillis), this);
        tt.setToX(newTraX);
        tt.setToY(newTraY);
        pt = new ParallelTransition(st, tt);
        pt.play();
    }
    private void stopTransition() {
        if (pt != null) pt.stop();
    }
}