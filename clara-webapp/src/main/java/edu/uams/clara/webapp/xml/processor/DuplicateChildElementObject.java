package edu.uams.clara.webapp.xml.processor;

import org.w3c.dom.Element;

public class DuplicateChildElementObject {
	 private boolean needDuplicate = true;
	    private Element element = null;
	  
	    public DuplicateChildElementObject() {  
	        super();  
	    }  
	  
	    public boolean isNeedDuplicate() {  
	        return needDuplicate;  
	    }  
	  
	    public void setNeedDuplicate(boolean needDuplicate) {  
	        this.needDuplicate = needDuplicate;  
	    }  
	  
	    public Element getElement() {  
	        return element;  
	    }  
	  
	    public void setElement(Element element) {  
	        this.element = element;  
	    }  
}
