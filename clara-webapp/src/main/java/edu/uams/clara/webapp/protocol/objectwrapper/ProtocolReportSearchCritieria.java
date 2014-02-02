package edu.uams.clara.webapp.protocol.objectwrapper;

import java.util.Date;

public class ProtocolReportSearchCritieria {
	public static enum ProtocolReportSearchField{
		PI, STAFF, STUDY_TYPE, DEPARTMENT, COLLEGE, SUB_DEPARTMENT;
	}
	
	private ProtocolReportSearchField protocolReportSearchField;
	private String keyword;
	private Date startDate;
	private Date endDate;
	private String studyType;
	private String detailedOrNot;

	public ProtocolReportSearchField getProtocolReportSearchField() {
		return protocolReportSearchField;
	}

	public void setProtocolReportSearchField(ProtocolReportSearchField protocolReportSearchField) {
		this.protocolReportSearchField = protocolReportSearchField;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getStudyType() {
		return studyType;
	}

	public void setStudyType(String studyType) {
		this.studyType = studyType;
	}

	public String getDetailedOrNot() {
		return detailedOrNot;
	}

	public void setDetailedOrNot(String detailedOrNot) {
		this.detailedOrNot = detailedOrNot;
	}
}
