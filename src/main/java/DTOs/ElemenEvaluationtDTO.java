package DTOs;

import java.util.List;


public class ElemenEvaluationtDTO {
    private String ID;
    private String name;
    private String description;
    private String project;

    private List<EvaluationDTO> evaluations;

    ElemenEvaluationtDTO() {
    }

    ElemenEvaluationtDTO(String ID, String name, String description, String project, List<EvaluationDTO> evaluations) {
        this.ID = ID;
        this.name = name;
        this.description = description;
        this.project=project;
        this.evaluations = evaluations;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProject(String project) { this.project = project;    }

    public void setEvaluations(List<EvaluationDTO> evaluations) {
        this.evaluations = evaluations;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getProject() {return project;}

    public List<EvaluationDTO> getEvaluations() {
        return evaluations;
    }
}
