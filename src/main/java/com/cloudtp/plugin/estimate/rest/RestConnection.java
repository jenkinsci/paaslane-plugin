package com.cloudtp.plugin.estimate.rest;

import com.cloudtp.plugin.estimate.ApplicationStats;
import com.cloudtp.plugin.estimate.ApplicationStatsResponse;
import com.cloudtp.plugin.estimate.BooleanDto;
import com.cloudtp.plugin.estimate.EstimateSummary;
import com.cloudtp.plugin.estimate.EstimateSummaryResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

import org.apache.commons.httpclient.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class providing only the connection to the AppDynamics REST interface. Checks
 * all connection parameters and maintains the connection to the REST interface.
 */
public class RestConnection {
	private static final String REST_AUTHORIZATION = "Authorization";
	private static final String REST_PARAM_ESTIMATE_ARTIFACT_PATH = null;

	private static final String REST_SEGMENT_ESTIMATE_REQUEST = "/restapi/estimate/estimateModule";
	private static final String REST_SEGMENT_ESTIMATE_STATUS = "/restapi/application/name";
	private static final String REST_SEGMENT_ESTIMATES_PER_APP = "/restapi/applicationeffortestimationstats/application/%d";
	private static final String REST_SEGMENT_CREATE_REPORT = "/restapi/applicationeffortestimation/create";

	private static final String PARAM_DEFAULT_ROLLUP = "false";
	private static final String PARAM_DEFAULT_OUTPUT = "JSON";

	private static final Logger LOG = Logger.getLogger(RestConnection.class.getName());
	
	public static final int SUCCESS = 1;
	public static final int FAILURE = 0;
	public static final int NO_AUTH = -1;

	private String parsedToken = null;

	private final ObjectMapper jsonMapper = new ObjectMapper();
	private final WebResource restResource;

	public RestConnection(final String restUri, final String token) {
		final String parsedRestUri = parseRestUri(restUri);

		parsedToken = parseToken(token);

		DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
				Boolean.TRUE);
		Client restClient = ApacheHttpClient.create(config);
		restClient.setFollowRedirects(true);

		restResource = restClient.resource(parsedRestUri);
		restResource.header(REST_AUTHORIZATION, token);
	}

	public int estimateApplication(final String name,
			final String rootPath, final String artifactPath,
			final String language, boolean estimate) {
		int result = SUCCESS;

		String combinedPath = rootPath + File.separator + artifactPath;

		try {
			final File fileToUpload = new File(combinedPath);

			final FormDataMultiPart multiPart = new FormDataMultiPart();

			if (fileToUpload == null || !fileToUpload.exists()) {
				LOG.log(Level.INFO, "File " + combinedPath	+ " does not exist, ");
				result = FAILURE;
			} else {
				multiPart.field("applicationName", name);
				multiPart.field("moduleName", fileToUpload.getName());
				multiPart.field("languageProfileName", language);
				multiPart.field("autoEstimate", String.valueOf(estimate));

				multiPart.bodyPart(new FileDataBodyPart("archive",
						fileToUpload, MediaType.valueOf("application/zip")));

				ClientResponse response = restResource
						.path(REST_SEGMENT_ESTIMATE_REQUEST)
						.type(MediaType.MULTIPART_FORM_DATA_TYPE)
						.header(REST_AUTHORIZATION, parsedToken)
						.post(ClientResponse.class, multiPart);

				LOG.fine(String.format(
						"Response from PaaSLane server ==> code: %s",
						response.getStatus()));
				int responseStatus = response.getStatus(); 
				if(responseStatus == HttpStatus.SC_OK){
					result = SUCCESS;
				} else if (responseStatus == HttpStatus.SC_UNAUTHORIZED){
					result = NO_AUTH;
				} else {
					LOG.log(Level.INFO, "Response " + responseStatus + " from " + combinedPath
							+ " with the PaaSLane REST interface, ");
					result = FAILURE;
				}
			}
		} catch (Exception e) {
			LOG.log(Level.INFO, "Some problem estimating " + combinedPath
					+ " with the PaaSLane REST interface, "
					+ "see stack-trace for more information", e);
			result = FAILURE;
		}

		return result;
	}

	public ApplicationStats getApplicationStatistics(final String name) {
		ApplicationStats result = null;

		try {
			ApplicationStatsResponse appStatsResponse = null;

			ClientResponse response = restResource
					.path(REST_SEGMENT_ESTIMATE_STATUS + "/" + name)
					.header(REST_AUTHORIZATION, parsedToken)
					.get(ClientResponse.class);

			if (response.getStatus() == HttpStatus.SC_OK) {
				// String output = response.getEntity(String.class);
				appStatsResponse = response
						.getEntity(new GenericType<ApplicationStatsResponse>() {
						});
				if ((appStatsResponse == null)
						|| appStatsResponse.getErrorFound()
						|| (appStatsResponse.getList().size() == 0)) {
					result = null;
				} else {
					result = appStatsResponse.getList().get(0);
				}
			}

			LOG.fine(String.format(
					"Response from PaaSLane server ==> code: %s",
					response.getStatus()));

		} catch (Exception e) {
			LOG.log(Level.INFO,
					"Some problem getting the status of application " + name
							+ " with the PaaSLane REST interface, "
							+ "see stack-trace for more information", e);
			result = null;
		}

		return result;
	}

	public EstimateSummary getLastEstimateSummary(Long appId) {
		EstimateSummary result = null;

		try {
			EstimateSummaryResponse estSummaryResponse = null;

			ClientResponse response = restResource
					.path(String.format(REST_SEGMENT_ESTIMATES_PER_APP, appId))
					.header(REST_AUTHORIZATION, parsedToken)
					.get(ClientResponse.class);

			if (response.getStatus() == HttpStatus.SC_OK) {
				// String output = response.getEntity(String.class);
				estSummaryResponse = response
						.getEntity(new GenericType<EstimateSummaryResponse>() {
						});
				if ((estSummaryResponse == null)
						|| estSummaryResponse.getErrorFound()
						|| (estSummaryResponse.getList().size() == 0)) {
					LOG.log(Level.INFO, "Estimate Summary came back null, empty, or with an error");
					result = null;
				} else {
					result = estSummaryResponse.getList().get(0);
				}
			}

			LOG.fine(String.format(
					"Response from PaaSLane server ==> code: %s",
					response.getStatus()));

		} catch (Exception e) {
			LOG.log(Level.INFO,
					"Some problem getting the estimate summary for application "
							+ appId + " with the PaaSLane REST interface, "
							+ "see stack-trace for more information", e);
			result = null;
		}
		return result;
	}
	
	public boolean createReport(String applicationName, String reportConfigName, String reportName) {
		boolean result = false;
		
		try {
			ClientResponse response = restResource
			.path(REST_SEGMENT_CREATE_REPORT)
			.queryParam("applicationName", applicationName)
			.queryParam("reportConfigName", reportConfigName)
			.queryParam("reportName", reportName)
			.header(REST_AUTHORIZATION, parsedToken)
			.method("POST", ClientResponse.class);
			
			if (response.getStatus() == 200) {
				BooleanDto responseDto = response.getEntity(new GenericType<BooleanDto>() {});
				if (responseDto.getResult() && !responseDto.getErrorFound()) {
					result = true;
				} else {
					LOG.log(Level.WARNING, "Failed to create a report");
					for (String message : responseDto.getMessages()) {
						LOG.log(Level.WARNING, message);
					}
				}
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Error calling PaaSLane to create a report");
		}
		
		return result;
	}

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

	private String parseToken(final String token) {
		return token;
	}

	private String parseRestUri(final String restUri) {
		StringBuilder parsedRestUri = new StringBuilder(parseRestSegment(restUri));

		String[] uriOrderedSegments = { "paaslane" };
		for (String segment : uriOrderedSegments) {
			if (!restUri.contains(segment)) {
				parsedRestUri.append(segment + "/");
			}
		}
		LOG.fine("Parsed REST uri: " + parsedRestUri.toString());
		return parsedRestUri.toString();
	}

	private String parseRestSegment(String restSegment) {
		return restSegment.endsWith("/")? restSegment : restSegment+"/";
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
	
	/**
	 * Reads the incoming bytes of an <code>InputStream</code>
	 * and attempts to build a <code>JSONObject</code>.
	 * @param inputStream The input stream to read the object from.
	 * @return an instance of a <code>JSONObject</code>; or <code>null</code>.
	 * @throws NullPointerException when the input is <code>null</code>.
	 * @throws IOException when the input stream cannot be read.
	 * @throws JSONException when the stream contents are malformed.
	 */
	public JSONObject fromInputStream(InputStream inputStream) throws IOException, JSONException {
		StringBuilder stringBuilder;
		int chr;
		
		stringBuilder = new StringBuilder();
		while((chr=inputStream.read()) != -1)
			stringBuilder.append((char)chr);
		return new JSONObject(stringBuilder.toString());
	}
	
	/**
	 * Gets a list of report configuration names from a REST end point.
	 * It requests a JSON resource at <code>GET /restapi/reportconfig/</code>.
	 * @return a <code>JSONObject</code>; or <code>null</code>.
	 * @throws IOException when the entity input stream cannot be read.
	 * @throws JSONException when the entity input stream contents are malformed.
	 */
	public JSONObject getReportConfigs() throws IOException, JSONException {
		ClientResponse response;
		
		response = restResource
				.path("/restapi/reportconfig/")
				.header(REST_AUTHORIZATION, parsedToken)
				.accept("application/json")
				.get(ClientResponse.class);
		
		if(response.getStatus() == HttpStatus.SC_OK) {
			return fromInputStream(response.getEntityInputStream());
		}
		
		return null;
	}

}
