# CodeGeeX 项目备忘录

## 项目概览
- 项目名称: Rulength (Game001)
- 技术栈: Java 17+, JavaFX, Jackson, Maven
- 模块名: Rulength (module-info.java)
- 入口: game.App → javafx.application.Application
- 窗口默认: 800×600

## 目录结构
```
cash1/
├── assets/
│   ├── config/
│   │   ├── cameraConfig.json    → CameraConfig
│   │   ├── gameConfig.json      → GameConfig
│   │   ├── miniMapConfig.json   → MiniMapConfig
│   │   ├── readme.json          → 配置字段说明
│   │   ├── settingsConfig.json  → SettingsConfig
│   │   └── stageConfig.json     → StageConfig
│   ├── slice/
│   │   ├── Test.json          → SliceConfig (测试用例)
│   │   └── registry.json      → slice注册表
│   ├── textLan/
│   │   └── Simplified Chinese.json
│   └── uiImages/
│       ├── backgrounds/ (bg.jpg, blank.png, map.png, newspaper.png, title.png)
│       └── male/ (1~4.png)
├── src/main/java/
│   ├── config/       — 配置类 + ConfigLoader + SliceInjector
│   ├── event/        — EventBus + 事件record
│   │   └── input/   — 鼠标输入事件包装
│   ├── game/         — Game主类 + App入口
│   │   └── slice/   — 游戏切片（UI元素基类体系）
│   ├── game/window/  — Camera, MiniMap, Stage
│   └── util/         — ImageManager, PaneSizeManager
└── pom.xml
```

## 启动流程
App.start() → new Stage(javafxStage) → 加载StageConfig → new Game(this)
Game构造: initRoot → initMap → initCamera → initStatic → initMove → initMiniMap
root子节点顺序: map → camera → static1 → move → miniMap（后者在上层）

## 配置体系
- ConfigLoader: JSON → Map → 反射赋值到Config类（支持int/double/boolean/String）
- GameConfig: corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity, mapImagePath, startMenuImagePath
- CameraConfig: offsetX, offsetY, zoom, minZoom, maxZoom, ZOOM_STEP, lerpDrag, lerpZoom
- MiniMapConfig: sizeRatio, margin, bgColor, borderColor, borderWidth, borderRadius, thumbOpacity, thumbImagePath, viewportStrokeColor, viewportStrokeWidth, viewportOpacity, viewportMinSize
- StageConfig: title, width, height, fullScreenExitHint
- SettingsConfig: opacity

## Slice配置注入体系
- SliceConfig: name, moved, width, height (通用属性) + extra Map (特殊属性容器)
- SliceInjector: 读取registry.json → 自动加载所有slice JSON → 反射注入通用属性 + 特殊属性归入extra
- registry.json: 记录assets/slice/下所有需要加载的JSON文件名
- 通用属性: name(String), moved(boolean,决定MoveSlice/StaticSlice), width(double), height(double), mapX(double,默认0), mapY(double,默认0), opacity(double,默认1.0), borderColor(String,默认transparent), borderWidth(double,默认0), borderRadius(double,默认0), backgroundColor(String,默认transparent), textColor(String,默认black), insertTop(double,默认0), insertRight(double,默认0), insertBottom(double,默认0), insertLeft(double,默认0), fontSize(double,默认12), wrapText(boolean,默认false)
- 特殊属性: 存储在extra Map中，通过getIntExtra/getDoubleExtra/getBooleanExtra/getStringExtra获取
- 支持热更新: SliceInjector.reload()重新加载所有配置
- SliceRegistryGenerator: 自动扫描assets/slice/下所有.json文件，生成registry.json（排除自身，按文件名排序）

## 语言配置
- TextLan工具类: 加载 assets/textLan/ 下的语言文件，键值对查找，缺失返回键本身
- 当前使用: Simplified Chinese.json
- 键命名格式: 文件名+下划线+文本内容，如 SettingsUI_Title
- 所有UI文本应使用 TextLan.get("key") 获取，不硬编码

## 设置界面
- SettingsUI: 全屏设置面板，ESC键打开/关闭，blank.png背景+半透明遮罩
- 4个配置分组(游戏/窗口/相机/小地图)，修改立即生效
- 保存: 写入config JSON文件 | 取消: 从JSON重新加载 | 还原: 从defaultConfig恢复
- defaultConfig目录: 保存所有默认配置副本

## 核心类详解

### EventBus (event/EventBus.java)
- 线程安全: ConcurrentHashMap + CopyOnWriteArrayList
- subscribe() → 返回 Subscription 可退订
- SafeListener 包装: active标志控制生命周期
- publish() 同步 / publishAsync() 异步（用Game.mainPool线程池）
- 事件分发: 匹配自身类型+所有父类+接口（typeCache缓存）

### Camera (game/window/Camera.java)
- 透明StackPane，覆盖在map上方，拦截鼠标/滚轮输入
- 双缓冲: target状态(输入修改) + render状态(AnimationTimer每帧插值)
- 静态字段: offsetX/Y, zoom（全局可读）
- 单例: instance引用，提供 jumpTo()/freeze() 静态方法
- 拖拽: 右键拖拽，isDragging时lerpDrag=1.0即时跟随
- 缩放: 以鼠标位置为中心，ZOOM_STEP倍率
- clamp: offset范围 = [-0.5*viewport*(zoom-1), +0.5*viewport*(zoom-1)]
- lerpAndPublish: 变化>0.01时发布MapTransformEvent
- 输入: addEventFilter拦截 + EventBus订阅(来自Slice转发)

### MiniMap (game/window/MiniMap.java)
- StackPane，右上角，1/4视口大小（sizeRatio=0.25）
- 缩略图ImageView + 白色视口矩形框Rectangle
- Rectangle必须setManaged(false)+setLayoutX/Y（StackPane会覆盖setX/Y）
- 坐标公式: 与moveWithMap一致
  - 场景→本地: localX = (sceneX - mapW/2 - offsetX) / zoom + mapW/2
  - 本地→场景: sceneX = (localX - mapW/2) * zoom + mapW/2 + offsetX
- 点击跳转: 小地图坐标→本地坐标→场景坐标→调整offset→Camera.jumpTo()

### Game (game/Game.java)
- 全局EventBus: bus (static)
- 线程池: mainPool（参数由GameConfig配置）
- multiX/Y: 窗口缩放倍率 = currentSize/ORIGIN_SIZE
- moveWithMap(): 订阅MapTransformEvent，setTranslateX/Y + setScaleX/Y
- sendRootSizeChangedEvent(): 窗口resize时发布StageSizeChange
- 地图/菜单背景图片路径由GameConfig配置

### Stage (game/window/Stage.java)
- 封装javafx.stage.Stage
- root=StackPane, scene=Scene
- 构造时加载StageConfig并show，然后创建Game

## Slice体系 (game/slice/)

### 接口
- LifeCycled: load/unload生命周期，disposables自动释放，onLoad/onUnload/reset
- TextSized: 设置Labeled控件字体大小（CSS -fx-font-size）
- Coordinatable: 坐标转换接口 finalTraToMapX/Y, finalMapToTraX/Y

### Slice (抽象基类, extends Button)
- mapX/mapY: DoubleProperty，地图坐标
- name: StringProperty
- 拖拽: 左键拖拽Slice自身，右键转发为MousePressed/Dragged/Released到EventBus
- addAndRegisterEventFilter(): 注册filter并在unload时自动移除
- reloadTra(): 根据mapX/Y重算translateX/Y
- 订阅MapTransformEvent → reloadTra()

### StaticSlice (extends Slice)
- 不跟随地图缩放移动（固定在屏幕位置，受multiX/Y影响）
- 坐标转换: traX = mapX * multiX (无zoom/offset)
- 拖拽时freeze() Camera
- 订阅StageSizeChange → reloadTra()

### MoveSlice (extends Slice)
- 跟随地图缩放移动
- 坐标转换: traX = mapX * zoom * multiX + offsetX
- 反转: mapX = (traX - offsetX) / zoom / multiX
- 拖拽时除以zoom（抵消缩放对鼠标移动的影响）

### Player (extends MoveSlice)
- 简单子类，初始位置(0,0)

## 事件列表
| 事件 | 字段 | 发布者 | 订阅者 |
|------|------|--------|--------|
| MapTransformEvent | offsetX, offsetY, zoom | Camera.lerpAndPublish | MiniMap, Slice, moveWithMap |
| StageSizeChange | width, height, multiX/Y, old*4 | Game.sendRootSizeChangedEvent | Camera, MiniMap, PaneSizeManager, Slice |
| MousePressed | MouseEvent | Slice.addFilters | Camera |
| MouseDragged | MouseEvent | Slice.addFilters | Camera |
| MouseReleased | MouseEvent | Slice.addFilters | Camera |
| MouseScrolled | ScrollEvent | Slice.addFilters | Camera |

## 工具类
- ImageManager: 图片加载+缓存（HashMap），支持classpath和文件路径，loadFit按比例缩放
- PaneSizeManager: 订阅StageSizeChange自动调整Pane尺寸，支持倍率
- SliceInjector: Slice配置注入工具，读取registry.json自动加载所有slice JSON，反射注入通用属性，特殊属性归入extra容器，支持热更新

## 坐标系总结
- Camera offset: 视口中心相对地图中心的偏移（像素）
- Camera zoom: 缩放倍率，1.0=原始大小
- moveWithMap变换: setTranslateX(offsetX), setScaleX(zoom)
  - StackPane居中，缩放以中心为原点
  - 场景坐标 = (本地坐标 - mapW/2) * zoom + mapW/2 + offsetX
  - 本地坐标 = (场景坐标 - mapW/2 - offsetX) / zoom + mapW/2
- Slice地图坐标: mapX/Y → translateX/Y 通过Coordinatable转换
- multiX/Y: 窗口实际大小 / 初始大小，用于Slice坐标适配


