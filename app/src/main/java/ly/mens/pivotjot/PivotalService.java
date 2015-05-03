package ly.mens.pivotjot;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ly.mens.pivotjot.model.Project;
import ly.mens.pivotjot.model.Story;
import ly.mens.pivotjot.model.User;

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
    private static final String ENDPOINT_LOGIN = "https://www.pivotaltracker.com/services/v5/me";
    private static final String ENDPOINT_PROJECTS = "https://www.pivotaltracker.com/services/v5/projects";
    private static final String ENDPOINT_POST = "https://www.pivotaltracker.com/services/v5/projects/%d/stories";

    private static final Gson GSON = new Gson();

    private String token;

    public PivotalService() {
        super("PivotalService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        token = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_TOKEN, null);
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
        Reader reader = null;
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(ENDPOINT_LOGIN).openConnection();
            String encoded = new String(Base64.encode((username + ":" + password).getBytes("utf-8"), Base64.NO_WRAP), "utf-8");
            conn.setRequestProperty("Authorization", "Basic " + encoded);
            int response = conn.getResponseCode();
            switch (response) {
                case 200:
                    reader = new InputStreamReader(conn.getInputStream());
                    User user = GSON.fromJson(reader, User.class);
                    token = user.getApiToken();
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                            .putString(KEY_TOKEN, token)
                            .apply();
                    sendLocalBroadcast(new Intent(BROADCAST_AUTH_SUCCESS));
                    break;
                case 403:
                    sendLocalBroadcast(new Intent(BROADCAST_AUTH_ERROR)
                            .putExtra(EXTRA_HAS_TOKEN, token != null));
                    break;
                default:
                    sendLocalBroadcast(new Intent(BROADCAST_NETWORK_ERROR));
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) { }
            }
        }
    }

    private void listProjects() {
        Reader reader = null;
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(ENDPOINT_PROJECTS).openConnection();
            conn.addRequestProperty("X-TrackerToken", token);
            int response = conn.getResponseCode();
            switch (response) {
                case 200:
                    reader = new InputStreamReader(conn.getInputStream());
                    Type listType = new TypeToken<List<Project>>() {
                    }.getType();
                    List<Project> projects = GSON.fromJson(reader, listType);
                    sendLocalBroadcast(new Intent(BROADCAST_PROJECT_LIST)
                            .putExtra(EXTRA_PROJECTS, new ArrayList<>(projects)));
                    break;
                case 403:
                    sendLocalBroadcast(new Intent(BROADCAST_AUTH_ERROR)
                            .putExtra(EXTRA_HAS_TOKEN, token != null));
                    break;
                default:
                    sendLocalBroadcast(new Intent(BROADCAST_NETWORK_ERROR));
                    break;
            }

        } catch (IOException e) {
            sendLocalBroadcast(new Intent(BROADCAST_NETWORK_ERROR));
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) { }
            }
        }
    }

    private void postStory(int projectId, String title) {
        // TODO: Allow configuration of description and story type
        Story story = new Story(title, getString(R.string.description_placeholder));
        String content = GSON.toJson(story);
        Writer writer = null;
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(String.format(ENDPOINT_POST, projectId)).openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.addRequestProperty("X-TrackerToken", token);
            conn.addRequestProperty("Content-Type", "application/json");
            conn.connect();
            writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(content);
            writer.close();
            int response = conn.getResponseCode();
            switch (response) {
                case 200:
                case 201:
                    sendLocalBroadcast(new Intent(BROADCAST_POST_SUCCESS));
                    break;
                case 403:
                    sendLocalBroadcast(new Intent(BROADCAST_AUTH_ERROR)
                            .putExtra(EXTRA_HAS_TOKEN, token != null));
                    break;
                default:
                    sendLocalBroadcast(new Intent(BROADCAST_NETWORK_ERROR));
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendLocalBroadcast(new Intent(BROADCAST_NETWORK_ERROR));
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException e) { }
            }
        }
    }

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

    public static boolean hasToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).contains(KEY_TOKEN);
    }
}
