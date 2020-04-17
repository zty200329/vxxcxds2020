package com.vx.dao;

import com.vx.model.Activity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ActivityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Activity record);

    Activity selectByPrimaryKey(Long id);

    List<Activity> selectAll();

    int updateByPrimaryKey(Activity record);
}