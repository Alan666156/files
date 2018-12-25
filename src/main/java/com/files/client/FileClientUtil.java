package com.files.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;

@Slf4j
public class FileClientUtil {

	public static String constructFilePath(String group, String fileName) {
		return constructFilePath(group, fileName, fileName);
	}
	
	/**
	 * 构建文件访问url路径
	 * @param group 文件组别
	 * @param fileName 文件名
	 * @param refName 图片名称
	 * @return
	 */
	public static String constructFilePath(String group, String fileName, String refName) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.hasText(fileName) && StringUtils.hasText(group)) {
			sb.append("/file/").append(group).append("/download/");
			try {
				//转码拼接文件名
				sb.append(URLEncoder.encode(fileName, "UTF-8"));
			} catch (Exception e) {
				sb.append(fileName);
				log.warn("failed encode fileName:{}", fileName, e);
			}
			//转码拼接原上传时的文件名
			if (StringUtils.hasText(refName)) {
				sb.append("/");
				try {
					sb.append(URLEncoder.encode(refName, "UTF-8"));
				} catch (Exception e) {
					sb.append(refName);
					log.warn("failed encode refName:{}", refName, e);
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * 通过文件url，封装文件对象
	 * @param url
	 * @return
	 */
	public static FileInfo parseFilePath(String url) {
		if (StringUtils.isEmpty(url)) {
			return null;
		}
		String group = null;
		int idx = url.indexOf("?=");
		if (idx > -1) {
			url = url.substring(0, idx);
		}
		idx = url.indexOf("/file/");
		if (idx > -1) {
			//获取文件组后的url
			url = url.substring(idx + 6);
			idx = url.indexOf("/");
			if (idx > -1) {
				group = url.substring(0, idx);
			}

			String fileName = null;
			String refName = null;
			idx = url.indexOf("/download/");
			if (idx > -1) {
				int refIdx = url.lastIndexOf("/");
				if (refIdx > idx + 10) {
					fileName = url.substring(idx + 10, refIdx);
					try {
						refName = URLDecoder.decode(url.substring(refIdx + 1), "UTF-8");
					} catch (Exception e) {
						refName = url.substring(refIdx + 1);
						log.error("failed encode refName:{}", refName, e);
					}
				} else {
					fileName = url.substring(idx + 10);
				}
			}
			return new FileInfo(group, fileName, refName);
		} else {
			return null;
		}
	}

}
