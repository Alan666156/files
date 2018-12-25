package com.files.dao;

import com.files.domain.FileConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface FileConfigDao extends JpaRepository<FileConfig, Long> {
	
	public List<FileConfig> findByAppId(String appid);
}
