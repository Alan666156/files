package com.files.service;

import com.files.client.FileInfo;
import com.files.client.exception.BusinessException;
import com.files.client.exception.SystemException;
import com.files.domain.FileConfig;
import com.files.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 临时文件处理
 * @author fhx
 * @date 2018年12月13日
 */
@Slf4j
@Service
@Transactional
public class FileService extends AbstractFileService {
	public static final String DEFAULT_GROUP = "tmp";

	private File getGroupDir(String group, FileConfig config) {
		return new File(DEFAULT_GROUP.equalsIgnoreCase(group) ? fileTmpDir : fileDir, config.getPath());
	}

	/**
	 * 将文件保存到指定目录，并返回新的文件名
	 *
	 * @param request
	 * @param response
	 * @param group
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, String> uploadFile(HttpServletRequest request, HttpServletResponse response, String group)
			throws BusinessException {
		Map<String, String> map = new HashMap<>();
		try {
			if (request instanceof MultipartHttpServletRequest) {
				MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
				Map<String, MultipartFile> fileMap = mRequest.getFileMap();
				if (fileMap != null && !fileMap.isEmpty()) {
					for (String orgFileName : fileMap.keySet()) {
						MultipartFile file = fileMap.get(orgFileName);
						if (file != null && !file.isEmpty()) {
							map.put("orgFileName", FilenameUtils.getName(file.getOriginalFilename()));
							//上传临时目录
							String fileName = this.storeTempFile(file, group);
							map.put("fileName", fileName);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("failed upload file", e);
			throw new BusinessException("failed upload file");
		}

		return map;
	}
	/**
	 * 将文件保存到临时文件夹的目录中，返回新的文件名
	 * 
	 * @param file 要保存的文件
	 * @return 当前文件保存在硬盘中的文件名
	 * @throws SystemException
	 */
	public String storeTempFile(MultipartFile file) throws SystemException {
		return storeTempFile(file, DEFAULT_GROUP);
	}

	/**
	 * 将文件保存到临时文件夹的分组（group）中，返回新的文件名
	 * 
	 * @param file  要保存的文件
	 * @param group  文件分组
	 * @return 当前文件保存在硬盘中的文件名
	 * @throws SystemException
	 */
	public String storeTempFile(MultipartFile file, String group) throws SystemException {
		try {
			// 获取分组的配置信息
			FileConfig config = getFileConfig(group);
			if (config == null) {
				throw new SystemException("invalid group:" + group);
			}
			// 获取临时文件夹中该分组的文件夹
			File dir = getGroupDir(group, config);
			if (dir.exists() == false) {
				if (dir.mkdirs() == false) {
					throw new SystemException("failed to create folder:" + dir.getAbsolutePath());
				}
			}
			// 将文件写入本地文件中
			File rFile = null;
			do {
				rFile = new File(dir, DateUtils.dateToString(new Date() ,DateUtils.FORMAT_DATE_YYYYMMDD) + "-" + UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(file.getOriginalFilename()));
			} while (rFile.exists());
			FileUtils.copyInputStreamToFile(file.getInputStream(), rFile);
			return rFile.getName();
		} catch (IOException e) {
			log.error("failed to upload file", e);
			throw new SystemException("failed to upload file", e);
		}
	}

	public File getFile(String fileName, String group) throws Exception {
		FileConfig config = getFileConfig(group);
		if (config == null) {
			throw new SystemException("invalid group:" + group);
		}
		File dir = getGroupDir(group, config);
		if (dir.exists()) {
			File rFile = new File(dir, fileName);
			if (rFile.exists()) {
				return rFile;
			}
		}
		return null;
	}
	
	/**
	 * 删除临时文件
	 *
	 */
	public void removeTmpFile() {
		try {
			FileConfig config = getFileConfig(DEFAULT_GROUP);
			//获取指定目录
			File dir = new File(fileTmpDir, config.getPath());
			if (dir.exists()) {
				File[] files = dir.listFiles();
				if(files.length > 0){
					log.info("wait clear file total : {}", files.length);
					for(File file : files){
						if(file.exists()){
							file.delete();
						}
					}
				}else{
					log.info("tmp dir not found file");
				}

			} else {
				log.info("tmp dir not exist");
			}
		} catch (Exception e) {
			log.error("failed to remove tmp file", e);
		}
	}

	/**
	 * 文件删除
	 * @param fileInfo
	 * @throws SystemException
	 */
	public void deleteFile(FileInfo fileInfo) throws SystemException {
		if (fileInfo == null || StringUtils.isEmpty(fileInfo.getFileName())) {
			log.warn("empty fileInfo:{},return", fileInfo);
			return;
		}
		try {
			// 获取分组文件夹
			String group = (StringUtils.isEmpty(fileInfo.getGroup()) ? DEFAULT_GROUP : fileInfo.getGroup());
			FileConfig config = getFileConfig(group);
			if (config == null) {
				throw new SystemException("invalid group:" + group);
			}
			//获取指定目录
			File dir = getGroupDir(group, config);
			if (dir.exists()) {
				// 当分组文件夹存在时，获取目标文件对象
				File rFile = new File(dir, fileInfo.getFileName());
				if (rFile.exists()) {
					// 当目标文件存在时，删除文件
					rFile.delete();
				}
			}
		} catch (Exception e) {
			log.warn("failed to delete file", e);
			throw new SystemException("file delete failed", e);
		}
	}

	/**
	 * 文件移动
	 * @param tmpFileInfo 临时文件组
	 * @param targetGroup 目标文件组
	 * @throws SystemException
	 */
	public void switchFileGroup(FileInfo tmpFileInfo, String targetGroup) throws SystemException {
		if (tmpFileInfo == null || StringUtils.isEmpty(tmpFileInfo.getFileName()) || StringUtils.isEmpty(targetGroup)) {
			log.warn("empty fileInfo:{} or targetGroup:{},return", tmpFileInfo, targetGroup);
			return;
		}
		try {
			if (!targetGroup.equalsIgnoreCase(tmpFileInfo.getGroup())) {
				// 获取待移动文件组
				String group = (StringUtils.isEmpty(tmpFileInfo.getGroup()) ? DEFAULT_GROUP : tmpFileInfo.getGroup());
				FileConfig config = getFileConfig(group);
				if (config == null) {
					throw new SystemException("invalid group:" + group);
				}
				//获取目标文件组
				FileConfig targetConfig = getFileConfig(targetGroup);
				if (targetConfig == null) {
					throw new SystemException("invalid group:" + targetGroup);
				}
				//获取原文件目录
				File srcDir = getGroupDir(group, config);
				if (srcDir.exists()) {
					//获取原文件
					File srcFile = new File(srcDir, tmpFileInfo.getFileName());
					if (srcFile.exists()) {
						File targetDir = getGroupDir(targetGroup, targetConfig);
						FileUtils.moveFileToDirectory(srcFile, targetDir, true);
					}
				}
			}
		} catch (Exception e) {
			log.warn("failed to move file", e);
			throw new SystemException("file move failed", e);
		}
	}

}
