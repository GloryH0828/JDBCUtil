package com.gloryh.entity;

import com.gloryh.annotation.Column;
import com.gloryh.annotation.PrimaryKey;
import com.gloryh.annotation.Table;
import lombok.Data;

@Data
@Table("admin")
public class Admin {
    @PrimaryKey
    @Column("admin_id")
    private Integer id;
    @Column("admin_username")
    private String username;
    @Column("admin_password")
    private String password;
}
