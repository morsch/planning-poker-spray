package agido.pp.spray.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.BasicWatchers;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

import agido.pp.spray.tasks.Tasks.Task;

public class JiraTaskImporter {

	JiraRestClient client;

	String jqlPattern = "status = Analysis AND \"Scrum Team\" = \"%s\"";

	public JiraTaskImporter() {
		init();
	}

	private void init() {
		try {
			File propFile = new File("conf/jira.properties");
			System.out.println(propFile.getAbsolutePath());
			
			if (!propFile.exists())
				throw new FileNotFoundException(
						"properties file jira.properties not found!");
			Properties p = new Properties();
			p.load(new FileInputStream(propFile));
			JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
			client = factory.createWithBasicHttpAuthentication(
					new URI(p.getProperty("url")), p.getProperty("username"),
					p.getProperty("password"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Task> getTasks(String team) throws RemoteException {
		List<Task> taskList = new ArrayList<Task>();
		try {
			Promise<SearchResult> searchResultPromise = client
					.getSearchClient().searchJql(
							String.format(jqlPattern, team));
			SearchResult searchResult = searchResultPromise.get(10l,
					TimeUnit.SECONDS);
			// logger.info("found " + searchResult.getTotal() + " issues");
			for (Issue issue : searchResult.getIssues()) {
				BasicWatchers watchers = issue.getWatchers();
				int numWatchers = watchers.getNumWatchers();
				String title = issue.getKey();
				// logger.info(" adding task " + title + "; "+ numWatchers +
				// " watcher(s)");
				taskList.add(new Task(title, issue.getSummary(), team,
						numWatchers));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return taskList;
	}
}
