package edu.uams.clara.webapp.protocol.businesslogic.protocolform.validator.ruleinitiator.unitconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.UnitConverter;

/**
 * base unit is days
 * @author h0cked
 *
 */
public class AgeUnitConverter implements UnitConverter{

	private final static Logger logger = LoggerFactory
	.getLogger(AgeUnitConverter.class);


	@Override
	public Number convert(Number value, String unit) {

		logger.trace("value: " + value + "; unit: " + unit);

		if(unit.equals("Years")){
			value =  value.doubleValue() * 365;
		}else if (unit.equals("Months")){
			value = value.doubleValue() * 30;
		}else if (unit.equals("Weeks")) {
			value = value.doubleValue() * 7;
		}

		logger.trace("converted value: " + value + " [Days]");
		return value;
	}

}
