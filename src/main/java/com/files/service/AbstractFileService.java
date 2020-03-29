package com.files.service;

import com.files.dao.FileConfigDao;
import com.files.domain.FileConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public abstract class AbstractFileService {

	private static final String DEFAULT_FUNC = "0";
	@Autowired
	private FileConfigDao fileConfigDao;
	@Autowired
	private RedisService redisService;

	@Value("${file.tmpDir}")
	protected String fileTmpDir;
	@Value("${file.dir}")
	protected String fileDir;

	/**
	 * 获取目标应用的所有配置信息集合，key为配置种类，value为配置信息类FileConfig
	 * 
	 * @param appId
	 *            应用标识
	 * @return 目标应用的所有配置信息集合
	 */
//	@Cacheable(value = "fileConfigCache", key = "'fileconfig_'+#appId")
	public Map<String, FileConfig> getFileConfigs(String appId) {
		List<FileConfig> configs = fileConfigDao.findByAppId(appId);
		if (configs != null) {
			return configs.stream().collect(Collectors.toMap(FileConfig::getStatus, (v) -> v));
		} else {
			return null;
		}
	}

	/**
	 * 获取目标应用的默认配置
	 * 
	 * @param appId
	 *            应用标识
	 * @return 默认配置信息
	 */
	public FileConfig getFileConfig(String appId) {
		return getFileConfig(appId, null);
	}

	/**
	 * 获取目标应用的目标配置，当配置名(func)为空时，查询默认配置
	 * 
	 * @param appId
	 *            应用标识
	 * @param func
	 *            配置名
	 * @return 目标配置信息
	 */
	public FileConfig getFileConfig(String appId, String func) {
		Map<String, FileConfig> configs = getFileConfigs(appId);
		if (StringUtils.isEmpty(func)) {
			func = DEFAULT_FUNC;
		}
		if (configs == null) {
			log.warn("no file configs found, return");
			return null;
		}
		FileConfig config = configs.get(func);
		if (config == null) {
			log.warn("invalid func:{} use default", func);
			config = configs.get(DEFAULT_FUNC);
		}
		return config;
	}
}
