# Felix - Trystage Bedwars Staff Mod

一个为 TrystageBedwars 服务器管理员设计的 Fabric 客户端模组，专门用于TBW服务器的玩家管理。
(本Repo为修改公示版)

## 功能特性

### 核心功能
- **玩家选择系统**：通过十字准星对准玩家，按 `R` 键选择目标玩家
- **多功能菜单**：提供七种主要管理功能：
  1. **传送玩家** - 将玩家传送到指定位置或服务器
  2. **封禁玩家** - 以多种理由封禁玩家（作弊、组队、漏洞利用等）
  3. **禁言玩家** - 禁言玩家（刷屏、歧视言论等）
  4. **查询历史** - 查看玩家历史记录
  5. **举报玩家** - 举报玩家不当行为
  6. **答疑解惑** - 常见问题解答
  7. **提醒警告** - 向玩家发送警告
- **玩家列表获取**：按 `U` 键快速获取当前服务器玩家列表

### 用户界面
- **HUD 显示**：实时显示目标玩家信息和3D模型
- **交互式菜单**：使用方向键、滚轮和中键进行导航
  - ↑↓/滚轮：选择选项
  - →/中键：确认选择
  - ←：返回上级菜单
- **通知系统**：多种类型的通知（成功、信息、警告、错误）
- **音效反馈**：不同操作配有相应的音效提示

### 技术特性
- **事件驱动架构**：基于自定义事件总线的模块化设计
- **mixin 注入**：修改客户端标识和鼠标输入处理
- **防误操作**：防机器人检测，确保只对真实玩家操作
- **异步命令执行**：避免游戏卡顿的命令队列系统

## 系统要求

- **Minecraft 版本**: 1.20.4
- **Fabric Loader**: ≥ 0.16.14
- **Fabric API**: ≥ 0.97.2+1.20.4
- **Java 版本**: 17+

## 安装方法

### 直接使用（已构建版本）
1. 下载最新的 `.jar` 文件
2. 放入 Minecraft 的 `mods` 文件夹
3. 启动游戏

### 从源码构建
```bash
# 克隆仓库
git clone https://github.com/Trystage/PublicFelix.git
cd PublicFelix

# 构建模组 (Windows)
gradlew build

# 构建后的文件位于 build/libs/felix-1.0-SNAPSHOT.jar
```

## 使用方法

### 基本操作
1. **选择目标玩家**：
   - 将十字准星对准玩家
   - 按下 `R` 键选择该玩家
   - 成功选择后会有音效和通知提示

2. **打开功能菜单**：
   - 选择玩家后，HUD 会自动显示
   - 使用方向键或滚轮浏览功能
   - 按右键或中键进入子菜单

3. **重置目标**：
   - 按 `H` 键清除当前选择的目标

4. **获取玩家列表**：
   - 按 `U` 键向服务器发送 `/glist` 命令

### 功能详解

#### 传送玩家
- 传送到此服务器：将玩家传送到你所在的服务器
- 传送到此：将玩家传送到你的位置
- 传送到所在服务器：将玩家传送到其所在服务器
- 传送到玩家：将玩家传送到其他玩家

#### 封禁玩家
提供多种封禁理由：
- 开挂作弊
- 联合组队
- 欺负队友
- 利用漏洞
- 可疑账号
- 垃圾广告

#### 禁言玩家
禁言理由：
- 刷屏
- 歧视行为
- 戏弄玩家
- 不合适的言论

#### 查询历史
- 最近10条
- 最近15条
- 所有记录

#### 举报玩家
- 作弊行为
- 不良言论
- 其它

#### 答疑解惑
常见问题解答：
- 如何获得主播类rank
- 如何加入交流群
- 如何反馈问题
- 如何举报
- 如何加入我们

#### 提醒警告
警告类型：
- 开挂作弊
- 消息违规
- 刷屏广告

## 项目结构

```
PublicFelix/
├── src/client/java/win/trystage/felix/
│   ├── FelixClient/FelixClient.java      # 主入口点
│   ├── client/
│   │   ├── Onlines.java                  # 玩家列表功能
│   │   ├── core/
│   │   │   ├── CommandExecutor.java      # 命令执行器
│   │   │   └── RawSound.java             # 音效播放器
│   │   ├── event/
│   │   │   └── EventManager.java         # 事件管理系统
│   │   ├── input/
│   │   │   └── KeyBindings.java          # 按键绑定
│   │   ├── mixin/
│   │   │   ├── ClientBrandMixin.java     # 客户端标识修改
│   │   │   └── MouseMixin.java           # 鼠标输入处理
│   │   ├── ui/
│   │   │   ├── Notifications.java        # 通知系统
│   │   │   └── features/
│   │   │       ├── Features.java         # 功能实现
│   │   │       └── MenuFeature.java      # 菜单功能接口
│   │   ├── uis/
│   │   │   └── UIS.java                  # 用户界面系统
│   │   └── util/
│   │       ├── AntiBot.java              # 防机器人检测
│   │       └── ScrollWheelHelper.java    # 滚轮辅助
├── src/client/resources/
│   ├── fabric.mod.json                   # 模组元数据
│   ├── felix.mixins.json                 # mixin 配置
│   └── assets/felix/                     # 资源文件（音效、图标）
├── build.gradle                          # 构建配置
├── gradle.properties                     # 版本配置
└── LICENSE                               # GPL v3 许可证
```

## 开发指南

### 环境设置
1. 安装 JDK 17 或更高版本
2. 安装 IntelliJ IDEA 或 Eclipse
3. 导入项目为 Gradle 项目

### 添加新功能
1. 在 `Features.java` 中添加新的功能方法
2. 实现 `MenuFeature` 接口
3. 在 `UIS.java` 中注册功能
4. 添加对应的菜单选项

### 构建配置
主要配置位于 `gradle.properties`：
```properties
minecraft_version=1.20.4
yarn_mappings=1.20.4+build.3
loader_version=0.16.14
mod_version=1.0-SNAPSHOT
maven_group=win.trystage
archives_base_name=felix
fabric_version=0.97.2+1.20.4
```

## 许可证

所有权属于TrystageBedwars

完整许可证文本请查看 [LICENSE](LICENSE) 文件。[中文LICENCE][LICENCE_CN]

## 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 问题反馈

遇到问题时，请：
1. 检查是否满足系统要求
2. 查看控制台日志中的错误信息
3. 在 GitHub Issues 中提交问题报告
4. 提供详细的复现步骤和环境信息

## 免责声明

本模组仅供TBW服务器管理员在合法范围内使用。请遵守所在服务器的规则和 Minecraft 的使用条款。开发者不对滥用本模组的行为负责。

## 更新日志

### v1.0-SNAPSHOT
- 初始版本发布
- 实现核心管理功能
- 添加完整的用户界面
- 支持 Minecraft 1.20.4

---

**开发者**：Trystage4C01，stuffed-cat，catbababa
