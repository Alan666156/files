package com.files.domain;

import lombok.Data;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

/**
 * 文件配置对象
 */
@Data
@Entity
@Table(name = "file_config")
@EntityListeners(AuditingEntityListener.class)
public class FileConfig extends AbstractPersistable<Long> implements Cloneable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 分配系统
	 */
	private String appId;
	/**
	 * 状态
	 */
	private String status;
	/**
	 * 文件存放目录
	 */
	private String path;
	/**
	 * 文件大小
	 */
	private Long maxSize;

	/**
	 * 超时时间
	 */
	private Integer timeout;
}
