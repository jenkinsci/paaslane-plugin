package com.cloudtp.plugin.estimate;

import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;

import org.apache.tools.ant.DirectoryScanner;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.cloudtp.plugin.estimate.rest.RestConnection;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public class EstimateBuilder extends Builder {
    private static final Logger LOG = Logger.getLogger(EstimateBuilder.class.getName());

    private final String name;
    private final String uri;
    private final String token;
    private final String archiveFilePath;
    private final String regWhichIncludedModules;
    private final String ruleSets;
    private final String language;
    private final String regexExclude;
    private final Boolean testOnly;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public EstimateBuilder(String name, String token, String archiveFilePath, String regWhichIncludedModules, String ruleSets, String uri, String language, String regexExclude, Boolean testOnly) {
        this.name = name;
        this.token = token;
        this.archiveFilePath = archiveFilePath;
        this.regWhichIncludedModules = regWhichIncludedModules;
        this.ruleSets = ruleSets;
        this.uri = uri;
        this.language = language;
        this.regexExclude = regexExclude;
        this.testOnly = testOnly;
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

	public String getRuleSets() {
		return ruleSets;
	}

	public String getUri() {
		return uri;
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

	@Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // This also shows how you can consult the global configuration of the builder
		listener.getLogger().println("Name: " + name);
		listener.getLogger().println("URI: " + uri);
		listener.getLogger().println("Token: " + token);
		listener.getLogger().println("File Path: " + archiveFilePath);
		listener.getLogger().println("Regular Expression for included modules: " + regWhichIncludedModules);
		listener.getLogger().println("Exclude Regular Expressions for included modules: " + regexExclude);
		listener.getLogger().println("List of Rule Sets: " + ruleSets);
		listener.getLogger().println("Language: " + language);
		listener.getLogger().println("TestOnly: " + testOnly);
		
	    // RestConnection connection = new RestConnection(uri, token);
	    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
	    taskExecutor.setCorePoolSize(2);
	    taskExecutor.setMaxPoolSize(5);
	    taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
	    taskExecutor.initialize();
	    
	    
	    List<Pattern> patterns = new ArrayList<Pattern>();
	    
	    for(String patt : regexExclude.split(",")){
	    	patterns.add(Pattern.compile(patt.trim()));
	    }
	    
	    DirectoryScanner scanner = new DirectoryScanner();
	    scanner.setIncludes(new String[]{regWhichIncludedModules});
	    scanner.setBasedir(archiveFilePath);
	    scanner.setCaseSensitive(false);
	    scanner.scan();
	    
	    String[] files = scanner.getIncludedFiles();

	    listener.getLogger().println("Reviewing " + files.length + " files.");

		UploadModuleTask nextTask = null, currentTask = null;

	    
	    boolean process;
	    int numFiles = 0;
	    for(String path : files){
			if(testOnly){
				listener.getLogger().println("Checking " + path);
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
					listener.getLogger().println("\tWould have sent " + path);
				} else {
		    		numFiles++;
		    		nextTask = currentTask;
		    		currentTask = new UploadModuleTask(name, archiveFilePath, path, language, uri, token, false);
					if(nextTask != null){
						taskExecutor.execute(nextTask);
					}
				}
	    	}
	    }
	    
	    if(currentTask != null){
	    	currentTask.setEstimate(true);
	    	taskExecutor.execute(currentTask);
	    }
		listener.getLogger().println("Queued up " + numFiles  + " items"  );

	    int numLeft; 
	    while( (numLeft = (taskExecutor.getThreadPoolExecutor().getQueue().size() + 
	    					taskExecutor.getThreadPoolExecutor().getActiveCount())) > 0){
	    	try {
				listener.getLogger().println(numLeft  + " more to process..." );
				Thread.sleep(30 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

		listener.getLogger().println("Sent up " + taskExecutor.getThreadPoolExecutor().getCompletedTaskCount()  + " items"  );

		boolean result = (taskExecutor.getThreadPoolExecutor().getCompletedTaskCount() == numFiles);

	    taskExecutor.shutdown();

	    listener.getLogger().println("All modules sent" );


        return result;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

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
			
			do{
				RestConnection connection = new RestConnection(localUri, localToken);
				result = connection.estimateApplication(applicationName, archivePath, filePath, lang, estimate);
				if((result == false) && (index < 5)){
					LOG.fine("Retrying file \"" + filePath + "\" for the  \"" + index + "\" time.");
				}
				index++;
			}while((result == false) && (index < 5));

			LOG.fine("Sent file \"" + filePath + "\" resulting in a response of \"" + result + "\"");
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
         * Performs on-the-fly validation of the form field 'token'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckToken(@QueryParameter String value)
                throws IOException, ServletException {
            if (!RestConnection.validateToken(value))
                return FormValidation.error("The token must be provided");
            return FormValidation.ok();
        }
         
        /**
         * Performs on-the-fly validation of the form field 'archiveFilePath'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckArchiveFilePath(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a Path to Artifact");
            return FormValidation.ok();
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

