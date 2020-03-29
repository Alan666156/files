package com.files.client;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件对象
 */
@Data
public class FileInfo implements Serializable{
	private static final long serialVersionUID = 244908327847257915L;
	/**
	 * 文件所在的分组
	 */
	private String group;
	/**
	 * 保存在文件服务器的文件名
	 */
	private String fileName;
	/**
	 *  文件上传时的原名
	 */
	private String orgFileName;
	private Long updateTime;

	public FileInfo() {
	}

	public FileInfo(String url) {
		FileInfo info = FileClientUtil.parseFilePath(url);
		if (info != null) {
			this.group = info.group;
			this.fileName = info.fileName;
			this.orgFileName = info.orgFileName;
		}
	}

	public FileInfo(String group, String fileName, String orgName) {
		this.group = group;
		this.fileName = fileName;
		this.orgFileName = orgName;
	}

	public FileInfo(String group, String fileName) {
		this.group = group;
		this.fileName = fileName;
	}
}
