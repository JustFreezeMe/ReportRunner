package reportRunner.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reportRunner.Controller.*;
import ru.alfabank.bexysuttx.Controller.*;
import reportRunner.FaultTolerance.FaultToleranceProperties;

@Component
public class TestControllerFactory {

    private final ApplicationContext context;

    @Autowired
    public TestControllerFactory(ApplicationContext context) {
        this.context = context;
    }

    public TestController createTestController(String pageId) {
        // Получаем зависимости из контекста
        MaxPerfController maxPerf = context.getBean(MaxPerfController.class);
        ConfirmMaxController confirmMax = context.getBean(ConfirmMaxController.class);
        ReliabilityController reliability = context.getBean(ReliabilityController.class);
        FaultToleranceController faultTolerance = context.getBean(FaultToleranceController.class);
        ReliabilityConfig reliabilityConfig = context.getBean(ReliabilityConfig.class);
        MaxPerformanceConfig maxPerformanceConfig = context.getBean(MaxPerformanceConfig.class);
        ConfirmMaxConfig confirmMaxConfig = context.getBean(ConfirmMaxConfig.class);
        FaultToleranceProperties faultToleranceProperties = context.getBean(FaultToleranceProperties.class);
        // Создаем экземпляр TestController с переданными зависимостями
        TestController testController = new TestController(maxPerf, confirmMax, reliability, faultTolerance, maxPerformanceConfig, confirmMaxConfig, reliabilityConfig, faultToleranceProperties);

        // Устанавливаем pageId
        testController.setPageId(pageId);

        return testController;
    }
}