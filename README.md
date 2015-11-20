# SMS
实验性质的短信网关

[![Build Status](https://travis-ci.org/HP-Enterprise/SMS.svg?branch=dev)](https://travis-ci.org/HP-Enterprise/SMS)

## 前提条件
- [JDK 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Gradle 2.5+](http://gradle.org/gradle-download/)


## 配置文件
本项目使用的配置文件位于
- [$/src/main/resources/application.yml](https://github.com/HP-Enterprise/SMS/blob/dev/src/main/resources/application.yml)
- 默认激活dev配置,因此,可以在相同位置创建一个名为`application-dev.yml`的配置文件,按自己的需要重载配置项
- 也可以通过定义一个名为spring.profiles.active的系统属性来指定激活的配置,例如:
```SHELL
gradle -Dspring.profiles.active=product run
```
那么 $/src/main/resources/application-product.yml 将被激活.


## 运行
```SHELL
gradle run
```