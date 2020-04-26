package com.vx.dao;

import com.vx.dto.ActivityDTO;
import com.vx.model.Activity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ActivityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Activity record);

    Activity selectByPrimaryKey(Long id);

    List<Activity> selectAll();

    int updateByPrimaryKey(Activity record);

    List<ActivityDTO> selectAsDistance(@Param("minlat")double minlat,@Param("maxlat")double maxlat,
                                       @Param("minlng")double minlng,@Param("maxlng")double maxlng,
                                       @Param("dis") double dis);
    List<ActivityDTO> selectAsDistance1();

    List<ActivityDTO> selectByOpenid(@Param("openid") String openId);

    List<ActivityDTO> findStore(@Param("name") String name);
}