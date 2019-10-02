package DTOs;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class EvaluationDTO {
    private String ID;
    private String datasource;
    private LocalDate evaluationDate;
    private Float value;
    private String rationale;
    private int mismatchDays;
    private ArrayList<String> missingElements;

    public EvaluationDTO() {
    }

    public EvaluationDTO(String ID, String datasource,
                         LocalDate evaluationDate,
                         float value,
                         String rationale) {
        this.ID = ID;
        if (datasource!=null && !datasource.equalsIgnoreCase("null"))
            this.datasource = datasource;

        this.evaluationDate = evaluationDate;
        this.value = value;

        if (rationale!=null && !rationale.equalsIgnoreCase("null"))
            this.rationale=rationale;
    }

    public EvaluationDTO(String ID, String datasource,
                         String evaluationDate, String value,
                         String rationale) {
        this.ID = ID;
        if (datasource!=null && !datasource.equalsIgnoreCase("null"))
            this.datasource = datasource;

        this.evaluationDate = parseDate(evaluationDate);
        this.value = parseValue(value);

        if (rationale!=null && !rationale.equalsIgnoreCase("null"))
            this.rationale=rationale;
    }

    private LocalDate parseDate(String evaluationDate) {
        try {
            return LocalDate.parse(evaluationDate);
        } catch (DateTimeParseException d) {
            return LocalDate.MIN;
        }
    }

    private Float parseValue(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return Float.NaN;
        }
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public void setEvaluationDate(LocalDate evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public String getID() {
        return ID;
    }

    public String getDatasource() {
        return datasource;
    }

    public LocalDate getEvaluationDate() {
        return evaluationDate;
    }

    public Float getValue() {
        return value;
    }

    public String getRationale() {
        return rationale;
    }

    public int getMismatchDays() {
        return mismatchDays;
    }

    public void setMismatchDays(int mismatchDays) {
        this.mismatchDays = mismatchDays;
    }

    public ArrayList<String> getMissingElements() {
        return missingElements;
    }

    public void setMissingElements(ArrayList<String> missingElements) {
        this.missingElements = missingElements;
    }

}

