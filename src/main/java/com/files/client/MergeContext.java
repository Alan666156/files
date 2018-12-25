package com.files.client;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Set;

@Data
public class MergeContext {
	private Object srcObj;
	private Object targetObj;
	private Class<?> targetClass;
	private Set<String> excludePath;
	private String targetGroup;

	// 临时属性
	private String curPath;
	private Object curSrcObj;
	private Object curTargetObj;

	private MergeContext() {
	}

	public static MergeContext createContext(Object srcObj, Object targetObj) {
		MergeContext ctx = new MergeContext();
		ctx.srcObj = srcObj;
		ctx.targetObj = targetObj;
		ctx.curSrcObj = srcObj;
		ctx.curTargetObj = targetObj;
		return ctx;
	}

	public static MergeContext createContext(Object srcObj, Class<?> targetClass)
			throws InstantiationException, IllegalAccessException {
		MergeContext ctx = new MergeContext();
		ctx.srcObj = srcObj;
		ctx.targetClass = targetClass;
		ctx.targetObj = targetClass.newInstance();
		ctx.curSrcObj = srcObj;
		ctx.curTargetObj = ctx.targetObj;
		return ctx;
	}

	public void updateCurPath(String name) {
		curPath = generateCurPath(curPath, name);
	}

	public String generateCurPath(String curPath, String name) {
		if (StringUtils.isEmpty(curPath)) {
			return name;
		} else {
			return new StringBuilder(curPath).append(".").append(name).toString();
		}
	}
}
