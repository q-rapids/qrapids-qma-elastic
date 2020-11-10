package evaluation;

import DTOs.MetricEvaluationDTO;
import DTOs.Relations.RelationDTO;
import DTOs.Relations.SourceRelationDTO;
import DTOs.Relations.TargetRelationDTO;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import util.Constants;
import util.Queries;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.Queries.getLatestRelationsDate;

public class Relations {
    public static ArrayList<RelationDTO> getRelations(String projectID) throws IOException {
        SearchResponse sr = getLatestRelationsDate(projectID);
        SearchHit hit = sr.getHits().getAt(0);
        LocalDate date = LocalDate.parse(Queries.getStringFromMap(hit.getSource(), Constants.EVALUATION_DATE));
        return getRelations(projectID, date);
    }

    public static ArrayList<RelationDTO> getRelations(String projectID, LocalDate dateTo) throws IOException {
        LocalDate dateFrom = dateTo.minusDays(15);
        ArrayList<RelationDTO> relationDTO = new ArrayList<>();

        SearchResponse responseRelations = Queries.getRelations(dateFrom, dateTo, projectID);
        SearchHits hits = responseRelations.getHits();
        Map<String, Boolean> processedElements = new HashMap<>();

        for (SearchHit hit : hits) { // results come sorted by date
            Map<String, Object> hitSource = hit.getSource();
            String sourceID = getRelationLabel(Queries.getStringFromMapOrDefault(hitSource, Constants.SOURCEID, ""));
            String targetID = getRelationLabel(Queries.getStringFromMapOrDefault(hitSource, Constants.TARGETID, ""));

            String sourceTargetKey = sourceID+"->"+targetID;
            String targetType = Queries.getStringFromMapOrDefault(hitSource, Constants.TARGETTPYE, "");
            if (!processedElements.containsKey(sourceTargetKey) && checkTargetType(targetType)) {
                String sourceValue = Queries.getStringFromMapOrDefault(hitSource, Constants.VALUE, "");
                String targetValue = Queries.getStringFromMapOrDefault(hitSource, Constants.TARGETVALUE, "");
                String sourceType = Queries.getStringFromMapOrDefault(hitSource, Constants.SOURCETYPE, "");
                String sourceCategory = Queries.getStringFromMapOrDefault(hitSource, Constants.SOURCELABEL, "");
                String weight = Queries.getStringFromMapOrDefault(hitSource, Constants.WEIGHT, "");

                SourceRelationDTO sourceDTO = new SourceRelationDTO(sourceID, sourceValue, sourceCategory, sourceType);
                TargetRelationDTO targetDTO = new TargetRelationDTO(targetID, targetValue, targetType);
                relationDTO.add(new RelationDTO(weight, sourceDTO, targetDTO));
                processedElements.put(sourceTargetKey, true);
            }
        }
        return relationDTO;
    }

    private static String getRelationLabel(String elementID) {
        try {
            return elementID.split("-")[1];
        } catch (Exception e) {
            return "ERROR";
        }
    }

    private static boolean checkTargetType(String targetType) {
        return (targetType.equals(Constants.FACTOR_TYPE) || targetType.equals(Constants.STRATEGIC_INDICATOR_TYPE));
    }

    public static boolean setStrategicIndicatorFactorRelation(String projectID, String[] factorID,
                                                              String strategicIndicatorID, LocalDate evaluationDate,
                                                              double[] weight, double[] sourceValue,
                                                              String[] sourceCategories, String targetValue)
            throws IOException {

        return Queries.setFactorSIRelationIndex(projectID, factorID, weight, sourceValue, sourceCategories,
                strategicIndicatorID, evaluationDate, targetValue);
    }

    public static boolean setQualityFactorMetricRelation(String projectID, String[] metrics,
                                                              String qualityFactorID, LocalDate evaluationDate,
                                                              double[] weight, double[] sourceValue,
                                                              String[] sourceCategories, String targetValue)
            throws IOException {

        return Queries.setMetricQFRelationIndex(projectID, metrics, weight, sourceValue, sourceCategories,
                qualityFactorID, evaluationDate, targetValue);
    }
}
