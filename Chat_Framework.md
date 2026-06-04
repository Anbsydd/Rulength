# 对话框架 / 要求

## 1. 项目背景
- 项目名称: Rulength (Game001)
- 主要技术栈: Java, JavaFX, Jackson, EventBus
- 项目路径: D:/IdeaProjects/Game001/cash1/

## 2. 沟通要求
- 使用中文交流
- 代码注释使用中文
- 修改代码前先理解现有架构和设计模式
- 遵循项目已有的代码风格和命名规范

## 3. 开发规范
- 配置参数提取为 Config 类 + JSON 文件（参考 CameraConfig / MiniMapConfig）
- 使用 EventBus 发布/订阅模式进行组件间通信
- 使用 ConfigLoader 加载 JSON 配置
- 新增配置需同步更新: Config类、JSON文件、使用方注入

## 4. 变更流程
- 每次修改前先阅读相关文件，理解上下文
- 修改后确认文件一致性（import、构造函数、调用方等）
- 清理调试日志和冗余注释
- 修改完成后简要说明变更内容

