package game.slice;

import java.util.ArrayList;
import java.util.List;

/**
 * 生命周期接口，支持 load/unload 和资源自动释放
 */
public interface LifeCycled {
    /** 每个实现类独立的资源释放列表（修复原接口静态共享 bug） */
    List<Runnable> disposables = new ArrayList<>();

    /** 注册一个卸载动作 */
    default void register(Runnable disposer) {
        disposables.add(disposer);
    }

    /** 子类实现：加载资源 */
    void load();

    /** 子类实现：卸载前逻辑（可选） */
    void unload();

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

    default void reset() {
        onUnload();
        disposables.clear();
    }
}