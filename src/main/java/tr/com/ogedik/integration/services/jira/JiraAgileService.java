package tr.com.ogedik.integration.services.jira;

import org.springframework.stereotype.Service;
import tr.com.ogedik.commons.rest.request.client.HttpRestClient;
import tr.com.ogedik.commons.rest.request.client.helper.RequestURLDetails;
import tr.com.ogedik.commons.rest.request.model.JiraConfigurationProperties;
import tr.com.ogedik.commons.rest.response.BoardsResponse;
import tr.com.ogedik.commons.rest.response.RestResponse;
import tr.com.ogedik.commons.rest.response.SprintResponse;
import tr.com.ogedik.commons.rest.response.model.JQLSearchResult;
import tr.com.ogedik.commons.util.MapUtils;
import tr.com.ogedik.commons.validator.MandatoryFieldValidator;
import tr.com.ogedik.integration.constants.JiraRestConstants;
import tr.com.ogedik.integration.services.configuration.ConfigurationIntegrationService;
import tr.com.ogedik.integration.util.IntegrationUtil;

/*
 * @author enes.erciyes
 */
@Service
public class JiraAgileService {
    private final ConfigurationIntegrationService configurationService;
    public static final String WORKLOG = "worklog";
    public static final String SPRINT = "sprint";

    public JiraAgileService(ConfigurationIntegrationService configurationService) {
        this.configurationService = configurationService;
    }

    public JQLSearchResult getIssuesInASprintSearchResult(String sprintCode, String fields){
        JiraConfigurationProperties properties = configurationService.getJiraConfigurationProperties();
        MandatoryFieldValidator.getInstance().validate(properties);

        RequestURLDetails requestURLDetails = new RequestURLDetails(properties.getBaseURL(),
                JiraRestConstants.EndPoint.SPRINT(sprintCode), MapUtils.of("fields", fields));

        RestResponse<JQLSearchResult> searchResponse = HttpRestClient.doGet(requestURLDetails, IntegrationUtil.initJiraHeaders(properties),
                JQLSearchResult.class);

        return searchResponse.getBody();
    }

    public BoardsResponse getAllBoards(){
        JiraConfigurationProperties properties = configurationService.getJiraConfigurationProperties();
        MandatoryFieldValidator.getInstance().validate(properties);
        RequestURLDetails requestURLDetails = new RequestURLDetails(properties.getBaseURL(),
                JiraRestConstants.EndPoint.BOARDS, null);

        RestResponse<BoardsResponse> boardsResponse = HttpRestClient.doGet(requestURLDetails, IntegrationUtil.initJiraHeaders(properties),
                BoardsResponse.class);

        return boardsResponse.getBody();
    }

    public SprintResponse getSprintsInABoard(String boardId) {
        JiraConfigurationProperties properties = configurationService.getJiraConfigurationProperties();
        MandatoryFieldValidator.getInstance().validate(properties);
        RequestURLDetails requestURLDetails = new RequestURLDetails(properties.getBaseURL(),
                JiraRestConstants.EndPoint.SPRINTS(boardId), null);

        RestResponse<SprintResponse> sprintResponse = HttpRestClient.doGet(requestURLDetails, IntegrationUtil.initJiraHeaders(properties),
                SprintResponse.class);

        return sprintResponse.getBody();
    }
}
