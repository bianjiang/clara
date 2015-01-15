package edu.uams.clara.integration.outgoing.ctms.domain;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;


@Entity
@Table(name="aria_users")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class AriaUser extends AbstractDomainEntity {

	private static final long serialVersionUID = -8809592078380896872L;

	@Column(name="sapID", length = 500)
	private String sapID;
	
	@Column(name="pi_serial")
	private long piSerial;
	
	@Column(name="userID", length=500)
	private String userID;
	
	@Column(name="username", length=500)
	private String username;
	
	@Column(name="lname", length = 500)
	private String lname;
	
	@Column(name="first", length = 500)
	private String first;
	
	@Column(name="mi", length = 500)
	private String mi;
	
	@Column(name="sex", length = 500)
	private String sex;
	
	@Column(name="prim_email", length = 500)
	private String primemail;
	
	@Column(name="prim_phone", length = 500)
	private String primphone;
	
	
	@Column(name="prim_slot", length = 500)
	private String primslot;
	
	@Column(name="prim_city", length = 500)
	private String primcity;
	
	@Column(name="prim_state", length = 500)
	private String primstate;
	
	@Column(name="prim_zip", length = 500)
	private String primzip;
	
	@Column(name="stat", length = 500)
	private String stat;
	
	@Column(name="va_status", length = 500)
	private String vastatus;
	
	@Column(name="col", length = 500)
	private String col;
	
	@Column(name="col_name", length = 500)
	private String colname;
	
	@Column(name="dept_name", length = 500)
	private String deptname;
	
	@Column(name="division_name", length = 500)
	private String divisionname;
	
	@Column(name="full_name", length = 500)
	private String fullname;
	

	public String getSapID() {
		return sapID;
	}

	public void setSapID(String sapID) {
		this.sapID = sapID;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public long getPiSerial() {
		return piSerial;
	}

	public void setPiSerial(long piSerial) {
		this.piSerial = piSerial;
	}

	public String getMi() {
		return mi;
	}

	public void setMi(String mi) {
		this.mi = mi;
	}

	public String getPrimemail() {
		return primemail;
	}

	public void setPrimemail(String primemail) {
		this.primemail = primemail;
	}

	public String getPrimphone() {
		return primphone;
	}

	public void setPrimphone(String primphone) {
		this.primphone = primphone;
	}

	public String getPrimslot() {
		return primslot;
	}

	public void setPrimslot(String primslot) {
		this.primslot = primslot;
	}


	public String getPrimcity() {
		return primcity;
	}

	public void setPrimcity(String primcity) {
		this.primcity = primcity;
	}

	public String getPrimstate() {
		return primstate;
	}

	public void setPrimstate(String primstate) {
		this.primstate = primstate;
	}

	public String getPrimzip() {
		return primzip;
	}

	public void setPrimzip(String primzip) {
		this.primzip = primzip;
	}

	public String getStat() {
		return stat;
	}

	public void setStat(String stat) {
		this.stat = stat;
	}

	public String getVastatus() {
		return vastatus;
	}

	public void setVastatus(String vastatus) {
		this.vastatus = vastatus;
	}

	public String getCol() {
		return col;
	}

	public void setCol(String col) {
		this.col = col;
	}

	public String getColname() {
		return colname;
	}

	public void setColname(String colname) {
		this.colname = colname;
	}

	public String getDeptname() {
		return deptname;
	}

	public void setDeptname(String deptname) {
		this.deptname = deptname;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getDivisionname() {
		return divisionname;
	}

	public void setDivisionname(String divisionname) {
		this.divisionname = divisionname;
	}
	

}
