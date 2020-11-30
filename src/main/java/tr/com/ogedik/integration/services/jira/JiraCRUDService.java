package tr.com.ogedik.integration.services.jira;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tr.com.ogedik.commons.rest.request.client.HttpRestClient;
import tr.com.ogedik.commons.rest.request.client.helper.RequestURLDetails;
import tr.com.ogedik.commons.rest.request.model.CreateUpdateWorklogRequest;
import tr.com.ogedik.commons.rest.request.model.JiraConfigurationProperties;
import tr.com.ogedik.commons.rest.request.model.JiraCreateWorklogRequest;
import tr.com.ogedik.commons.rest.response.RestResponse;
import tr.com.ogedik.commons.validator.MandatoryFieldValidator;
import tr.com.ogedik.integration.constants.JiraRestConstants;
import tr.com.ogedik.integration.services.configuration.ConfigurationIntegrationService;
import tr.com.ogedik.integration.util.IntegrationUtil;

/*
 * @author enes.erciyes
 */
@Service
public class JiraCRUDService {

  @Autowired private ConfigurationIntegrationService configurationService;

  public Boolean createWorklog(CreateUpdateWorklogRequest createUpdateWorklogRequest) {
    JiraConfigurationProperties properties = configurationService.getJiraConfigurationProperties();
    MandatoryFieldValidator.getInstance().validate(properties);

    JiraCreateWorklogRequest request =
        JiraCreateWorklogRequest.builder()
            .comment(createUpdateWorklogRequest.getComment())
            .started(createUpdateWorklogRequest.getStarted())
            .timeSpentSeconds(createUpdateWorklogRequest.getTimeSpentSeconds())
            .build();

    RequestURLDetails requestURLDetails =
        new RequestURLDetails(
            properties.getBaseURL(),
            JiraRestConstants.EndPoint.CREATE(createUpdateWorklogRequest.getIssueKey()),
            null);

    RestResponse<String> response =
        HttpRestClient.doPost(
            requestURLDetails, request, IntegrationUtil.initJiraHeaders(properties), String.class);

    return response .getHttpStatusCode() == HttpStatus.CREATED.value();
  }

  public Boolean updateWorklog(CreateUpdateWorklogRequest updateWorklogRequest) {
    JiraConfigurationProperties properties = configurationService.getJiraConfigurationProperties();
    MandatoryFieldValidator.getInstance().validate(properties);

    JiraCreateWorklogRequest request =
        JiraCreateWorklogRequest.builder()
            .comment(updateWorklogRequest.getComment())
            .started(updateWorklogRequest.getStarted())
            .timeSpentSeconds(updateWorklogRequest.getTimeSpentSeconds())
            .build();

    RequestURLDetails requestURLDetails =
        new RequestURLDetails(
            properties.getBaseURL(),
            JiraRestConstants.EndPoint.UPDATE(
                updateWorklogRequest.getIssueKey(), updateWorklogRequest.getId()),
            null);

    RestResponse<String> response =
        HttpRestClient.doPut(
            requestURLDetails, request, IntegrationUtil.initJiraHeaders(properties), String.class);

    return response.getHttpStatusCode() == HttpStatus.OK.value();
  }
}
