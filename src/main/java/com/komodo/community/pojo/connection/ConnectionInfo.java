package com.komodo.community.pojo.connection;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ZhangGJ
 * @Date 2021/04/05 10:22
 */
@Data
public class ConnectionInfo implements Serializable {

    private static final long serialVersionUID = 4918883975797925025L;

    private String driver;

    private String url;

    private String username;

    private String password;
}
