package reportRunner.Controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reportRunner.Config.ConfirmMaxConfig;
import reportRunner.Config.MaxPerformanceConfig;
import reportRunner.Config.ReliabilityConfig;
import reportRunner.FaultTolerance.FaultToleranceProperties;
import reportRunner.Results.ReportResult;

import java.util.List;

@Data
@Component
@Scope("prototype")
public class TestController {
    private MaxPerfController maxPerf;
    private ConfirmMaxController confirmMax;
    private ReliabilityController reliability;
    private FaultToleranceController faultTolerance;
    private String pageId;

    private final MaxPerformanceConfig maxPerformanceConfig;
    private final ConfirmMaxConfig confirmMaxConfig;
    private final ReliabilityConfig reliabilityConfig;
    private final FaultToleranceProperties faultToleranceProperties;

    // Явный конструктор с параметрами
    @Autowired
    public TestController(
            @Lazy @Autowired(required = false) MaxPerfController maxPerf,
            @Lazy @Autowired(required = false) ConfirmMaxController confirmMax,
            @Lazy @Autowired(required = false) ReliabilityController reliability,
            @Lazy @Autowired(required = false) FaultToleranceController faultTolerance,
            MaxPerformanceConfig maxPerformanceConfig,
            ConfirmMaxConfig confirmMaxConfig,
            ReliabilityConfig reliabilityConfig,
            FaultToleranceProperties faultToleranceProperties) {
        this.maxPerformanceConfig = maxPerformanceConfig;
        this.confirmMaxConfig = confirmMaxConfig;
        this.reliabilityConfig = reliabilityConfig;
        this.faultToleranceProperties = faultToleranceProperties;
        this.maxPerf = maxPerformanceConfig.getEnable() ? maxPerf : null;
        this.confirmMax = confirmMaxConfig.getEnable() ? confirmMax : null;
        this.reliability = reliabilityConfig.getEnable() ? reliability : null;
        this.faultTolerance = faultToleranceProperties.getEnable() ? faultTolerance : null;
    }

    public ReportResult processReliabilityReport() {
        return reliability.processReliabilityReport(getPageId());
    }

    public ReportResult processMaxPerfReport() {
        return maxPerf.processMaxPerformanceReport(getPageId());
    }

    public ReportResult processConfirmMaxReport() {
        return confirmMax.processConfirmMaxReport(getPageId());
    }

    public List<ReportResult> processFaultToleranceReport() {
        return faultTolerance.processFaultToleranceReport(getPageId());
    }
}
