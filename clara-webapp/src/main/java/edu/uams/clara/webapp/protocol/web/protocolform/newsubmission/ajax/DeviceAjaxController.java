package edu.uams.clara.webapp.protocol.web.protocolform.newsubmission.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.protocol.dao.thing.DeviceDao;
import edu.uams.clara.webapp.protocol.domain.thing.Device;

@Controller
public class DeviceAjaxController {
	
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory
			.getLogger(DeviceAjaxController.class);
	
	
	private DeviceDao deviceDao;

	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/devices/list", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<Device>> getDevices() {
		List<Device> deviceList = deviceDao.listThings();

		Map<String, List<Device>> devices = new HashMap<String, List<Device>>(0);
		devices.put("devices", deviceList);
		return devices;
	}

	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/devices/search", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, List<Device>> searchByName(@RequestParam("name") String name) {

		List<Device> deviceList = deviceDao.searchByName(name);

		Map<String, List<Device>> devices = new HashMap<String, List<Device>>(0);
		devices.put("devices", deviceList);
		return devices;
	}

	@Autowired(required=true)
	public void setDeviceDao(DeviceDao deviceDao) {
		this.deviceDao = deviceDao;
	}

	public DeviceDao getDeviceDao() {
		return deviceDao;
	}

	
}
