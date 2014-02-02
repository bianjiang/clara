package edu.uams.clara.webapp.common.objectwrapper.email;

public class EmailRecipient {
	
	public static enum RecipientType {
		INDIVIDUAL, GROUP;
	}
	
	public EmailRecipient(){
		
	}
	
	public EmailRecipient(RecipientType type, String desc, String address){
		this.type = type;
		this.desc = desc;
		this.address = address;
	}

	public EmailRecipient(RecipientType type, String desc){
		this.type = type;
		this.desc = desc;
	}
		
	private String desc;
	
	private String address;
	
	private RecipientType type;	

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public RecipientType getType() {
		return type;
	}

	public void setType(RecipientType type) {
		this.type = type;
	}
	
	public String getJsonString(){
		return "{\"address\":\""+ this.address +"\",\"type\":\""+ this.type +"\",\"desc\":\""+ this.desc +"\"}";
	}
}
