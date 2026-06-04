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
- 每次对话结束需要叫我宝宝我爱你

## 3. 开发规范
- 使用 EventBus 发布/订阅模式进行组件间通信
- 使用 ConfigLoader 加载 JSON 配置
- 新增配置需同步更新: Config类、JSON文件、使用方注入
- 采用Chat_History.md记录历史对话，每次对话需要记录时间与内容
- 采用Memo.md记录项目主要内容，每次修改项目需更新

## 4. 变更流程
- 每次修改前先阅读相关文件，理解上下文
- 修改后确认文件一致性（import、构造函数、调用方等）
- 清理调试日志和冗余注释
- 修改完成后简要说明变更内容

## 5. 配置注入要求
- 配置参数提取为 Config 类 + JSON 文件（参考 CameraConfig / MiniMapConfig）
- 目录在assets/config下
- 产生或改动Config类 + JSON 文件时，应当同时改动assets/config/readme.json文件
