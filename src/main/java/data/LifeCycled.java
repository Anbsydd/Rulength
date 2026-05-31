package data;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.ScrollEvent;

import java.util.ArrayList;
import java.util.List;

public interface LifeCycled {
    List<Runnable> disposables = new ArrayList<>();
    /** 注册一个卸载动作 */
    default void register(Runnable disposer) {
        disposables.add(disposer);
    }
    /** 子类实现：加载资源 */
    void load();
    
    /** 子类实现：卸载前逻辑（可选） */
    void unload();
    
    /**
    * 默认的加载方法，用于执行资源的加载操作
    * 使用递归方式实现，但存在潜在风险
    */
    default void onLoad() {
        load();
    }
    default void onUnload() {
        unload();
        
        for (Runnable d : disposables) {
            try {
                d.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    default void reset(){
        onUnload();
        disposables.clear();
    }
    
    
    
}
