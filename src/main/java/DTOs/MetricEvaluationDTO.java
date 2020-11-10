package DTOs;

import java.util.List;

public class MetricEvaluationDTO extends ElemenEvaluationtDTO {
    List<String> factors;

    public MetricEvaluationDTO(String ID, String name, String description, String projectID,
                               List<EvaluationDTO> evaluations, List<String> factors) {
        super(ID, name, description, projectID, evaluations);
        setFactors(factors);
    }

    public MetricEvaluationDTO() {
        super();
    }


    public String getMetricEntryID(int index_evaluation) {
        return getProject() + "-" + getID() + "-" + getEvaluations().get(index_evaluation).getEvaluationDate();
    }

    public void setFactors(List<String> factors) {
        this.factors = factors;
    }

    public List<String> getFactors() {
        return this.factors;
    }
}
