package com.files.job;

import com.files.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *  定时任务
 * @author fhx
 * @date 2018年12月17日
 */
@Slf4j
@Service
public class ClearTmpFileJob {

	@Resource
	private FileService fileService;

	/**
	 * 定时任务
	 */
//	@Scheduled(cron = "0 0/1 * * * ?")
	@Scheduled(cron="0 0 03 * * ?")
	public void clearTimeoutFile() {
		log.info("start the clear timeout file job");
		try {
			fileService.removeTmpFile();
		} catch (Exception e) {
			log.warn("failed to loop timeout file queue", e);
		}
		log.info("finished the clear timeout file job");
	}
}
