package edu.uams.clara.webapp.protocol.mixin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties( { "person", "authorities"})
public interface UserJsonSerializationMixIn {

}
