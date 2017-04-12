package com.dante.biz.fileUpload.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dante.biz.fileUpload.dao.ImageMapper;
import com.dante.model.fileUpload.Image;
import com.dante.biz.fileUpload.service.IImageService;
import com.dante.model.base.PageParam;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class ImageService implements IImageService{
	
	@Autowired
	private ImageMapper imageMapper;

	@Override
	public Image getImageById(Image i) throws Exception {
		return imageMapper.selectByPrimaryKey(i.getImageId());
	}

	@Override
	public List<Image> queryImageList(PageParam params) throws Exception {
		return imageMapper.queryImageList(params); 
	}

	@Override
	public PageInfo<Image> pageImageList(PageParam params) throws Exception {
		Page<Image> result = PageHelper.startPage(params.getPageNum(), params.getPageSize());
		imageMapper.queryImageList(params); 
		return result.toPageInfo();
	}

	@Override
	public void addImage(Image image) throws Exception {
		imageMapper.insert(image);
	}

	@Override
	public int updateImage(Image image) throws Exception {
		return imageMapper.updateByPrimaryKeySelective(image);
	}

	@Override
	public int deleteImage(Image image) throws Exception {
		return imageMapper.deleteByPrimaryKey(image.getImageId());
	}

}
