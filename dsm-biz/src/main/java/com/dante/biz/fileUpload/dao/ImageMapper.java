package com.dante.biz.fileUpload.dao;

import java.util.List;

import com.dante.model.fileUpload.Image;
import com.dante.model.base.PageParam;

public interface ImageMapper {
    int deleteByPrimaryKey(String imageId);

    int insert(Image record);

    int insertSelective(Image record);

    Image selectByPrimaryKey(String imageId);

    int updateByPrimaryKeySelective(Image record);

    int updateByPrimaryKey(Image record);

	List<Image> queryImageList(PageParam params);
}