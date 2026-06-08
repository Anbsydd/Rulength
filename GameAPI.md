# GameAPI 说明文档

> ⚠️ **修改 GameAPI.java 时须同步更新本文档**

## 概述

`GameAPI` 是游戏操作 API，位于 `core` 包，与 `CoreAPI` 互补：

| API | 职责 | 示例 |
|-----|------|------|
| **CoreAPI** | 基础设施门面 | `bus`、`mainPool`、`multiX/Y`、`stageRef` |
| **GameAPI** | 游戏操作门面 | `spawnSlice`、`cameraJumpTo`、`findSlice` |

## 初始化

```java
// 在 Game 构造函数中，子系统初始化完成后调用一次
GameAPI.init(camera, miniMap, movePane, staticPane, javafxStage);
```

注入的内部引用：
- `Camera camera` — 相机实例
- `MiniMap miniMap` — 小地图实例
- `StackPane movePane` — MoveSlice 容器层
- `StackPane staticPane` — StaticSlice 容器层
- `Stage javafxStage` — JavaFX 舞台

## API 参考

### Slice 管理

| 方法 | 说明 |
|------|------|
| `spawnSlice(SliceConfig)` | 根据配置创建 Slice 并加入场景和碰撞池 |
| `removeSlice(Slice)` | 从场景和碰撞池移除 Slice |
| `findSlice(String)` | 按名称查找 Slice |
| `getAllSlices()` | 获取所有已加载的 Slice 列表 |

### Camera 操作

| 方法 | 说明 |
|------|------|
| `cameraJumpTo(double, double)` | 跳转到指定偏移（保持缩放不变） |
| `cameraFreeze()` | 冻结相机动画，停在当前位置 |
| `getCameraZoom()` | 获取当前缩放倍数 |

### 配置热更新

| 方法 | 说明 |
|------|------|
| `applyCameraConfig(CameraConfig)` | 运行时更新相机配置 |
| `applyMiniMapConfig(MiniMapConfig)` | 运行时更新小地图配置 |
| `applyStageConfig(StageConfig)` | 运行时更新窗口配置 |

## 数据流

```
SettingsUI → GameAPI.applyCameraConfig() → Camera.use()
SettingsUI → GameAPI.applyMiniMapConfig() → MiniMap.applyConfig()
SettingsUI → GameAPI.applyStageConfig() → javafxStage.setXxx()
     Game → GameAPI.init() + GameAPI.spawnSlice() → Slice 创建
DialogSystem → GameAPI.findSlice() → Slice 查找
```

## 注意事项

1. **初始化前不可调用** — `GameAPI.init()` 未调用时各静态方法可能抛出 NPE，用 `isInitialized()` 检查
2. **Slice 对象池** — `allSlices` 由 GameAPI 管理，Game 中的碰撞系统通过 `GameAPI.getAllSlices()` 读取
3. **不包含 Slice 实例方法** — `setMapX/Y`、`getName/setName` 等直接调用 `slice.method()` 即可
