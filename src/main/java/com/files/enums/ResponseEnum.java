package com.files.enums;

/**
 * 返回响应状态枚举
 *
 */
public enum ResponseEnum {
	
	/** 格式校验枚举 **/
	VALIDATE_ISNULL("100001","{0} 数据项为空!"),
	VALIDATE_FORMAT("100002","{0} 数据项格式有误!"),

	/** 系统枚举 **/
	SYS_SUCCESS("000000","成功"),
	SYS_UNAUTHORIZED("400001","用户未认证"),
	SYS_EXIST("400002","用户已存在"),
	SYS_NOT_FOUND("400003","未找到'%s'相关信息"),
	SYS_NOT_FOUND_USER("400004","未找到客户相关信息"),
	SYS_AUTHORIZED_FAIL("400005","用户认证失败"),
	SYS_SAVE_FAIL("400006","保存失败"),

	SYS_FAILD("999999","系统忙"),
	SYS_TIMEOUT("900002","请求超时"),

	SYS_WX_PAY_FAILD("500001","支付失败，请稍后重试！"),
	/** 第三方HTTP接口 **/
	HTTP_RESPONSE_ERROR("H000001","第三方接口'%s'异常：{1}"),
	
	FILE_REPORT_SUCCESS("000000","操作成功！"),
	FILE_REPORT_UPLOAD_ERROR("000001","上传文件失败！"),
	FILE_REPORT_SAVE_ERROR("000002","记录保存失败！"),
	FILE_REPORT_PARAM_ERROR("000003","参数错误！");

	private final String code;
	private final String desc;

	private ResponseEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
	
	
	
}
