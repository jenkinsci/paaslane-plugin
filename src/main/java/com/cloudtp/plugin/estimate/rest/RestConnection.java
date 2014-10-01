package com.cloudtp.plugin.estimate.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import org.apache.commons.httpclient.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.MediaType;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class providing only the connection to the AppDynamics REST interface.
 * Checks all connection parameters and maintains the connection to the REST
 * interface.
 */
public class RestConnection {
  private static final String REST_AUTHORIZATION = "Authorization";
  private static final String REST_PARAM_ESTIMATE_ARTIFACT_PATH = null;

  private static final String REST_SEGMENT_ESTIMATE_REQUEST = "/restapi/estimate/estimateModule";

  private static final String PARAM_DEFAULT_ROLLUP = "false";
  private static final String PARAM_DEFAULT_OUTPUT = "JSON";

  private static final Logger LOG = Logger.getLogger(RestConnection.class.getName());
  
  private String parsedToken = null;
  
  private final ObjectMapper jsonMapper = new ObjectMapper();
  private final WebResource restResource;

  public RestConnection(final String restUri, final String token) {
    final String parsedRestUri = parseRestUri(restUri);
    
    parsedToken = parseToken(token);

    DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
    config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    Client restClient = ApacheHttpClient.create(config);
    restClient.setFollowRedirects(true);

    restResource = restClient.resource(parsedRestUri);
    restResource.header(REST_AUTHORIZATION, token);
    
  }

  public boolean estimateApplication(final String name, final String rootPath, final String artifactPath, final String language, boolean estimate){
	  boolean result = true;
	  
	  String combinedPath = rootPath + File.separator + artifactPath;

	    try {
	    	final File fileToUpload = new File(combinedPath);
	    	
	    	final FormDataMultiPart multiPart = new FormDataMultiPart();
	    	
	    	if(fileToUpload == null || !fileToUpload.exists()){
	    		result = false;
	    	} else {
	    		multiPart.field("applicationName", name);
	    		multiPart.field("moduleName", artifactPath);
	    		multiPart.field("languageProfileName", language);
	    		multiPart.field("autoEstimate", String.valueOf(estimate));
	    		
	    		
		    	multiPart.bodyPart(new FileDataBodyPart("archive", fileToUpload, MediaType.valueOf("application/zip")));
	
		    	ClientResponse response = restResource.path(REST_SEGMENT_ESTIMATE_REQUEST).
		            type(MediaType.MULTIPART_FORM_DATA_TYPE).
		            header(REST_AUTHORIZATION, parsedToken).
		            post(ClientResponse.class, multiPart);
	
		    	LOG.fine(String.format("Response from PaaSLane server ==> code: %s", response.getStatus()));
		    	result = (response.getStatus() == HttpStatus.SC_OK);
	    	}
	      } catch (Exception e) {
	        LOG.log(Level.INFO, "Some problem estimating " + combinedPath + " with the PaaSLane REST interface, " +
	            "see stack-trace for more information", e);
	        result = false;
	      }
	    
	  return result;
  }

/*  public MetricData fetchMetricData(final String metricPath, int durationInMinutes, long buildStartTime) {
    String encodedMetricPath = encodeRestSegment(metricPath);
    MultivaluedMap<String, String> paramMap = new MultivaluedMapImpl();
    paramMap.add(REST_PARAM_METRIC_PATH, encodedMetricPath);

    if (buildStartTime > 0) {
      paramMap.add(REST_PARAM_TIME_RANGE_TYPE, PARAM_TIME_RANGE_TYPE_AFTER_TIME);
      paramMap.add(REST_PARAM_START_TIME, Long.toString(buildStartTime));
    } else {
      paramMap.add(REST_PARAM_TIME_RANGE_TYPE, PARAM_TIME_RANGE_TYPE_BEFORE_NOW);
    }
    paramMap.add(REST_PARAM_DURATION_IN_MINS, Integer.toString(durationInMinutes));
    paramMap.add(REST_PARAM_ROLLUP, PARAM_DEFAULT_ROLLUP);
    paramMap.add(REST_PARAM_OUTPUT, PARAM_DEFAULT_OUTPUT);

    MetricData resultData = null;
    try {
      ClientResponse response = restResource.path(REST_SEGMENT_METRIC_DATA).
          queryParams(paramMap).
          accept(MediaType.APPLICATION_JSON_TYPE).
          get(ClientResponse.class);

      if (response.getStatus() == 200) {
        String jsonOutput = response.getEntity(String.class);
        LOG.fine(String.format("Response from AppDynamics server ==> code: %s | output: %s",
            response.getStatus(), jsonOutput));

        List<MetricData> metricList = jsonMapper.readValue(jsonOutput, new TypeReference<List<MetricData>>() {});
        resultData = metricList.get(0); // Always expect only single 'MetricData' value
        LOG.fine("Successfully fetched metrics for path: " + resultData.getMetricPath());
      }
    } catch (Exception e) {
      LOG.log(Level.INFO, "Some problem fetching metrics from the AppDynamics REST interface, " +
          "see stack-trace for more information", e);
    }

    return resultData;
  }

*/
  public static boolean validateRestUri(final String restUri) {
    if (isFieldEmpty(restUri)) {
      return false;
    }

    if (restUri.startsWith("http://") || restUri.startsWith("https://")) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean validateToken(final String token) {
    return !isFieldEmpty(token);
  }

  private String parseToken(final String token){
	  return token;
  }

  private String parseRestUri(final String restUri) {
    StringBuilder parsedRestUri = new StringBuilder(parseRestSegment(restUri));

    String[] uriOrderedSegments = {"paaslane"};
    for (String segment : uriOrderedSegments) {
      if (!restUri.contains(segment)) {
        parsedRestUri.append(segment + "/");
      }
    }
    LOG.fine("Parsed REST uri: " + parsedRestUri.toString());
    return parsedRestUri.toString();
  }

  private String parseRestSegment(final String restSegment) {
    String parsedSegment = restSegment;
    if (!restSegment.endsWith("/")) {
      parsedSegment += "/";
    }

    return parsedSegment;
  }

  private static boolean isFieldEmpty(final String field) {
    if (field == null || field.isEmpty()) {
      return true;
    }

    final String trimmedField = field.trim();
    if (trimmedField.length() == 0) {
      return true;
    }

    return false;
  }

}
