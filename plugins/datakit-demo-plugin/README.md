# 插件开发脚手架
为了方便开发者快速开发与平台适配的插件，而搭建的插件开发脚手架，开发者可在此脚手架之上开发业务功能。该脚手架配置的各项依赖版本已经经过验证，和平台兼容性最好，因此建议不要修改依赖版本。
## 注意事项
1、为保证插件包安装可用，请根据 插件开发手册文档 （见平台项目doc目录）

2、架构使用的框架版本非必要请不要修改。

## 新插件修改示意点
### POM.xml修改
1. 修改artifactId保证工程唯一
2. 修改build插件`spring-brick-maven-packager`中pluginInfo.id,。
   这个id必须与其他插件区分，保证唯一，后面所有的插件id均与此值保持一致。
### 后端
1. 修改PluginExtensionInfoConfig.PLUGIN_ID的值为pluginInfo.id
### 前端
1. utils/const.js中的PLUGIN_ID为pluginInfo.id

## 常见问题
1、插件包安装时报错"非法插件包"。
> 安装的插件包不对导致，执行打包命令后，在target目录下有两个jar包，应该用带有-repackage后缀的jar包进行安装，其他的是原始jar包，而非插件包。
