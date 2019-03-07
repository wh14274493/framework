package com.pm.framework.pool.impl_by_vector;

import com.pm.framework.config.Config;
import com.pm.framework.pool.ConnectionPool;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class DataConnectionPool implements ConnectionPool {

    // 空闲连接池
    private List<Connection> idleConnections = new Vector<Connection>();
    // 活跃连接池
    private List<Connection> activeConnections = new Vector<Connection>();
    //记录当前存活连接数
    public static int count = 0;

    public DataConnectionPool() {
        init();
    }


    public void init() {
        // 初始化连接数
        for (int i = 0; i < Config.initConnections; i++) {
            Connection connection = createConnection();
            // 放入空闲连接池
            idleConnections.add(connection);
        }
    }

    // 获取连接
    public synchronized Connection getConnection() throws InterruptedException {
        Connection connection = null;
        if (count < Config.maxActiveConnections) {
            // 活跃连接数小于最大连接数
            // 判断空闲连接池是否还有剩余连接
            if (idleConnections.size() > 0) {
                connection = idleConnections.remove(0);
                activeConnections.add(connection);
                return connection;
            } else {
                // 空闲连接池没有剩余的话，直接创建 创建失败的话进行递归
                connection = createConnection();
                if (!isAvaliable(connection)) {
                    count--;
                    getConnection();
                }
                activeConnections.add(connection);
            }
        } else {
            // 活跃连接数大于最大连接数 需要阻塞等待
            wait(Config.connTimeOut);
        }
        return connection;
    }

    // 创建连接
    private synchronized Connection createConnection() {
        try {
            Class.forName(Config.driverName);
            Connection connection = DriverManager.getConnection(Config.url, Config.userName, Config.password);
            if (isAvaliable(connection)) {
                count++;
                return connection;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 判断连接是否有效
    private boolean isAvaliable(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    // 释放连接
    public synchronized void releaseConnection(Connection connection) throws SQLException {
        // 如果空闲池的连接数大于最大空闲连接数 则直接关闭连接
        if(isAvaliable(connection)){
            activeConnections.remove(connection);
            if (idleConnections.size() >= Config.maxConnections) {
                connection.close();
                count--;
            } else {
                // 否则将连接从活动连接池转移到空闲连接池
                idleConnections.add(connection);
            }
            // 唤醒所有的等待线程
            notifyAll();
        }
    }
}
