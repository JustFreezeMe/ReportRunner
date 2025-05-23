package reportRunner.PC;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reportRunner.Config.PerformanceCenterConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
public class GetLatestTestInfo {
    @Autowired
    PerformanceCenterConfig performanceCenterConfig;
    private CookieStore cookieStore;

    public int getLatestTestId() throws IOException {
        //Получаем токен доступа к Perfomance Center API
        getAccessToken(performanceCenterConfig.getPcUrl(), performanceCenterConfig.getPcDomain(), performanceCenterConfig.getPcProject());

        //Получаем список всех тестов
        String testListUrl = performanceCenterConfig.getPcUrl() + "LoadTest/rest/domains/" +
                performanceCenterConfig.getPcDomain() + "/projects/" +
                performanceCenterConfig.getPcProject() + "/runs";

        String testListJson = getResponseJson(testListUrl);

        //Разбиваем ответ и находим ID последнего теста
        int latestTestId = -1;
        JSONArray jsonArray = new JSONArray(testListJson);

        latestTestId = jsonArray.getJSONObject(0).getInt("ID");


        return latestTestId;
    }

    private String getResponseJson(String testListUrl) throws IOException {
        //Создаем запрос для получения Json-ответа
        HttpGet httpGet = new HttpGet(testListUrl);
        httpGet.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        }

    }


    private void getAccessToken(String pcUrl, String pcDomain, String pcProject) throws IOException {

        //Создаем запрос для получения токена доступа
        String tokenUrl = pcUrl + "LoadTest/rest/authentication-point/authenticate";
        HttpGet httpGet = new HttpGet(tokenUrl);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic VV9NMUcxRzpXZXN0eTQzMjN3ZXN0eQ==");

        cookieStore = new BasicCookieStore();

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            Arrays.stream(response.getHeaders("Set-Cookie")).map(h -> h.getValue()).forEach(System.out::println);
        }
    }

//    public TestIdExtended getTestInfo (int testId,TestIdExtended testIdExtended ) throws IOException {
//        String infoTest = PC_URL + "LoadTest/rest/domains/" + PC_DOMAIN + "/projects/" + PC_PROJECT + "/runs/" + testId+"/Extended";
//        HttpGet httpGet = new HttpGet(infoTest);
//        httpGet.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
//        String responseString;
//        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
//            HttpEntity entity = response.getEntity();
//            responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
//        }
//        JSONObject json = new JSONObject(responseString);
//
//        testIdExtended.setId(testId);
//        testIdExtended.setStartTime(json.getString("StartTime"));
//        testIdExtended.setEndTime(json.getString("EndTime"));
//        testIdExtended.setTotalPassedTransactions(json.getInt("TotalPassedTransactions"));
//        testIdExtended.setTotalFailedTransactions(json.getInt("TotalFailedTransactions"));
//        testIdExtended.setTotalErrors(json.getInt("TotalErrors"));
//
//        System.out.println("Last Test Info: "+ testIdExtended.toString());
//        return testIdExtended;
//    }
}


