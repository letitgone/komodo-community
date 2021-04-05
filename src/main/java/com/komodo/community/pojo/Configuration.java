package com.komodo.community.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ZhangGJ
 * @Date 2021/04/03 11:51
 */
@Data
public class Configuration implements Serializable {

    private static final long serialVersionUID = 7992912427491474523L;

    private DataBaseInfo dataBaseInfo;
}
