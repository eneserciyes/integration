package tr.com.ogedik.integration.constants;

/** @author orkun.gedik */
public class JiraRestConstants {

  public static class EndPoint {
    public static final String USER = "/rest/api/2/user";
    public static final String SEARCH = "/rest/api/2/search";
    public static final String SESSION = "/rest/auth/1/session";
    public static final String BOARDS = "/rest/agile/1.0/board";

    public static String SPRINT(String sprintCode) {return "/rest/agile/1.0/sprint/" + sprintCode; }
    public static String SPRINT_ISSUES(String sprintCode) {
      return "/rest/agile/1.0/sprint/" + sprintCode + "/issue";
    }

    public static String SPRINTS(String boardId) {
      return "/rest/agile/1.0/board/" + boardId + "/sprint";
    }

    public static String CREATE(String issueKey) {
      return "/rest/api/2/issue/" + issueKey + "/worklog";
    }

    public static String UPDATE(String issueKey, long worklogId) {
      return "/rest/api/2/issue/" + issueKey + "/worklog/" + worklogId;
    }
  }

  public static class Headers {
    public static String AUTHORIZATION = "Authorization";
  }
}
