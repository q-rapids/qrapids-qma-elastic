package DTOs.Relations;

public class RelationDTO {
    private String weight;
    private SourceRelationDTO source;
    private TargetRelationDTO target;

    public RelationDTO(String weight, SourceRelationDTO source, TargetRelationDTO target) {
        this.weight = weight;
        this.source = source;
        this.target = target;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public SourceRelationDTO getSource() {
        return source;
    }

    public void setSource(SourceRelationDTO source) {
        this.source = source;
    }

    public TargetRelationDTO getTarget() {
        return target;
    }

    public void setTarget(TargetRelationDTO target) {
        this.target = target;
    }
}
