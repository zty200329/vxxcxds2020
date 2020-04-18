package com.vx.dao;

import com.vx.model.ActivityUserHistory;
import java.util.List;

public interface ActivityUserHistoryMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ActivityUserHistory record);

    ActivityUserHistory selectByPrimaryKey(Long id);

    List<ActivityUserHistory> selectAll();

    int updateByPrimaryKey(ActivityUserHistory record);
}