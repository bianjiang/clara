package edu.uams.clara.core.springmvc.extension.json;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class HibernateAwareObjectMapper extends ObjectMapper {
        /**
	 * 
	 */
	private static final long serialVersionUID = -2955337788046566317L;

		public HibernateAwareObjectMapper() {
                Hibernate4Module hm = new Hibernate4Module();
                registerModule(hm);
                
                configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }

        public void setPrettyPrint(boolean prettyPrint) {
                configure(SerializationFeature.INDENT_OUTPUT, prettyPrint);
        }
}