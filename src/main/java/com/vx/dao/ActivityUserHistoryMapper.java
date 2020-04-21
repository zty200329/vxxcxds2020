package com.vx.dao;

import com.vx.model.ActivityUserHistory;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ActivityUserHistoryMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ActivityUserHistory record);

    ActivityUserHistory selectByPrimaryKey(Long id);

    List<ActivityUserHistory> selectAll();

    int updateByPrimaryKey(ActivityUserHistory record);

    ActivityUserHistory selectByOpenId(Long id,Long activityId,Long sonActivityId);
}