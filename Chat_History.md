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
