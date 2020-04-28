package com.vx.dao;

import com.vx.model.Type;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TypeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Type record);

    Type selectByPrimaryKey(Integer id);

    List<Type> selectAll();

    int updateByPrimaryKey(Type record);
}