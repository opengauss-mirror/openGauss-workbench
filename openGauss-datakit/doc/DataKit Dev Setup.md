# OpenGauss Datakit 开发环境搭建流程

## 推荐环境
- java 17+
- maven 3.9.0+
- node v18+（含npm）

此外，为保证依赖下载顺利，需配置好maven镜像源和node镜像源。

## 后端

### 工具
- IDE：Intellij IDEA 2023.3.3
- OS：OpenEuler 22.03
- JDK：Java version: 17.0.10, vendor: Oracle Corporation
- Maven：3.9.0+

### 准备
DataKit版本7.0.0-RC2开始，项目基础平台升级为Springboot 3.5.6,在项目编译前需本地编译且安装SpringBrick组件。
1、下载SpringBrick项目源码，命令如下：
```
git clone https://gitcode.com/wang4721/springboot-plugin-framework-parent.git
```
2、进入springboot-plugin-framework-parent项目根目录，使用Maven进行编译安装
```
mvn clean install -Dmaven.test.skip=true
```
3、执行成功后，从gitcode上下载DataKit项目，命令如下：
```
git clone https://gitcode.com/opengauss/openGauss-workbench.git
```

安装好OpenGauss，详细步骤见[README.md](https://gitee.com/opengauss/openGauss-workbench/tree/master/README.md)。将整个openGauss-workbench文件夹导入IDEA。想调试插件的话，应该先运行一次根目录的`build.sh`，命令如下：
```shell
sh build.sh
```

### visualtool-api Debug流程
项目以`visualtool-*`为核心框架，Debug流程时启动这个核心框架后再动态加载`plugins`下的各个插件。

要启动核心框架，需更改dev环境的配置文件，它在`openGauss-datakit/visualtool-api/src/main/resources/`下，也就是`application-dev.yml`文件，修改其中的url, username, password等字段，示例如下：
```ymal
system:
  defaultStoragePath: /ops/debug_files
  whitelist:
    enabled: false
server:
  port: 9494
  servlet:
    context-path: /
logging:
  file:
    path: ./logs/
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    # For openGauss
    driver-class-name: org.opengauss.Driver
    url: jdbc:opengauss://127.0.0.1:5432/db_datakit?currentSchema=public&batchMode=off
    username: opengauss_test
    password: Sample@123
swagger:
  enabled: true
  pathMapping: /dev-api
plugin:
  sortInitPluginIds:
  decrypt:
    enable: false
    className: com.gitee.starblues.common.cipher.AesPluginCipher
    plugins:
      - pluginId: example-basic-1
        props:
          secretKey: mmfvXes1XckCi8F/y9i0uQ==
  version: 0.0.0
```

配好`application-dev.yml`就可以调试了，这时用`visualtool-api`中的`AdminApplication.java`中的`org.opengauss.admin.AdminApplication`类启动调试即可。

### plugins Debug流程
以上只启动了`visualtool-*`这个核心框架，想要调试`plugins`文件夹下的各个插件，则需要在项目根目录下创建`visualtool-plugin`文件夹，将`sh build.sh`生成的各插件的jar拖到这个文件夹下，重新启动框架即可。

### 前后端联调
如果您有前后端联调的需求，同时也为方便起见，应在`openGauss-datakit/visualtool-framework/src/main/java/org/opengauss/admin/framework/config/SecurityConfig.java`这个文件中，
- 修改`.anyRequest().authenticated()`字段为`.anyRequest().permitAll()`
- 修改`.headers().frameOptions().disable()`为`.headers().disable()`

示例如下：
```java
...
        httpSecurity
                ...
                .antMatchers("/prometheus").permitAll()
//                .anyRequest().authenticated()
                .anyRequest().permitAll()
                .and()
//                .headers().frameOptions().disable();
                .headers().disable();
...
```
这样修改后并非意味着一定不需要密码登录。这样做会有以下效果
- 前端各个插件`yarn dev`时可以连接到后端，否则无法连接（需要登录）
- 前端访问一些涉及到查询登录用户的个人信息的api时仍然会无法访问，因为没有登录，但是绝大部分前端功能还是能用的
- 访问后端`visualtool-ui`的`:9494`这个端口时还是会进入登录界面
- 在修改代码重新运行后端的情况下，不刷新浏览器界面（即F5或者浏览器地址栏Enter一下）则不需要重新登录，刷新界面则需要在浏览器控制台运行如下命令清空一下localstorage：
  ```javascript
  localStorage.clear();
  ```
  否则会卡在某个地方，建议遇到任何未知错误时都`localStorage.clear();`一下

至此，后端环境搭建部分告一段落。

## 前端
### 工具
- Node：v18.19.0
- npm：10.2.3
- yarn：1.22.21
### 前端环境搭建
前端调试需要先启动相应功能的后端服务。这里以调试base_ops插件的前端为例，后端自然需要将`base-ops-6.0.0-repackage.jar`放入`visualtool-plugin`中，同时运行以下命令：
```shell
cd plugins\base-ops\web-ui
# 执行npm install安装依赖
npm install
# 然后执行 npm run dev 或者 yarn dev 启动前端服务
yarn dev
```
启动完成后，执行窗口会输出Local地址，在本例中为http://localhost:8081/static-plugin/base-ops/，然后就可以登录调试了。

### 可能遇到的问题
```
Watchpack Error (watcher): Error: ENOSPC: System limit for number of file watchers reached, watch 'xxxx'
```
参考StackOverflow上这个帖子：[React Native Error: ENOSPC: System limit for number of file watchers reached](https://stackoverflow.com/questions/55763428/react-native-error-enospc-system-limit-for-number-of-file-watchers-reached)。

解决办法是更改系统设置如下：
```shell
# insert the new value into the system config
echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf && sudo sysctl -p

# check that the new value was applied
cat /proc/sys/fs/inotify/max_user_watches

# config variable name (not runnable)
fs.inotify.max_user_watches=524288
```