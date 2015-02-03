package com.cloudtp.plugin.estimate;

import java.util.List;

public class EstimateSummaryResponse {

	
	public EstimateSummaryResponse() {
		super();
	}

	
	private Boolean errorFound = false;
	private List<String> messages = null;
	private String dtoName;
	private List<EstimateSummary> list;
	private Boolean empty = true;
	
	public Boolean getErrorFound() {
		return errorFound;
	}
	public void setErrorFound(Boolean errorFound) {
		this.errorFound = errorFound;
	}
	public List<String> getMessages() {
		return messages;
	}
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	public String getDtoName() {
		return dtoName;
	}
	public void setDtoName(String dtoName) {
		this.dtoName = dtoName;
	}
	public Boolean getEmpty() {
		return empty;
	}
	public void setEmpty(Boolean empty) {
		this.empty = empty;
	}
	public List<EstimateSummary> getList() {
		return list;
	}
	public void setList(List<EstimateSummary> list) {
		this.list = list;
	}
	
}
