package edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums;

/**
 * this enum enlist the different type of constraints, some constraint check can have extra params,
 * the inner classes are used to define the names of the keys, which are also used as the key of the HashMap where the actual extra params are stored (Map<String, Object>)
 * @author h0cked
 *
 */
public enum ConstraintType {

	REQUIRED, DATE, NUMBER, NUMBERRANGE, LENGTH, CONTAINS, EQUAL, MEMBEROF, NOINTERSECT, UNIQUE, CONTAINSNA, NOTCONTAINS, NOTEQUAL, INTERSECT, NOTCONTAINSMULTIPLE;

	public static class NumberRange{
		public enum ParamKeys{
			MIN, MAX, UNIT, MINUNIT, MAXUNIT, UNITCONVERTER;
		}
	}


	public static class Contains{
		public enum ParamKeys{
			VALUE;
		}
	}
	
	public static class NotContainsMultiple{
		public enum ParamKeys{
			VALUE;
		}
	}

	public static class Date{
		public enum ParamKeys{
			DATA_FORMAT;
		}
	}

	public static class LENGTH{
		public enum ParamKeys{
			MIN, MAX;
		}
	}

	public static class Equal{
		public enum ParamKeys{
			VALUE;
		}
	}
	
	public static class NotEqual{
		public enum ParamKeys{
			VALUE;
		}
	}

	public static class MemberOf{
		public enum ParamKeys{
			VALUES;
		}
	}

	public static class NonIntersect{
		public enum ParamKeys{
			VALUES;
		}
	}
	
	public static class Intersect{
		public enum ParamKeys{
			VALUES;
		}
	}
	
	public static class Unique{
		public enum ParamKeys{
			VALUE;
		}
	}
	
	public static class ContainsNA{
		public enum ParamKeys{
			VALUE;
		}
	}
	
	public static class NotContains{
		public enum ParamKeys{
			VALUE;
		}
	}
}
