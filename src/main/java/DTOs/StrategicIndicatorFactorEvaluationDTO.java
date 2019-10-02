package DTOs;

import java.util.List;

public class StrategicIndicatorFactorEvaluationDTO extends StrategicIndicatorEvaluationDTO{

    private final List<FactorEvaluationDTO> factors;

    public StrategicIndicatorFactorEvaluationDTO(StrategicIndicatorEvaluationDTO strategicIndicator, List<FactorEvaluationDTO> factors){
        super(strategicIndicator.getID(), strategicIndicator.getName(), strategicIndicator.getDescription(), strategicIndicator.getProject(), strategicIndicator.getEvaluations(), strategicIndicator.getEstimation());
        this.factors = factors;
    }

    public List<FactorEvaluationDTO> getFactors() {
        return factors;
    }
}
