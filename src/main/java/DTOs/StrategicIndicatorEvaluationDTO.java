package DTOs;

import java.util.List;

public class StrategicIndicatorEvaluationDTO extends ElemenEvaluationtDTO {

    private List<EstimationEvaluationDTO> estimation;

    public StrategicIndicatorEvaluationDTO(String ID, String name, String description, String project, List<EvaluationDTO> evaluations, List<EstimationEvaluationDTO> estimation) {
        super(ID, name, description, project, evaluations);
        this.estimation = estimation;
    }

    public StrategicIndicatorEvaluationDTO() {}

    public List<EstimationEvaluationDTO> getEstimation() {
        return estimation;
    }

    public void setEstimation(List<EstimationEvaluationDTO> estimation) {
        this.estimation = estimation;
    }
}
