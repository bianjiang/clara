package edu.uams.clara.webapp.protocol.domain.budget.code;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProcedureWrapper implements Serializable, Comparable<ProcedureWrapper> {

	private static final long serialVersionUID = 3873968874255693152L;	
	
	private CPTCode cptCode;
	
	private List<PhysicianChargeProcedure> physicianProcedures = new ArrayList<PhysicianChargeProcedure>();
	
	private HospitalChargeProcedure hospitalProcedure;
	
	@Override
	public int compareTo(ProcedureWrapper o) {
		return this.cptCode.getCode().compareTo(o.getCptCode().getCode());
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = (int) (31 * hash + this.cptCode.getCode().hashCode());
		return hash;
	}

	@Override
	public boolean equals(Object aThat) {
		if (this == aThat)
			return true;
		if (!(aThat instanceof ProcedureWrapper))
			return false;

		ProcedureWrapper that = (ProcedureWrapper) aThat;
		return (this.cptCode.getCode().equals(that.getCptCode().getCode()));

	}
	
	public void setCptCode(CPTCode cptCode) {
		this.cptCode = cptCode;
	}

	public CPTCode getCptCode() {
		return cptCode;
	}
	
	public void setPhysicianProcedures(List<PhysicianChargeProcedure> physicianProcedures) {
		this.physicianProcedures = physicianProcedures;
	}

	public List<PhysicianChargeProcedure> getPhysicianProcedures() {
		return physicianProcedures;
	}

	public void setHospitalProcedure(HospitalChargeProcedure hospitalProcedure) {
		this.hospitalProcedure = hospitalProcedure;
	}

	public HospitalChargeProcedure getHospitalProcedure() {
		return hospitalProcedure;
	}
}
