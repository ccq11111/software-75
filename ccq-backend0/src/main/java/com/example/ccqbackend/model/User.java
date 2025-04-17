package com.example.ccqbackend.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class User {

    @Id
    private String id;  // 用户ID

    private String username;  // 用户名
    private String password;  // 密码
    private String email;     // 邮箱
    private String phone;     // 电话
}

