package com.dante.biz.ueditor.service;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dante.common.util.PathFormat;
import com.dante.common.util.StorageManager;
import com.dante.model.ueditor.AppInfo;
import com.dante.model.ueditor.BaseState;
import com.dante.model.ueditor.FileType;
import com.dante.model.ueditor.MIMEType;
import com.dante.model.ueditor.MultiState;
import com.dante.model.ueditor.State;

@Service("ueditorFileService")
public class UeditorFileService implements IUeditorFileService {

	@Value("dsm_biz.rootPath")
	private String rootPath;


	@Override
	public State listFile(int index, Map<String, Object> conf) {
		// 取出参数
		conf.put("rootPath",rootPath);
		String directory = rootPath + (String) conf.get("dir");
		String[] allowFiles = getAllowFiles(conf.get("allowFiles"));
		int count = (Integer) conf.get("count");

		File dir = new File(directory);
		State state = null;

		if (!dir.exists()) {
			return new BaseState(false, AppInfo.NOT_EXIST);
		}

		if (!dir.isDirectory()) {
			return new BaseState(false, AppInfo.NOT_DIRECTORY);
		}

		Collection<File> list = FileUtils.listFiles(dir, allowFiles, true);

		if (index < 0 || index > list.size()) {
			state = new MultiState(true);
		} else {
			Object[] fileList = Arrays.copyOfRange(list.toArray(), index, index + count);
			state = getState(fileList);
		}

		state.putInfo("start", index);
		state.putInfo("total", list.size());

		return state;

	}

	private State getState(Object[] files) {

		MultiState state = new MultiState(true);
		BaseState fileState = null;

		File file = null;

		for (Object obj : files) {
			if (obj == null) {
				break;
			}
			file = (File) obj;
			fileState = new BaseState(true);
			fileState.putInfo("url", PathFormat.format(getPath(file)));
			state.addState(fileState);
		}

		return state;

	}

	private String getPath(File file) {

		String path = file.getAbsolutePath();

		return path.replace(rootPath, "/");

	}

	private String[] getAllowFiles(Object fileExt) {

		String[] exts = null;
		String ext = null;

		if (fileExt == null) {
			return new String[0];
		}

		exts = (String[]) fileExt;

		for (int i = 0, len = exts.length; i < len; i++) {

			ext = exts[i];
			exts[i] = ext.replace(".", "");

		}

		return exts;

	}

	@Override
	public State saveBase64(String content, Map<String, Object> conf) {

		byte[] data = decode(content);

		long maxSize = ((Long) conf.get("maxSize")).longValue();

		if (!validSize(data, maxSize)) {
			return new BaseState(false, AppInfo.MAX_SIZE);
		}

		String suffix = FileType.getSuffix("JPG");

		String savePath = PathFormat.parse((String) conf.get("savePath"), (String) conf.get("filename"));

		savePath = savePath + suffix;
		String physicalPath = (String) conf.get("rootPath") + savePath;

		State storageState = StorageManager.saveBinaryFile(data, physicalPath);

		if (storageState.isSuccess()) {
			storageState.putInfo("url", PathFormat.format(savePath));
			storageState.putInfo("type", suffix);
			storageState.putInfo("original", "");
		}

		return storageState;
	}

	private byte[] decode(String content) {
		return Base64.decodeBase64(content);
	}

	private boolean validSize(byte[] data, long length) {
		return data.length <= length;
	}

	@Override
	public State capture(List<String> list, Map<String, Object> conf) {

		MultiState state = new MultiState(true);

		for (String source : list) {
			state.addState(captureRemoteData(source, conf));
		}

		return state;

	}

	public State captureRemoteData(String urlStr, Map<String, Object> conf) {

		HttpURLConnection connection = null;
		URL url = null;
		String suffix = null;

		List<String> filters = Arrays.asList((String[]) conf.get("filter"));
		List<String> allowTypes = Arrays.asList((String[]) conf.get("allowFiles"));
		long maxSize = (Long) conf.get("maxSize");
		String filename = (String) conf.get("filename");
		String savePath = (String) conf.get("savePath");

		try {
			url = new URL(urlStr);

			if (!validHost(filters, url.getHost())) {
				return new BaseState(false, AppInfo.PREVENT_HOST);
			}

			connection = (HttpURLConnection) url.openConnection();

			connection.setInstanceFollowRedirects(true);
			connection.setUseCaches(true);

			if (!validContentState(connection.getResponseCode())) {
				return new BaseState(false, AppInfo.CONNECTION_ERROR);
			}

			suffix = MIMEType.getSuffix(connection.getContentType());

			if (!validFileType(allowTypes, suffix)) {
				return new BaseState(false, AppInfo.NOT_ALLOW_FILE_TYPE);
			}

			if (!validFileSize(connection.getContentLength(), maxSize)) {
				return new BaseState(false, AppInfo.MAX_SIZE);
			}

			savePath = getPath(savePath, filename, suffix);
			String physicalPath = this.rootPath + savePath;

			State state = StorageManager.saveFileByInputStream(connection.getInputStream(), physicalPath);

			if (state.isSuccess()) {
				state.putInfo("url", PathFormat.format(savePath));
				state.putInfo("source", urlStr);
			}

			return state;

		} catch (Exception e) {
			return new BaseState(false, AppInfo.REMOTE_FAIL);
		}

	}

	private String getPath(String savePath, String filename, String suffix) {

		return PathFormat.parse(savePath + suffix, filename);

	}

	private boolean validHost(List<String> filters, String hostname) {
		try {
			InetAddress ip = InetAddress.getByName(hostname);

			if (ip.isSiteLocalAddress()) {
				return false;
			}
		} catch (UnknownHostException e) {
			return false;
		}

		return !filters.contains(hostname);

	}

	private boolean validContentState(int code) {

		return HttpURLConnection.HTTP_OK == code;

	}

	private boolean validFileType(List<String> allowTypes, String type) {

		return allowTypes.contains(type);

	}

	private boolean validFileSize(int size, long maxSize) {
		return size < maxSize;
	}

}
