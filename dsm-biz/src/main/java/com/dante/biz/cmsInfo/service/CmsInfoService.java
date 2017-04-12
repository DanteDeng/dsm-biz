package com.dante.biz.cmsInfo.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dante.biz.cmsInfo.dao.CmsInfoMapper;
import com.dante.biz.fileUpload.service.IFileOperationService;
import com.dante.common.util.StrUtil;
import com.dante.controller.BaseController;
import com.dante.model.base.CommonResult;
import com.dante.model.base.PageParam;
import com.dante.model.cmsInfo.CmsInfo;
import com.dante.model.fileUpload.FileInfoParam;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
@Service("cmsInfoService")
public class CmsInfoService implements ICmsInfoService{
	
	public Log logger = LogFactory.getLog(BaseController.class);
	
	private final String BASE64_IMG_PREFIX= "data:image/png;base64,";
	
	private final String CMS_FILE_PATH = "cms/cmsImage";
	
	@Autowired
	private CmsInfoMapper cmsInfoMapper;
	
	@Autowired
	private IFileOperationService fileOperationService;

	@Override
	public CmsInfo getCmsInfoById(CmsInfo cmsInfo) throws Exception {
		return cmsInfoMapper.selectByPrimaryKey(cmsInfo.getCmsId());
	}

	@Override
	public List<CmsInfo> queryCmsInfoList(PageParam params) throws Exception {
		return cmsInfoMapper.queryCmsInfoList(params);
	}

	@Override
	public PageInfo<CmsInfo> pageCmsInfoList(PageParam params) throws Exception {
		logger.info("pageCmsInfoList----------params: "+params.getPageNum()+params.getPageSize());
		Page<CmsInfo> page = PageHelper.startPage(params.getPageNum(), params.getPageSize());
		cmsInfoMapper.queryCmsInfoList(params);
		return page.toPageInfo();
	}

	@Override
	public int deleteCmsInfo(CmsInfo cmsInfo) throws Exception {
		return cmsInfoMapper.deleteByPrimaryKey(cmsInfo.getCmsId());
	}

	@Override
	public CommonResult saveCmsInfo(CmsInfo cmsInfo) throws Exception {
		CommonResult result = new CommonResult();
		//校验参数
		String errorMessage = validateSaveCmsInfoParam(cmsInfo);
		if(errorMessage!=null){		
			result.setCode(CommonResult.RESULT_FAIL);
			result.setMessage(errorMessage);
			return result;
		}
		String cmsId = cmsInfo.getCmsId();
		//logger.info("saveCmsInfo----start------cmsInfo.getCmsImageUrl()\n"+cmsInfo.getCmsImageUrl());
		uploadCmsImage(cmsInfo);	//上传图片文件
		if(StrUtil.isEmpty(cmsId)){	//未传入主键ID进行新增
			cmsInfoMapper.insert(cmsInfo);
			result.setCode(CommonResult.RESULT_SECCESS);
			result.setMessage(CommonResult.INSERT_SUCCESS);
		}else{		//传入主键ID进行修改
			int updateNum = cmsInfoMapper.updateByPrimaryKeySelective(cmsInfo);
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

	/**
	 * 上传图片文件逻辑
	 * @param cmsInfo
	 */
	private void uploadCmsImage(CmsInfo cmsInfo) {
		String cmsImageUrl = cmsInfo.getCmsImageUrl();		//获取文件内容字段值
		//logger.info("cmsImageUrl---------:\n"+cmsImageUrl+"cmsImageUrl!=null:\n"+(cmsImageUrl!=null)+"cmsImageUrl.startsWith(BASE64_IMG_PREFIX):\n"+cmsImageUrl.startsWith(BASE64_IMG_PREFIX));
		if(cmsImageUrl!=null){
			if(cmsImageUrl.startsWith(BASE64_IMG_PREFIX)){	//如果是为保存为文件的base64字符创，执行保存逻辑
				FileInfoParam fileInfo = new FileInfoParam();
				fileInfo.setFileString(cmsImageUrl.replace(BASE64_IMG_PREFIX, ""));
				fileInfo.setFilePath(CMS_FILE_PATH);
				fileInfo.setFileName(formatCmsImageName());
				fileInfo.setCheckSameFile(true);			//设置不去保存重复的文件
				try {
					String fileUrl = fileOperationService.saveFile(fileInfo);
					if(fileUrl==null){
						cmsInfo.setCmsImageUrl("");
					}else{
						cmsInfo.setCmsImageUrl(fileUrl);
					}
				} catch (Exception e) {
					logger.error("uploadCmsImage------Exception: ",e);
					cmsInfo.setCmsImageUrl("");
				}
			}
		}
	}

	private String formatCmsImageName() {
		long timestamp = System.currentTimeMillis();
		String cmsImageName = timestamp+".jpg";
		return cmsImageName;
	}

	private String validateSaveCmsInfoParam(CmsInfo cmsInfo) {
		if(StrUtil.isEmpty(cmsInfo.getCmsTitle())
				||StrUtil.isEmpty(cmsInfo.getCmsAuthor())
				||StrUtil.isEmpty(cmsInfo.getCmsContent())){
			return CommonResult.LACK_REQUIRED_PARAM;
		}
		return null;
	}

}
