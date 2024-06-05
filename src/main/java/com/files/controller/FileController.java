package com.files.controller;

import com.alibaba.fastjson.JSON;
import com.files.client.FileInfo;
import com.files.client.Result;
import com.files.client.exception.BusinessException;
import com.files.service.FileService;
import com.files.client.FileClientUtil;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 文件上传下载
 * @author fhx
 * @date 2018年12月17日
 */
@Slf4j
@Controller
public class FileController {
	@Resource
	private FileService fileService;
	@Value("${server.port}")
	private String ipport;
	private static String separator = File.separator;
	@Value("${file.dir}")
	private String dir;
	
	/**
	 * 图片上传入口
	 * @param request
	 * @param response
	 * @param group 系统分配的文件组别
	 * @return http://10.8.30.29:9999/file/product/download/6faf3cb9-5e72-434d-b8e7-b704eca81af7.png/6AAC901C-04C5-40fd-B02E-7FF7067FB604.png
	 */
	@RequestMapping(value = "/file/{group}/upload", method = { RequestMethod.POST })
	@ResponseBody
	public Result<String> uploadTempFile(HttpServletRequest request, HttpServletResponse response, @PathVariable String group) throws BusinessException {
		Result<String> result = Result.create(true);
		Map<String, String> fileInfo = fileService.uploadFile(request, response, group);
		result.setData(FileClientUtil.constructFilePath(group, fileInfo.get("fileName"), fileInfo.get("orgFileName")));
		log.info("文件上传结果：{}", JSON.toJSONString(result));
		return result;
	}



	/**
	 * 图片查看下载
	 * @param request
	 * @param response
	 * @param fileName 文件名
	 * @param group 文件组别
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/file/{group}/download/{fileName}", method = { RequestMethod.GET })
	public void downloadFile(HttpServletRequest request, HttpServletResponse response, @PathVariable String fileName, @PathVariable String group) throws BusinessException {
		// 通过注解获取fileName时无法获取后缀
		String servletPath = request.getServletPath();
		FileInfo fileInfo = FileClientUtil.parseFilePath(servletPath);
		downloadFileInternal(response, group, fileInfo.getFileName(), null);
	}

	/**
	 * 图片查看下载
	 * @param request
	 * @param response
	 * @param fileName 文件名
	 * @param group 文件所在组别
	 * @param refName 原上传文件名
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/file/{group}/download/{fileName}/{refName}", method = { RequestMethod.GET })
	public void downloadFileWithRefName(HttpServletRequest request, HttpServletResponse response, @PathVariable String fileName, @PathVariable String group, @PathVariable String refName)
			throws BusinessException {
		// 通过注解获取refName时无法获取后缀
		String servletPath = request.getServletPath();
		FileInfo fileInfo = FileClientUtil.parseFilePath(servletPath);
		downloadFileInternal(response, group, fileName, fileInfo.getOrgFileName());
	}

	
	private void downloadFileInternal(HttpServletResponse response, String group, String fileName, String refName) throws BusinessException {
		response.setCharacterEncoding("utf-8");
		String dFileName = (StringUtils.isEmpty(refName) ? fileName : refName);
		try {
			response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(dFileName, "UTF-8"));
		} catch (Exception e) {
			response.setHeader("Content-Disposition", "attachment;fileName=file." + FilenameUtils.getExtension(dFileName));
		}

		try {
			File file = fileService.getFile(fileName, group);
			if (file.exists()) {
				InputStream inputStream = new FileInputStream(file);
				OutputStream os = response.getOutputStream();
				byte[] b = new byte[1024];
				int length;
				while ((length = inputStream.read(b)) > 0) {
					os.write(b, 0, length);
				}
				inputStream.close();
			} else {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			}
		} catch (Exception e) {
			log.error("got exception when download  tmp file:" + fileName, e);
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
	}

	@RequestMapping(value = "/file/data/test1", method = { RequestMethod.GET })
	@ResponseBody
	public String test1(HttpServletRequest request, HttpServletResponse response)
			throws BusinessException, IOException {
		String file = "C:/data/tmpFiles/test.json";
		return FileUtils.readFileToString(new File(file), Charsets.UTF_8);
	}
}
