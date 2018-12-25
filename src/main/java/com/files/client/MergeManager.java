package com.files.client;

import com.files.client.annotation.CustomProperty;
import com.files.client.annotation.FileField;
import com.files.client.annotation.PropertyIn;
import com.files.client.exception.BusinessException;
import com.files.client.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件处理合并对象
 */
@Slf4j
@Component
public class MergeManager {

	public MergeResult merge(Object srcObj, Object targetObj) throws BusinessException {
		MergeContext ctx = MergeContext.createContext(srcObj, targetObj);
		return merge(ctx);
	}

	public MergeResult merge(Object srcObj, Class<?> targetClass) throws BusinessException {
		MergeContext ctx;
		try {
			ctx = MergeContext.createContext(srcObj, targetClass);
			return merge(ctx);
		} catch (Exception e) {
			log.error("failed to create merge context with target class:{}", targetClass, e);
			throw new BusinessException("invalid target class:" + targetClass);
		}
	}

	@SuppressWarnings("unchecked")
	public MergeResult merge(MergeContext ctx) throws BusinessException {
		MergeResult result = new MergeResult();
		Object srcObj = ctx.getCurSrcObj();
		Object targetObj = ctx.getCurTargetObj();

		if (srcObj != null && targetObj != null) {
			Field[] fields = srcObj.getClass().getDeclaredFields();
			if (fields != null) {
				for (Field field : fields) {
					// 有@CustomProperty注解则忽略当前字段
					CustomProperty custPropAnno = field.getAnnotation(CustomProperty.class);
					if (custPropAnno != null || "serialVersionUID".equalsIgnoreCase(field.getName())) {
						continue;
					}
					if (ctx != null && ctx.getExcludePath() != null && ctx.getExcludePath().contains(ctx.generateCurPath(ctx.getCurPath(), field.getName()))) {
						continue;
					}
					field.setAccessible(true);
					String targetPropName = field.getName();
					try {
						PropertyIn propAnno = field.getAnnotation(PropertyIn.class);
						if (propAnno != null && !StringUtils.isEmpty(propAnno.refProp())) {
							targetPropName = propAnno.refProp();
						}

						Field targetField = null;
						try {
							targetField = RequestUtil.getTargetField(targetObj, targetPropName);
						} catch (Exception e) {
							log.info("failed to get property:{} in class:{}", targetPropName, targetObj.getClass(),
									e);
						}
						if (targetField == null) {
							continue;
						}

						Object valueObj = field.get(srcObj);
						// TODO
						boolean handleFile = false;
						if (Collection.class.isAssignableFrom(field.getType())) {
							if (valueObj == null) {
								// 此处解决hibernate集合关联问题
								Object targetFieldObj = RequestUtil.getPropertyValue(targetObj, targetPropName);
								if (targetFieldObj != null && targetFieldObj instanceof Collection) {
									((Collection<?>) targetFieldObj).clear();
									valueObj = targetFieldObj;
								}
							} else {
								Type targetGenericType = RequestUtil.getFieldGenericType(targetField);
								Collection<Object> tmpTargetColl = null;
								Object targetFieldObj = RequestUtil.getPropertyValue(targetObj, targetPropName);
								// 此处解决hibernate集合关联问题
								if (targetFieldObj != null) {
									tmpTargetColl = (Collection<Object>) targetFieldObj;
									tmpTargetColl.clear();
								}
								boolean needIdx = false;
								if (Set.class.isAssignableFrom(targetField.getType())) {
									if (tmpTargetColl == null) {
										tmpTargetColl = new HashSet<>();
									}
									if (propAnno != null && !StringUtils.isEmpty(propAnno.sortBy())) {
										needIdx = true;
									}
								} else {
									if (tmpTargetColl == null) {
										tmpTargetColl = new ArrayList<>();
									}
								}

								if (RequestUtil.getFieldGenericType(field).equals(FileInfo.class)) {
									Class<?> targetClass = Class.forName(targetGenericType.getTypeName());
									Collection<Object> targetCollection = (Collection<Object>) RequestUtil
											.getPropertyValue(targetObj, targetPropName);
									handleFileCollection(result, targetClass, targetCollection, tmpTargetColl, valueObj,
											field, needIdx, ctx);
									handleFile = true;
								} else {
									int idx = 0;
									// 解决Set重复记录问题
									Set<Object> ids = new HashSet<>();

									for (Object obj : (Collection<?>) valueObj) {
										Object tmpTargetObj = null;
										try {
											// TODO: need more handler
											tmpTargetObj = Class.forName(targetGenericType.getTypeName()).newInstance();
											ctx.updateCurPath(field.getName());
											ctx.setCurSrcObj(obj);
											ctx.setCurTargetObj(tmpTargetObj);
											merge(ctx);
											if (needIdx) {
												RequestUtil.setFieldValue(tmpTargetObj, propAnno.sortBy(), idx++);
											}
										} catch (Exception e) {
											log.info("failed to init object for type:{}", targetGenericType.getTypeName(), e);
											tmpTargetObj = obj;
										}
										tmpTargetObj = enhanceObject(tmpTargetObj, field, targetField);
										if (RequestUtil.isBasicType(tmpTargetObj) && !ids.contains(tmpTargetObj)) {
											tmpTargetColl.add(tmpTargetObj);
											ids.add(tmpTargetObj);
										} else if (RequestUtil.isBasicType(tmpTargetObj) == false) {
											Object id = null;
											try {
												id = RequestUtil.getPropertyValue(tmpTargetObj, "id");
											} catch (Exception e) {
												log.info("failed to get id property for obj:{}", tmpTargetObj, e);
											}
											if (id == null || !ids.contains(id)) {
												tmpTargetColl.add(tmpTargetObj);
												ids.add(id);
											}
										}
									}
								}
								valueObj = tmpTargetColl;
							}
						} else {
							if (field.getType().equals(FileInfo.class)) {
								// TODO: 需要注意valueObj为空的情况
								handleFileProp(valueObj, targetObj, field, result, true, ctx);
								handleFile = true;
							} else {
								if (valueObj == null) {
									// do nothing
								} else {
									if (RequestUtil.isBasicType(valueObj)) {
										valueObj = enhanceObject(valueObj, field, targetField);
									} else {
										Object targetFieldObj = RequestUtil.getPropertyValue(targetObj, targetPropName);
										try {
											if (targetFieldObj == null) {
												targetFieldObj = targetField.getType().newInstance();
											}
											ctx.updateCurPath(field.getName());
											ctx.setCurSrcObj(valueObj);
											ctx.setCurTargetObj(targetFieldObj);
											merge(ctx);
										} catch (Exception e) {
											log.warn("failed to initiate target object", e);
										}
										valueObj = targetFieldObj;
									}
								}
							}
						}
						if (!handleFile) {
							RequestUtil.setFieldValue(targetObj, targetPropName, valueObj);
						}
					} catch (SystemException e) {
						log.error("failed to merge obj", e);
					} catch (Exception e) {
						log.error("failed to merge property:{} from class:{} to class:{}", targetPropName,
								srcObj.getClass().getName(), targetObj.getClass().getName(), e);
						throw new BusinessException("failed to merge property", e);
					}
				}
			}
		} else {
			result.setSuccess(false);
			result.setMessage("miss arguments");
		}
		return result;
	}
	
	private void handleFileCollection(MergeResult result, Class<?> targetClass, Collection<Object> targetCollection,
			Collection<Object> tmpTargetColl, Object valueObj, Field field, boolean needIdx, MergeContext ctx)
			throws SystemException {
		if (valueObj != null && valueObj instanceof Collection) {
			//注解处理
			FileField fileAnno = field.getAnnotation(FileField.class);
			if (fileAnno != null && !StringUtils.isEmpty(fileAnno.fileName())) {
				if (((Collection<?>) valueObj).size() > 0) {
					Long now = System.currentTimeMillis();
					Map<String, Object> oldMap = new HashMap<>();
					Set<String> newFileNameSet = new HashSet<>();
					if (targetCollection != null && targetCollection.size() > 0) {
						for (Object targetObj : targetCollection) {
							try {
								String oldFileName = (String) RequestUtil.getPropertyValue(targetObj,
										fileAnno.fileName());
								if (StringUtils.hasText(oldFileName)) {
									oldMap.put(oldFileName, targetObj);
								}
							} catch (Exception e) {
								log.warn("failed to get field:{} from object which class:{}", fileAnno.fileName(),
										targetClass);
								throw new SystemException("fail to get oldFileName");
							}
						}
					}
					List<FileInfo> files2Add = result.getFiles2Add();
					if (files2Add == null) {
						files2Add = new ArrayList<>();
						result.setFiles2Add(files2Add);
					}
					List<FileInfo> files2Remove = result.getFiles2Remove();
					if (files2Remove == null) {
						files2Remove = new ArrayList<>();
						result.setFiles2Remove(files2Remove);
					}
					PropertyIn propAnno = field.getAnnotation(PropertyIn.class);
					int idx = 0;
					// 将集合中的FileInfo都转为目标类型，存入新集合
					for (Object src : (Collection<?>) valueObj) {
						try {
							FileInfo info = (FileInfo) src;
							String fileName = info.getFileName();
							newFileNameSet.add(fileName);
							if (StringUtils.hasText(fileName)) {
								Object target = targetClass.newInstance();
								// 判断是否是新增的文件，是新增的文件，则设置文件的更新时间
								if (!oldMap.containsKey(fileName)) {
									info.setUpdateTime(now);
									if (needSwitchGroup(info, ctx)) {
										files2Add.add(info);
									}
								} else {
									// 是原有的文件时，将updateTime设为原来的值
									if (StringUtils.hasText(fileAnno.updateTime())) {
										info.setUpdateTime((Long) RequestUtil.getPropertyValue(oldMap.get(fileName),
												fileAnno.updateTime()));
									}
								}
								handleFileProp(info, target, field, result, false, ctx);
								if (needIdx) {
									RequestUtil.setFieldValue(target, propAnno.sortBy(), idx++);
								}
								tmpTargetColl.add(target);
							}
						} catch (Exception e) {
							log.warn("fail to wrap FileInfo to class:{} and add files2Add", targetClass, e);
							throw new SystemException("fail to wrap FileInfo");
						}
					}
					// 判断是否有旧文件被删除
					for (String oldFileName : oldMap.keySet()) {
						if (!newFileNameSet.contains(oldFileName)) {
							FileInfo info = new FileInfo();
							info.setFileName(oldFileName);
							info.setGroup(ctx.getTargetGroup());
							files2Remove.add(info);
						}
					}
				}
			}
		}
	}

	private Object enhanceObject(Object obj, Field srcField, Field targetField) throws Exception {
		Object ret = obj;
		if (obj != null) {
			PropertyIn propAnno = srcField.getAnnotation(PropertyIn.class);
			if (propAnno != null && !StringUtils.isEmpty(propAnno.enumCls())
					&& !propAnno.enumCls().equals(String.class)) {
				ret = RequestUtil.getEnumOrdinal(propAnno.enumCls(), (String) ret);
			}

			if (propAnno != null && !StringUtils.isEmpty(propAnno.dateFormat())) {
				DateFormat df = new SimpleDateFormat(propAnno.dateFormat());
				Date date = df.parse((String) obj);
				// String to Long
				if (targetField.getType().equals(Long.class)) {
					ret = date.getTime();
				} else {
					ret = date;
				}
			}

			// Long to Date
			if (obj instanceof Long && Date.class.equals(targetField.getType())) {
				ret = new Date((Long) obj);
			}
		}
		return ret;
	}

	private void handleFileProp(Object src, Object target, Field field, MergeResult result, boolean operateFiles, MergeContext ctx) throws SystemException {
		FileField fileAnno = field.getAnnotation(FileField.class);
		Date now = new Date();
		// 获取旧文件保存在硬盘中的文件名
		String oldFileName = null;
		try {
			Field fileNameField = target.getClass().getDeclaredField(fileAnno.fileName());
			fileNameField.setAccessible(true);
			oldFileName = (String) fileNameField.get(target);
		} catch (Exception e) {
			log.error("failed to get field:{} from object which class:{}", fileAnno.fileName(), target);
			throw new SystemException("fail to get oldFileName");
		}
		List<FileInfo> files2Add = result.getFiles2Add();
		if (files2Add == null) {
			files2Add = new ArrayList<>();
			result.setFiles2Add(files2Add);
		}
		List<FileInfo> files2Remove = result.getFiles2Remove();
		if (files2Remove == null) {
			files2Remove = new ArrayList<>();
			result.setFiles2Remove(files2Remove);
		}
		FileInfo info = (FileInfo) src;
		if (info == null) {
			info = new FileInfo();
		}
		if (hasNewFile(info, oldFileName)) {
			if (operateFiles) {
				if (needSwitchGroup(info, ctx)) {
					FileInfo addInfo = new FileInfo(info.getGroup(), info.getFileName(), info.getOrgFileName());
					files2Add.add(addInfo);
				}

				info.setUpdateTime(now.getTime());
			}
		}
		if (needRemoveOldFile(info, oldFileName)) {
			if (operateFiles) {
				FileInfo removeInfo = new FileInfo();
				removeInfo.setFileName(oldFileName);
				removeInfo.setGroup(ctx.getTargetGroup());
				files2Remove.add(removeInfo);

				info.setUpdateTime(now.getTime());
			}
		}
		mergeFileInfo(info, target, fileAnno);
	}

	private boolean needSwitchGroup(FileInfo info, MergeContext ctx) {
		if (info != null && StringUtils.hasText(info.getGroup()) && ctx != null
				&& StringUtils.hasText(ctx.getTargetGroup()) && !ctx.getTargetGroup().equals(info.getGroup())) {
			return true;
		}
		return false;
	}

	private void mergeFileInfo(FileInfo info, Object target, FileField fileAnno) throws SystemException {
		if (!StringUtils.isEmpty(fileAnno.fileName())) {
			try {
				RequestUtil.setFieldValue(target, fileAnno.fileName(), info.getFileName());
			} catch (Exception e) {
				log.error("failed to set field:{} with value:{} for object with class:{}", fileAnno.orgName(),
						info.getFileName(), target.getClass());
				throw new SystemException("failed to set field value", e);
			}
		}
		if (!StringUtils.isEmpty(fileAnno.orgName())) {
			try {
				RequestUtil.setFieldValue(target, fileAnno.orgName(), info.getOrgFileName());
			} catch (Exception e) {
				log.error("failed to set field:{} with value:{} for object with class:{}", fileAnno.orgName(),
						info.getOrgFileName(), target.getClass());
				throw new SystemException("failed to set field value", e);
			}
		}
		if (!StringUtils.isEmpty(fileAnno.updateTime())) {
			try {
				RequestUtil.setFieldValue(target, fileAnno.updateTime(), info.getUpdateTime());
			} catch (Exception e) {
				log.error("failed to set field:{} with value:{} for object with class:{}", fileAnno.updateTime(),
						info.getUpdateTime(), target.getClass());
				throw new SystemException("failed to set field value", e);
			}
		}
	}

	private boolean needRemoveOldFile(FileInfo info, String oldFileName) {
		if (StringUtils.hasText(oldFileName) && (info == null || StringUtils.isEmpty(info.getFileName())
				|| !oldFileName.equals(info.getFileName()))) {
			return true;
		}
		return false;
	}

	private boolean hasNewFile(FileInfo info, String oldFileName) {
		if (info != null) {
			String fileName = info.getFileName();
			// 修改文件（新增或更改）
			if (!StringUtils.isEmpty(fileName) && !fileName.equals(oldFileName)) {
				return true;
			}
		}
		return false;
	}

}
