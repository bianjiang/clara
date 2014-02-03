package edu.uams.clara.webapp.protocol.businesslogic.validator.constraint.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.ConstraintHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums.ConstraintType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/form/validator/constraint/impl/ConstraintHandlerImplTest-context.xml"})
public class ConstraintHandlerTest {

	private final static Logger logger = LoggerFactory
	.getLogger(ConstraintHandlerTest.class);

	private ConstraintHandler constraintHandler;

	@Test
	public void testRequiredConstraint(){

		Constraint requiredConstraint = new Constraint();
		requiredConstraint.setConstraintType(ConstraintType.REQUIRED);

		/*
		Object nullObject = null;
		assertFalse(constraintHandler.evaluateConstraint(nullObject, requiredConstraint));

		String stringObject = "";
		assertFalse(constraintHandler.evaluateConstraint(stringObject, requiredConstraint));
		stringObject = "whatever";
		assertTrue(constraintHandler.evaluateConstraint(stringObject, requiredConstraint));
	*/

	}

	@Autowired(required=true)
	public void setConstraintHandler(ConstraintHandler constraintHandler) {
		this.constraintHandler = constraintHandler;
	}


	public ConstraintHandler getConstraintHandler() {
		return constraintHandler;
	}



}
