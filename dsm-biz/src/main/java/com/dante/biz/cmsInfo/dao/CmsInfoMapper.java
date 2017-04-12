package com.dante.biz.cmsInfo.dao;

import java.util.List;

import com.dante.model.base.PageParam;
import com.dante.model.cmsInfo.CmsInfo;

public interface CmsInfoMapper {
    int deleteByPrimaryKey(String cmsId);

    int insert(CmsInfo record);

    int insertSelective(CmsInfo record);

    CmsInfo selectByPrimaryKey(String cmsId);

    int updateByPrimaryKeySelective(CmsInfo record);

    int updateByPrimaryKey(CmsInfo record);

	List<CmsInfo> queryCmsInfoList(PageParam params);
}