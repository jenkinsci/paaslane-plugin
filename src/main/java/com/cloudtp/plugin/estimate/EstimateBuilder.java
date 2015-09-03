package com.cloudtp.plugin.estimate;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.EnvironmentList;
import hudson.model.TaskListener;
import hudson.model.AbstractProject;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.cloudtp.plugin.estimate.rest.RestConnection;

import javax.servlet.ServletException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link EstimateBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Alan Zall	
 */
public class EstimateBuilder extends Builder implements Serializable{
    private static final Logger LOG = Logger.getLogger(EstimateBuilder.class.getName());

    private static final String FAIL_REASON_NEVER = "never";
    private static final String FAIL_REASON_ALERT_COUNT_GREATER = "alertCountGreater";
    private static final int GENERATE_REPORT_MAX_RETRIES = 5;
    
    private boolean uploadSucceeded = true;
    private boolean autoEstimate;
    
    private final String name;
    private final String uri;
    private final String saasuri;
    private final String token;
    private final String archiveFilePath;
    private final String regWhichIncludedModules;
    private final String reportConfigName;
    private final String language;
    private final String regexExclude;
    private final Boolean testOnly;
	private final Long maxNumberOfViolations;
	private final Long maxNumberOfBlockerViolations;
	private final Long maxNumberOfImportantViolations;
	private final Long maxNumberOfWarningViolations;
	private final Long maxNumberOfOptimizationViolations;
	private final Boolean failBlockTotalVio;
	private final Boolean failBlockBlockerVio;
	private final Boolean failBlockImportantVio;
	private final Boolean failBlockWarningVio;
	private final Boolean failBlockOptimizationVio;
	

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public EstimateBuilder(String name, String token, String archiveFilePath, String regWhichIncludedModules, String reportConfigName, String uri, String saasuri, String language, String regexExclude, Boolean testOnly, 
    						Long maxNumberOfViolations, Boolean failBlockTotalVio, 
    						Long maxNumberOfBlockerViolations, Boolean failBlockBlockerVio, 
    						Long maxNumberOfImportantViolations, Boolean failBlockImportantVio, 
    						Long maxNumberOfOptimizationViolations, Boolean failBlockOptimizationVio, 
    						Long maxNumberOfWarningViolations, Boolean failBlockWarningVio) {
        this.name = name;
        this.token = token;
        this.archiveFilePath = archiveFilePath;
        this.regWhichIncludedModules = regWhichIncludedModules;
        this.reportConfigName = reportConfigName;
        this.uri = uri;
        this.saasuri = saasuri;
        this.language = language;
        this.regexExclude = regexExclude;
        this.testOnly = testOnly;
        this.maxNumberOfViolations = maxNumberOfViolations;
    	this.maxNumberOfBlockerViolations = (maxNumberOfBlockerViolations == null ? 0L : maxNumberOfBlockerViolations);
    	this.maxNumberOfImportantViolations = (maxNumberOfImportantViolations == null ? 0L : maxNumberOfImportantViolations);
    	this.maxNumberOfWarningViolations = (maxNumberOfWarningViolations == null ? 0L : maxNumberOfWarningViolations);
    	this.maxNumberOfOptimizationViolations = (maxNumberOfOptimizationViolations == null ? 0L : maxNumberOfOptimizationViolations);
        this.failBlockTotalVio = failBlockTotalVio;
        this.failBlockBlockerVio = failBlockBlockerVio;
        this.failBlockImportantVio = failBlockImportantVio;
        this.failBlockWarningVio = failBlockWarningVio;
        this.failBlockOptimizationVio = failBlockOptimizationVio;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return name;
    }

    
    public String getToken() {
		return token;
	}

	public String getArchiveFilePath() {
		return archiveFilePath;
	}

	public String getRegWhichIncludedModules() {
		return regWhichIncludedModules;
	}

	public String getReportConfigName() {
		return reportConfigName;
	}

	public String getUri() {
		return uri;
	}

	public String getSaasuri() {
		return saasuri;
	}

	public String getLanguage() {
		return language;
	}

	public String getRegexExclude() {
		return regexExclude;
	}
	
	public Boolean getTestOnly() {
		return testOnly;
	}
	
	public Boolean getFailBlockTotalVio() {
		return failBlockTotalVio;
	}

	
	public Boolean getFailBlockBlockerVio() {
		return failBlockBlockerVio;
	}

	public Boolean getFailBlockImportantVio() {
		return failBlockImportantVio;
	}

	public Boolean getFailBlockWarningVio() {
		return failBlockWarningVio;
	}

	public Boolean getFailBlockOptimizationVio() {
		return failBlockOptimizationVio;
	}

	public Long getMaxNumberOfViolations() {
		return maxNumberOfViolations;
	}

	
	public Long getMaxNumberOfBlockerViolations() {
		return maxNumberOfBlockerViolations;
	}

	public Long getMaxNumberOfImportantViolations() {
		return maxNumberOfImportantViolations;
	}

	public Long getMaxNumberOfWarningViolations() {
		return maxNumberOfWarningViolations;
	}

	public Long getMaxNumberOfOptimizationViolations() {
		return maxNumberOfOptimizationViolations;
	}
	
	public boolean isUploadSucceeded() {
		return uploadSucceeded;
	}

	public void setUploadSucceeded(boolean uploadSucceeded) {
		this.uploadSucceeded = uploadSucceeded;
	}

	@Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		boolean result = true;
		
		AbstractProject project = build.getProject();
		
		// This is a solution for dropping the new plugin version on an existing workspace
		// Putting this code in the constructor was causing issues
		this.autoEstimate = true;
		if (this.reportConfigName != null && !this.reportConfigName.equals("") && !this.reportConfigName.equals("null")) {
			this.autoEstimate = false;
		}
		
		listener.getLogger().println("Project: " + project.getBuildDir().getAbsolutePath());
				
		SendFileCallable sendFilesTask = new SendFileCallable(listener);

        WriteMetricsFileCallable writePlotsFilesTask = new WriteMetricsFileCallable(listener);

        WaitForResultsCallable waitFForResultsTask = new WaitForResultsCallable(listener);

        
        writeOutPreamble(listener);
		
        try {
			result = build.getWorkspace().act(sendFilesTask);
			listener.getLogger().println("Pertinent files have been uploaded");
		} catch (IOException e) {
			listener.getLogger().println("Got IO exception, check logs...");
			LOG.log(Level.FINE, "Got IO exception, check logs...", e);
			result = false;
		} catch (InterruptedException e) {
			listener.getLogger().println("Got interupted, check logs...");
			LOG.log(Level.FINE, "Got interupted, check logs...", e);
			result = false;
		}

        if(result){
			Long applicationId = null;
			try {
				applicationId = launcher.getChannel().call(waitFForResultsTask);
			} catch (IOException e) {
				listener.getLogger().println("Got IO exception, check logs...");
				LOG.log(Level.FINE, "Got IO exception, check logs...", e);
				applicationId = null;
			} catch (InterruptedException e) {
				listener.getLogger().println("Got interupted, check logs...");
				LOG.log(Level.FINE, "Got interupted, check logs...", e);
				applicationId = null;
			}
			
			if(applicationId != null){
				
				try {
					EstimateSummary summaryObject = getSummaryData(applicationId);
					
					result = buildPasses(listener, summaryObject);

					writePlotsFilesTask.setSummaryObject(summaryObject);
					
					result = build.getWorkspace().act(writePlotsFilesTask);
					
					listener.getLogger().println("Successfully waited for the results to complete.");
				} catch (IOException e) {
					listener.getLogger().println("IO Exception, check the logs");
					LOG.log(Level.FINE, "IO Exception", e);
				} catch (InterruptedException e) {
					listener.getLogger().println("Exception, check the logs");
					LOG.log(Level.FINE, "Exception", e);
				}
			} else {
				listener.getLogger().println("Failed to wait for all of the results to complete.");
				result = false;
			}

		} else {
			listener.getLogger().println("Failed to send the modules to be processed. Verify that your uri endpoints and your token are correct and up to date.");
			result = false;
		}

	    listener.getLogger().println("Process complete" );


        return result;
    }

	/**
	 * @param listener
	 */
	private void writeOutPreamble(BuildListener listener) {
		// This also shows how you can consult the global configuration of the builder
		listener.getLogger().println("Name: " + name);
		listener.getLogger().println("Agent URI: " + uri);
		listener.getLogger().println("SaaS URI: " + saasuri);
		listener.getLogger().println("Token: " + token);
		listener.getLogger().println("File Path: " + archiveFilePath);
		listener.getLogger().println("Regular Expression for included modules: " + regWhichIncludedModules);
		listener.getLogger().println("Exclude Regular Expressions for included modules: " + regexExclude);
		listener.getLogger().println("Report Config: " + reportConfigName);
		listener.getLogger().println("Language: " + language);
		listener.getLogger().println("TestOnly: " + testOnly);
		if(failBlockTotalVio != null && failBlockTotalVio )
			listener.getLogger().println("Fail when total vios > : " + maxNumberOfViolations);
		if(failBlockBlockerVio != null && failBlockBlockerVio )
			listener.getLogger().println("Fail when blocker vios > : " + maxNumberOfBlockerViolations);
		if(failBlockImportantVio != null && failBlockImportantVio )
			listener.getLogger().println("Fail when important vios > : " + maxNumberOfImportantViolations);
		if(failBlockOptimizationVio != null && failBlockOptimizationVio )
			listener.getLogger().println("Fail when optimization vios > : " + maxNumberOfOptimizationViolations);
		if(failBlockWarningVio != null && failBlockWarningVio )
			listener.getLogger().println("Fail when warning vios > : " + maxNumberOfWarningViolations);
	}

	private boolean buildPasses(BuildListener listener, EstimateSummary summ) {
		boolean result = true;
		
		if(summ==null){
			listener.getLogger().println("Unble to get estimation results back.");
			result = false;
		} else {
			
			if(failBlockTotalVio != null && failBlockTotalVio){
				if(summ.getNumOfViolations() <= maxNumberOfViolations){
					listener.getLogger().println("Total number of alerts(" + summ.getNumOfViolations() + ") are within configured limits.");
				} else {
					listener.getLogger().println("Total number of alerts(" + summ.getNumOfViolations() + ") exceeds configured limits(" + maxNumberOfViolations + ").");
					result = false;
				}
			}
			
			if(failBlockBlockerVio != null && failBlockBlockerVio){
				if(summ.getBlockerViolations() <= maxNumberOfBlockerViolations){
					listener.getLogger().println("Number of blocker alerts(" + summ.getBlockerViolations() + ") are within configured limits.");
				} else {
					listener.getLogger().println("Number of blocker alerts(" + summ.getBlockerViolations() + ") exceeds configured limits(" + maxNumberOfBlockerViolations + ").");
					result = false;
				}
			}
			
			if(failBlockImportantVio != null && failBlockImportantVio){
				if(summ.getImportantViolations() <= maxNumberOfImportantViolations){
					listener.getLogger().println("Number of important alerts(" + summ.getImportantViolations() + ") are within configured limits.");
				} else {
					listener.getLogger().println("Number of important alerts(" + summ.getImportantViolations() + ") exceeds configured limits(" + maxNumberOfImportantViolations + ").");
					result = false;
				}
			}
			
			if(failBlockOptimizationVio != null && failBlockOptimizationVio){
				if(summ.getOptimizationViolations() <= maxNumberOfOptimizationViolations){
					listener.getLogger().println("Number of optimization alerts(" + summ.getOptimizationViolations() + ") are within configured limits.");
				} else {
					listener.getLogger().println("Number of optimization alerts(" + summ.getOptimizationViolations() + ") exceeds configured limits(" + maxNumberOfOptimizationViolations + ").");
					result = false;
				}
			}
			
			if(failBlockWarningVio != null && failBlockWarningVio){
				if(summ.getWarningViolations() <= maxNumberOfWarningViolations){
					listener.getLogger().println("Number of warning alerts(" + summ.getWarningViolations() + ") are within configured limits.");
				} else {
					listener.getLogger().println("Number of warning alerts(" + summ.getWarningViolations() + ") exceeds configured limits(" + maxNumberOfWarningViolations + ").");
					result = false;
				}
			}
			
		}	
		
		return result;
	}

	private Boolean savePlotMetrics(File workspace, BuildListener listener, EstimateSummary summ){
		Boolean result = true;
		if(summ==null){
			result = false;
		} else {
			String filePath = workspace.getAbsolutePath() + File.separator + "paaslane-metrics" + File.separatorChar;

			listener.getLogger().println("Writing out statistics to \"" + filePath + "\".");

			try
			{
				File metricsDir = new File(filePath);
				metricsDir.mkdirs();
				// Effort Metrics
				writeMetricFile(filePath + "blockerCtp.properties", summ.getBlockerCtp().toPlainString() );
				writeMetricFile(filePath + "importantCtp.properties", summ.getImportantCtp().toPlainString() );
				writeMetricFile(filePath + "warningCtp.properties", summ.getWarningCtp().toPlainString() );
				writeMetricFile(filePath + "optimizationCtp.properties", summ.getOptimizationCtp().toPlainString() );
				writeMetricFile(filePath + "totalCtp.properties", summ.getTotalCtp().toPlainString() );
				
				// Alert Metrics
				writeMetricFile(filePath + "blockerViolations.properties", summ.getBlockerViolations() );
				writeMetricFile(filePath + "importantViolations.properties", summ.getImportantViolations() );
				writeMetricFile(filePath + "warningViolations.properties", summ.getWarningViolations() );
				writeMetricFile(filePath + "optimizationViolations.properties", summ.getOptimizationViolations() );
				writeMetricFile(filePath + "numOfViolations.properties", summ.getNumOfViolations() );
			}
			catch(IOException ioe)
			{
				listener.getLogger().println("Problem writing out the estimate summary results.");
				result = false;
			}
			catch(SecurityException se)
			{
				listener.getLogger().println("Problem with access when writing out the estimate summary results.");
				result = false;
			}
			catch(Exception e)
			{
				listener.getLogger().println("Problem parsing the estimate summary results.");
				result = false;
			}
		}
		return result;
	}

	public static void writeMetricFile(String fileName, Object numberValue) throws IOException {
		
		File metricFile = new File(fileName);
		if (!metricFile.exists()) {
			metricFile.createNewFile();
		}
		
		FileWriter fw = new FileWriter(fileName);
	 
		if(numberValue==null)
			numberValue = "0";//default to zero.
		fw.write("YVALUE=" + numberValue);
	 
		fw.close();
	}

	private EstimateSummary getSummaryData(Long applicationId) {
		EstimateSummary summ = null;
		
		RestConnection connection = new RestConnection(getSaasuri(), getToken());
		summ = connection.getLastEstimateSummary(applicationId);

		connection = null;
		return summ;
	}

	private Long waitForResults(BuildListener listener) {
		ApplicationStats stats = null;
		boolean done = false;
		int retryCount = 0;
		Long applicationId = null;
		Long lastCount = Long.MAX_VALUE, count;
		
		RestConnection connection = new RestConnection(getSaasuri(), getToken());
		do{
			try{
				stats = connection.getApplicationStatistics(getName());
				if(stats == null  
						|| (stats.getLastProfileDate() == null)
						|| (stats.getLastEstimateDate() == null)
						|| (stats.getNumRunningProfiles() > 0)
						|| (!stats.getLastProfileDate().before(stats.getLastEstimateDate()))
				   ){
					if(stats == null){
						if(listener != null) listener.getLogger().println("Waiting to start profiling.");
					} else if((count = stats.getNumRunningProfiles()) > 0){
						if(count != lastCount){
							if(listener != null) listener.getLogger().println("Number of profiling tasks remaining: " + stats.getNumRunningProfiles());
							lastCount = count;
						}
					} else if (stats.getLastProfileDate() == null 
								|| stats.getLastEstimateDate() == null
								|| !stats.getLastProfileDate().before(stats.getLastEstimateDate())){
						if (autoEstimate) {
							if(listener != null) listener.getLogger().println("Waiting for estimate report to be generated: ");
						} else {
							if (retryCount < GENERATE_REPORT_MAX_RETRIES) {
								if(listener != null) listener.getLogger().println("Generating estimate report... ");
								connection.createReport(getName(), getReportConfigName(), getName() + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new Date()));
								retryCount++;
							} else {
								if(listener != null) listener.getLogger().println("Failed to generate report. Check report config name");
								done = true;
								applicationId = null;
							}
						}
					}
					Thread.sleep(30 * 1000);
				} else {
					applicationId = stats.getId();
					done = true;
				}
				
			}catch(InterruptedException e ){
				done = true;
				applicationId = null;
			}
		}while(!done);

		connection = null;
		return applicationId;
	}

	protected boolean sendTheFiles(File workspace, BuildListener listener){
	    // RestConnection connection = new RestConnection(uri, token);
	    String startPath = buildStartPath(workspace);

	    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
	    taskExecutor.setCorePoolSize(2);
	    taskExecutor.setMaxPoolSize(5);
	    taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
	    taskExecutor.initialize();
	    
	    if(listener != null) listener.getLogger().println("Reviewing from " + startPath + " directory.");
	    
	    List<Pattern> patterns = new ArrayList<Pattern>();
	    
	    for(String patt : regexExclude.split(",")){
	    	patterns.add(Pattern.compile(patt.trim()));
	    }
	    
	    DirectoryScanner scanner = new DirectoryScanner();
	    scanner.setIncludes(new String[]{regWhichIncludedModules});
		scanner.setBasedir(startPath );
	    scanner.setCaseSensitive(false);
	    scanner.scan();
	    
	    String[] files = scanner.getIncludedFiles();

	    if(listener != null) listener.getLogger().println("Reviewing " + files.length + " files.");

		UploadModuleTask nextTask = null, currentTask = null;

	    
	    boolean process;
	    int numFiles = 0;
	    for(String path : files){
			if(testOnly){
				if(listener != null) listener.getLogger().println("Checking " + path);
			}
			process = true;
	    	for(Pattern pat : patterns){
 	    		if(pat.matcher(path).matches()){
 	    			process = false;
 	    			break;
 	    		}
	    	}
	    	if(process){
				if(testOnly){
					if(listener != null) listener.getLogger().println("\tWould have sent " + path);
				} else {
		    		numFiles++;
		    		nextTask = currentTask;
		    		currentTask = new UploadModuleTask(name, startPath, path, language, uri, token, false);
					if(nextTask != null){
						taskExecutor.execute(nextTask);
					}
				}
	    	}
	    }
	    
	    if(currentTask != null){
	    	currentTask.setEstimate(autoEstimate);
	    	taskExecutor.execute(currentTask);
	    }
	    if(listener != null) listener.getLogger().println("Queued up " + numFiles  + " items"  );

	    int numLeft, prev = 0; 
	    while( (numLeft = (taskExecutor.getThreadPoolExecutor().getQueue().size() + 
	    					taskExecutor.getThreadPoolExecutor().getActiveCount())) > 0){
	    	try {
	    		if(prev != numLeft){
	    			if(listener != null) listener.getLogger().println(numLeft  + " more to process..." );
	    			prev = numLeft;
	    		}
				Thread.sleep(30 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }

	    if(listener != null) listener.getLogger().println("Sent up " + taskExecutor.getThreadPoolExecutor().getCompletedTaskCount()  + " items"  );

		boolean result = isUploadSucceeded() && (taskExecutor.getThreadPoolExecutor().getCompletedTaskCount() == numFiles);

	    taskExecutor.shutdown();
	    
	    return result;

	}

	private String buildStartPath(File workspace) {
		String result;
		if(archiveFilePath == null || archiveFilePath.trim().length() == 0){
			result = workspace.getAbsolutePath();
		} else {
			if(archiveFilePath.startsWith(File.separator)){
				result = archiveFilePath;
			} else {
				String front = workspace.getAbsolutePath();
				if(!front.endsWith(File.separator)){
					front = front + File.separator;
				}
				result = front + archiveFilePath; 
			}
		}
		return result;
	}
	
    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    private class SendFileCallable implements FileCallable<Boolean>{
    	private BuildListener listener;

    	public SendFileCallable(BuildListener listener){
    		this.listener = listener;
    	}

    	public Boolean invoke(File workspace, VirtualChannel channel) throws IOException{ 
    		listener.getLogger().println("Sending workspace from " + workspace.getAbsolutePath());
    		return sendTheFiles(workspace, listener);
    	}
    };

    private class WriteMetricsFileCallable implements FileCallable<Boolean>{
    	private BuildListener listener;
		private EstimateSummary summaryObject;

    	public WriteMetricsFileCallable(BuildListener listener){
    		this.listener = listener;
    		this.summaryObject = summaryObject;
    	}

    	public EstimateSummary getSummaryObject() {
			return summaryObject;
		}

		public void setSummaryObject(EstimateSummary summaryObject) {
			this.summaryObject = summaryObject;
		}

		public Boolean invoke(File workspace, VirtualChannel channel) throws IOException{ 
    		return savePlotMetrics(workspace, listener, summaryObject);
    	}
    };

    private class WaitForResultsCallable implements Callable<Long, IOException>{
    	private BuildListener listener;

    	public WaitForResultsCallable(BuildListener listener){
    		this.listener = listener;
    	}
    	
    	public Long call() throws IOException{
    		return waitForResults(listener);
    	}
    };


    
    
    private class UploadModuleTask implements Runnable{
    	String applicationName;
    	String archivePath;
    	String filePath;
    	String lang;
    	String localUri;
    	String localToken;
    	boolean estimate;

    	
    	
		public UploadModuleTask(String applicationName, String archivePath,
				String filePath, String lang, String localUri, String localToken, boolean estimate) {
			super();
			this.applicationName = applicationName;
			this.archivePath = archivePath;
			this.filePath = filePath;
			this.lang = lang;
			this.localUri = localUri;
			this.localToken = localToken;
			this.estimate = estimate;
		}



		public void setEstimate(boolean estimate) {
			this.estimate = estimate;
		}



		public void run() {
			LOG.fine("Sending file \"" + filePath + "\".");

			boolean result;
			int index = 0;
			int returnVal;
			
			do{
				RestConnection connection = new RestConnection(localUri, localToken);
				returnVal = connection.estimateApplication(applicationName, archivePath, filePath, lang, estimate);
				
				result = (returnVal == RestConnection.SUCCESS);
				
				if(keepTrying(index, returnVal)){
					LOG.fine("Retrying archive \"" + archivePath + "\" file \"" + filePath + "\" for the  \"" + index + "\" time with resulting " + returnVal + ".");
				}
			}while(keepTrying(index++, returnVal));

			if(result){
				LOG.fine("Sent file \"" + filePath + "\" resulting in a response of \"" + result + "\"");
			} else {
				setUploadSucceeded(false);
				LOG.fine("Failed to send file \"" + filePath + "\" resulting in a response of \"" + result + "\"");
			}
			
			
		}



		/**
		 * @param index
		 * @param returnVal
		 * @return
		 */
		private boolean keepTrying(int index, int returnVal) {
			boolean result = (
								((returnVal == RestConnection.FAILURE) && (index < 5)) ||	
								((returnVal == RestConnection.NO_AUTH) && (index < 2))
							  );
			
			return result;
		}
    	
    	
    }
    
    /**
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
    	
    	/**
    	 * Whether the token is authorized.
    	 */
    	private boolean isAuthorized;
    	
    	/**
    	 * The a list of report configuration names.
    	 */
    	private org.json.JSONObject reportConfigsDto;
    	
        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }
        
        /**
         * Performs on-the-fly validation of the form field 'uri'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckUri(@QueryParameter String value)
                throws IOException, ServletException {
            if (!RestConnection.validateRestUri(value))
                return FormValidation.error("The URL is not a well formed URL");
            return FormValidation.ok();
        }
        
        /**
         * Performs on-the-fly validation of the form field 'saasuri'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckSaasuri(@QueryParameter String value)
                throws IOException, ServletException {
            if (!RestConnection.validateRestUri(value))
                return FormValidation.error("The SaaS URL is not a well formed URL");
            return FormValidation.ok();
        }
        
        /**
         * Fills in report configuration items.
         * @return a `ListBoxModel` to populate report configurations.
         */
        public ListBoxModel doFillReportConfigNameItems(
        		@QueryParameter("token") String token,
        		@QueryParameter("saasuri") String saasURI,
        		@QueryParameter("language") String language)
        	throws JSONException
        {
        	ListBoxModel items;
        	org.json.JSONArray configsListDto;
        	org.json.JSONObject configDto;
        	
        	if(!isAuthorized || reportConfigsDto == null) {
        		return new ListBoxModel();
        	}
        	
        	items = new ListBoxModel();
        	configsListDto = reportConfigsDto.getJSONArray("list");
    
        	// Jenkins represents language as 'java' or 'csharp'
        	// The DTO will represent language as 'Java' or 'C#" or 'Python'
        	String translatedLanguage = language.equals("java") ? "Java" : "C#";
        	
        	for(int i=0; i < configsListDto.length(); i++) {
        		configDto = configsListDto.getJSONObject(i);
        		JSONArray rulesetTemplateRankings = configDto.getJSONObject("rulesetTemplateDto").getJSONArray("ruleSetTemplateRankings");
        		for (int j = 0; j < rulesetTemplateRankings.length(); j++) {
        			if (rulesetTemplateRankings.getJSONObject(j).getString("language").equals(translatedLanguage)) {
        				items.add(configDto.getString("name"));
        				break;
        			}
        		}
        	}
        	return items;
        }
        
        /**
         * Validates the report configuration drop-down menu
         * by checking if an authorized token is being used.
         * @return
         */
        public FormValidation doCheckReportConfigName(
        		@QueryParameter("token") String token,
        		@QueryParameter("saasuri") String saasURI)
        	throws IOException, JSONException
        {
        	RestConnection conn;
        	     	
        	if(isAuthorized) {
        		return FormValidation.ok();
            }
        	
        	conn = new RestConnection(saasURI, token);
        	reportConfigsDto = conn.getReportConfigs();

        	if(reportConfigsDto != null) {
        		setAuthorized(true);
        		return FormValidation.ok();
            }
        	
    		return FormValidation.warning("Enter an authorized token to run automatic reporting. Then refresh this page.");
        }
        
        /**
         * Performs on-the-fly validation of the form field 'token'.
         * It establishes a REST connection and queries the status
         * To assert the given token is valid.
         * 
         * @param token
         *      This parameter receives the value that the user has typed in the token field.
         * @param saasURI
         * 		This parameter receives the value that the user has typed in the 'saasuri' field.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         * 		
         * 
         * @see doCheckTokenEmpty for form validation (check for required field).
         */
        public FormValidation doCheckToken(
        		@QueryParameter("token") String token,
        		@QueryParameter("saasuri") String saasURI)
        	throws IOException, ServletException, JSONException
        {
        	RestConnection conn;
        	
        	if (!RestConnection.validateToken(token)) {
        		setAuthorized(false);
                return FormValidation.error("The token must be provided.");
        	}
        	
        	conn = new RestConnection(saasURI, token);
        	reportConfigsDto = conn.getReportConfigs();
        	
        	if(reportConfigsDto == null) {
        		setAuthorized(false);
        		return FormValidation.error("The token is not authorized.");
        	}
        	
        	setAuthorized(true);
    		return FormValidation.ok();
        }
        
        /**
         * Sets the authorized state.
         * @param isAuthorized whether the token is authorized. 
         */
        public void setAuthorized(boolean isAuthorized) {
        	if(!isAuthorized) {
        		reportConfigsDto = null;
        	}
        	this.isAuthorized = isAuthorized;
        }
         
        /**
         * Performs on-the-fly validation of the form field 'regWhichIncludedModules'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckRegWhichIncludedModules(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a regular expression");
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'maxNumberOfViolations'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckMaxNumberOfViolations(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() > 0){
            	try{
            		if(Long.valueOf(value) < 0L){
                        return FormValidation.error("Please provide a non-negative number of maximum allowable alerts.");
            		}
            	}catch (NumberFormatException e ){
                    return FormValidation.error("Please provide a maximum number of allowable alerts.");
            	}
            }
            
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "PaaSLane Estimation Request";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().

            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }   
    }
}

