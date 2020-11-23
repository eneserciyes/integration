package tr.com.ogedik.integration.services.jira;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tr.com.ogedik.commons.model.JiraUser;
import tr.com.ogedik.commons.rest.request.client.HttpRestClient;
import tr.com.ogedik.commons.rest.request.client.helper.RequestURLDetails;
import tr.com.ogedik.commons.rest.request.model.AuthenticationRequest;
import tr.com.ogedik.commons.rest.request.model.JiraConfigurationProperties;
import tr.com.ogedik.commons.rest.request.model.MailServerProperties;
import tr.com.ogedik.commons.rest.request.model.sessions.JiraSession;
import tr.com.ogedik.commons.rest.request.model.sessions.UnauthorizedJiraSession;
import tr.com.ogedik.commons.rest.response.RestResponse;
import tr.com.ogedik.commons.util.MapUtils;
import tr.com.ogedik.commons.validator.MandatoryFieldValidator;
import tr.com.ogedik.integration.constants.JiraRestConstants;
import tr.com.ogedik.integration.services.configuration.ConfigurationIntegrationService;
import tr.com.ogedik.integration.util.IntegrationUtil;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.Properties;

/**
 * This service processes the received request from other microservices in the project and routes
 * those requests to the integration Jira instance. In order to resolve request URL of the Jira,
 * sends a http request to retrieve Jira configurations to configuration microservice.
 *
 * <p>Request structure is as follows: Any service -> Integration <-> Configuration -> Jira
 *
 * @author orkun.gedik
 */
@Service
public class JiraIntegrationService {

  @Autowired private ConfigurationIntegrationService configurationService;
  private static final Logger logger = LogManager.getLogger(JiraIntegrationService.class);
  /**
   * Authenticates to configured Jira instance.
   *
   * @param authenticationRequest authentication request information
   * @return {@code true} if the given request is able to authenticate.
   */
  public JiraSession authenticate(AuthenticationRequest authenticationRequest) {
    JiraConfigurationProperties properties = configurationService.getJiraConfigurationProperties();
    properties.setUsername(authenticationRequest.getUsername());
    properties.setPassword(authenticationRequest.getPassword());

    return connect(properties);
  }

  /**
   * Creates a session on the Jira instance
   *
   * @param properties
   * @return {@code true} if session is created successfully. {@code false} if the request returns a
   *     status code which is not 200
   * @implNote Jira instance returns 404 in case of Jira is down Jira instance returns 401 in case
   *     of given username and password in the {@code properties} are wrong. Jira instance returns
   *     500 in case of there is an internal problem in Jira.
   */
  public JiraSession connect(JiraConfigurationProperties properties) {
    MandatoryFieldValidator.getInstance().validate(properties);

    RequestURLDetails requestURLDetails =
        new RequestURLDetails(properties.getBaseURL(), JiraRestConstants.EndPoint.SESSION, null);
    AuthenticationRequest authenticationRequest =
        AuthenticationRequest.builder()
            .username(properties.getUsername())
            .password(properties.getPassword())
            .build();
    RestResponse<JiraSession> authResponse =
        HttpRestClient.doPost(requestURLDetails, authenticationRequest, JiraSession.class);

    return authResponse.getHttpStatusCode() == HttpStatus.OK.value()
        ? authResponse.getBody()
        : new UnauthorizedJiraSession();
  }

  /**
   * Sends a get request with the Authorization header to Jira instance to be able to retrieve given
   * user details. Username passes as a query parameter in the request.
   *
   * @param username to be retrieved username
   * @return retrieved {@link JiraUser}
   */
  public JiraUser getJiraUser(String username) {
    JiraConfigurationProperties properties = configurationService.getJiraConfigurationProperties();
    MandatoryFieldValidator.getInstance().validate(properties);

    RequestURLDetails requestURLDetails =
        new RequestURLDetails(
            properties.getBaseURL(),
            JiraRestConstants.EndPoint.USER,
            MapUtils.of("username", username));
    RestResponse<JiraUser> userResponse =
        HttpRestClient.doGet(
            requestURLDetails, IntegrationUtil.initJiraHeaders(properties), JiraUser.class);

    return userResponse.getBody();
  }

  public Boolean connectMail(MailServerProperties properties) {
    boolean auth = Boolean.parseBoolean(properties.getAuth());
    String enctype = properties.getEnctype();
    String port  = properties.getPort();
    String host = properties.getHost();
    String username = properties.getUsername();
    String password = properties.getPassword();

    boolean result = false;
    try {
      Properties props = new Properties();
      if (auth) {
        props.setProperty("mail.smtp.auth", "true");
      } else {
        props.setProperty("mail.smtp.auth", "false");
      }
      if (enctype.endsWith("TLS")) {
        props.setProperty("mail.smtp.starttls.enable", "true");
      } else if (enctype.endsWith("SSL")) {
        props.setProperty("mail.smtp.startssl.enable", "true");
      }
      Session session = Session.getInstance(props, null);
      Transport transport = session.getTransport("smtp");
      int portInt = Integer.parseInt(port);
      transport.connect(host, portInt, username, password);
      transport.close();
      result = true;

    } catch(AuthenticationFailedException e) {
      logger.log(Level.ERROR,"SMTP: Authentication Failed - {}", e.getMessage());

    } catch(MessagingException e) {
      logger.log(Level.ERROR, "SMTP: Messaging Exception Occurred - {}", e.getMessage());
    } catch (Exception e) {
      logger.log(Level.ERROR, "SMTP: Unknown Exception - {}", e.getMessage());
    }

    return result;
  }
}
