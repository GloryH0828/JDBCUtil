package com.gloryh.test;

import com.gloryh.entity.Student;
import com.gloryh.utils.C3P0Tools;
import com.gloryh.utils.JDBCTools;

import java.sql.Connection;

public class Test {


    public static void main(String[] args) throws Exception {
        //获取连接
        Connection connection= C3P0Tools.getConnection();
        Student student=new Student();
        student.setName("test");
        student.setUsername("22228888");
        student.setPassword("123321");
        student.setSex("男");
        student.setPhone("17620000000");
        student.setClassId(1);
        Boolean result=JDBCTools.addByBean(connection,student);
        System.out.println(result);
    }
}
