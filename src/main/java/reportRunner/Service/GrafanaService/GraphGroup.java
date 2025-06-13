package reportRunner.Service.GrafanaService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reportRunner.Config.UtilityConfig;
import reportRunner.Service.TimeSeriesDatabaseService.Prometheus.PrometheusController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Data
public class GraphGroup {

    private String title;
    private String groupId;
    private String groupName;
    private List<GraphPanel> panels;
    private List<String> variables;
    private String variable;
    private boolean needPod;

    private final UtilityConfig utilityConfig;

    public GraphGroup(UtilityConfig utilityConfig) {
        this.utilityConfig = utilityConfig;
    }

    private List<GraphGroup> loadAndParseGraphGroup(List<GraphGroup> graphGroup, GrafanaService grafanaService) throws URISyntaxException, IOException {

        for (GraphGroup group : graphGroup) {
            String graphData = grafanaService.createGetGraphsRequest(group);
            grafanaService.parseGraphNames(graphData, group.getPanels());
        }
        return graphGroup;
    }

    private String buildGraphName(String variable, GraphPanel panel) {
        String name = panel.getPanelName() + "_" + variable;
        name = name.replaceAll("[^a-zA-Z0-9_%]", "_");
        return name.replaceAll("_+", "_");
    }

    private String fetchPodNameIfRequired(GraphGroup graphGroup, String variable, Map<String, Long> timestamps, PrometheusController prometheus)
            throws IOException, InterruptedException {
        if (graphGroup.isNeedPod()) {
            return prometheus.getVariableName(
                    variable,
                    String.valueOf(timestamps.get("startTimestamp") / 1000),
                    String.valueOf(timestamps.get("endTimestamp") / 1000)
            );
        }
        return "";
    }

    public List<GraphGroup> processGraphGroups(List<GraphGroup> graphGroups,
                                               Map<String, Long> timestamps,
                                               GrafanaService grafanaService,
                                               PrometheusController prometheus) throws URISyntaxException, IOException {

        graphGroups = loadAndParseGraphGroup(graphGroups, grafanaService);

        ExecutorService executor = Executors.newWorkStealingPool();

        List<CompletableFuture<Void>> futures = graphGroups.stream()
                .flatMap(graphGroup -> graphGroup.getPanels().stream().map(panelName ->
                        CompletableFuture.runAsync(() -> {
                            try {
                                String graphName = buildGraphName(graphGroup.getVariable(), panelName);
                                if (utilityConfig.getGraphsNeeded()) {
                                    String podName = fetchPodNameIfRequired(graphGroup, graphGroup.getVariable(), timestamps, prometheus);
                                    grafanaService.downloadImage(graphName, graphGroup, panelName.getPanelId(), graphGroup.getVariable(), podName);
                                }
                                panelName.setPanelName(graphName);
                            } catch (IOException e) {
                                log.error("Ошибка загрузки графика: {}", e.getMessage());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt(); // Восстановить флаг прерывания
                                log.error("Процесс был прерван: {}", e.getMessage());
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        }, executor)
                ))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        return graphGroups;
    }
}
