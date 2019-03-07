package com.pm.framework.pool.impl_by_queue;

import com.pm.framework.config.Config;
import com.pm.framework.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DataConnectionPool implements ConnectionPool {

    // 空闲连接池
    BlockingQueue<Connection> idleConnections = new LinkedBlockingQueue<Connection>(Config.maxConnections);
    //记录当前存活连接数
    public static AtomicInteger count = new AtomicInteger(0);

    public DataConnectionPool() {
        init();
    }


    public void init() {
        // 初始化连接数
        for (int i = 0; i < Config.initConnections; i++) {
            Connection connection = createConnection();
            // 放入空闲连接池
            if (isAvaliable(connection)) {
                idleConnections.offer(connection);
            }
        }
    }

    // 获取连接
    public synchronized Connection getConnection() throws InterruptedException {
        Connection connection = null;
        if (count.get() < Config.maxActiveConnections) {
            // 如果没有到达最大连接数
            if (idleConnections.size() > 0) {
                // 空闲池有连接直接取
                connection = idleConnections.poll(Config.connTimeOut, TimeUnit.MILLISECONDS);
            } else {
                // 空闲池没有连接 直接等待
                connection = createConnection();
            }
        } else {
            // 超过最大连接数 则需要等待空闲连接池的空闲连接
            connection = idleConnections.poll(Config.connTimeOut / 1000, TimeUnit.SECONDS);
        }
        // 检查获取的连接是否有效，无效的话进行递归操作
        if (!isAvaliable(connection)) {
            connection = getConnection();
        }
        return connection;
    }

    // 创建连接
    private Connection createConnection() {
        Connection connection = null;
        try {
            Class.forName(Config.driverName);
            connection = DriverManager.getConnection(Config.url, Config.userName, Config.password);
            count.getAndIncrement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    // 判断连接是否有效
    private boolean isAvaliable(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                count.getAndDecrement();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    // 释放连接
    public void releaseConnection(Connection connection) throws SQLException {
        // 如果空闲池的连接数大于最大空闲连接数 则直接关闭连接
        if (isAvaliable(connection)) {
            if (idleConnections.size() >= Config.maxConnections) {
                connection.close();
                count.getAndDecrement();
            } else {
                // 否则将连接从活动连接池转移到空闲连接池
                idleConnections.offer(connection);
            }
        }
    }
}
