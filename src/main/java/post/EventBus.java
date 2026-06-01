package post;

import game.Game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


public class EventBus {
    
    // 监听器 Map，线程安全
    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();
    
    // 类型缓存，提高 dispatch 性能
    private final Map<Class<?>, Set<Class<?>>> typeCache = new ConcurrentHashMap<>();
    
    /**
     * 订阅事件
     * 返回 Subscription，可在 unload 时取消订阅
     */
    public <T> Subscription subscribe(Class<T> eventType, Consumer<T> listener) {
        // 包装 listener，保证 async 任务安全
        SafeListener<T> safeListener = new SafeListener<>(listener);
        
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(safeListener);
        
        // 返回可退订句柄
        return () -> {
            safeListener.active = false;
            unsubscribe(eventType, safeListener);
        };
    }
    
    /**
     * 取消订阅
     */
    private <T> void unsubscribe(Class<T> eventType, Consumer<?> listener) {
        List<Consumer<?>> list = listeners.get(eventType);
        if (list != null) list.remove(listener);
    }
    
    /**
     * 同步发布事件：立即调用监听器
     */
    public <T> void publish(T event) {
        dispatch(event, false);
    }
    
    /**
     * 异步发布事件：立即返回，由线程池执行
     */
    public <T> void publishAsync(T event) {
        dispatch(event, true);
    }
    /**
     * 核心分发逻辑
     */
    @SuppressWarnings("unchecked")
    private <T> void dispatch(T event, boolean async) {
        Class<?> eventType = event.getClass();
        
        // 获取所有相关类型（自身 + 父类 + 接口）
        for (Class<?> type : getAllEventTypes(eventType)) {
            List<Consumer<?>> list = listeners.get(type);
            if (list == null) continue;
            
            for (Consumer<?> c : list) {
                Consumer<T> handler = (Consumer<T>) c;
                if (handler instanceof SafeListener<T> sl) {
                    if (!sl.active) continue;
                    if (async) Game.mainPool.submit(() -> sl.listener.accept(event));
                    else try {
                        sl.listener.accept(event);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                } else {
                    if (async) Game.mainPool.submit(() -> handler.accept(event));
                    else handler.accept(event);
                }
            }
        }
    }
    
    /**
     * 获取事件类型的全部相关类型（父类 + 接口 + 自身）
     * 缓存计算结果，提高性能
     */
    private Set<Class<?>> getAllEventTypes(Class<?> cls) {
        return typeCache.computeIfAbsent(cls, this::computeAllEventTypes);
    }
    
    private Set<Class<?>> computeAllEventTypes(Class<?> cls) {
        Set<Class<?>> set = new LinkedHashSet<>();
        Queue<Class<?>> queue = new LinkedList<>();
        queue.add(cls);
        
        while (!queue.isEmpty()) {
            Class<?> c = queue.poll();
            if (c == null || c == Object.class) continue;
            if (set.add(c)) {
                queue.add(c.getSuperclass());
                queue.addAll(Arrays.asList(c.getInterfaces()));
            }
        }
        return set;
    }
    
    /**
     * 安全包装监听器
     */
    private static class SafeListener<T> implements Consumer<T> {
        final Consumer<T> listener;
        volatile boolean active = true;
        
        SafeListener(Consumer<T> listener) {
            this.listener = listener;
        }
        
        @Override
        public void accept(T t) {
            if (active) listener.accept(t);
        }
    }
    
    /**
     * 订阅句柄，用于退订
     */
    public interface Subscription {
        void unsubscribe();
    }
}
