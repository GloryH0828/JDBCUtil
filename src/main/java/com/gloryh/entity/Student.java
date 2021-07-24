package com.gloryh.entity;

import com.gloryh.annotation.Column;
import com.gloryh.annotation.Table;
import lombok.Data;

@Data
public class Student {
    @Column("student_id")
    private Integer id;
    @Column("student_name")
    private String name;
    @Column("student_username")
    private String username;
    @Column("student_password")
    private String password;
    @Column("student_sex")
    private String sex;
    @Column("student_phone")
    private String phone;
    @Column("student_email")
    private String email;
    @Column("class_id")
    private Integer classId;
}
