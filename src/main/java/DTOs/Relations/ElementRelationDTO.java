package DTOs.Relations;

public class ElementRelationDTO {
    private String ID;
    private String value;
    private String type;

    public ElementRelationDTO(String ID, String value, String type) {
        this.ID = ID;
        this.value = value;
        this.type = type;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
