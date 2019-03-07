package com.pm.framework.config;

public interface Config {
    /* 链接属性 */
    String driverName = "";

    String url = "";

    String userName = "root";

    String password = "";

    int minConnections = 1; // 空闲池，最小连接数

    int maxConnections = 6; // 空闲池，最大连接数

    int initConnections = 3;// 初始化连接数

    long connTimeOut = 3000;// 重复获得连接的频率

    int maxActiveConnections = 8;// 最大允许的连接数，和数据库对应

    long connectionTimeOut = 1000 * 60 * 20;// 连接超时时间，默认20分钟
}
