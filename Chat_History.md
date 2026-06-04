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
