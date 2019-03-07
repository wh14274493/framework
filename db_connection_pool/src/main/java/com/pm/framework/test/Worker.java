package com.pm.framework.test;

import com.pm.framework.pool.ConnectionPool;
import com.pm.framework.pool.impl_by_queue.DataConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

public class Worker implements Runnable {

    private ConnectionPool dataConnectionPool;

    public Worker(ConnectionPool dataConnectionPool){
        this.dataConnectionPool=dataConnectionPool;
    }
    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        try {
            Connection connection = dataConnectionPool.getConnection();
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName()+"获取连接【"+connection+"】,当前活跃连接数："+DataConnectionPool.count.get());
            dataConnectionPool.releaseConnection(connection);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
