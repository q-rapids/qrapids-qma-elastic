package DTOs;

import java.util.List;

public class EstimationEvaluationDTO {

    private List<QuadrupletDTO<Integer, String, Float, Float>> estimation;

    public EstimationEvaluationDTO(List<QuadrupletDTO<Integer, String, Float, Float>> estimation) {
        this.estimation = estimation;
    }

    public List<QuadrupletDTO<Integer, String, Float, Float>> getEstimation() {
        return estimation;
    }

    public void setEstimation(List<QuadrupletDTO<Integer, String, Float, Float>> estimation) {
        this.estimation = estimation;
    }
}
