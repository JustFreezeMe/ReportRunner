package reportRunner.Service.TimeSeriesDatabaseService.Prometheus;

public class PrometheusQuery {

    public String getPodNamesQuery(String namespace, String application) {

        return "kube_pod_container_info%7Bnamespace='$namespace',container='$application'%7D" //%7B %7D это {}
                .replace("$namespace", namespace)
                .replace("$application", application);
    }

    public String getMesosInstanceQuery(String application) {
        return "jvm_memory_used_bytes%7Bapplication='$application'%7D"
                .replace("$application", application);
    }
}
