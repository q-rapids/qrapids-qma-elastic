package DTOs;

import java.util.List;

public class MetricEvaluationDTO extends ElemenEvaluationtDTO {

    public MetricEvaluationDTO(String ID, String name, String description, String projectID, List<EvaluationDTO> evaluations) {
        super(ID, name, description, projectID, evaluations);
    }

    public MetricEvaluationDTO() {
        super();
    }
}
