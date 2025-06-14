package reportRunner.Service.HttpService;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.springframework.stereotype.Service;
import reportRunner.Config.CertConfig;
import reportRunner.Config.ConfluenceConfig;

import java.io.File;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class HttpRequestService {
    CertConfig certConfig;
    private final ConfluenceConfig confluenceConfig;

    public HttpRequestService(ConfluenceConfig confluenceConfig) {
        this.confluenceConfig = confluenceConfig;
        this.certConfig = new CertConfig(confluenceConfig);
    }

    public HttpRequest createPutHttpRequest(String body, String pageId, String credentials) {

        return HttpRequest.newBuilder()
                .uri(URI.create(confluenceConfig.getConfluenceUrl() + "/rest/api/content/" + pageId))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    public HttpRequest getPageVersionRequest(String pageid, String credentials) {
        return HttpRequest.newBuilder()
                .uri(URI.create(confluenceConfig.getConfluenceUrl() + "/rest/api/content/" + pageid + "?expand=version"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/json")
                .GET()
                .build();

    }

    public HttpRequest postCreateChildrenPage(String body, String credentials) {
        return HttpRequest.newBuilder()
                .uri(URI.create(confluenceConfig.getConfluenceUrl() + "/rest/api/content/"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    public List<HttpPost> uploadFileToConfluence(String pageId, String credentials, Map<String, File> files) {

        List<HttpPost> requests = new ArrayList<>();
        String uploadUrl = confluenceConfig.getConfluenceUrl() + "/rest/api/content/" + pageId + "/child/attachment";

        for (Map.Entry<String,File> entry : files.entrySet()) {
            File file = entry.getValue();

            HttpPost uploadFile = new HttpPost(uploadUrl);
            uploadFile.addHeader("Authorization", "Basic " + credentials);
            uploadFile.addHeader("X-Atlassian-Token", "no-check");

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("file",new FileBody(file));
            uploadFile.setEntity(builder.build());

            requests.add(uploadFile);
        }

        return requests;
    }

    public HttpRequest getAttachmentsIdsRequest(String pageId, String credentials) {
        String uploadUrl = confluenceConfig.getConfluenceUrl() + "/rest/api/content/" + pageId + "/child/attachment";

        return HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Basic " + credentials)
                .header("X-Atlassian-Token", "no-check")
                .header("Content-Type", "application/json")
                .GET()
                .build();
    }

    public HttpRequest deleteAttachmentRequest(String attachmentId, String credentials) {

        String deleteUrl = confluenceConfig.getConfluenceUrl() + "/rest/api/content/" + attachmentId;
        return HttpRequest.newBuilder()
                .uri(URI.create(deleteUrl))
                .header("Authorization", "Basic " + credentials)
                .header("X-Atlassian-Token", "no-check")
                .DELETE()
                .build();
    }
}
