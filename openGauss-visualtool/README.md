## 代码结构说明
```
├── visualtool-api          //服务端API模块
│   ├── pom.xml        
│   ├── com.xx.ops          //运维业务包
│   ├── com.xx.modeling     //数据建模业务包
├── visualtool-common       //公共工具模块
│   ├── pom.xml
│   ├── src
├── visualtool-framework    //集成框架模块
│   ├── pom.xml
│   ├── src
├── visualtool-service      //系统业务代码模块(service、mapper、domain、xml)
│   ├── pom.xml
│   ├── com.xx.xx.ops       //运维业务包
│   ├── com.xx.xx.modeling  //数据建模业务包
├── visualtool-ui           //前端模块
```
## 支持的服务器系统
openEuler 20.3LTS（x86_x64，ARM），centos7.x（x86_x64）
## 安装步骤

1、使用root用户，创建新用户og_ops，并授予sudo免密权限

> + 添加用户 **useradd og_ops**
> + 设置密码 **passwd og_ops**
> + 增加文件编辑权限 **chmod u+w /etc/sudoers**
> + 编辑文件 **/etc/sudoers**，在文件底部增加：**og_ops ALL=(ALL) NOPASSWD: ALL**
> + 回收文件编辑权限 **chmod u-w /etc/sudoers**

2、使用root用户，在项目根目录下执行install.sh脚本

3、使用og_ops用户，在项目根目录下执行server.sh start启动服务

> servier.sh 支持参数（start|stop|restart|status）

4、访问地址http://ip:9494
### 注意事项：
1、平台使用的数据库，当前仅支持openGauss数据库，并且需要提前创建database。
2、需要将部署服务器IP配置在平台使用的数据库（openGauss）的白名单列表中。
## 卸载部署步骤
1、在项目根目录下执行uninstall.sh脚本

## 后端说明
> 1、后端返回给前端的响应编码，统一在org.opengauss.admin.common.enums.ResponseCode中定义，按照规则划分模块，规则如下：
+ 501xx 一体化模块；比如50101、50102

## 前端说明

## 主程序接口暴露
为了更好的通过插件扩展系统功能，主程序需要暴露接口给插件扩展和使用。主要有两类接口需要暴露：
+ 给插件扩展的Interface
> 在visualtool-service模块的org.opengauss.admin.system.plugin.extract包里定义。
+ 给插件调用的Service，
> 在visualtool-service模块的org.opengauss.admin.system.plugin.facade包里定义。

## 相关文档
[openGauss安装文档]( https://docs.opengauss.org/zh/docs/3.0.0/docs/installation/%E5%8D%95%E8%8A%82%E7%82%B9%E5%AE%89%E8%A3%85.html )

[主程序开放接口文档]( https://fullstack-dao.feishu.cn/docx/doxcnIa9e0ChR4bJWlx4IyBfzjf )

[主程序前端开发规范指导]( https://fullstack-dao.feishu.cn/docx/doxcnyE9BNt2mm0WV5o2dPqxAec )

[插件开发手册]( https://fullstack-dao.feishu.cn/docx/doxcnu2EjetnyXmL1sYIooyrivp )

[主程序扩展手册]( https://fullstack-dao.feishu.cn/docx/doxcnV63pz1w4bn4Zn1y2lxwJnf )

## 常见问题
1、插件框架依赖包无法拉取到。
> 本地maven settings中的mirror配置的mirrorOf参数如果配置的通配符*，则在后面追加上 ,!maven-public 即可。
> 