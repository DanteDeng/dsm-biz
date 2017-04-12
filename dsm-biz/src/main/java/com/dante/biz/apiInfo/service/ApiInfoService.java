package com.dante.biz.apiInfo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dante.biz.apiInfo.dao.ApiColumnMapper;
import com.dante.biz.apiInfo.dao.ApiInfoMapper;
import com.dante.biz.apiInfo.dao.ColumnInfoMapper;
import com.dante.common.util.StrUtil;
import com.dante.model.apiInfo.ApiColumn;
import com.dante.model.apiInfo.ApiInfo;
import com.dante.model.apiInfo.ApiInfoParam;
import com.dante.model.apiInfo.ColumnInfo;
import com.dante.model.base.CommonResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("apiInfoService")
public class ApiInfoService implements IApiInfoService{
	
	@Autowired
	private ApiInfoMapper apiInfoMapper;
	
	@Autowired
	private ApiColumnMapper apiColumnMapper;
	
	@Autowired
	private ColumnInfoMapper columnInfoMapper;

	@Override
	public List<ApiInfo> queryApiInfoList(ApiInfoParam param) throws Exception {
		return apiInfoMapper.queryApiInfoList(param);
	}
	
	@Override
	public ApiInfo queryApiInfoDetail(ApiInfo apiInfo) throws Exception {
		String apiInfoId = apiInfo.getApiInfoId();
		ApiInfo record = apiInfoMapper.selectByPrimaryKey(apiInfoId);
		ApiColumn apiColumn = new ApiColumn();	//查询出入参用作参数
		apiColumn.setApiInfoId(apiInfoId);
		apiColumn.setApiColumnType("0");		//查询入参条件
		List<ApiColumn> paramInList = apiColumnMapper.selectByApiColumn(apiColumn);
		apiColumn.setApiColumnType("1");		//查询出参条件
		List<ApiColumn> paramOutList = apiColumnMapper.selectByApiColumn(apiColumn);
		record.setParamInList(paramInList);
		record.setParamOutList(paramOutList);
		return record;
	}

	@Override
	public PageInfo<ApiInfo> pageApiInfoList(ApiInfoParam param) throws Exception {
		Page<ApiInfo> page = PageHelper.startPage(param.getPageNum(), param.getPageSize());
		apiInfoMapper.queryApiInfoList(param);
		return page.toPageInfo();
	}

	@Override
	public CommonResult saveApiInfo(ApiInfo apiInfo) throws Exception {
		CommonResult result = new CommonResult();
		//校验参数
		String errorMessage = validateSaveApiInfoParam(apiInfo);
		if(errorMessage!=null){		
			result.setCode(CommonResult.RESULT_FAIL);
			result.setMessage(errorMessage);
			return result;
		}
		String apiInfoId = apiInfo.getApiInfoId();
		if(StrUtil.isEmpty(apiInfoId)){	//未传入主键ID进行新增
			apiInfoMapper.insert(apiInfo);
			//保存出参入参
			saveParamList(apiInfo);
			
			result.setCode(CommonResult.RESULT_SECCESS);
			result.setMessage(CommonResult.INSERT_SUCCESS);
		}else{		//传入主键ID进行修改
			int updateNum = apiInfoMapper.updateByPrimaryKeySelective(apiInfo);
			//删除出参入参
			apiColumnMapper.deleteByApiInfoId(apiInfoId);
			//保存出参入参
			saveParamList(apiInfo);
			if(updateNum>0){
				result.setCode(CommonResult.RESULT_SECCESS);
				result.setMessage(CommonResult.UPDATE_SUCCESS);
			}else{
				result.setCode(CommonResult.RESULT_FAIL);
				result.setMessage(CommonResult.UPDATE_FAIL);
			}
		}
		return result;
	}

	private void saveParamList(ApiInfo apiInfo) {
		List<ApiColumn> paramInList = apiInfo.getParamInList();
		saveApiColumn(apiInfo, paramInList);	//入参保存
		List<ApiColumn> paramOutParam = apiInfo.getParamOutList();
		saveApiColumn(apiInfo, paramOutParam);	//出参保存
	}

	//保存参数
	private void saveApiColumn(ApiInfo apiInfo, List<ApiColumn> paramInList) {
		if(paramInList!=null){	//有设置入参则保存入参
			for (ApiColumn apiColumn : paramInList) {
				apiColumn.setApiInfoId(apiInfo.getApiInfoId());
				apiColumnMapper.insert(apiColumn);
				ColumnInfo columnInfo = columnInfoMapper.selectByPrimaryKey(apiColumn.getColumnInfoId());
				if(columnInfo==null){
					columnInfo = new ColumnInfo();
					columnInfo.setColumnInfoId(apiColumn.getColumnInfoId());
					columnInfo.setColumnInfoName(apiColumn.getColumnInfoName());
					columnInfo.setColumnInfoType("0");  //新增字段设置默认普通类型
					columnInfoMapper.insert(columnInfo);
				}
			}
		}
	}

	private String validateSaveApiInfoParam(ApiInfo apiInfo) {
		if(StrUtil.isEmpty(apiInfo.getApiInfoName())
				||StrUtil.isEmpty(apiInfo.getApiInfoUrl())){
			return CommonResult.LACK_REQUIRED_PARAM;
		}
		return null;
	}

	@Override
	public CommonResult deleteApiInfo(ApiInfo apiInfo) throws Exception {
		CommonResult result = new CommonResult();
		
		String apiInfoId = apiInfo.getApiInfoId();
		if(StrUtil.isEmpty(apiInfoId)){	//未传入主键ID提示错误信息
			result.setCode(CommonResult.RESULT_FAIL);
			result.setMessage(CommonResult.DELETE_NO_VALUE);
		}else{		//传入主键ID进行删除
			int deleteNum = apiInfoMapper.deleteByPrimaryKey(apiInfoId);
			if(deleteNum>0){
				//删除出参入参
				apiColumnMapper.deleteByApiInfoId(apiInfoId);
				result.setCode(CommonResult.RESULT_SECCESS);
				result.setMessage(CommonResult.DELETE_SUCCESS);
			}else{
				result.setCode(CommonResult.RESULT_FAIL);
				result.setMessage(CommonResult.DELETE_FAIL);
			}
		}
		return result;
	}

}
