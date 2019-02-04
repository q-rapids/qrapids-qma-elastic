package DTOs;

import java.util.List;

public class StrategicIndicatorFactorEvaluationDTO {
    private final String ID;
    private final String name;

    private final String rationale;

    private final List<FactorEvaluationDTO> factors;

    public StrategicIndicatorFactorEvaluationDTO(String ID, String name, String rationale, List<FactorEvaluationDTO> factors) {
        this.ID = ID;
        this.name = name;
        this.rationale=rationale;
        this.factors = factors;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getRationale() { return rationale; }

    public List<FactorEvaluationDTO> getFactors() {
        return factors;
    }
}
