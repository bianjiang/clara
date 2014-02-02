package edu.uams.clara.webapp.report.service.customreport.impl;

import java.util.Map;

import com.google.common.collect.Maps;

import edu.uams.clara.webapp.report.service.customreport.CustomReportService;
import edu.uams.clara.webapp.report.service.customreport.CustomReportServiceContainer;

public class CustomReportServiceContainerImpl implements CustomReportServiceContainer {
	
	private Map<String, CustomReportService> customerReportServices = Maps.newHashMap();

	@Override
	public CustomReportService getCustomReportService(String name) {
		// TODO Auto-generated method stub
		return customerReportServices.get(name);
	}

	public Map<String, CustomReportService> getCustomerReportServices() {
		return customerReportServices;
	}

	public void setCustomerReportServices(Map<String, CustomReportService> customerReportServices) {
		this.customerReportServices = customerReportServices;
	}

}
