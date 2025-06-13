package reportRunner.Utility;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CredentialsUtility {

    public String createCredentials(String username, String password) {

        String credentials = username + ":" + password;//CONFLUENCE_USERNAME + ":" + CONFLUENCE_PASSWORD;

        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
