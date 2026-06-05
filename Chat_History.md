# 历史对话记录

## 对话 1: MiniMap 白色框位置修复
- 问题: 拖拽移动时白色框没有跟随
- 原因: 坐标公式错误 + Rectangle 在 StackPane 中 setX/setY 被布局管理器覆盖
- 修复: 修正坐标变换公式 + setManaged(false) + setLayoutX/setLayoutY

## 对话 2: MiniMap 配置提取
- 要求: 将小地图参数提取为 JSON 配置文件（参照 CameraConfig）
- 结果: 创建 MiniMapConfig.java + miniMapConfig.json，共 12 个参数
- 待办: 运行时动态修改配置

## 对话 3: 项目备忘录与框架整理
- 要求: 全面阅读项目文件，整理备忘录；分离要求文件和历史记录文件
- 结果: 
  - CodeGeeX_Memo.md — 完整项目信息备忘录
  - Chat_Framework.md — 仅保留对话要求
  - Chat_History.md — 历史对话记录（本文件）

## 对话 4: 配置文件字段说明
- 时间: 2026-06-04
- 要求: 在 assets/config/readme.json 中记录所有配置文件的字段说明
- 结果: 创建 readme.json，包含 stageConfig/cameraConfig/miniMapConfig 共 24 个字段说明

## 对话 5: 全项目配置提取
- 时间: 2026-06-04
- 要求: 遍历项目文件，将硬编码参数提取到config文件中
- 分析: Camera/MiniMap/Stage 已有Config注入，无需改动；Slice.canBeDragged是实例属性不需提取
- 提取内容:
  - Game.java 线程池参数 (4,8,60,100) → GameConfig.corePoolSize/maxPoolSize/keepAliveSeconds/queueCapacity
  - Game.java 地图背景路径 "uiImages/backgrounds/map.png" → GameConfig.mapImagePath
  - Game.java 开始菜单背景路径 "uiImages/backgrounds/bg.jpg" → GameConfig.startMenuImagePath
- 新增文件: GameConfig.java + gameConfig.json
- 同步更新: readme.json (添加gameConfig字段说明), Memo.md (添加GameConfig信息)

## 对话 6: 设置界面 + 语言配置
- 时间: 2026-06-04
- 要求: 创建全屏设置界面，支持所有config配置的修改；按Agent.md第6节规范使用语言配置
- 新增文件:
  - SettingsUI.java — 全屏设置面板（ESC打开/关闭，保存/取消/还原默认配置）
  - ConfigWriter.java — 配置写入工具类
  - TextLan.java — 语言配置工具类
  - assets/defaultConfig/ — 默认配置副本目录
  - assets/textLan/Simplified Chinese.json — 简体中文语言文件
- 修改文件:
  - Game.java — 添加SettingsUI初始化、apply方法、语言加载
  - MiniMap.java — 添加applyConfig方法
  - SettingsUI.java — 所有文本使用TextLan.get()替代硬编码
- 语言键命名格式: 文件名_文本内容，如 SettingsUI_Title

## 对话 7: Slice JSON配置注入体系
- 时间: 2026-06-04
- 要求: 采用JSON文件创建Slice，通用属性(name, moved, width, height)和特殊属性分离；采用反射机制自动注入slice文件夹下所有JSON文件
- 新增文件:
  - assets/slice/registry.json — slice注册表，记录所有需要被加载的JSON文件
  - SliceConfig.java — Slice配置类，通用属性(name/moved/width/height) + 特殊属性容器(extra Map)
  - SliceInjector.java — Slice注入工具类，读取registry.json并自动加载所有slice配置
- 修改文件:
  - assets/slice/Test.json — 从空JSON改为包含完整测试用例(name, moved, width, height, health, attack)
- 设计要点:
  - 通用属性通过反射注入SliceConfig字段，特殊属性自动归入extra容器
  - moved字段决定继承MoveSlice还是StaticSlice
  - SliceInjector提供loadAll/get/reload等方法，支持热更新
  - 新增slice只需在registry.json中添加文件名即可自动注入

## 对话 11: SliceConfig增加mapX/mapY基础属性
- 时间: 2026-06-05
- 要求: 增加基础属性mapX/mapY，注入时设置到Slice的mapX/mapY
- 修改文件:
  - SliceConfig.java — 新增mapX(double,默认0)和mapY(double,默认0)基础属性
  - SliceInjector.java — BASE_FIELDS集合新增mapX、mapY
  - ConfiguredMoveSlice.java — applyConfig中添加setMapX(config.mapX)/setMapY(config.mapY)
  - ConfiguredStaticSlice.java — 同上
- 设计要点:
  - Slice类本身已有mapX/mapY的DoubleProperty及setMapX/setMapY方法
  - SliceConfig的mapX/mapY通过ConfiguredSlice注入到Slice的mapX/mapY，实现JSON配置地图坐标

## 对话 12: SliceConfig增加insert内边距属性
- 时间: 2026-06-05
- 要求: 新增基础属性insert，调整文字距离边框的边距，设置为四个属性
- 修改文件:
  - SliceConfig.java — 新增4个基础属性：insertTop, insertRight, insertBottom, insertLeft（double，默认0）
  - SliceInjector.java — BASE_FIELDS集合新增4个字段名
  - ConfiguredMoveSlice.java — CSS样式中添加-fx-padding，使用四个insert属性
  - ConfiguredStaticSlice.java — 同上
- 设计要点:
  - 使用JavaFX CSS的-fx-padding属性，格式为"上 右 下 左"
  - 四个方向独立控制，默认均为0

## 对话 13: SliceConfig增加fontSize字体大小属性
- 时间: 2026-06-05
- 要求: 新增基础属性fontSize字体大小
- 修改文件:
  - SliceConfig.java — 新增fontSize(double,默认12)基础属性
  - SliceInjector.java — BASE_FIELDS集合新增fontSize
  - ConfiguredMoveSlice.java — CSS样式中添加-fx-font-size
  - ConfiguredStaticSlice.java — 同上
- 设计要点:
  - 使用JavaFX CSS的-fx-font-size属性，单位px

## 对话 14: SliceConfig增加wrapText自动换行属性
- 时间: 2026-06-05
- 要求: 新增字段控制文本自动换行行为
- 修改文件:
  - SliceConfig.java — 新增wrapText(boolean,默认false)基础属性
  - SliceInjector.java — BASE_FIELDS集合新增wrapText
  - ConfiguredMoveSlice.java — applyConfig中添加setWrapText(config.wrapText)
  - ConfiguredStaticSlice.java — 同上
- 设计要点:
  - Slice继承Button→Labeled，Labeled自带setWrapText方法
  - wrapText=true时文本超出宽度自动换行，false则截断或溢出

## 对话 15: 新增SliceRegistryGenerator工具类
- 时间: 2026-06-05
- 要求: 在util里新增工具程序类，自动注册assets/slice里所有slice到registry
- 修改文件:
  - util/SliceRegistryGenerator.java — 新增工具类
- 设计要点:
  - 扫描assets/slice/下所有.json文件（排除registry.json自身）
  - 按文件名排序后写入registry.json的slices数组
  - 使用Jackson美化格式输出
  - 提供generate()方法供代码调用，main()方法可直接运行

## 对话 17: 渲染帧率上限与垂直同步配置
- 时间: 2026-06-05
- 要求: 新增配置控制帧率上限和垂直同步，突破JavaFX 60fps限制
- 修改文件:
  - GameConfig.java — 新增maxFrameRate(int,默认60)和vSync(boolean,默认true)
  - App.java — 添加main方法，在launch()前读取配置设置prism.refreshRate和prism.vsync系统属性
  - gameConfig.json — 添加maxFrameRate和vSync配置项
- 设计要点:
  - prism.refreshRate和prism.vsync必须在JavaFX初始化前设置
  - 在App.main()中launch()之前读取配置并System.setProperty
  - 配置读取失败时回退到默认值(60Hz, vSync=true)
  - 突破60fps需显示器支持高刷新率+vSync关闭
