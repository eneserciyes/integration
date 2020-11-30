package tr.com.ogedik.integration.services.jira;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tr.com.ogedik.commons.rest.request.client.HttpRestClient;
import tr.com.ogedik.commons.rest.request.client.helper.RequestURLDetails;
import tr.com.ogedik.commons.rest.request.model.JiraConfigurationProperties;
import tr.com.ogedik.commons.rest.response.RestResponse;
import tr.com.ogedik.commons.rest.response.UserSearchResponse;
import tr.com.ogedik.commons.util.MapUtils;
import tr.com.ogedik.commons.validator.MandatoryFieldValidator;
import tr.com.ogedik.integration.constants.JiraRestConstants;
import tr.com.ogedik.integration.services.configuration.ConfigurationIntegrationService;
import tr.com.ogedik.integration.util.IntegrationUtil;

/*
 * @author enes.erciyes
 */
@Service
public class JiraUserService {

    @Autowired
    private ConfigurationIntegrationService configurationService;

    public UserSearchResponse searchForUsers(String searchQuery){
        JiraConfigurationProperties properties = configurationService.getJiraConfigurationProperties();
        MandatoryFieldValidator.getInstance().validate(properties);
        RequestURLDetails requestURLDetails =
                new RequestURLDetails(
                        properties.getBaseURL(),
                        JiraRestConstants.EndPoint.USER_SEARCH,
                        MapUtils.of("username", searchQuery, "startAt", "0"));

        RestResponse<UserSearchResponse> searchResponse =
                HttpRestClient.doGet(
                        requestURLDetails, IntegrationUtil.initJiraHeaders(properties), UserSearchResponse.class);

        return searchResponse.getBody();

    }
}
