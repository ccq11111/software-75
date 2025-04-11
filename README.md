# PurseAI - 个人财务管理应用

PurseAI是一个基于JavaFX开发的个人财务管理应用程序，提供账单记录、财务摘要、储蓄计划等功能，帮助用户更好地管理个人财务。

## 功能特点

### 用户认证
- 用户注册与登录系统
- 安全的密码管理
- 用户会话管理

### 账单管理 (Billing)
- 记录日常收支
- 多种收支类别（生活费用、通讯费用、工资收入、化妆品等）
- 支持日期筛选和模糊搜索
- CSV导入功能

### 财务摘要 (Summary)
- 可视化支出和收入分析
- 按周、月、年查看财务状况
- 饼图展示不同类别的收支比例

### 储蓄计划 (Saving)
- 创建个性化储蓄计划
- 设置储蓄周期和目标金额
- 多币种支持
- 储蓄计划进度跟踪

### 其他功能
- 响应式用户界面，支持窗口大小调整
- AI助手功能（对话式财务建议）
- 全局侧边栏导航

## 系统要求

- Java 11或更高版本
- JavaFX 17.0.2或更高版本
- 支持的操作系统：Windows、macOS、Linux

## 安装指南

### 预构建安装包

我们为不同操作系统提供了预构建的安装包：

- Windows: 下载并运行`.exe`安装文件
- macOS: 下载`.dmg`文件并拖动到应用程序文件夹
- Linux: 下载并安装`.deb`或`.rpm`包（取决于您的发行版）

### 从源代码构建

#### 先决条件

- JDK 14或更高版本（包含jpackage工具）
- Maven 3.6或更高版本
- 对于Windows：Windows 10或更高版本
- 对于macOS：macOS 10.15 (Catalina)或更高版本
- 对于Linux：现代Linux发行版（Ubuntu 20.04、Fedora 32等）

#### 使用统一构建脚本

最简单的方法是使用统一构建脚本，它会检测您的操作系统并运行相应的构建脚本：

##### Windows:
```batch
build.bat
```

##### macOS/Linux:
```bash
chmod +x build.sh
./build.sh
```

#### 手动构建

1. 克隆仓库：
   ```bash
   git clone https://github.com/Shirley041767/test-commit.git
   cd test-commit
   ```

2. 使用Maven构建项目：
   ```bash
   mvn clean package
   ```

3. 运行应用程序：
   ```bash
   java -jar target/login-register-app-1.0-SNAPSHOT.jar
   ```

## 使用指南

### 登录/注册

1. 启动应用程序后，您将看到登录界面
2. 新用户可以点击"没有账号？点击注册"创建新账户
3. 已有账户的用户可以直接输入凭据登录
4. 测试账户：
   - 用户名：123
   - 密码：123

### 账单管理

1. 在侧边栏点击"账单"图标进入账单管理界面
2. 使用表单添加新的收支记录
3. 选择类别、输入产品名称、金额和日期
4. 点击"构建"按钮添加记录
5. 使用"查询"按钮进行模糊搜索

### 财务摘要

1. 在侧边栏点击"摘要"图标进入财务摘要界面
2. 查看支出和收入的饼图分析
3. 点击"周"、"月"、"年"按钮切换不同时间段的数据

### 储蓄计划

1. 在侧边栏点击"储蓄"图标进入储蓄计划界面
2. 填写计划名称、开始日期、周期和次数
3. 使用滑块设置储蓄金额
4. 选择货币类型
5. 点击"创建"按钮添加新的储蓄计划

### AI助手

1. 点击侧边栏底部的AI图标打开AI助手对话框
2. 输入您的财务问题获取智能建议

## 开发者信息

### 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── loginapp/
│   │               ├── Launcher.java                # 应用启动器
│   │               ├── MainApp.java                 # JavaFX主应用程序类
│   │               ├── LoginRegisterController.java # 登录注册控制器
│   │               ├── BaseViewController.java      # 基础视图控制器（侧边栏）
│   │               ├── BillingViewController.java   # 账单视图控制器
│   │               ├── SummaryViewController.java   # 摘要视图控制器
│   │               ├── SavingViewController.java    # 储蓄视图控制器
│   │               └── ...
│   └── resources/
│       ├── fxml/                                    # FXML布局文件
│       │   ├── LoginView.fxml
│       │   ├── RegisterView.fxml
│       │   ├── BaseView.fxml
│       │   ├── BillingViewContent.fxml
│       │   └── ...
│       └── images/                                  # 图像资源
│           ├── app_icon.png
│           └── ...
```

### 技术栈

- Java 11
- JavaFX 17 - UI框架
- FXML - UI布局描述语言
- Maven - 项目构建工具
- jpackage - 应用打包工具
