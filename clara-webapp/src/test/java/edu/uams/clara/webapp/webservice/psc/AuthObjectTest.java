package edu.uams.clara.webapp.webservice.psc;

public class AuthObjectTest {
	private String _SSOSessionID = "";
	private String _ltValue = "";
	private String _UserName = "";
	public String get_UserName() {
		return _UserName;
	}
	public void set_UserName(String _UserName) {
		this._UserName = _UserName;
	}
	public String get_Password() {
		return _Password;
	}
	public void set_Password(String _Password) {
		this._Password = _Password;
	}
	private String _Password = "";
	
	public String get_SSOSessionID() {
		return _SSOSessionID;
	}
	public void set_SSOSessionID(String _SSOSessionID) {
		this._SSOSessionID = _SSOSessionID;
	}
	public String get_ltValue() {
		return _ltValue;
	}
	public void set_ltValue(String _ltValue) {
		this._ltValue = _ltValue;
	}
	public AuthObjectTest(String aSSO, String altValue){
		_SSOSessionID = aSSO;
		_ltValue = altValue;
	}
}
