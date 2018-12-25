package com.files.client;

import com.files.client.exception.BusinessException;
import com.files.client.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class RequestUtil {

	/**
	 * 获取目标类的指定属性，属性名通过<code>.</code>分割对象层级。如propName=aa.bb,则从targetObj类获取aa属性，再从aa的类获取bb属性。
	 * 
	 * @param targetObj
	 *            目标对象
	 * @param propName
	 *            目标对象的属性名
	 * @return
	 * @throws Exception
	 */
	public static Field getTargetField(Object targetObj, String propName) throws Exception {
		String[] propArr = StringUtils.tokenizeToStringArray(propName, "\\.");
		Class<? extends Object> localTargetClass = targetObj.getClass();
		Field targetField = null;
		for (String targetPropName : propArr) {
			targetField = localTargetClass.getDeclaredField(targetPropName);
			localTargetClass = targetField.getType();
		}
		return targetField;
	}

	/**
	 * 检测目标对象的对应属性（用<code>.</code>分割对象层级）的路径上的对象是否存在，不存在则创建对象，返回最低一级属性所在的对象。
	 * 如propName=aa.bb,则从targetObj类获取aa属性的对象，为null时创建新对象，对targetObj类aa属性赋值，返回targetObj类aa属性的对象。
	 * 
	 * @param targetObj
	 *            目标对象
	 * @param propName
	 *            对应属性
	 * @return
	 * @throws Exception
	 */
	public static Object checkTargetObj(Object targetObj, String propName) throws Exception {
		String[] propArr = StringUtils.tokenizeToStringArray(propName, "\\.");
		Object lowestTargetObj = targetObj;
		if (propArr.length > 1) {
			for (int i = 0; i < propArr.length - 1; i++) {
				Field tmpField = lowestTargetObj.getClass().getDeclaredField(propArr[i]);
				tmpField.setAccessible(true);
				Object propObj = tmpField.get(lowestTargetObj);
				// 当对象不存在时，创建新的实例
				if (propObj == null) {
					propObj = tmpField.getType().newInstance();
					tmpField.set(targetObj, propObj);
				}
				lowestTargetObj = propObj;
			}
		}
		return lowestTargetObj;
	}

	public static boolean isBasicType(Object obj) {
		boolean ret = false;
		if (obj != null
				&& (obj instanceof Number || obj instanceof String || obj instanceof Date || obj instanceof Boolean)) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 获取对象对应字段的值（从对应的get方法获取），字段名可以用"."分割。如propName="name.length"，则获取获取目标对象的name，再在name的对象中获取length的值。
	 * 
	 * @param targetObj
	 *            目标对象
	 * @param propName
	 *            对应字段
	 * @return
	 * @throws Exception
	 */
	public static Object getPropertyValue(Object targetObj, String propName) throws Exception {
		if (targetObj == null) {
			return null;
		}
		String[] propArr = StringUtils.tokenizeToStringArray(propName, "\\.");
		if (targetObj instanceof Collection) {
			List<Object> ret = new ArrayList<>();
			for (Object obj : (Collection<?>) targetObj) {
				ret.add(getPropertyValue(obj, propName));
			}
			return ret;
		} else {
			Object tmpTargetObj = targetObj;
			for (int i = 0; i < propArr.length; i++) {
				if (tmpTargetObj instanceof Collection) {
					tmpTargetObj = getPropertyValue(tmpTargetObj, propArr[i]);
				} else {
					try {
						Method mtd = tmpTargetObj.getClass().getMethod(generateGetMethodName(propArr[i]), null);
						tmpTargetObj = mtd.invoke(tmpTargetObj, null);
					} catch (Exception e) {
						Field tmpField = tmpTargetObj.getClass().getDeclaredField(propArr[i]);
						tmpField.setAccessible(true);
						tmpTargetObj = tmpField.get(tmpTargetObj);
					}
				}
				if (tmpTargetObj == null) {
					return null;
				}
			}
			return tmpTargetObj;
		}

	}

	private static String generateGetMethodName(String fieldName) {
		return new StringBuilder("get").append(fieldName.substring(0, 1).toUpperCase()).append(fieldName.substring(1))
				.toString();
	}

	public static Map<?, ?> transferMap(Collection<?> coll, String matchKey) {
		Map<Object, Object> map = new HashMap<>();
		if (coll != null) {
			for (Object obj : coll) {
				try {
					Field field = obj.getClass().getDeclaredField(matchKey);
					field.setAccessible(true);
					Object valueObj = field.get(obj);
					if (valueObj != null) {
						map.put(valueObj, obj);
					}
				} catch (Exception e) {
					log.warn("failed to get value for field:{} of class:{}", matchKey, obj.getClass().getName());
				}
			}
		}
		return map;
	}

	public static Integer getEnumOrdinal(Class<?> enumCls, String value) {
		Integer ret = null;
		try {
			Method mtd = enumCls.getDeclaredMethod("getOrdinal", String.class);
			if (mtd != null) {
				mtd.setAccessible(true);
				ret = (Integer) mtd.invoke(null, value);
			}
		} catch (Exception e) {
			log.warn("failed to get ordinal from class:{} for value:{}", enumCls.getName(), value);
		}
		return ret;
	}

	public static String getEnumDisplayName(Class<?> enumCls, int ordinal) {
		String ret = null;
		try {
			Method mtd = enumCls.getDeclaredMethod("values");
			if (mtd != null) {
				mtd.setAccessible(true);
				Object item = ((Object[]) mtd.invoke(null))[ordinal];
				Field field = item.getClass().getDeclaredField("displayName");
				field.setAccessible(true);
				ret = (String) field.get(item);
			}
		} catch (Exception e) {
			log.warn("failed to get display from class:{} for ordinal:{}", enumCls.getName(), ordinal);
		}
		return ret;
	}

	public static Type getFieldGenericType(Field field) {
		if (field.getGenericType() instanceof ParameterizedType) {
			Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
			return types[0];
		} else {
			return field.getType();
		}
	}

	public static Class<?> getFieldGenericClass(Field field) {
		if (field.getGenericType() instanceof ParameterizedType) {
			Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
			return (Class<?>) types[0];
		} else {
			return field.getType();
		}
	}

	public static void setFieldValue(Object obj, String fieldName, Object fieldValue) throws Exception {
		if (obj != null) {
			fillTargetObj(obj, fieldName);
			Field field = getTargetField(obj, fieldName);
			if (field != null) {
				field.setAccessible(true);
				Object targetObj = checkTargetObj(obj, fieldName);
				field.set(targetObj, fieldValue);
			}
		}
	}

	private static void fillTargetObj(Object targetObj, String propName) throws Exception {
		String[] propArr = StringUtils.tokenizeToStringArray(propName, "\\.");
		if (propArr.length > 1) {
			for (int i = 0; i < propArr.length - 1; i++) {
				Field tmpField = targetObj.getClass().getDeclaredField(propArr[i]);
				tmpField.setAccessible(true);
				Object propObj = tmpField.get(targetObj);
				if (propObj == null) {
					propObj = tmpField.getType().newInstance();
				}
				tmpField.set(targetObj, propObj);
				targetObj = propObj;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static List<?> sortList(List<?> list, String propName) {
		if (list != null) {
			((List<?>) list).sort((Object o1, Object o2) -> {
				try {
					Object prop1 = getPropertyValue(o1, propName);
					Object prop2 = getPropertyValue(o2, propName);
					if (prop1 == null) {
						return -1;
					} else if (prop2 == null) {
						return 1;
					} else {
						return ((Comparable<Object>) prop1).compareTo((Comparable<Object>) prop2);
					}
				} catch (Exception e) {
					log.warn("failed to compare object:{} and object:{} with property:{}", o1, o2, propName, e);
					return -1;
				}
			});
		}
		return list;
	}

	public static Object transferString2Obj(String str, Class<?> targetClz) throws BusinessException, SystemException {
		return transferString2Obj(str, targetClz, null);
	}

	public static Object transferString2Obj(String str, Class<?> targetClz, String dateFormat)
			throws BusinessException, SystemException {
		Object obj = null;
		if (null != str) {
			if (Number.class.isAssignableFrom(targetClz) || Boolean.class.isAssignableFrom(targetClz)) {
				try {
					obj = targetClz.getConstructor(String.class).newInstance(str);
				} catch (Exception e) {
					log.warn("failed to transfer string:{} to type:{}", str, targetClz, e);
					throw new SystemException("failed to construct param", e);
				}
			} else if (String.class.equals(targetClz)) {
				obj = str;
			} else if (Date.class.equals(targetClz)) {
				if (StringUtils.hasText(dateFormat)) {
					try {
						DateFormat df = new SimpleDateFormat(dateFormat);
						obj = df.parse(str);
					} catch (ParseException e) {
						log.warn("failed to parse date:{} with format:{}", str, dateFormat);
						throw new BusinessException("can't parse date:" + str + " with format:" + dateFormat, e);
					}
				} else {
					throw new SystemException("dateFormat is null for date String:" + str);
				}
			} else {
				throw new SystemException("type:" + targetClz + " not supported yet.");
			}
		}
		return obj;
	}
}
