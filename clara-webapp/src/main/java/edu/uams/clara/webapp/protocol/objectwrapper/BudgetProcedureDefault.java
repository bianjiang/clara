package edu.uams.clara.webapp.protocol.objectwrapper;

public class BudgetProcedureDefault {
	public class Defaults{
		private String billingNotes;

		public String getBillingNotes() {
			return billingNotes;
		}

		public void setBillingNotes(String billingNotes) {
			this.billingNotes = billingNotes;
		}
	}
	
	private String type;
	
	private String cptCode;
	
	private Defaults defaults;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCptCode() {
		return cptCode;
	}
	public void setCptCode(String cptCode) {
		this.cptCode = cptCode;
	}
	public Defaults getDefaults() {
		return defaults;
	}
	public void setDefaults(Defaults defaults) {
		this.defaults = defaults;
	}
}
