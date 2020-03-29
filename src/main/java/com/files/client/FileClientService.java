package com.files.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * 接口调用
 * @author fhx
 * @date 2018年12月13日
 */
@FeignClient(value = "files", name = "files")
public interface FileClientService {
	
	@RequestMapping(value = "/file/delete", method = RequestMethod.POST)
	public Result<?> deleteFile(@RequestBody FileInfo fileInfo);

	@RequestMapping(value = "/file/switchgroup", method = RequestMethod.POST)
	public Result<?> switchFileGroup(@RequestBody FileInfo tmpFileInfo,
			@RequestParam(value = "targetGroup") String targetGroup);

	@RequestMapping(value = "/file/size", method = RequestMethod.POST)
	public Result<Long> getFileSize(@RequestBody FileInfo fileInfo);
}
