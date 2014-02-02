package edu.uams.clara.webapp.common.security.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;

import edu.uams.clara.webapp.common.security.UserPasswordHelper;
/**
 * passwordEncoder can be either md5 or sha or custom,
 * you don't have to have a saltsource, but it has to be consistent with your authentication provider...
 * @author bianjiang
 *
 */
public class UserPasswordHelperImpl implements UserPasswordHelper {
	
	private PasswordEncoder passwordEncoder;
	private SaltSource saltSource;
	
	public UserPasswordHelperImpl(){
		
	}	

	@Override
	public String encryptPassword(String password, UserDetails userDetails){
		
		Object salt = null;
		
		if(this.saltSource != null){
			 salt = this.saltSource.getSalt(userDetails);
		}
		
		return passwordEncoder.encodePassword(password, salt);
	}
	
	@Override
	public boolean isPasswordValid(String rawPass, UserDetails userDetails) {
        Object salt = null;

        if (this.saltSource != null) {
            salt = this.saltSource.getSalt(userDetails);
        }        
        
        return passwordEncoder.isPasswordValid(userDetails.getPassword(), rawPass, salt);
    }

	@Autowired(required=true)
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}	

	@Autowired(required=false)
	public void setSaltSource(SaltSource saltSource) {
		this.saltSource = saltSource;
	}

	public SaltSource getSaltSource() {
		return saltSource;
	}
	
}
