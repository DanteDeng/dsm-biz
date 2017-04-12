package com.dante.biz.apiInfo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dante.biz.apiInfo.dao.ColumnInfoMapper;
import com.dante.model.apiInfo.ColumnInfo;

@Service("columnInfoService")
public class ColumnInfoService implements IColumnInfoService{
	
	@Autowired
	private ColumnInfoMapper columnInfoMapper;

	@Override
	public ColumnInfo queryColumnInfoDetail(ColumnInfo columnInfo) throws Exception {
		String columnInfoId = columnInfo.getColumnInfoId();
		return columnInfoMapper.selectByPrimaryKey(columnInfoId);
	}

}
