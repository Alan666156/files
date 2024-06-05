package com.files.dao;

import com.files.domain.FileConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * file config数据库查询
 * @author fhx
 * @date 2018年12月17日
 */
public interface FileConfigDao extends JpaRepository<FileConfig, Long> {

	/**
	 * 根据应用id查询
	 * @param appid
	 * @return
	 */
	List<FileConfig> findByAppId(String appid);
}
