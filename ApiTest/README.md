

# DataKit API测试框架

ApiTest项目是用于测试DataKit服务API接口的测试框架，项目在DataKit门禁中运行，用于保证DataKit服务的可用性和稳定性。项目使用TestNG作为测试框架，使用RestAssured作API接口测试。下面将测试项目的使用和开发进行讲解，并对项目所用三方框架的基础功能进行介绍，更多高级用法，请自行查阅资料。

# 1 TestNG简介

TestNG是一个用于编写和运行自动化测试的Java测试框架。

## 1.1 测试用例

```java
import org.testng.annotations.Test;

public class BasicTest {
    @Test
    public void testMethod1() {
        System.out.println("Test Method 1");
    }
}
```

## 1.2 执行测试

- **IDEA中**：右键点击测试类或方法，选择 `Run`。

- **命令行（Maven）**：

  ```sh
  mvn test
  ```

## 1.3 配置文件

`testng.xml` 是 TestNG 的核心配置文件，用于定义测试套件（Test Suite）、测试组、参数化配置等，示例如下：。

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- 定义一个测试套件，可以包含多个 <test> -->
<suite name="Suite Name">

    <!-- 定义一个测试模块，可以包含多个类或方法 -->
    <test name="Test Name">

        <!-- 指定要运行的测试类，多个测试类顺序执行 -->
        <classes>
            <class name="com.example.TestClass1"/>
            <class name="com.example.TestClass2">
                <methods>

                    <!-- 包含指定的方法 -->
                    <include name="testMethod1"/>

                    <!-- 排除指定的方法 -->
                    <exclude name="testMethod2"/>
                </methods>
            </class>
        </classes>
    </test>
</suite>
```

## 1.4 主要注解

- `@Test`注解：标记某个方法为测试方法
  - `priority`属性：当前测试类中，为测试方法分配优先级数值（整数），数值越小越先执行。默认为0。
  - `dependsOnMethods `属性：标记当前方法在哪些方法之后执行。
- `@BeforeClass` / `@AfterClass`注解：当前测试类前后执行

示例如下：

```java
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DemoTest {
    @BeforeClass
    public void beforeClassA() {
        System.out.println("本测试类中第一个执行");
    }

    @Test
    public void step0() {
        System.out.println("优先级为0，优先执行");
    }

    @Test(dependsOnMethods = {"step0"})
    public void loginTest() {
        System.out.println("必须等 step0() 执行成功后才执行，优先级默认为0");
    }

    @Test(priority = 1)
    public void step1() {
        System.out.println("优先级为1，在0之后执行");
    }

    @Test(priority = 1)
    public void aStep1() {
        System.out.println("优先级为1，在0之后执行，与相同优先级执行顺序不分先后");
    }

    @Test(priority = 2)
    public void step2() {
        System.out.println("优先级为2，在1之后执行");
    }
}
```

# 2 RestAssured简介

RestAssured 是一个 Java 语言编写的开源框架，专为**简化和自动化 RESTful API 的测试**过程而设计。

## 2.1 快速示例

```java
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class SimpleGetTest {
    public static void main(String[] args) {
        Response response = RestAssured.given()
                .baseUri("https://www.baidu.com")
                .when()
                .get("/");

        response.then()
                .statusCode(200);

        System.out.println(response.getBody().asString());
    }
}
```

## 2.2 框架核心类

### 2.2.1 RestAssured (核心入口类)
- 框架的主入口点

- 静态全局变量

  ```java
  RestAssured.baseURI = "https://api.example.com"; // 设置基础URI
  RestAssured.port = 8080; // 设置端口
  RestAssured.basePath = "/api"; // 设置基础路径
  ```

### 2.2.2 RequestSpecification (请求规范)
- 构造HTTP请求
- 通过 `RestAssured.given()` 方法获取实例
- 负责设置请求参数、头信息、body等
- 示例：
  ```java
  RequestSpecification request = RestAssured.given()
      .header("Content-Type", "application/json")
      .queryParam("page", 1);
  ```

### 2.2.3 Response (响应对象)
- 表示HTTP响应
- 通过 `RequestSpecification.when().get()/post()` 等方法返回
- 提供响应验证和提取方法
- 主要方法：
  ```java
  Response response = get("/users"); // 获取响应对象
  response.getStatusCode(); // 获取状态码
  response.getBody().asString(); // 获取响应体字符串
  response.jsonPath().get("name"); // 使用JsonPath提取值
  response.then().body("data", Matchers.equalTo("Success")); // 验证响应体中的字段
  ```

## 2.3 发送请求示例

### 2.3.1 GET 请求

#### 2.3.1.1 普通 GET 请求
```java
given()
.when()
    .get("https://api.example.com/users")
.then()
    .statusCode(200);
```

#### 2.3.1.2 GET 请求使用 ? 传参 (查询参数)
```java
given()
    .queryParam("page", 2)  // 添加查询参数
    .queryParam("limit", 10)
.when()
    .get("https://api.example.com/users")
.then()
    .statusCode(200)
    .body("page", equalTo(2));
```

#### 2.3.1.3 GET 请求使用路径传参
```java
given()
.when()
    .get("https://api.example.com/users/{id}", 123)  // 路径参数
.then()
    .statusCode(200)
    .body("id", equalTo(123));
```

### 2.3.2 POST 请求

#### 2.3.2.1 POST 请求使用 JSON 传参
```java
// 准备JSON请求体
Map<String, Object> requestBody = new HashMap<>();
requestBody.put("name", "John");
requestBody.put("age", 30);

given()
    .contentType(ContentType.JSON)  // 设置Content-Type
    .body(requestBody)             // 设置请求体
.when()
    .post("https://api.example.com/users")
.then()
    .statusCode(201);
```

或者使用字符串形式的JSON:
```java
given()
    .contentType(ContentType.JSON)
    .body("{\"name\":\"John\", \"age\":30}")
.when()
    .post("https://api.example.com/users");
```

#### 2.3.2.2 POST 请求使用 form 表单传参
```java
given()
    .contentType(ContentType.URLENC)  // 设置表单Content-Type
    .formParam("username", "testuser")  // 表单参数
    .formParam("password", "test123")
.when()
    .post("https://api.example.com/login")
.then()
    .statusCode(200);
```

### 2.3.3 PUT /DELETE请求

与get/post请求类似，仅将请求方法换成put/delete即可，此处不一一举例。

## 2.4 Response校验示例

### 2.4.1 响应状态码校验

```java
response.then().statusCode(200);
```

### 2.4.2 响应体字段值校验

响应数据

```json
{
    "msg": "success",
    "code": 200,
    "data": {
        "userId": 7,
        "userName": "test",
        "nickName": "test_nick",
        "phonenumber": "12345678945",
    },
}
```

校验方式

```java
response.then().body("code", Matchers.equalTo(200))
               .body("data.userName", Matchers.equalTo("test"));
```

### 2.4.3 数组数据校验

响应数据

```json
{
    "total": 2,
    "rows": [
        {
            "userId": 1,
            "userName": "admin",
            "nickName": "超级管理员",
            "phonenumber": "phoneNumber",
        },
        {
            "userId": 7,
            "userName": "test",
            "nickName": "test_nick",
            "phonenumber": "12345678945",
        }
    ],
    "code": 200,
    "msg": "success"
}
```

校验方式

```java
// 校验响应体数组中的数据
response.then()
        .body("code", Matchers.equalTo(200))
        .body("rows.userName", Matchers.hasItem("test"));
// 取出响应体数组中的数据
int userId = response.jsonPath().getInt("rows.find { it.userName == 'test' }.userId");
```

以上为常见的响应体校验和取值方式，其他用法，请自行查阅资料。

# 3 测试框架使用指引

## 3.1 准备DataKit服务

本项目为DataKit API接口测试项目，因此需要提前搭建好DataKit服务为当前测试项目提供API接口。

## 3.2 配置项目

将DataKit服务信息配置到`src/test/resources/application.yml`配置文件中，并配置服务器和数据库信息。

## 3.3 启动测试

配置完成后，命令行切换到ApiTest目录下，执行`mvn test`。

# 4 测试框架开发指引

## 4.1 开发测试用例

添加测试用例前，请先按照**测试框架使用指引**步骤，配置好配置文件，并跑通老的测试用例，以保证新的测试用例可以直接调试。

### 4.1.1 创建测试类

在对应目录结构中创建测试类，当前设计存储测试类的目录结构如下：

```sh
--plugins
  --base.ops
    --xxx.java
  --data.migration
  --...
--visualtool.api
  --ops
    --xxx.java
  --xxx.java
```

创建测试类时，测试类示例如下：

```java
package org.opengauss.plugins.data.migration;

import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengauss.visualtool.api.SysLogControllerTest;
import org.testng.annotations.BeforeClass;

public class MigrationTaskResourceControllerTest {
    private static final Logger logger = LogManager.getLogger(MigrationTaskResourceControllerTest.class);

    // 设置此类的测试方法的基础路径
    @BeforeClass
    public void setTestBasePath() {
        RestAssured.basePath = "/plugins/data-migration/resource";
        logger.info("MigrationTaskResourceControllerTest start.");
    }
}
```

### 4.1.2 创建测试方法

```java
// 创建测试方法
    @Test
    public void sourceClustersTest() {
        // getRequestSpecification()为已经封装好的带token的RequestSpecification对象，直接使用即可
        Response response = getRequestSpecification().when()
                .get("/sourceClusters");

        // 由于没有提前添加集群，所以此处校验接口响应的其他字段
        // 响应信息格式：{"msg":"success","code":200,"data":{"sourceClusters":[]}}
        response.then()
                .statusCode(200)
                .body("msg", Matchers.equalTo("success"));
    }
```

### 4.1.3 调试测试方法

鼠标悬浮在方法上，右键，点击Run。

### 4.1.4 添加测试类到XML中

将新建的测试类添加到`src/test/resources/testng.xml`文件中。

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd" >
<suite name="ApiTest">
    <test name="visualtool-api">
        <classes>
            <class name="org.opengauss.visualtool.api.SysLoginControllerTest"/>
            <!-- ... -->
            <class name="org.opengauss.visualtool.api.ops.OpsHostTagControllerTest"/>
        </classes>
    </test>
    <test name="base-ops">
        <classes>

        </classes>
    </test>
    <test name="data-migration">
        <classes>
            <class name="org.opengauss.plugins.data.migration.MigrationTaskResourceControllerTest"/>
        </classes>
    </test>
</suite>
```

### 4.1.5 集成测试

命令行切换到`ApiTest`目录下，执行`mvn test`。