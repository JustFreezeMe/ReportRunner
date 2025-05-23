package reportRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import reportRunner.Run.ReportRunner;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


@EnableConfigurationProperties
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {
        ApplicationContext context = SpringApplication.run(Application.class, args);
        ReportRunner reportRunner = context.getBean(ReportRunner.class);
//        VictoriaMetricsConfig vm = new VictoriaMetricsConfig();
//        vm.setVmUrl("http://vm.ankk8slt.moscow.alfaintra.net");
//        VictoriaMetricsController vmController = new VictoriaMetricsController(vm);
//        String requestQuery = vmController.getRequestsCountQueryVm("OK","60m");
//        String queryResult = vmController.sendRequestToVm(requestQuery,"1747637100","1747644690");
//        System.out.println(vmController.parseRequestResult(queryResult));
        try {
            reportRunner.run();
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(Application.class);
            logger.error("Ошибка при запуске тестов", e);
        }



    }
}



