package com.threeatom.stefen2024.controller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface mapper {


    //简单写一个通过username来找id的查询
    @Select("select description from `user` where username = {username}  ")
    public String select(@Param("username")String username);
}
