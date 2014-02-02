package edu.uams.clara.webapp.common.objectwrapper;

import java.util.List;

public class PagedList<T> {

	private long total;
	private int start;
	private int limit;
	
	private List<T> list;
	
	public void setStart(int start) {
		this.start = start;
	}

	public int getStart() {
		return start;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public List<T> getList() {
		return list;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getTotal() {
		return total;
	}
	
}
