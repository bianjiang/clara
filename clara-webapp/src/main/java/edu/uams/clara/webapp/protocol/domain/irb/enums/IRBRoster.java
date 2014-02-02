package edu.uams.clara.webapp.protocol.domain.irb.enums;

public enum IRBRoster {
	WEEK_1("Week 1"), WEEK_2("Week 2"), WEEK_3("Week 3"), WEEK_4("Week 4");
	
	private String description;
	private IRBRoster(String description){
		this.description = description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	
}
