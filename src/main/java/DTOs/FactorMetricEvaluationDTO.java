package DTOs;

import java.util.List;

public class FactorMetricEvaluationDTO {
    private String ID;
    private String name;
    private String project;
    private List<MetricEvaluationDTO> metrics;

    public FactorMetricEvaluationDTO(String ID, String name, String project, List<MetricEvaluationDTO> metrics) {
        this.ID = ID;
        this.name = name;
        this.project=project;
        this.metrics = metrics;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getProject() {return project; }

    public List<MetricEvaluationDTO> getMetrics() {
        return metrics;
    }
}
