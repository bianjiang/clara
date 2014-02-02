package edu.uams.clara.webapp.common.businesslogic.form.validator.constraint;

/*
 * convert the value according to the input unit to its base unit.. the base unit is defined by each implementation
 */
public interface UnitConverter {

	Number convert(Number value, String unit);

}
