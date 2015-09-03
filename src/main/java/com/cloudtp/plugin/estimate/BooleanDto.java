package com.cloudtp.plugin.estimate;

import java.util.List;

/**
 * TODO: This should be a dependency on the CloudTP Common libarary.
 * We should not be defining BooleanDto yet again
 */
public class BooleanDto {
	private final String dtoName = "boolean";
	private boolean result;
	private boolean errorFound;
	private List<String> messages;
	
	public boolean getResult() {
		return result;
	}
	public void setResult(boolean result) {
		this.result = result;
	}
	
	public boolean getErrorFound() {
		return errorFound;
	}
	public void setErrorFound(boolean errorFound) {
		this.errorFound = errorFound;
	}
	
	public List<String> getMessages() {
		return messages;
	}
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
}
