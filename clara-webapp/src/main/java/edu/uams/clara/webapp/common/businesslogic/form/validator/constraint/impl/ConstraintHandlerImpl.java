package edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.ConstraintHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.UnitConverter;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.UserValue;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums.ConstraintType;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.exception.CannotResolveUserValueException;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.exception.ConstraintHandlerNotImplemented;
import edu.uams.clara.webapp.common.businesslogic.form.validator.util.ValueObjectHelper;

public class ConstraintHandlerImpl implements ConstraintHandler {
	private final static Logger logger = LoggerFactory
			.getLogger(ConstraintHandler.class);

	@Override
	public boolean evaluateConstraint(Object value, Constraint constraint, Map<String, List<String>> values, Map<String, Object> cachedValues) {
		Assert.notNull(constraint);

		switch (constraint.getConstraintType()) {
		/**
		 * REQUIRED: return true, if it's not empty. value cannot be null, if
		 * it's a String, it cannot be empty, if it's a number it must be larger
		 * than zero
		 */
		case REQUIRED:

			if (value == null)
				return false;
			if (value instanceof String) {
				return !((String) value).isEmpty();
			} else if (value instanceof Number) {
				// as long as it's a number
				return true;
				/*
				 * if(value instanceof Double){ return ((Double)value > 0 );
				 * }else if(value instanceof Float){ return ((Float)value > 0 );
				 * }else if(value instanceof Long){ return ((Long)value > 0 );
				 * }else{ return ((Number)value).intValue() > 0; }
				 */
			} else if (value instanceof List<?>) {
				return !((List<?>) value).isEmpty();
			}
			break;
		case CONTAINS:
			// the value has to be a list...otherwise use EQUAL or MEMBEROF
			// contains only check single value, for check a complete list use
			// INTERSECT...
			Object testObject = constraint.getParams().get(
					ConstraintType.Contains.ParamKeys.VALUE.toString());

			if (value instanceof List) {
				if (testObject instanceof List) {
					return false;
				} else {
					return ((List) value).contains(testObject);
				}
			}
			break;
		case NOTCONTAINSMULTIPLE:
			// the value has to be a list...otherwise use EQUAL or MEMBEROF
			// contains only check single value, for check a complete list use
			// INTERSECT...
			Object realValue = constraint.getParams().get(
					ConstraintType.Contains.ParamKeys.VALUE.toString());

			if (value instanceof List) {
				if (realValue instanceof List) {
					return false;
				} else {
					int occurrences = Collections.frequency((List) value, realValue);

					if (occurrences > 1) {
						return false;
					} else {
						return true;
					}
				}
			}
			break;
		case NOTCONTAINS:
			// the value has to be a list...otherwise use EQUAL or MEMBEROF
			// contains only check single value, for check a complete list use
			// INTERSECT...
			Object tsObject = constraint.getParams().get(
					ConstraintType.NotContains.ParamKeys.VALUE.toString());

			if (value instanceof List) {
				if (tsObject instanceof List) {
					return false;
				} else {
					return !((List) value).contains(tsObject);
				}
			}
			break;
		case MEMBEROF:
			Object memObject = constraint.getParams().get(
					ConstraintType.MemberOf.ParamKeys.VALUES.toString());
			
			if (value == null){
				return false;
			}
			
			List<String> list = Arrays.asList(memObject.toString().split(","));

			return list.contains(value.toString());
		case EQUAL:
			//logger.info("ConstraintType.Equal.ParamKeys.VALUE.toString()" + ConstraintType.Equal.ParamKeys.VALUE.toString());
			Object depObject = constraint.getParams().get(
					ConstraintType.Equal.ParamKeys.VALUE.toString());
			
			if (value == null){
				return false;
			}
			//logger.debug("value: " + value + " actual value: " + depObject);
			return value.equals(depObject);
		case NOTEQUAL:
			//logger.info("ConstraintType.NotEqual.ParamKeys.VALUE.toString()" + ConstraintType.NotEqual.ParamKeys.VALUE.toString());
			Object vObject = constraint.getParams().get(
					ConstraintType.NotEqual.ParamKeys.VALUE.toString());
			//logger.debug("$$$$$$$$$$$$ value: " + value);
			boolean res = false;

			if (value == null){
				return true;
			} else {
				if (!value.equals(vObject)){
					res = true;
				}
			}
			
			return res;
		case NUMBER:
			//Check if the value is a Number or not, used to non-required field
			if (value == null)
				return true;
			try{
				Float.parseFloat(value.toString());
				return true;
			} catch (Exception e){
				e.printStackTrace();
				return false;
			}
		case NUMBERRANGE:
			if (value == null)
				return false;
			
			try{
				Float.parseFloat(value.toString());
			} catch (Exception e){
				e.printStackTrace();
				return false;
			}

			Number numberValue = (Number) value;

			Object unitConverterObject = constraint.getParams().get(
					ConstraintType.NumberRange.ParamKeys.UNITCONVERTER.toString());

			UnitConverter unitConverter = null;
			if(unitConverterObject != null && unitConverterObject instanceof UnitConverter){
				unitConverter = (UnitConverter) unitConverterObject;
			}

			if(numberValue != null && unitConverter != null){
				Object unit = constraint.getParams().get(
						ConstraintType.NumberRange.ParamKeys.UNIT.toString());

				if(unit instanceof UserValue){
					String unitKey = ((UserValue)unit).getValueKey();

					try{
						unit = (String)ValueObjectHelper.getTypeSafeValueObject(unitKey, String.class, values, cachedValues);
					}catch(Exception ex){
						//ideally this shouldn't happen, since the user value should have its own NUMBER constraint and should have been evaluated before this one
						//so if this happens, it means there is a flaw in the rule definitions. throw exception!
						throw new CannotResolveUserValueException("the user value (key: " + unitKey + ") cannot be resolved correclty!", ex);
					}
				}

				numberValue = unitConverter.convert((Number)numberValue, unit.toString());

			}

			Object min = constraint.getParams().get(
					ConstraintType.NumberRange.ParamKeys.MIN.toString());

			Object max = constraint.getParams().get(
					ConstraintType.NumberRange.ParamKeys.MAX.toString());



			if(min != null){
				if(min instanceof UserValue){
					String minValueKey = ((UserValue)min).getValueKey();
					try{
						min = (Number)ValueObjectHelper.getTypeSafeValueObject(minValueKey, Number.class, values, cachedValues);
					}catch(Exception ex){
						//ideally this shouldn't happen, since the user value should have its own NUMBER constraint and should have been evaluated before this one
						//so if this happens, it means there is a flaw in the rule definitions. throw exception!
						throw new CannotResolveUserValueException("the user value (key: " + minValueKey + ") cannot be resolved correclty!", ex);
					}
				}else{
					try{
						min = (Number) min;
					}catch(Exception ex){
						min = null;
						ex.printStackTrace();
					}
				}

				if(min != null && unitConverter != null){
					Object minUnit = constraint.getParams().get(
							ConstraintType.NumberRange.ParamKeys.MINUNIT.toString());

					if(minUnit instanceof UserValue){
						String minUnitKey = ((UserValue)minUnit).getValueKey();

						try{
							minUnit = (String)ValueObjectHelper.getTypeSafeValueObject(minUnitKey, String.class, values, cachedValues);
						}catch(Exception ex){
							//ideally this shouldn't happen, since the user value should have its own NUMBER constraint and should have been evaluated before this one
							//so if this happens, it means there is a flaw in the rule definitions. throw exception!
							throw new CannotResolveUserValueException("the user value (key: " + minUnitKey + ") cannot be resolved correclty!", ex);
						}
					}

					min = unitConverter.convert((Number)min, minUnit.toString());

				}
			}


			if(max != null){
				if(max instanceof UserValue){
					String maxValueKey = ((UserValue)max).getValueKey();
					try{
						max = (Number)ValueObjectHelper.getTypeSafeValueObject(maxValueKey, Number.class, values, cachedValues);
					}catch(Exception ex){
						//ideally this shouldn't happen, since the user value should have its own NUMBER constraint and should have been evaluated before this one
						//so if this happens, it means there is a flaw in the rule definitions. throw exception!
						throw new CannotResolveUserValueException("the user value (key: " + maxValueKey + ") cannot be resolved correclty!", ex);
					}
				}else{
					try{
						max = (Number) max;
					}catch(Exception ex){
						max = null;
						ex.printStackTrace();
					}
				}

				if(max != null && unitConverter != null){
					Object maxUnit = constraint.getParams().get(
							ConstraintType.NumberRange.ParamKeys.MAXUNIT.toString());



					if(maxUnit instanceof UserValue){
						String maxUnitKey = ((UserValue)maxUnit).getValueKey();

						try{
							maxUnit = (String)ValueObjectHelper.getTypeSafeValueObject(maxUnitKey, String.class, values, cachedValues);
						}catch(Exception ex){
							//ideally this shouldn't happen, since the user value should have its own NUMBER constraint and should have been evaluated before this one
							//so if this happens, it means there is a flaw in the rule definitions. throw exception!
							throw new CannotResolveUserValueException("the user value (key: " + maxUnitKey + ") cannot be resolved correclty!", ex);
						}
					}

					max = unitConverter.convert((Number)max, maxUnit.toString());
				}
			}

			if (min != null | max != null) {
				boolean isInRange = true;
				if (min != null) {
					isInRange = numberValue.doubleValue() >= ((Number)min).doubleValue();
				}
				if (max != null) {
					isInRange = numberValue.doubleValue() <= ((Number)max).doubleValue();
				}
				return isInRange;
			}

			break;
		case NOINTERSECT:

			if(value == null) return true;

			Assert.notNull(constraint.getParams());
			Object targetListObject = constraint.getParams().get(
					ConstraintType.NonIntersect.ParamKeys.VALUES.toString());

			if(targetListObject == null) return true;

			List<?> targetList = null;

			if(targetListObject instanceof UserValue){
				String targetListValueKey = ((UserValue)targetListObject).getValueKey();

				targetListObject = ValueObjectHelper.getTypeSafeValueObject(targetListValueKey, List.class, values, cachedValues);
			}

			try{
				targetList = (List)targetListObject;
			}catch(Exception ex){
				ex.printStackTrace();
			}

			if(targetList == null) return true;

			List<Object> intersectList = new ArrayList<Object>(0);

			if(value instanceof List){
				for(Object v:(List)value){
					if(targetList.contains(v)) {
						intersectList.add(v);
					}
				}
			}else{
				if(targetList.contains(value)){
					intersectList.add(value);
				}
			}

			if(intersectList.size() > 0){
				constraint.addResult("INTERSECTING_VALUES", intersectList);
				constraint.finalizeErrorMessage();
				return false;
			}else{
				return true;
			}
		case INTERSECT:

			if(value == null) return false;

			Assert.notNull(constraint.getParams());
			Object inTtargetListObject = constraint.getParams().get(
					ConstraintType.Intersect.ParamKeys.VALUES.toString());

			if(inTtargetListObject == null) return false;
			
			String targetObjectStr = String.valueOf(inTtargetListObject);
			
			if (!targetObjectStr.contains(",")) return false;

			List<?> inTargetList = null;

			if(inTtargetListObject instanceof UserValue){
				String inTargetListValueKey = ((UserValue)inTtargetListObject).getValueKey();

				inTtargetListObject = ValueObjectHelper.getTypeSafeValueObject(inTargetListValueKey, List.class, values, cachedValues);
			}

			try{
				inTargetList = Arrays.asList(targetObjectStr.split(","));;
			}catch(Exception ex){
				ex.printStackTrace();
			}

			if(inTargetList == null) return false;

			List<Object> inIntersectList = new ArrayList<Object>(0);

			if(value instanceof List){
				for(Object v:(List)value){
					if(inTargetList.contains(v)) {
						inIntersectList.add(v);
					}
				}
			}else{
				if(inTargetList.contains(value)){
					inIntersectList.add(value);
				}
			}

			if(inIntersectList.size() > 0){
				constraint.addResult("INTERSECTING_VALUES", inIntersectList);
				constraint.finalizeErrorMessage();
				return true;
			}else{
				return false;
			}
		case UNIQUE:
			Object uniqueObject = constraint.getParams().get(
					ConstraintType.Contains.ParamKeys.VALUE.toString());
			if (value instanceof List) {
				if (uniqueObject instanceof List) {
					return false;
				} else {
					if (((List) value).lastIndexOf(uniqueObject) > 1)
						return false;
					else{
						return ((List) value).contains(uniqueObject);
					}
				}
			}
			break;
		case CONTAINSNA:
			Object naObject = constraint.getParams().get(
					ConstraintType.Contains.ParamKeys.VALUE.toString());
			if (value instanceof List) {
				if (naObject instanceof List) {
					return false;
				} else {
					if (((List) value).contains(naObject) && ((List) value).size() > 1)
						return false;
					else{
						return true;
					}
				}
			}
		default:
			throw new ConstraintHandlerNotImplemented(constraint
					.getConstraintType().toString()
					+ " handler is not impelmented!");
		}
		return false;
	}

	private boolean isNumericString(String testString) {
		String pattern = "^(\\-|\\+)*[0-9]*(\\.)?[0-9]+$";

		Pattern p = Pattern.compile(pattern);

		Matcher m = p.matcher(testString);

		return m.find();
	}

}
