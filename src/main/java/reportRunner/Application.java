package reportRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import reportRunner.Run.ReportRunner;


@EnableConfigurationProperties
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);
        ReportRunner reportRunner = context.getBean(ReportRunner.class);
        try {
            reportRunner.run();
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(Application.class);
            logger.error("Ошибка при запуске тестов", e);
        }
    }
}



