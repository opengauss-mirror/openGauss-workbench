**openGauss DataKit Platform产品使用手册**

# **文档历史**

| **修订日期** | **修订内容** | **版本号** | **编写人** |
| --- | --- | --- | --- |
| 2022/12/18 | 初稿  | 1.0 | 张绍鹏 |
| 2022/12/21 | 内部评审后修改 | 1.1 | 张绍鹏 |
| 2023/1/13 | 按要求改为md格式 | 1.2 | 张绍鹏 |

# **关于本手册**

## **产品介绍**

openGauss的安装、运维场景对于初级用户或单纯想要测试openGauss数据库基本特性的使用者来说技术难度较大、过程较为复杂、学习曲线较为陡峭，尤其企业版安装对一般用户来说操作难度很大。使用可视化运维平台可以屏蔽openGauss的技术细节，让普通用户能够快速上手体验功能，让运维人员能够快速在企业环境中部署、卸载各类openGauss集群，减少了用户的学习成本和运维成本，实现了对openGauss各种常见操作的可视化，屏蔽了各种不同openGauss版本中的运维命令差异，可以让用户使用相同的方式操作数据库，不用知道命令细节也可以使用openGauss数据库的各项能力，让用户可以专注于自身的业务领域。

因此需要开发一些有针对性的运维监控工具，为不同配置不同运维要求的客户提供运维技术支撑，这些都将是openGauss社区的宝贵资产。而社区急需一个一体化的平台通过插件的方式将这些工具进行整合，并支持方便快捷的个性化配置。对于社区合作伙伴已经开发的Web工具，合作伙伴也可以根据《插件开发指南》的要求自行改造为插件，整合到一体化平台中。

本产品是基于Web的openGauss的可视化的一体化平台系统，目的是方便客户使用和管理openGauss可视化工具，可以为客户降低openGauss数据库安装使用门槛，做到安全中心管理，插件管理，以及其它功能包括一键化部署、卸载、组件化安装、多版本升级、日常运维和。本文档主要适用openGauss3.0.0+版本。

## **主要功能介绍**

本产品主要包含以下几个功能特性：

- 插件平台（详见平台开发文档，该手册不再赘述）
- 用于插件开发的前后端一体的开发框架（详见插件开发文档，该手册不再赘述）
- Mysql数据迁移工具插件实现
- 安全中心（账号管理、角色权限管理和白名单管理）
- 插件管理模块
- 日志中心，包括操作日志和系统日志（日志级别/切割规则/保留天数的设置和日志下载）
- 国际化（中/英）
- 主题切换（黑/白）
- 基础模块（用户中心、修改个人密码、登录/登出）

## **预期读者**

- 用户
- 测试人员
- 开发人员

## **名词解释**

| **名词**   | **解释**                   | **备注**                                           |
| ---------- | -------------------------- | -------------------------------------------------- |
| 超级管理员 | 系统安装后即可用的内置账号 | 该账号不可删除，而且是唯一可以访问“安全中心”的账号 |
|            |                            |                                                    |
|            |                            |                                                    |
|            |                            |                                                    |
|            |                            |                                                    |
|            |                            |                                                    |
|            |                            |                                                    |
|            |                            |                                                    |

##                  

## **版本及功能列表**

| **版本号** | **新增功能** | **发布时间** |
| --- | --- | --- |
| V1.0 | 首次发布 | 2022-12-18 |

## **平台基础模块（登录/登出/用户中心/密码）**

### **概述**

平台基础模块包括登录平台，登出平台，修改密码，用户中心。该工具是一个本地（内网）工具，不是面向公网开放的平台，所以没有注册用户的需求，但是有用户管理能力，超级管理员可在平台内部新增用户来达到用户管理的能力。

### **角色**

所有用户

### **界面**

1、登录平台

![platform-login](./_resources/platform-login.png)

2、退出登录

![platform-logout](./_resources/platform-logout.png)

3、修改密码

![platform-change-password-menu](./_resources/platform-change-password-menu.png)



<img src="./_resources/platform-user-change-password.png" alt="platform-user-change-password" style="zoom:50%;" />



4、用户中心

点击用户中心

![platform-user-center-menu](./_resources/platform-user-center-menu.png)

点击用户名旁边的“编辑”图标

![platform-user-center-view](./_resources/platform-user-center-view.png)

修改用户信息，包括：

- 账号名
- 用户昵称
- 手机号码（系统会检查唯一性）
- 邮箱（optional）

![platform-edit-user-info](./_resources/platform-edit-user-info.png)

点击更换头像，弹窗选择图片上传，Upload success后显示出头像

![platform-upload-avatar](./_resources/platform-upload-avatar.png)

![platform-new-avatar](./_resources/platform-new-avatar.png)

## **国际化和黑白主题**

### **概述**

本平台支持国际化热切换（中文和英文），支持黑白主题切换。

### **角色**

所有用户

### **界面**

1. 切换语言

![platform-change-language](./_resources/platform-change-language.png)

**切换到英文后的效果：**

![platform-English](./_resources/platform-English.png)

1. 切换主题

**点击右上角的太阳/月亮图标即可切换主题：**

<img src="./_resources/platform-theme-light.png" alt="platform-theme-light" style="zoom:67%;" />

点击右上角太阳/月亮按钮进行黑白主题toggle切换

![platform-theme-dark](./_resources/platform-theme-dark.png)

黑色主题的效果：

![platform-theme-darkmode](./_resources/platform-theme-darkmode.png)

## **插件管理页面**

### **概述**

在本页面可以完成对插件的集中管理，包括以下操作：插件一览，安装插件，卸载插件，停用插件，启用插件等。

### **角色**

拥有插件管理权限的角色可以访问

### **界面**

1、插件一览

![platform-plugin-mgmt](./_resources/platform-plugin-mgmt.png)

**说明：**

每个插件包括以下信息：

- 图标
- 名称
- 版本
- 开发者
- 简介

2、安装插件

![platform-plugin-upload-jar](./_resources/platform-plugin-upload-jar.png)

**说明：**

- 在插件管理页面，点击右上角“安装插件”按钮
- 将插件jar包拖拽到窗口里 或 打开文件选择器选择插件jar包
- 点击“播放”按钮进行安装
- 安装成功后：插件一览列表会出现对应的插件信息卡片，左侧系统菜单会出现插件相关的菜单项。

3、卸载插件

为了提供更灵活的菜单配置能力，平台允许插件创建二级菜单。又为了让同类插件的菜单可以归类于一级菜单下，平台也允许插件在其它插件菜单下创建二级菜单，这样就会存在插件菜单间的依赖，所以在卸载插件时要注意：

*如果其它插件在你的插件菜单下创建了子菜单项，那你需要先卸载那个插件消除依赖后才能卸载你的插件。*

插件被卸载后，该插件下的所有Tab标签页都会被自动关闭。

## **安全中心**

### **概述**

安全中心提供登录权限和账号管理，以及访问白名单管理。

### **角色**

出于安全性设计的考量，平台只允许超级管理员（即安装后就有的系统内置账号）可以访问安全中心。其它Role配置权限时甚至看不到安全中心的菜单，所以也无法给其它Role配置安全中心的权限。

### **界面**

1、白名单管理

![platform-security-whitelist](./_resources/platform-security-whitelist.png)



<img src="./_resources/platform-security-whitelist-add.png" alt="platform-security-whitelist-add" style="zoom:70%;" />

**说明：**

- 左侧菜单栏找到“安全中心-访问白名单”菜单，点击打开“白名单管理”页面；
- 点击右上角“添加白名单”按钮
- 在弹出框中填写白名单名称和允许访问的IP地址，多个IP地址间用逗号隔开，确认即可完成添加。

2、角色与权限菜单

本系统支持通过创建特定的权限组合来管理用户权限，权限组合被定义为“角色”，一个用户只能拥有一个角色。

![platform-security-role-list](./_resources/platform-security-role-list.png)

点击“添加角色”

创建或编辑“角色”时，可以为“角色”配置权限范围，包括已经安装的插件的访问权限。

*注意：出于安全性设计的考虑，任何添加的”角色“都无法拥有”安全中心“的访问权限，”安全中心“的权限被固定在”超级管理员“身上。*

<img src="./_resources/platform-security-add-role.png" alt="platform-security-add-role" style="zoom:67%;" />



**说明：**

- 左侧菜单栏找到“安全中心-角色菜单”，点击打开“角色菜单”管理页面；
- 点击右上角“添加角色”按钮
- 在弹出框中填写角色名称，并勾选允许访问的菜单项，可以选择多个菜单项，可以全选，可以添加备注，确认即可完成添加。

3、账号与权限

![platform-security-account-list](./_resources/platform-security-account-list.png)

<img src="./_resources/platform-security-add-user.png" alt="platform-security-add-user" style="zoom:67%;" />

**说明：**

- 左侧菜单栏找到“安全中心-账号与权限”，点击打开“账号与权限”管理页面；
- 点击右上角“添加用户”按钮
- 在弹出框中填写以下字段：
    - 账号：登录用户名
    - 密码：登录密码
    - 昵称：
    - 手机号码：唯一手机号
    - 角色：不选择角色则没有任何权限
    - 状态：启用或停用
    - 备注：可选填

点击确定完成添加账号。

![platform-security-reset-password-dialog](./_resources/platform-security-reset-password-dialog.png)

点击“重置密码”可以为某个用户重置密码

## **日志中心**

### **概述**

日志中心提供系统日志管理和操作日志管理功能。

### **角色**

拥有日志中心访问权限的账号均可以访问该功能。

### **界面**

1、系统日志

![platform-logs-system-list](./_resources/platform-logs-system-list.png)



<img src="./_resources/platform-logs-system-configuration.png" alt="platform-logs-system-configuration" style="zoom:67%;" />



**说明：**

- 左侧菜单栏找到“日志中心-系统日志”，点击打开“系统日志”管理页面；
- 可以通过搜索找到对应的日志文件，点击下载日志文件
- 点击右上角“日志设置”按钮
- 在弹出框中进行系统日志配置：
    - 日志输出级别：四个级别DEBUG，INFO，WARN，ERROR
    - 保留天数：输入天数，操作天数的日志会被自动清除
    - 单个日志文件大小：单个日志文件达到这个限制会自动创建新的日志文件
    - 最大占用空间：所有日志文件被允许占用的最大存储空间

2. 操作日志

![platform-logs-operation-list](./_resources/platform-logs-operation-list.png)

<img src="./_resources/platform-logs-operation-details.png" alt="platform-logs-operation-details" style="zoom:67%;" />



**说明：**

1左侧菜单栏找到“日志中心-操作日志”，点击打开“操作日志”管理页面；

2可以通过搜索找到对应的日志文件

3点击“详情”查看该操作的详细参数

## 设备管理

用户通过【资源中心】-【设备管理】进入本功能画面，在这个画面上，左侧为物理机基本信息，右侧为实时监控和操作面板，监控指标包括网络速率、CPU使用率、内存使用率、磁盘使用率。用户可以根据ip、操作系统、标签对设备进行筛选。在root密码没有选择保存的情况下，实时数据将无法获取。

![](./_resources/equipment-manage.png)

点击【创建】按钮可以对物理机进行创建，在弹出的对话框中，用户需要输入内网IP、外网IP、ssh端口号、root密码等创建物理机。点击标签下拉框，可以选择标签，也可以输入字符来创建标签。点击连通性测试可以测试到设备的ssh连接是否正常。

![](./_resources/equipment-manage-create.png)

点击操作面板的【编辑】可以对物理机进行编辑，包括ip、标签、密码等的修改。

![](./_resources/equipment-manage-edit.png)

点击【标签设置】，可以对勾选的多个设备批量设置标签。

![](./_resources/equipment-manage-tag-batch.png)

点击【标签管理】，可以展示当前所有的标签和关联的主机数量，用户可以在这里对标签做统一的修改或者删除。

![](./_resources/equipment-manage-tag-manage.png)

在需要大批量导入的场景中，用户可以点击【批量导入】按钮，在弹出的对话框中点击【导入模板下载】下载导入模板。

![](./_resources/server-manage-template-download.png)

打开下载好的模板文件，输入对应的序号、服务器名称、内网IP等信息。

![](./_resources/server-manage-template.png)

点击【选择文件】将需要导入的文件上传，然后点击【确定】开始解析。

![](./_resources/server-manage-parsing-files.png)

等待解析完成，完成后会出现解析结果，如果导入数据有误，点击【下载错误报告按钮】下载错误报告查看具体原因。

![](./_resources/server-manage-import-completed.png)

打开错误报告文件会显示导入失败的记录并显示其失败原因。

![](./_resources/server-manage-error-reporting.png)

## 实例管理

用户通过【资源中心】-【实例管理】进入本功能画面，在这个画面上，分为节点信息和集群信息两级展示，集群信息一层展示集群名称、数据库类型、运行状态等基本信息，节点一层展示节点所在主机的基本信息、角色、运行状态、实时监控数据等。

用户可以在这个画面上根据集群名称、ip地址、数据库类型对实例进行过滤搜索。

![](./_resources/jdbc-manage.png)

点击【创建】后，用户在新增对话框内可以输入对应数据库节点的连接信息和扩展属性，并可以测试连通性。

在不勾选自定义名称的情况下将以前两个节点的IP地址和端口进行组合作为集群名称。

![](./_resources/jdbc-manage-create.png)

在需要大批量导入的场景中，用户可以先点击【下载模板】下载导入模板
然后点击【点击上传】进行导入，导入中出现的问题将提示用户进行修改。

## 安装包管理

【安装包管理】支持【新增安装包】、【批量检查】、【批量删除】。

![](./_resources/package-management.png)

【新增安装包】支持openEluer20.03、openEluer22.03、centOs7三个操作系统，其余选项根据机器类型进行选择，下载链接自动关联，点击确定即可下载成功。

![](./_resources/package-management-add.png)

下载成功后，点击【批量检查】或者【检查】可检查安装包是否下载成功、点击【更新】可替换安装包。

![](./_resources/package-management-other.png)

【更新安装包】界面（点击【离线上传】进行替换安装包）

![](./_resources/package-management-update.png)

## 集群管理

### 集群列表

【集群列表】展示已安装或者已导入datakit中的集群，有启停、卸载、备份数据、删除等功能。

![](./_resources/cluster-manage-list.png)

### 并行安装任务

#### 草稿箱

【草稿箱】中的集群任务通过【创建并行安装任务】创建、创建成功后如果【环境监测】成功，则可以进行【发布】，【发布】后进入【任务列表】界面进行安装。

![](./_resources/cluster-manage-task-draft.png)

![](./_resources/cluster-manage-task-draft-publish.png)

点击【复制】可以快速复制操作系统、架构、安装包相同的安装任务、删除支持【批量删除】和单个【删除】、【编辑】可以修改任务的配置、【执行日志下载】可以下载日志查看“创建”-“执行成功”的流程

![](./_resources/cluster-manage-task-draft-other.png)

点击【任务ID】可以查看【任务详情】，点击【编辑】进行修改。

![](./_resources/cluster-manage-task-look.png)

![](./_resources/cluster-manage-task-detail.png)

#### 任务列表

任务列表界面展示通过环境检查的安装任务、可以点击【一键执行】或者【批量执行】进行安装。

![](./_resources/cluster-manage-tasks-list-execute.png)


## **系统设置**

### **概述**

系统设置可以让用户设置整个Datakit平台上的全局配置。

### **角色**

拥有系统设置权限的账号均可以访问该功能。

### **界面**

用户通过点击右上角【系统设置】菜单进入该功能。
![](./_resources/sys-setting-enter.jpg)
在这个画面上包含文件上传路径、迁移套件在线下载地址、迁移套件安装包名称、迁移套件jar包名称四个设置项。
![](./_resources/sys-setting.png)
其中文件上传路径指的是所有通过Datakit页面上传文件的存放路径，该路径必须以/结尾，并且不能与其它用户使用同一个文件夹以防止文件互相覆盖。

文件上传路径的初始默认值位于基座工程的sql文件中，路径为visualtool-api/src/main/resources/db/openGauss-visualtool.sql。
如需更改文件上传路径的默认值/ops/files，可以修改sys_setting表中的初始数据为其它路径。
![](./_resources/sys-setting-file-path-sql.jpg)

迁移相关的三个设置：迁移套件在线下载地址、迁移套件安装包名称、迁移套件jar包名称用于数据迁移在线安装迁移套件时作为默认值。
其中在线下载地址指的是下载迁移套件的路径，迁移套件jar名称指的解压缩安装包后启动目标文件的名称。

