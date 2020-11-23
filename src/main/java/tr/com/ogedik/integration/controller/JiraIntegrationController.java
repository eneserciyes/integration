package tr.com.ogedik.integration.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tr.com.ogedik.commons.constants.Services;
import tr.com.ogedik.commons.rest.AbstractController;
import tr.com.ogedik.commons.rest.request.model.AuthenticationRequest;
import tr.com.ogedik.commons.rest.request.model.CreateUpdateWorklogRequest;
import tr.com.ogedik.commons.rest.request.model.JiraConfigurationProperties;
import tr.com.ogedik.commons.rest.request.model.MailServerProperties;
import tr.com.ogedik.commons.rest.request.model.sessions.JiraSession;
import tr.com.ogedik.commons.rest.response.AbstractResponse;
import tr.com.ogedik.integration.services.jira.JiraAgileService;
import tr.com.ogedik.integration.services.jira.JiraCRUDService;
import tr.com.ogedik.integration.services.jira.JiraIntegrationService;
import tr.com.ogedik.integration.services.jira.JiraSearchService;

/** @author orkun.gedik */
@RestController
public class JiraIntegrationController extends AbstractController {
  private static final Logger logger = LogManager.getLogger(JiraIntegrationController.class);

  private final JiraIntegrationService jiraIntegrationService;

  private final JiraSearchService jiraSearchService;

  private final JiraCRUDService jiraCRUDService;

  private final JiraAgileService jiraAgileService;

  public JiraIntegrationController(
      JiraIntegrationService jiraIntegrationService,
      JiraSearchService jiraSearchService,
      JiraCRUDService jiraCRUDService,
      JiraAgileService jiraAgileService) {
    this.jiraIntegrationService = jiraIntegrationService;
    this.jiraSearchService = jiraSearchService;
    this.jiraCRUDService = jiraCRUDService;
    this.jiraAgileService = jiraAgileService;
  }

  @PostMapping(Services.Path.JIRA_AUTH)
  public AbstractResponse authenticateJira(
      @RequestBody AuthenticationRequest authenticationRequest) {
    logger.info("A request has been received to authenticate configured jira instance");
    JiraSession result = jiraIntegrationService.authenticate(authenticationRequest);

    return AbstractResponse.build(
        result, result.isAuthorized() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED);
  }

  @PostMapping(Services.Path.TEST_CONNECTION)
  public AbstractResponse connectJira(@RequestBody JiraConfigurationProperties properties) {
    logger.info("A request has been received to authenticate configured jira instance");
    JiraSession result = jiraIntegrationService.connect(properties);

    return AbstractResponse.build(
        result, result.isAuthorized() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED);
  }

  @PostMapping(Services.Path.TEST_MAIL)
  public AbstractResponse connectMailServer(@RequestBody MailServerProperties properties){

    Boolean result = jiraIntegrationService.connectMail(properties);
    return AbstractResponse.build(result);
  }

  @GetMapping(Services.Path.JIRA_USER)
  public AbstractResponse getJiraUser(@RequestParam(name = "username") String username) {
    logger.info("A request has been received to retrieve jira user with name {}", username);
    return AbstractResponse.build(jiraIntegrationService.getJiraUser(username));
  }

  @GetMapping(Services.Path.ISSUES)
  public AbstractResponse getRecentIssues() {
    return AbstractResponse.build(jiraSearchService.getRecentIssues());
  }

  @GetMapping(Services.Path.LOGGED_ISSUES)
  public AbstractResponse getIssuesWithWorklogs(
      @RequestParam(name = "username") String username,
      @RequestParam(name = "startDate") String startDate,
      @RequestParam(name = "endDate") String endDate) {
    logger.info(
        "A request has been received to retrieve all the worklogs of a user between {} and {}",
        startDate,
        endDate);
    return AbstractResponse.build(
        jiraSearchService.getWorklogSearchResult(username, startDate, endDate));
  }

  @GetMapping(Services.Path.ISSUES_IN_SPRINT)
  public AbstractResponse getIssuesInASprint(
      @RequestParam(name = "sprintCode") String sprintCode,
      @RequestParam(name = "fields") String fields) {
    logger.info(
        "A request has been received to retrieve issues in sprint with code {}", sprintCode);
    return AbstractResponse.build(
        jiraAgileService.getIssuesInASprintSearchResult(sprintCode, fields));
  }

  @GetMapping(Services.Path.BOARDS)
  public AbstractResponse getAllBoards() {
    return AbstractResponse.build(jiraAgileService.getAllBoards());
  }

  @GetMapping(Services.Path.SPRINTS)
  public AbstractResponse getSprintsInABoard(@RequestParam String boardId) {
    return AbstractResponse.build(jiraAgileService.getSprintsInABoard(boardId));
  }

  @GetMapping(Services.Path.SPRINT + "/{sprintCode}")
  public AbstractResponse getSprint(@PathVariable(name="sprintCode") String sprintCode){
    return AbstractResponse.build(jiraAgileService.getSprint(sprintCode));
  }

  @PostMapping(Services.Path.WORKLOG)
  public AbstractResponse createNewWorklog(
      @RequestBody CreateUpdateWorklogRequest createWorklogRequest) {
    logger.info(
        "A request has been received to create a new worklog in issue {}",
        createWorklogRequest.getIssueKey());
    return AbstractResponse.build(jiraCRUDService.createWorklog(createWorklogRequest));
  }

  @PutMapping(Services.Path.WORKLOG)
  public AbstractResponse updateWorklog(
      @RequestBody CreateUpdateWorklogRequest updateWorklogRequest) {
    logger.info(
        "A request has been received to update the new worklog with id {}",
        updateWorklogRequest.getIssueKey());

    return AbstractResponse.build(jiraCRUDService.updateWorklog(updateWorklogRequest));
  }
}
