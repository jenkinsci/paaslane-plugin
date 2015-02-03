package com.cloudtp.plugin.estimate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class EstimateSummary implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5002684074162835452L;
	
	private Boolean errorFound = false;
	private List<String> messages = null;
	private String dtoName;

	private String applicationName;
	private String effortEstimationName;
	private BigDecimal totalCtp;
	private long numOfViolations;
	private long linesOfCode;	
	private Date createDate;
	
	private long optimizationViolations;
	private long warningViolations;
	private long importantViolations;
	private long blockerViolations;
	
	private BigDecimal optimizationCtp;
	private BigDecimal warningCtp;
	private BigDecimal importantCtp;
	private BigDecimal blockerCtp;
	
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
	
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	public String getEffortEstimationName() {
		return effortEstimationName;
	}
	public void setEffortEstimationName(String effortEstimationName) {
		this.effortEstimationName = effortEstimationName;
	}
	
	public BigDecimal getTotalCtp() {
		return totalCtp;
	}
	public void setTotalCtp(BigDecimal totalCtp) {
		if(totalCtp == null){
			this.totalCtp = BigDecimal.ZERO;
		} else {
			this.totalCtp = totalCtp;
		}
	}
	
	public long getNumOfViolations() {
		return numOfViolations;
	}
	public void setNumOfViolations(long numOfViolations) {
		this.numOfViolations = numOfViolations;
	}
	
	public long getLinesOfCode() {
		return linesOfCode;
	}
	public void setLinesOfCode(long linesOfCode) {
		this.linesOfCode = linesOfCode;
	}
	
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public long getOptimizationViolations() {
		return optimizationViolations;
	}
	public void setOptimizationViolations(long optimizationViolations) {
		this.optimizationViolations = optimizationViolations;
	}
	
	public long getWarningViolations() {
		return warningViolations;
	}
	public void setWarningViolationss(long warningViolations) {
		this.warningViolations = warningViolations;
	}
	
	public long getImportantViolations() {
		return importantViolations;
	}
	public void setImportantViolations(long importantViolations) {
		this.importantViolations = importantViolations;
	}
	
	public long getBlockerViolations() {
		return blockerViolations;
	}
	public void setBlockerViolations(long blockerViolations) {
		this.blockerViolations = blockerViolations;
	}
	
	public BigDecimal getOptimizationCtp() {
		return optimizationCtp;
	}
	public void setOptimizationCtp(BigDecimal infoCtp) {
		if(infoCtp == null){
			this.optimizationCtp = BigDecimal.ZERO;
		} else {
			this.optimizationCtp = infoCtp;
		}
	}
	
	public BigDecimal getWarningCtp() {
		return warningCtp;
	}
	public void setWarningCtp(BigDecimal minorCtp) {
		if(minorCtp == null){
			this.warningCtp = BigDecimal.ZERO;
		} else {
			this.warningCtp = minorCtp;
		}
	}

	public BigDecimal getImportantCtp() {
		return importantCtp;
	}
	public void setImportantCtp(BigDecimal majorCtp) {
		if(majorCtp == null){
			this.importantCtp = BigDecimal.ZERO;
		} else {
			this.importantCtp = majorCtp;
		}
	}

	public BigDecimal getBlockerCtp() {
		return blockerCtp;
	}
	public void setBlockerCtp(BigDecimal blockerCtp) {
		if(blockerCtp == null){
			this.blockerCtp = BigDecimal.ZERO;
		} else {
			this.blockerCtp = blockerCtp;
		}
	}
}
