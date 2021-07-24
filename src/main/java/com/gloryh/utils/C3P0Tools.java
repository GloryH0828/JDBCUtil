package com.gloryh.utils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class C3P0Tools {
    private static ComboPooledDataSource dataSource;

    static {
        dataSource = new ComboPooledDataSource("c3p0");
    }

    /**
     * 获取连接对象
     * @return connection
     */
    public static Connection getConnection(){
        Connection connection =null;
        try {
            connection= dataSource.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return connection;
    }

    /**
     * 关闭连接。释放资源
     * @param connection 数据库连接对象
     * @param statement 执行SQL对象
     * @param resultSet 查询结果
     */
    public static void release(Connection connection, Statement statement, ResultSet resultSet){
        try {
            if(connection !=null){
                connection.close();
            }
            if(statement!=null){
                statement.close();
            }
            if(resultSet!=null){
                resultSet.close();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
