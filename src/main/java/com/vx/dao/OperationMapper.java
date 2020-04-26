package com.vx.dao;

import com.vx.dto.OperationDTO;
import com.vx.model.Operation;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OperationMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Operation record);

    Operation selectByPrimaryKey(Long id);

    List<Operation> selectAll();

    int updateByPrimaryKey(Operation record);

    List<OperationDTO> selectByActivityId(Long id);

    List<Operation> selectByActivityId2(Long id);
}