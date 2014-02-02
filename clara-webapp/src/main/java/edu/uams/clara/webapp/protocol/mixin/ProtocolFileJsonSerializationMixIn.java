package edu.uams.clara.webapp.protocol.mixin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties( { "user", "protocol", "parent"})
public interface ProtocolFileJsonSerializationMixIn {
	
	@JsonProperty("userId") long getUserId();
	@JsonProperty("protocolId") long getProtocolId();
	@JsonProperty("parentProtocolFileId") long getParentProtocolFileId();

}
