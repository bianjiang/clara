package edu.uams.clara.webapp.common.businesslogic.impl;

import java.util.HashMap;
import java.util.Map;

import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelper;
import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;


public class BusinessObjectStatusHelperContainerImpl implements BusinessObjectStatusHelperContainer {

	private Map<String, BusinessObjectStatusHelper> businessObjectStatusHelpers = new HashMap<String, BusinessObjectStatusHelper>(0);
	
	@Override
	public BusinessObjectStatusHelper getBusinessObjectStatusHelper(String name){
		return businessObjectStatusHelpers.get(name);
	}

	
	public void setBusinessObjectStatusHelpers(
			Map<String, BusinessObjectStatusHelper> businessObjectStatusHelpers) {
		this.businessObjectStatusHelpers = businessObjectStatusHelpers;
	}


	public Map<String, BusinessObjectStatusHelper> getBusinessObjectStatusHelpers() {
		return businessObjectStatusHelpers;
	}
}
