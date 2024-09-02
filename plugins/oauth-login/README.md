# 统一登录插件介绍

## 插件简介

oauth-login插件是“DataKit支持SSO与DevKit统一登录”需求新增的插件，此插件安装配置成功后，支持通过DevKit页面直接点击跳转至DataKit的首页，无需用户手动通过登录页面登录DataKit。

## 插件使用说明

1. 参数配置

使用此插件，用户需要在DataKit的配置文件中配置如下参数：

```yml
plugins:
  oauth-login:
    # A unique identifier obtained by registering with DevKit
    client-id: your_client_id
    # A secret key paired with the client_id
    client-secret: your_client_secret
    datakit-url: https://ip:port
    devkit-url: https://ip:port
    # SSL certificate
    ssl-key: your_ssl_key
```

其中，

- `client-id, client-secret, ssl-key`三个参数由DevKit提供，这三个参数是在DevKit注册DataKit信息成功后，返回的参数，用户将参数字符串配置到配置文件中即可。具体的注册方式，请参考DevKit关于统一登录到DataKit的说明文档。
- `datakit-url, devkit-url`两个参数分别未访问DataKit和访问DevKit服务的根地址。

2. 安装插件

上述参数配置成功后，启动DataKit，在前端通过插件管理页面的安装插件功能，将oauth-login插件的jar包上传安装即可。如果DataKit启动后，发现oauth-login插件已加载，则无需再次安装。

3. 功能使用

经上述步骤安装插件成功后，DataKit安装统一登录功能成功，用户便可以通过DevKit页面点击跳转到DataKit首页。

## DevKit登录DataKit的权限控制

通过DevKit点击登录到DataKit时，DataKit会自动根据DevKit的用户信息创建映射用户，然后自动登录。如果DataKit管理员想要管理DevKit使用DataKit的权限，只需在DataKit已有的用户管理系统中，管理此映射用户的权限即可。

映射用户的用户名命名逻辑为：DevKit用户名_SSO_6位随机数，如DevKit的用户名为“zhangsan”，则映射用户名就可能为“zhangsan_SSO_5169lf”。

上述权限控制逻辑只针对DevKit普通用户，如果通过DevKit的管理员用户“devadmin”登录DevKit，并点击跳转到DataKit时，此用户将直接拥有DataKit的管理员权限。