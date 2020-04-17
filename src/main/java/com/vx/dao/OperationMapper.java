package com.vx.dao;

import com.vx.model.Operation;
import java.util.List;

public interface OperationMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Operation record);

    Operation selectByPrimaryKey(Long id);

    List<Operation> selectAll();

    int updateByPrimaryKey(Operation record);
}