package edu.uams.clara.webapp.common.web.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.protocol.dao.thing.DeviceDao;
import edu.uams.clara.webapp.protocol.domain.thing.Device;

@Controller
public class DeviceAdminAjaxController{
	
	private final static Logger logger = LoggerFactory
			.getLogger(DeviceAdminAjaxController.class);
	
	private DeviceDao deviceDao;
	
	@RequestMapping(value = "/ajax/devices/list", method = RequestMethod.GET)
	public @ResponseBody Map<String, List<Device>> listUnapprovedDevices(){
		Map<String, List<Device>> devices = new HashMap<String, List<Device>>(0);
		List<Device> unapprovedDeviceList = deviceDao.listUnapprovedThingsByType();
		devices.put("devices", unapprovedDeviceList);
		return devices;
	}

	public DeviceDao getDeviceDao() {
		return deviceDao;
	}
	
	@Autowired(required = true)
	public void setDeviceDao(DeviceDao deviceDao) {
		this.deviceDao = deviceDao;
	}
	

}