package edu.uams.clara.webapp.report.domain.result;

import java.math.BigInteger;

public class AuditReport {
	private long protocolId;
	private String protocolTitle;
	private String piName;
	private String currentStatus;
	
	public AuditReport(BigInteger protocolId, String protocolTitle, String piName, String currentStatus){
		this.protocolId = protocolId.longValue();
		this.protocolTitle = protocolTitle;
		this.piName = piName;
		this.currentStatus = currentStatus;
	}
	
	public long getProtocolId() {
		return protocolId;
	}
	public void setProtocolId(long protocolId) {
		this.protocolId = protocolId;
	}
	public String getProtocolTitle() {
		return protocolTitle;
	}
	public void setProtocolTitle(String protocolTitle) {
		this.protocolTitle = protocolTitle;
	}

	public String getCurrentStatus() {
		return currentStatus;
	}
	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}
	public String getPiName() {
		return piName;
	}
	public void setPiName(String piName) {
		this.piName = piName;
	}
}
