package com.files.service;

import com.files.domain.FileConfig;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;

/**
 * 文件上传处理
 * @author fhx
 * @date 2018年12月17日
 */
@Slf4j
@Component
public class MyMultipartResolver extends StandardServletMultipartResolver {
	@Resource
	private FileService fileSrv;
	
	public MyMultipartResolver() {
		setResolveLazily(true);
	}

	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		StandardMultipartHttpServletRequest req = new StandardMultipartHttpServletRequest(request, true) {
			@Override
			protected void initializeMultipart() {
				checkSize(getRequest());
				super.initializeMultipart();
			}
		};
		return req;
	}

	/**
	 * 文件大小检查
	 * @param request
	 * @throws MultipartException
	 */
	private void checkSize(HttpServletRequest request) throws MultipartException {
		log.debug("request method:{} and url:{}", request.getMethod(), request.getRequestURL());
		String appId = "";
		if (request.getRequestURI().startsWith("/file/")) {
			appId = parseGroupFromUrl(request.getRequestURI());
			if (request.getRequestURI().contains("/tmp/")) {
				appId = FileService.DEFAULT_GROUP;
			}
			if(request.getRequestURI().contains("/uploadFiles/")){
				appId = "uploadFiles";
			}
			String func = request.getParameter("func");
			//获取配置信息
			FileConfig config = fileSrv.getFileConfig(appId, func);
			if (config == null) {
				throw new MultipartException("invalid request");
			}
			//验证上传文件大小是否超过限制
			if (request.getContentLengthLong() > config.getMaxSize()) {
				log.warn("upload exceed max size:{} for appId:{} and function:{}", config.getMaxSize(), appId, func);
				throw new MaxUploadSizeExceededException(config.getMaxSize());
			}
		}
	}
	
	/**
	 * 截取拆分
	 * @param uri
	 * @return appid
	 */
	private String parseGroupFromUrl(String uri) {
		Iterator<String> items = Splitter.on('/') .split(uri).iterator();
		List<String> list = Lists.newArrayList(items);
		return list.get(2);
	}
}
