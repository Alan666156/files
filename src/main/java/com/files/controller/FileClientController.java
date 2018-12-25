package com.files.controller;

import com.files.client.FileInfo;
import com.files.client.Result;
import com.files.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
/**
 * 微服务对应接口
 * @author fhx
 * @date 2018年12月17日
 */
@Slf4j
@RestController
public class FileClientController {
	@Autowired
	private FileService tmpFileService;
	
	/**
	 * 文件删除
	 * @param fileInfo
	 * @return
	 */
	@RequestMapping(value = "/file/delete", method = RequestMethod.POST)
	public Result<?> deleteFile(@RequestBody FileInfo fileInfo) {
		Result<Long> result = Result.create(true);
		try {
			tmpFileService.deleteFile(fileInfo);
		} catch (Exception e) {
			log.error("failed to delete file:{}", fileInfo, e);
			result.setCode("999999");
			result.setMessages("文件删除异常！");
		}
		return result;
	}
	
	/**
	 * 文件移动
	 * @param tmpFileInfo 当前临时文件
	 * @param targetGroup 目标文件组
	 * @return
	 */
	@RequestMapping(value = "/file/switchgroup", method = RequestMethod.POST)
	public Result<?> switchFileGroup(@RequestBody FileInfo tmpFileInfo, @RequestParam String targetGroup) {
		log.info("======文件组移动：{}--->{}=========",tmpFileInfo.getGroup(), targetGroup);
		Result<?> result = Result.create(true);;
		try {
			tmpFileService.switchFileGroup(tmpFileInfo, targetGroup);
		} catch (Exception e) {
			log.error("failed to switch file:{} to group:{}", tmpFileInfo, targetGroup, e);
			result.setCode("999999");
			result.setMessages("文件移动异常！");
		}
		return result;
	}

	@RequestMapping(value = "/file/size", method = RequestMethod.POST)
	public Result<Long> getFileSize(@RequestBody FileInfo fileInfo) {
		Result<Long> result = Result.create(true);
		try {
			File file = tmpFileService.getFile(fileInfo.getFileName(), fileInfo.getGroup());
			if (file != null && file.exists()) {
				result.setData((file.length()));
			}
		} catch (Exception e) {
			result.setCode("999999");
			log.error("failed to get file:{}", fileInfo);
		}
		return result;
	}
}
