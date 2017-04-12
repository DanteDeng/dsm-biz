package com.dante.biz.fileUpload.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import com.dante.biz.fileUpload.dao.FileInfoMapper;
import com.dante.common.util.FileInfoUtil;
import com.dante.common.util.StrUtil;
import com.dante.model.fileUpload.FileInfo;
import com.dante.model.fileUpload.FileInfoParam;

@Service("fileOperationService")
public class FileOperationService implements IFileOperationService{
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	@Value("${dsm_biz.rootPath}")
	private String rootPath;
	
	@Value("${dsm_biz.baseUrl}")
	private String baseUrl;
	
	@Autowired
	private FileInfoMapper fileInfoMapper;

	@Override
	public String saveFile(FileInfoParam fileInfo) throws Exception{
		String fileUrl = null;
		String filePath = FileInfoUtil.formatAbsolutePath(fileInfo.getFilePath(), rootPath);		//获得文件目录绝对路径
		String fileName = FileInfoUtil.formatPathString(fileInfo.getFileName(),fileInfo.getFileSuffix(),".");
		String fileWholeName = FileInfoUtil.formatPathString(filePath,fileName,File.separator);				//获得文件绝对路径
		String fileString = fileInfo.getFileString();	//获取文件内容
		if(StrUtil.isNotEmpty(filePath)&&StrUtil.isNotEmpty(fileString)){
			try {
				File directory = new File(filePath);						//创建输出文件目录
				if(!directory.exists()){									//目录不存在则创建文件夹
					directory.mkdirs();					
				}
				byte[] bytes = Base64Utils.decodeFromString(fileString);	//获取文件内容字节数组
				String fileMD5 = FileInfoUtil.getBytesMD5(bytes);			//获取传入文件的MD5值
				if(fileInfo.isCheckSameFile()){
					/**这里做文件去重操作，节省磁盘空间*/
					fileInfo.setCheckSubFile(true);
					fileInfo.setFilePath(FileInfoUtil.formatAbsolutePath(fileInfo.getFilePath(), rootPath)); 	//相对路径转绝对路径
					List<FileInfo> fileList = fileInfoMapper.queryFileInfoList(fileInfo);	//查询指定目录下全部文件（包括子目录下）
					String oldRepeatFileUrl = FileInfoUtil.getOldRepeatFileUrl(fileMD5, fileList, rootPath, baseUrl);
					if(oldRepeatFileUrl!=null){				//找到有相应的文件返回其url
						return oldRepeatFileUrl;
					}
				}
				
				File file = new File(fileWholeName);								//创建输出文件
				FileUtils.writeByteArrayToFile(file, bytes);				//文件写入
				logger.info("saveFile----file.getAbsolutePath()"+file.getAbsolutePath());
				/**这里做文件信息存库操作*/
				FileInfo record = FileInfoUtil.getFileInfo(file, fileMD5);
				fileInfoMapper.insert(record);
				fileUrl = FileInfoUtil.formatFileUrl(file.getAbsolutePath(), rootPath, baseUrl);
			} catch (FileNotFoundException e) {				//异常处理
				logger.error("saveFile----FileNotFoundException:", e);
				fileUrl = null;
			} catch (IOException e) {
				logger.error("saveFile----IOException:", e);
				fileUrl = null;
			}
		}
		return fileUrl;
	}

	@Override
	public List<String> listFile(FileInfoParam fileInfo) throws Exception{
		List<String> fileUrlList = null;
		String filePath = FileInfoUtil.formatAbsolutePath(fileInfo.getFilePath(), rootPath);		//获得文件目录绝对路径
		String fileName = getFileWholeName(fileInfo, filePath);
		if(StrUtil.isNotEmpty(filePath)){
			fileUrlList = new ArrayList<String>();
			if(StrUtil.isNotEmpty(fileName)){
				File file = new File(fileName);
				if(file.exists()&&file.isFile()){	//文件夹路径，返回文件的url
					String fileUrl = FileInfoUtil.formatFileUrl(file.getAbsolutePath(), rootPath, baseUrl);
					fileUrlList.add(fileUrl);
				}
			}else{
				File file = new File(filePath);
				if(file.exists()&&file.isDirectory()){	//文件夹路径，返回路径下全部文件的url
					String[] list = file.list();
					for (String string : list) {
						String fileUrl = FileInfoUtil.formatFileUrl(string, rootPath, baseUrl);
						fileUrlList.add(fileUrl);
					}
				}
			}
		}
		return fileUrlList;
	}

	@Override
	public boolean deleteFile(FileInfoParam fileInfo) throws Exception {
		boolean deleteFlag = false;
		String filePath = FileInfoUtil.formatAbsolutePath(fileInfo.getFilePath(), rootPath);		//获得文件目录绝对路径
		String fileName = getFileWholeName(fileInfo, filePath);
		if(StrUtil.isNotEmpty(filePath)){				//参数校验
			if(StrUtil.isNotEmpty(fileName)){
				File file = new File(fileName);
				if(file.exists()&&file.isFile()){
					deleteFlag = deleteFile(file);
				}
			}else{
				File file = new File(filePath);
				if(file.exists()&&file.isDirectory()){
					deleteFlag = deleteFile(file);
				}
			}
		}
		return deleteFlag;
	}

	//获取文件完整名称
	private String getFileWholeName(FileInfoParam fileInfo, String filePath) {
		String fileName = null;
		if(fileInfo.getFileName()!=null&&fileInfo.getFileSuffix()!=null){		//传入了文件名，只删除相应文件，否则删除相应目录下的文件
			fileName = FileInfoUtil.formatPathString(fileInfo.getFileName(),fileInfo.getFileSuffix(),".");				//获得文件绝对路径
			fileName = FileInfoUtil.formatPathString(filePath, fileName, File.separator);
		}
		return fileName;
	}

	/**
	 * 删除指定文件
	 */
	private boolean deleteFile(File file) {
		boolean deleteFlag=false;
		/**删除数据库中对应的数据*/
		FileInfo fi = FileInfoUtil.getFileInfo(file);
		fileInfoMapper.deleteFileInfo(fi);
		deleteFlag = file.delete();
		return deleteFlag;
	}
	
}
