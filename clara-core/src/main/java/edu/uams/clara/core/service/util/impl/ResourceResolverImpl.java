package edu.uams.clara.core.service.util.impl;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import edu.uams.clara.core.service.util.ResourceResolver;

/**
 * This class includes some utils for resolving resources such as resolving id from file name, etc.
 * @author jbian
 *
 */
public class ResourceResolverImpl implements ResourceResolver {
	
	private ResourceLoader resourceLoader;
	
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
		
	}
	
	private static final String CLASSPATH_SCHEME = "classpath:";

	private static final String CLASSPATH_STAR_SCHEME = "classpath*:";

	private static final String SLASH = "/";
	/**
	 * This is borrowed from webflow...
	 * Obtains an id from the resource. By default, the id becomes the portion of the path between the
	 * basePath and the filename. If no directory structure is available then the filename without the extension is
	 * used.
	 * <p>
	 * For example, '${basePath}/booking.xml' becomes 'booking' and '${basePath}/hotels/booking/booking.xml' becomes
	 * 'hotels/booking'
	 * @param basePath
	 * @param pattern
	 * @param resource the resource
	 * @return an id
	 */
	@Override
	public String getIdFromResource(String basePath, String pattern, Resource resource) {
		if (basePath == null) {
			return getIdFromFileName(resource);
		}
		basePath = removeClasspathScheme(basePath);
		String filePath;
		if (resource instanceof ContextResource) {
			filePath = ((ContextResource) resource).getPathWithinContext();
		} else if (resource instanceof ClassPathResource) {
			filePath = ((ClassPathResource) resource).getPath();
		} else if (resource instanceof FileSystemResource) {
			filePath = truncateFilePath(((FileSystemResource) resource).getPath(), basePath);
		} else if (resource instanceof UrlResource) {
			try {
				filePath = truncateFilePath(((UrlResource) resource).getURL().getPath(), basePath);
			} catch (IOException e) {
				throw new IllegalArgumentException("Unable to obtain path: " + e.getMessage());
			}
		} else {
			// default to the filename
			return getIdFromFileName(resource);
		}

		int beginIndex = 0;
		int endIndex = filePath.length();
		if (filePath.startsWith(basePath)) {
			beginIndex = basePath.length();
		} else if (filePath.startsWith(SLASH + basePath)) {
			beginIndex = basePath.length() + 1;
		}
		if (filePath.startsWith(SLASH, beginIndex)) {
			// ignore a leading slash
			beginIndex++;
		}
		if (filePath.lastIndexOf(SLASH) >= beginIndex) {
			// ignore the filename
			endIndex = filePath.lastIndexOf(SLASH);
		} else {
			// there is no path info, default to the filename
			return getIdFromFileName(resource);
		}
		return filePath.substring(beginIndex, endIndex);
	}
	
	private String truncateFilePath(String filePath, String basePath) {
		int basePathIndex = filePath.lastIndexOf(basePath);
		if (basePathIndex != -1) {
			return filePath.substring(basePathIndex);
		} else {
			return filePath;
		}
	}

	private String removeClasspathScheme(String basePath) {
		if (basePath.startsWith(CLASSPATH_SCHEME)) {
			return basePath.substring(CLASSPATH_SCHEME.length());
		} else if (basePath.startsWith(CLASSPATH_STAR_SCHEME)) {
			return basePath.substring(CLASSPATH_STAR_SCHEME.length());
		} else {
			return basePath;
		}
	}
	
	private String getIdFromFileName(Resource resource) {
		return StringUtils.stripFilenameExtension(resource.getFilename());
	}
	
	/**
	 * get resources based on basePath and pattern
	 * @param basePath
	 * @param pattern
	 * @return
	 * @throws IOException
	 */
	@Override
	public Resource[] getResources(String basePath, String pattern) throws IOException{
		if(resourceLoader instanceof ResourcePatternResolver){
			ResourcePatternResolver resolver = (ResourcePatternResolver) resourceLoader;
			Resource[] resources;
			if (basePath == null) {
				resources = resolver.getResources(pattern);
			} else {
				if (basePath.endsWith(SLASH) || pattern.startsWith(SLASH)) {
					resources = resolver.getResources(basePath + pattern);
				} else {
					resources = resolver.getResources(basePath + SLASH + pattern);
				}
			}
			
			return resources;
		}else{
			throw new IllegalStateException(
			"Cannot create page template resources from patterns without a ResourceLoader configured that is a ResourcePatternResolver");
		}
	}
}
