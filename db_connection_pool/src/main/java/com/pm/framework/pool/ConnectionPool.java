package com.pm.framework.pool;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionPool {
    Connection getConnection()throws InterruptedException ;
    void releaseConnection(Connection connection)throws SQLException;
}
