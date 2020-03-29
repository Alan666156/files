package com.files.controller;

import com.files.client.FileInfo;
import com.files.client.Result;
import com.files.enums.ResponseEnum;
import com.files.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

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
	@Autowired
	private Redisson redisson;

	/**
	 * 文件删除
	 * @param fileInfo
	 * @return
	 */
	@RequestMapping(value = "/file/delete", method = RequestMethod.POST)
	public Result<?> deleteFile(@RequestBody FileInfo fileInfo) {
		Result<Long> result = Result.create(true);
		RLock lock = redisson.getLock("seckill:" + fileInfo.getFileName());
		try {
			// 并发请求
			if (lock.tryLock(2, 10, TimeUnit.SECONDS)) {
				tmpFileService.deleteFile(fileInfo);
			}
		} catch (InterruptedException e) {
			log.error("获取分布式锁异常", e);
			result.setCode("999999");
			result.setMessages("文件删除异常！");
		}  catch (Exception e) {
			log.error("failed to delete file:{}", fileInfo, e);
			result.setCode(ResponseEnum.SYS_FAILD.getCode());
			result.setMessages("文件删除异常！");
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
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
		Result<?> result = Result.create(true);

		RLock lock = redisson.getLock("seckill:" + tmpFileInfo.getFileName());
		try {
			// 并发请求
			if (lock.tryLock(2, 10, TimeUnit.SECONDS)) {
				tmpFileService.switchFileGroup(tmpFileInfo, targetGroup);
			}
		} catch (InterruptedException e) {
			log.error("获取分布式锁异常", e);
			result.setCode("999999");
			result.setMessages("文件删除异常！");
		}  catch (Exception e) {
			log.error("failed to switch file:{} to group:{}", tmpFileInfo, targetGroup, e);
			result.setCode(ResponseEnum.SYS_FAILD.getCode());
			result.setMessages("文件移动异常！");
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
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
