package com.pm.framework.test;

import com.pm.framework.pool.ConnectionPool;
import com.pm.framework.pool.impl_by_queue.DataConnectionPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(15);
//        ExecutorService executorService = Executors.newFixedThreadPool(15);
        ConnectionPool dataConnectionPool = new DataConnectionPool();
        for (int i = 0; i < 50; i++) {
            executorService.execute(new Worker(dataConnectionPool));
        }
    }
}
