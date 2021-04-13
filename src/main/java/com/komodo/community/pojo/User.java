package com.komodo.community.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ZhangGJ
 * @Date 2021/04/08 22:07
 */
@Data
public class User implements Serializable {

    private static final long serialVersionUID = -7020228846196513799L;

    private Integer id;

    private String username;

    private String password;

    private String birthday;
}
