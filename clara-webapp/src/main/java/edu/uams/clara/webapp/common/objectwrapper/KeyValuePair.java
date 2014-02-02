package edu.uams.clara.webapp.common.objectwrapper;

public class KeyValuePair implements Comparable<KeyValuePair> {
	
	public KeyValuePair(String key, String value){
		this.key = key;
		this.value = value;
	}
	
	private String key;
	
	private String value;

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}


	@Override
	public int compareTo(KeyValuePair o) {
		return this.key.compareTo(o.getKey());
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = (int) (31 * hash + this.key.hashCode());
		return hash;
	}

	@Override
	public boolean equals(Object aThat) {
		if (this == aThat)
			return true;
		if (!(aThat instanceof KeyValuePair))
			return false;

		KeyValuePair that = (KeyValuePair) aThat;
		return (this.key.equals(that.key) && this.value.equals(that.value));

	}

	
}
