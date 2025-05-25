package reportRunner.Grafana;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reportRunner.Config.UtilityConfig;
import reportRunner.tsdb.Prometheus.PrometheusController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
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
    private Map<String, List<String>> grafanaVariables = new HashMap<>(); // новые переменные

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
                .flatMap(graphGroup -> {
                    List<Map<String, String>> combinations = generateVariableCombinations(
                            Optional.ofNullable(graphGroup.getGrafanaVariables()).orElse(Collections.emptyMap())
                    );

                    // Если переменных нет, добавим пустую комбинацию
                    if (combinations.isEmpty()) {
                        combinations.add(Collections.emptyMap());
                    }

                    return combinations.stream().flatMap(vars ->
                            graphGroup.getPanels().stream().map(panel ->
                                    CompletableFuture.runAsync(() -> {
                                        try {
                                            String graphName = buildGraphName(vars, panel);
                                            if (utilityConfig.getGraphsNeeded()) {
                                                String application = vars.get("application"); // может отсутствовать
                                                String podName = fetchPodNameIfRequired(graphGroup, application, timestamps, prometheus);
                                                grafanaService.downloadImage(graphName, graphGroup, panel.getPanelId(), application, podName, vars);
                                            }
                                            panel.setPanelName(graphName);
                                        } catch (IOException e) {
                                            log.error("Ошибка загрузки графика: {}", e.getMessage());
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                            log.error("Процесс был прерван: {}", e.getMessage());
                                        } catch (URISyntaxException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }, executor)
                            )
                    );
                })
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        return graphGroups;
    }


    private String buildGraphName(Map<String, String> vars, GraphPanel panel) {
        return vars.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("_")) + "_" + panel.getPanelId();
    }

    public List<Map<String, String>> generateVariableCombinations(Map<String, List<String>> variables) {
        List<Map<String, String>> result = new ArrayList<>();
        generateRecursive(result, new HashMap<>(), new ArrayList<>(variables.entrySet()), 0);
        return result;
    }

    private void generateRecursive(List<Map<String, String>> result, Map<String, String> current,
                                   List<Map.Entry<String, List<String>>> entries, int index) {
        if (index == entries.size()) {
            result.add(new HashMap<>(current));
            return;
        }
        Map.Entry<String, List<String>> entry = entries.get(index);
        for (String value : entry.getValue()) {
            current.put(entry.getKey(), value);
            generateRecursive(result, current, entries, index + 1);
        }
    }
}
