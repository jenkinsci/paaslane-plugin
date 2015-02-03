package com.cloudtp.plugin.estimate;

import java.util.Date;
import java.util.List;

public class ApplicationStats {

	private Boolean errorFound = false;
	private List<String> messages = null;
	private String dtoName;
	private Long id;
	private Long customerId;
	private String name;
	private int version;
	private Boolean hasestimate = false;
	private Date lastEstimateDate = null;
	private Boolean hasRunningProfiles = false;
	private Boolean hasProfiles = false; 
	private Date lastProfileDate = null;
	private Long numRunningProfiles;
	
	private Long estimateCount;
	private Long moduleCount;
	
	private Boolean isAdminUser = false;
	
	private Date lastReportSuccessDate;
	
	private Date lastReportCompletedDate;

	private Date latestUploadedModuleDate;

	private Long modulesWithRunningProfiles;
	
	private Long modulesWithCancelledProfiles;

	private Long modulesWithCompleteProfiles;
	
	private Long modulesWithFailedProfiles;

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Boolean getHasestimate() {
		return hasestimate;
	}

	public void setHasestimate(Boolean hasestimate) {
		this.hasestimate = hasestimate;
	}

	public Boolean getHasRunningProfiles() {
		return hasRunningProfiles;
	}

	public void setHasRunningProfiles(Boolean hasRunningProfiles) {
		this.hasRunningProfiles = hasRunningProfiles;
	}
	
	public Boolean getHasProfiles() {
		return hasProfiles;
	}
	
	public void setHasProfiles(Boolean hasProfiles) {
		this.hasProfiles = hasProfiles;
	}
	
	public Long getEstimateCount() {
		return this.estimateCount;
	}
	
	public void setEstimateCount(Long estCount) {
		this.estimateCount = estCount;
	}
	    
	public Long getNumRunningProfiles() {
		return numRunningProfiles;
	}

	public void setNumRunningProfiles(Long numRunningProfiles) {
		this.numRunningProfiles = numRunningProfiles;
	}

	public Long getModuleCount() {
		return moduleCount;
	}    
	
	public void setModuleCount(Long moduleCount) {
		this.moduleCount =moduleCount;
	}

	public Date getLastEstimateDate() {
		return lastEstimateDate;
	}

	public void setLastEstimateDate(Date lastEstimateDate) {
		this.lastEstimateDate = lastEstimateDate;
	}

	public Date getLastProfileDate() {
		return lastProfileDate;
	}

	public void setLastProfileDate(Date lastProfileDate) {
		this.lastProfileDate = lastProfileDate;
	}

	public Boolean getIsAdminUser() {
		return isAdminUser;
	}

	public void setIsAdminUser(Boolean isAdminUser) {
		this.isAdminUser = isAdminUser;
	}

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getLastReportSuccessDate() {
		return lastReportSuccessDate;
	}

	public void setLastReportSuccessDate(Date lastReportSuccessDate) {
		this.lastReportSuccessDate = lastReportSuccessDate;
	}

	public Date getLastReportCompletedDate() {
		return lastReportCompletedDate;
	}

	public void setLastReportCompletedDate(Date lastReportCompletedDate) {
		this.lastReportCompletedDate = lastReportCompletedDate;
	}

	public Date getLatestUploadedModuleDate() {
		return latestUploadedModuleDate;
	}

	public void setLatestUploadedModuleDate(Date latestUploadedModuleDate) {
		this.latestUploadedModuleDate = latestUploadedModuleDate;
	}

	public Long getModulesWithRunningProfiles() {
		return modulesWithRunningProfiles;
	}

	public void setModulesWithRunningProfiles(Long modulesWithRunningProfiles) {
		this.modulesWithRunningProfiles = modulesWithRunningProfiles;
	}

	public Long getModulesWithCancelledProfiles() {
		return modulesWithCancelledProfiles;
	}

	public void setModulesWithCancelledProfiles(Long modulesWithCancelledProfiles) {
		this.modulesWithCancelledProfiles = modulesWithCancelledProfiles;
	}

	public Long getModulesWithCompleteProfiles() {
		return modulesWithCompleteProfiles;
	}

	public void setModulesWithCompleteProfiles(Long modulesWithCompleteProfiles) {
		this.modulesWithCompleteProfiles = modulesWithCompleteProfiles;
	}

	public Long getModulesWithFailedProfiles() {
		return modulesWithFailedProfiles;
	}

	public void setModulesWithFailedProfiles(Long modulesWithFailedProfiles) {
		this.modulesWithFailedProfiles = modulesWithFailedProfiles;
	}  

	
}
