package ly.mens.pivotjot;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

import ly.mens.pivotjot.model.Project;

/**
 * Service for abstracting Pivotal's REST API through Intents and Broadcasts
 * Created by mensly on 3/05/2015.
 */
public class PivotalService extends IntentService {
    private static final String PREFIX = "ly.mens.pivotjot.";
    public static final String ACTION_AUTHENTICATE = PREFIX + "authenticate";
    public static final String ACTION_LIST_PROJECTS = PREFIX + "list_projects";
    public static final String ACTION_POST = PREFIX + "post";

    public static final String EXTRA_USERNAME = PREFIX + "username";
    public static final String EXTRA_PASSWORD = PREFIX + "password";
    public static final String EXTRA_TITLE = PREFIX + "title";
    public static final String EXTRA_PROJECT_ID = PREFIX + "project";
    public static final String EXTRA_PROJECTS = PREFIX + "projects";
    public static final String EXTRA_HAS_TOKEN = PREFIX + "has_token";

    public static final String BROADCAST_AUTH_SUCCESS = PREFIX + "authenticated";
    public static final String BROADCAST_AUTH_ERROR = PREFIX + "error_auth";
    public static final String BROADCAST_PROJECT_LIST = PREFIX + "project_list";
    public static final String BROADCAST_POST_SUCCESS = PREFIX + "posted";
    public static final String BROADCAST_NETWORK_ERROR = PREFIX + "error_network";

    private static final String KEY_TOKEN = "token";

    private String token;

    public PivotalService() {
        super("PivotalService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        token = "TESTING"; // TODO: Replace with SharedPreferences
    }

    private void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();
        if (action == null) {
            // Handle invalid data
            return;
        }
        if (token == null) {
            // Unauthenticated user
            sendLocalBroadcast(new Intent(BROADCAST_AUTH_ERROR).putExtra(EXTRA_HAS_TOKEN, false));
            return;
        }
        // Pass actions off to relevant methods
        switch (intent.getAction()) {
            case ACTION_LIST_PROJECTS:
                listProjects();
                break;
            case ACTION_AUTHENTICATE:
                authenticate(intent.getStringExtra(EXTRA_USERNAME), intent.getStringExtra(EXTRA_PASSWORD));
                break;
            case ACTION_POST:
                postStory(intent.getIntExtra(EXTRA_PROJECT_ID, 0), intent.getStringExtra(EXTRA_TITLE));
                break;
        }
    }

    private void authenticate(String username, String password) {
        // TODO: Implement
        sendLocalBroadcast(new Intent(BROADCAST_AUTH_SUCCESS));
    }

    private void listProjects() {
        // TODO: Implement
        ArrayList<Project> projects = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            Project project = new Project();
            project.projectId = i;
            project.projectName = "Sample Data " + i;
            projects.add(project);
        }
        sendLocalBroadcast(new Intent(BROADCAST_PROJECT_LIST)
                .putExtra(EXTRA_PROJECTS, projects));
    }

    private void postStory(int projectId, String title) {
        // TODO: Implement
        // TODO: Allow configuration of description and story type
        sendLocalBroadcast(new Intent(BROADCAST_POST_SUCCESS));
    }

    public static final class Methods {
        private static Intent createIntent(Context context, String action) {
            return new Intent(context, PivotalService.class).setAction(action);
        }

        public static void authenticate(Context context, String username, String password) {
            context.startService(createIntent(context, ACTION_AUTHENTICATE)
                    .putExtra(EXTRA_USERNAME, username)
                    .putExtra(EXTRA_PASSWORD, password));
        }

        public static void listProject(Context context) {
            context.startService(createIntent(context, ACTION_LIST_PROJECTS));
        }

        public static void postStory(Context context, int projectId, String title) {
            context.startService(createIntent(context, ACTION_POST)
                    .putExtra(EXTRA_PROJECT_ID, projectId)
                    .putExtra(EXTRA_TITLE, title));
        }
    }
}
