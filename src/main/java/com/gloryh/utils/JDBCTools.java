package com.gloryh.utils;

import com.gloryh.annotation.Column;
import com.gloryh.annotation.Table;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 这是一个CRUD工具类
 * @param <T> 传入的泛型类
 */
public class JDBCTools<T> {
    /**
     * 查询一条结果
     * @param connection 数据库连接对象
     * @param sql SQL 语句
     * @param clazz 传入的实体类
     * @param <T>  泛型化 实体类
     * @return 返回的查询结果实体类---泛型
     */
    public static<T> T getBean(Connection connection,String sql,Class clazz){
        Object object=null;
        PreparedStatement statement=null;
        ResultSet resultSet=null;
        try {
            //创建查询类
             statement = connection.prepareStatement(sql);
            //获取查询结果(默认只有一条)
             resultSet = statement.executeQuery();
            //完成实体类映射
             object= clazz.getConstructor(null).newInstance(null);
            //解析结果集
            parseDataToBean(resultSet,object);
        } catch (SQLException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException throwables) {
            throwables.printStackTrace();
        }finally {
            //关闭连接，释放资源
            C3P0Tools.release(connection,statement,resultSet);
        }
        return (T)object;
    }
    /**
     * 查询多条结果
     * @param connection 数据库连接对象
     * @param sql SQL 语句
     * @param clazz 传入的实体类
     * @param <T>  泛型化 实体类
     * @return 返回的查询结果实体类---泛型
     */
    public static<T> List<T> getBeans(Connection connection,String sql,Class clazz){
        List<T> list=new ArrayList<>();
        PreparedStatement statement=null;
        ResultSet resultSet=null;
        try {
            //创建查询类
            statement = connection.prepareStatement(sql);
            //获取查询结果
            resultSet = statement.executeQuery();
            //解析结果集
            parseDataToList(resultSet,clazz,list);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            //关闭连接，释放资源
            C3P0Tools.release(connection,statement,resultSet);
        }
        return  list;
    }

    /**
     * 传入参数的查询语句查询一条数据
     * @param connection 数据库连接对象
     * @param sql 查询语句
     * @param clazz 实体类
     * @param params 字段参数
     * @param <T> 泛型
     * @return 实体类泛型
     */
    public static<T> T getBean(Connection connection,String sql,Class clazz,Object... params){
        Object object=null;
        PreparedStatement statement=null;
        ResultSet resultSet=null;
        try {
            statement=connection.prepareStatement(sql);
            //替换SQL 中的参数
            fillParams(statement,params);
            //获取查询结果
            resultSet=statement.executeQuery();
            //完成实体类映射
            object=clazz.getConstructor(null).newInstance(null);
            //对结果进行处理
            parseDataToBean(resultSet,object);
        } catch (SQLException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException throwables) {
            throwables.printStackTrace();
        } finally {
            //释放资源
            C3P0Tools.release(connection,statement,resultSet);
        }

        return (T)object;
    }

    /**
     * 传入参数的查询语句查询多条数据
     * @param connection 数据库连接对象
     * @param sql 查询语句
     * @param clazz 实体类
     * @param params 字段参数
     * @param <T> 泛型
     * @return 实体类泛型
     */
    public static<T> List<T> getBeans(Connection connection,String sql,Class clazz,Object... params){
        List<T> list=new ArrayList<>();
        PreparedStatement statement=null;
        ResultSet resultSet=null;
        try {
            statement=connection.prepareStatement(sql);
            //替换SQL 中的参数
            fillParams(statement,params);
            //获取查询结果
            resultSet=statement.executeQuery();
            //对结果进行处理
            parseDataToList(resultSet,clazz,list);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            //释放资源
            C3P0Tools.release(connection,statement,resultSet);
        }

        return list;
    }

    /**
     * 更新（包括修改和删除）数据库内对应实体类信息
     * @param connection 数据库连接对象
     * @param sql SQL语句
     * @param params SQL语句参数
     * @return 更新结果
     */
    public static Boolean updateBean(Connection connection,String sql,Object... params){
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            fillParams(statement,params);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }finally {
            C3P0Tools.release(connection,statement,null);
        }

        return true;
    }
    public static Boolean insert(Connection connection,String sql,Object... params){
        PreparedStatement statement=null;
        try {
           statement= connection.prepareStatement(sql);
            fillParams(statement,params);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 向数据库插入一条数据
     * @param connection 数据库连接对象
     * @param t 要插入的实体类信息的泛型
     * @return 插入的结果
     */
    public static<T> Boolean addByBean(Connection connection,T t) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Object object=(Object) t;
        //解析获取实体类名
        String className = object.getClass().getName();
        //获取注释Table 是否存在
        Table table=object.getClass().getAnnotation(Table.class);
        String tableName=null;
        if(table != null){
            //存在，获取对应的数据库名
            tableName = table.value();
        }else {
            //不存在，以实体类名为表明
            tableName=className.substring(object.getClass().getName().lastIndexOf(".")+1).toLowerCase();
        }
        //获取所有的属性字段
        Field[] declaredFields = object.getClass().getDeclaredFields();
        List<Object> params=new ArrayList<>();
        StringBuilder sql= new StringBuilder("insert into " + tableName + "(");
        StringBuilder paramsSql=new StringBuilder(") values(");
        for (Field declaredField : declaredFields) {
            //获取字段注解
            Column column=declaredField.getAnnotation(Column.class);
            //获取属性名称
            String name=declaredField.getName();
            //获取方法名
            String methodName="get"+name.substring(0,1).toUpperCase()+name.substring(1);
            declaredField.setAccessible(true);
            //构造方法
            Method method=object.getClass().getMethod(methodName,null);
            //反射获取值
            Object value=method.invoke(object);
            //判断是否为null
            if(value!=null){
                params.add(value);
                sql.append(column.value()).append(",");
                paramsSql.append("?,");
            }

        }
        sql = new StringBuilder(sql.substring(0, sql.length()-1));
        paramsSql=new StringBuilder(paramsSql.substring(0, paramsSql.length()-1)+")");
        return insert(connection,sql.append(paramsSql).toString(),params.toArray());
    }

    /**
     * 将参数传入查询语句
     * @param statement 查询类
     * @param params 字段参数
     */
    public static void fillParams(PreparedStatement statement,Object... params){
       try{
           for (int i = 0; i < params.length; i++) {
               Object param=params[i];
               //获取参数类型
               String typeName=param.getClass().getTypeName();
               //根基类型进行相应的处理
               switch (typeName){
                   case "java.lang.Integer":
                       statement.setInt(i+1,(Integer)param);
                       break;
                   case "java.lang.Double":
                       statement.setDouble(i+1,(Double)param);
                       break;
                   case "java.util.Date":
                       statement.setDate(i+1,(Date) param);
                       break;
                   default:
                       statement.setString(i+1,(String) param);
                       break;
               }
           }
       } catch (SQLException throwables) {
           throwables.printStackTrace();
       }
    }

    /**
     * 将查询结果转换为 实体类
     * @param resultSet 查询结果
     * @param object 实体类
     */
    public static void parseDataToBean( ResultSet resultSet, Object object){
        try {
            //结果解析
            ResultSetMetaData metaData = resultSet.getMetaData();
            //获取字段数量
            int columnCount = metaData.getColumnCount();
            //解析字段
            while (resultSet.next()){
                parseObject(columnCount,object,resultSet,metaData);
            }
        } catch (SQLException | NoSuchMethodException | InvocationTargetException | IllegalAccessException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * 将查询结果这条转化为实体类后存入列表
     * @param resultSet 数据库查询结果
     * @param clazz 实体类
     * @param list 要存入的列表
     * @param <T> 泛型
     */
    private static <T> void parseDataToList(ResultSet resultSet, Class clazz, List<T> list) {
        try {
            //结果解析
            ResultSetMetaData metaData = resultSet.getMetaData();
            //获取查询结果的字段数量
            int columnCount = metaData.getColumnCount();
            //循环遍历查询结果
            while (resultSet.next()){
                //映射成实体类
                Object object=clazz.getConstructor(null).newInstance(null);
               parseObject(columnCount,object,resultSet,metaData);
                list.add((T)object);
            }
        } catch (SQLException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException throwables) {
            throwables.printStackTrace();
        }
    }
    public static void parseObject(int columnCount,Object object,ResultSet resultSet,ResultSetMetaData metaData) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SQLException {
        //循环遍历字段
        for (int i = 1; i <= columnCount; i++) {
            //获取字段名
            String columnName = metaData.getColumnName(i);
            //获取字段类型名
            String columnTypeName = metaData.getColumnTypeName(i);
            Object value=null;
            //根据类型对获取的值进行处理
            switch (columnTypeName){
                case "INT":
                    value=resultSet.getInt(i);
                    break;
                case "DOUBLE":
                    value=resultSet.getDouble(i);
                    break;
                case "DATE":
                    value=resultSet.getDate(i);
                    break;
                default:
                    value=resultSet.getString(i);
                    break;
            }
            //对实体类属性进行赋值
            //首先，根据注解获取对应属性信息
            //获取字段组
            Field[] declaredFields = object.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                //获取字段注解
                Column column=declaredField.getAnnotation(Column.class);
                //判断是否与字段名一致
                if (column!=null&&columnName.equals(column.value())){
                    //获取字段的名字
                    String name = declaredField.getName();
                    //组成set方法
                    String methodName="set"+name.substring(0,1).toUpperCase()+name.substring(1);
                    //反射赋值
                    Method method=object.getClass().getMethod(methodName,declaredField.getType());
                    method.invoke(object,value);
                }
            }
        }
    }
}
