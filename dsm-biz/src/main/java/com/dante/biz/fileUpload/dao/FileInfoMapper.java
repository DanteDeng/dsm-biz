package com.dante.biz.fileUpload.dao;

import java.util.List;

import com.dante.model.fileUpload.FileInfo;
import com.dante.model.fileUpload.FileInfoParam;

public interface FileInfoMapper {
    int deleteByPrimaryKey(String fileId);

    int insert(FileInfo record);

    int insertSelective(FileInfo record);

    FileInfo selectByPrimaryKey(String fileId);

    int updateByPrimaryKeySelective(FileInfo record);

    int updateByPrimaryKey(FileInfo record);
    
    List<FileInfo> queryFileInfoList(FileInfoParam param);

	int deleteFileInfo(FileInfo fileInfo);
}