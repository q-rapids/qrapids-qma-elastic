package DTOs;

import java.util.List;

public class FactorEvaluationDTO extends ElemenEvaluationtDTO {
    List<String>  strategic_indicators;

    public FactorEvaluationDTO(String ID, String name, String description, String projectID,
                               List<EvaluationDTO> evaluations, List<String> strategic_indicators) {
        super(ID, name, description, projectID, evaluations);
        setStrategicIndicators(strategic_indicators);
    }

    public FactorEvaluationDTO() {

    }

    public String getFactorEntryID(int index_evaluation) {
        return getProject() + "-" + getID() + "-" + getEvaluations().get(index_evaluation).getEvaluationDate();
    }
    public void setStrategicIndicators(List<String> strategic_indicators) {
        this.strategic_indicators = strategic_indicators;
    }
    public List<String> getStrategicIndicators() {
        return this.strategic_indicators;
    }
}
