#!/bin/bash

# Java 安装版本
JAVA_VERSION="java-1.8.0-openjdk"

# 检查 Java 是否已安装
if type -p java; then
    echo "Java 已安装"
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "当前Java版本: $java_version"
else
    echo "Java 未安装，开始安装 JDK 8..."
    sudo yum update -y
    sudo yum install -y $JAVA_VERSION
fi

# 检查 Java 安装是否成功
if type -p java; then
    echo "Java 安装成功"
else
    echo "Java 安装失败"
    exit 1
fi

# 设置 JAVA_HOME 和 PATH
export JAVA_HOME=$(dirname $(dirname $(readlink $(readlink $(which java)))))
export PATH=$JAVA_HOME/bin:$PATH
echo "JAVA_HOME 已设置为 $JAVA_HOME"

# 启动 Spring Boot 项目
SPRINGBOOT_JAR="proxy.jar"  # 替换为你的 Spring Boot 应用的 JAR 包路径
LOG_FILE="springboot.log"  # 日志文件路径

if [ -f "$SPRINGBOOT_JAR" ]; then
    echo "启动 Spring Boot 项目..."
    nohup java -jar $SPRINGBOOT_JAR > $LOG_FILE 2>&1 &
    echo "Spring Boot 项目已启动，日志输出到 $LOG_FILE"
else
    echo "Spring Boot 项目 JAR 文件未找到: $SPRINGBOOT_JAR"
    exit 1
fi