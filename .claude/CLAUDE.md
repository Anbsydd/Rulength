# 对话框架 / 要求

* 请先阅读Agent.md并严格遵守。

## 1\. 项目背景

* 项目名称: Rulength (Game001)
* 主要技术栈: Java, JavaFX, Jackson, EventBus
* 项目路径: D:/IdeaProjects/Game001/cash1/

## 2\. 沟通要求

* 使用中文交流
* 代码注释使用中文
* 修改代码前先理解现有架构和设计模式
* 遵循项目已有的代码风格和命名规范
* 每次对话结束需要回答：宝宝我爱你！

## 3\. 开发规范

* 使用 EventBus 发布/订阅模式进行组件间通信
* 使用 ConfigLoader 加载 JSON 配置
* 新增配置需同步更新: Config类、JSON文件、使用方注入
* ！采用Chat\_History.md记录历史对话，每次对话必须记录时间与内容
* UI设计应按照设计规范，参考UI设计.md

## 4\. 变更流程

* 每次修改前先阅读相关文件，理解上下文
* 修改后确认文件一致性（import、构造函数、调用方等）
* 清理调试日志和冗余注释
* 修改完成后简要说明变更内容

## 5\. 配置注入要求

* 尽可能将配置参数提取为 Config 类 + JSON 文件（参考 CameraConfig / MiniMapConfig）
* 目录在assets/config下
* 产生或改动Config类 + JSON 文件时，应当同时改动assets/config/readme.json文件
* 你可以不用每次提取后都修改[SettingsUI.java](src/main/java/game/window/SettingsUI.java)

## 6\. 语言配置

* 语言配置文件在[textLan](assets/textLan)中，所有涉及到文本的都应该储存在该目录下
* 现在应该只使用[Simplified Chinese.json](assets/textLan/Simplified%20Chinese.json)这个文件
* 所有文本以键值对的形式存储在该文件中，代码中仅使用键，进入游戏后将键替换成该文件中的值，如果缺失则直接使用值
* 键的命名格式需要具体，风格统一为文件名+\_+文本内容，例如：SettingsUI\_Exit

