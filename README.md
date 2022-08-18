# 插脚开发脚手架项目

## 注意事项
1、为保证插件包安装可用，请根据 [插件文档](https://fullstack-dao.feishu.cn/docx/doxcnu2EjetnyXmL1sYIooyrivp) 的规范和示例开发。

2、架构使用的框架版本非必要请不要修改。

## 常见问题
1、插件框架依赖包无法拉取到。
> 本地maven settings中的mirror配置的mirrorOf参数如果配置的通配符*，则在后面追加上 ,!maven-public 即可。

2、插件包安装时报错"非法插件包"。
> 安装的插件包不对导致，执行打包命令后，在target目录下有两个jar包，应该用带有-repackage后缀的jar包进行安装，其他的是原始jar包，而非插件包。
