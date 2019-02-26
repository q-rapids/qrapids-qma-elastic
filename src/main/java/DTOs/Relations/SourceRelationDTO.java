package DTOs.Relations;

public class SourceRelationDTO extends ElementRelationDTO {
    private String category;

    public SourceRelationDTO(String ID, String value, String category, String type) {
        super(ID, value, type);
        this.category = category;
    }
}
