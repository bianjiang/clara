package edu.uams.clara.core.service.util;

import java.io.IOException;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;

public interface ResourceResolver extends ResourceLoaderAware  {

	String getIdFromResource(String basePath, String pattern, Resource resource);

	Resource[] getResources(String basePath, String pattern) throws IOException;

}
