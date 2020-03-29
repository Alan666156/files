package com.files.util;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 接口返回定义
 * Created by Alan Fu on 2018/1/6.
 */
@Data
@ToString
public class Result<T> implements Serializable {
	private static final long serialVersionUID = -7917470817867029039L;

	/** 查询是否成功 */
	private Boolean success;
	/** 状态码 */
	private String code;
	/** 消息 */
	private String messages;
	/** 数据 */
	private T data;

	public Result(){}

	public Result(Boolean success){this.success = success;}

	public Result(Boolean success, String code){this.success = success; this.code = code;}

	public Result(Boolean success, String code, String messages) {
		this.success = success;
		this.code = code;
		this.messages = messages;
	}

	public Result(Boolean success, String code, String messages, T data) {
		this.success = success;
		this.code = code;
		this.messages = messages;
		this.data = data;
	}
}
