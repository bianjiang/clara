package edu.uams.clara.webapp.common.businesslogic.form.validator.util;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueObjectHelper {

	private final static Logger logger = LoggerFactory
			.getLogger(ValueObjectHelper.class);

	/**
	 * with cache
	 *
	 * @param valueKey
	 * @param valueType
	 * @param values
	 * @param cachedValues
	 * @return
	 */
	public static final Object getTypeSafeValueObject(String valueKey,
			Class<?> valueType, Map<String, List<String>> values,
			final Map<String, Object> cachedValues) {
		Object value = null;
		value = cachedValues.get(valueKey);

		if (value == null) {
			logger.trace(valueKey + " is not cached");

			value = getTypeSafeValueObject(valueKey, valueType, values);

			//only cache it when its type safe and has a value...
			if(value != null && valueType != null){

				logger.trace("cache " + valueKey);
				cachedValues.put(valueKey, value);
			}

		}

		return value;
	}

	/**
	 * without cache
	 *
	 * @param valueKey
	 * @param valueType
	 * @param values
	 * @return
	 */
	public static final Object getTypeSafeValueObject(String valueKey,
			Class<?> valueType, Map<String, List<String>> values) {
		Object value = null;

		List<String> valueList = values.get(valueKey);

		// valueType = List.class;
		// by default it comes as a List<String>
		// it converts it into the right class if defined...
		if (valueType != null) {

			if(valueType.equals(List.class)){
				value = valueList;
			}else{
				if (valueList != null && !valueList.isEmpty()) {
					String stringValue = valueList.get(0);
					value = castStringToObject(valueType, stringValue);
				} else {
					value = null;
				}
			}

		} else {
			value = valueList;
		}

		logger.trace(valueKey + " = {" + value + "}");

		return value;
	}

	public static final Object castStringToObject(Class<?> valueType,
			String stringValue) {
		Object value = null;
		logger.trace("cast Object to: " + valueType.getSimpleName());
		try {
			if (valueType.equals(Integer.class)) {
				return Integer.parseInt(stringValue);
			} else if (valueType.equals(Double.class)) {
				return Double.parseDouble(stringValue);
			} else if (valueType.equals(Long.class)) {
				return Long.parseLong(stringValue);
			} else if (valueType.equals(Float.class)) {
				return Float.parseFloat(stringValue);
			} else if(valueType.equals(Number.class)) {
				//cast to Double take the highest precision...
				return Double.parseDouble(stringValue);
			}else {
				try {
					value = valueType.cast(stringValue);
				} catch (ClassCastException cce) {
					cce.printStackTrace();
					// value = valueType.toString();
				}
			}
		} catch (Exception ex) {
			// ignore convertion exception, return null
			ex.printStackTrace();
		}

		return value;
	}
}
