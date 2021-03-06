package evaluation;

import DTOs.*;
import org.elasticsearch.action.update.UpdateResponse;
import util.Common;
import util.Constants;
import util.FormattedDates;
import util.Queries;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class Factor {
    private static Map<String, String> IDNames;
     /**
      *  This method returns the list of the factors and the last evaluation. The evaluation contains the evaluation
      *  date and value.
      *
      * @param projectId identifier of the project
      *
      * @return the list of factors evaluation
      * @throws IOException
     */
    public static List<FactorEvaluationDTO> getEvaluations(String projectId) throws IOException {

        List<FactorEvaluationDTO> ret;

        SearchResponse sr = Queries.getLatest(projectId, Constants.QMLevel.factors);
        Terms agg = sr.getAggregations().get("IDGroup");
        ret = Common.processFactorsBuckets(agg);

        return ret;
    }

    /**
     * This method returns the last evaluation of the factor passed as a parameter. The evaluation contains the evaluation
     * date and value.
     *
     * @param projectId identifier of the project
     * @param factorId identifier of the factor
     *
     * @return Factor evaluation
     * @throws IOException
     */
    public static FactorEvaluationDTO getSingleEvaluation(String projectId, String factorId) throws IOException {
        List<FactorEvaluationDTO> ret;
        FactorEvaluationDTO factorEvaluationDTO = null;

        SearchResponse sr = Queries.getLatestElement(projectId, Constants.QMLevel.factors, factorId);
        Terms agg = sr.getAggregations().get("IDGroup");
        ret = Common.processFactorsBuckets(agg);

        if (!ret.isEmpty()) {
            factorEvaluationDTO = ret.get(0);
        }

        return factorEvaluationDTO;
    }

    /**
     * This method returns the list of the factors and the evaluations belonging to a specific period defined by the
     * parameters from and to. The evaluation contains the evaluation date and value.
     *
     * @param projectId identifier of the project
     * @param from initial date from the range we are querying
     * @param to final date from the range we are querying
     *
     * @return the list of factors evaluations
     * @throws IOException
     */
    public static List<FactorEvaluationDTO> getEvaluations(String projectId, LocalDate from, LocalDate to)
            throws IOException {

        List<FactorEvaluationDTO> ret;

        SearchResponse sr = Queries.getRanged(Constants.QMLevel.factors, projectId,from , to);
        Terms agg = sr.getAggregations().get("IDGroup");
        ret = Common.processFactorsBuckets(agg);

        return ret;
    }

    /**
     * The external repository have two identifiers for each element, the field used by the repository (hard ID) and the
     * id and evaluation date used by the "users".
     *
     * @param projectId identifier of the project
     * @param qualityFactorID identifier of the quality factor
     * @param evaluationDate date when the evaluation has been computed
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static String getHardID(String projectId, String qualityFactorID, LocalDate evaluationDate)
            throws IOException {
        if (!projectId.isEmpty())
            return projectId + "-" + qualityFactorID + "-" + FormattedDates.formatDate(evaluationDate);
        else
            return qualityFactorID + "-" + FormattedDates.formatDate(evaluationDate);
    }

    /**
     * This method updates the value of an quality factors in a given date, if it doesn't exist
     * a new quality factor is created with the given data.
     *
     * @param projectId identifier of the project
     * @param factorID identifier of the quality factor
     * @param factorName name of the quality factor
     * @param factorDescription description of the quality factor
     * @param value evaluation value
     * @param evaluationDate date when the evaluation has been computed
     * @param estimation    in case we have a estimation (probavilities and probable values), instead of a single value
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static UpdateResponse setFactorEvaluation(String projectId,
                                                            String factorID,
                                                            String factorName,
                                                            String factorDescription,
                                                            Float value,
                                                            String info,
                                                            LocalDate evaluationDate,
                                                            EstimationEvaluationDTO estimation,
                                                            List<String> missingMetrics,
                                                            long datesMismatch,
                                                            List<String> indicators)
            throws IOException {
        UpdateResponse response;

        // unique id used to identify the entry
        String elastic_entry_ID=getHardID(projectId, factorID, evaluationDate);
        // We store the quality factor assessment
        response = Queries.setFactorValue(
                Constants.QMLevel.factors,
                elastic_entry_ID,
                projectId,
                factorID,
                factorName,
                factorDescription,
                evaluationDate,
                value,
                info,
                estimation,
                missingMetrics,
                datesMismatch,
                indicators);

        return response;

    }

    /**
     * This method updates the value of the strategic indicators relation for a list of factor.
     *
     * @param factors DTO with the factor information
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static UpdateResponse setStrategicIndicatorRelation(List<FactorEvaluationDTO> factors)
            throws IOException {

        UpdateResponse response = new UpdateResponse();

        for (FactorEvaluationDTO factor: factors){
            response = Queries.setFactorStrategicIndicatorRelation(factor);
        }

        return response;
    }


    /**
     * This method returns the list of the factors, for each factor it returns the list of metrics associated to this
     * factor and the last metric evaluation. The evaluation contains the evaluation date and value.
     *
     * @param projectId identifier of the project
     *
     * @return the list of factors' evaluations, for each factor it contains the list of the evaluation of the metrics
     *          used to compute the factor
     * @throws IOException
     */
    public static List<FactorMetricEvaluationDTO> getMetricsEvaluations(String projectId)
            throws IOException {

        List<FactorMetricEvaluationDTO> ret = new ArrayList<>();
        Map<String, String> IDNames = getFactorsIDNames(projectId);

        for (String factorID : IDNames.keySet()) {
            FactorMetricEvaluationDTO factorMetrics = getMetricsEvaluations(projectId,factorID);
            ret.add(factorMetrics);
        }

        resetFactorsIDNames();
        return ret;
    }

    /**
     * This method returns the list of the factors, for each factor it returns the list of metrics associated to this
     * factor. For each metric, it returns the evaluations belonging to the period defined by the parameters from and
     * to. The evaluation contains the evaluation date and value.
     *
     * @param projectId identifier of the project
     * @param from initial date from the range we are querying
     * @param to final date from the range we are querying
     *
     * @return the list of factors' evaluations, for each factor it contains the list of the evaluation of the metrics
     *          used to compute the factor
     * @throws IOException
     */
    public static List<FactorMetricEvaluationDTO> getMetricsEvaluations(String projectId, LocalDate from, LocalDate to)
            throws IOException {

        List<FactorMetricEvaluationDTO> ret = new ArrayList<>();
        Map<String, String> IDNames = getFactorsIDNames(projectId);

        for (String factorID : IDNames.keySet()) {
            FactorMetricEvaluationDTO factorMetrics = getMetricsEvaluations(projectId, factorID, from, to);
            ret.add(factorMetrics);
        }

        resetFactorsIDNames();
        return ret;
    }

    /**
     * This method returns the list of metrics associated to the factor evaluation passed as parameter and the last
     * metric evaluation. The evaluation contains the evaluation date and value.
     *
     * @param projectId identifier of the project
     * @param factorID identifier of the factor
     *
     * @return the list of factors' evaluations, for each factor it contains the list of the evaluation of the metrics
     *          used to compute the factor
     * @throws IOException
     */
    public static FactorMetricEvaluationDTO getMetricsEvaluations(String projectId, String factorID) throws IOException {

        Map<String, String> IDNames = getFactorsIDNames(projectId);
        String factorName = Queries.getStringFromStringMapOrDefault(IDNames, factorID, factorID);

        SearchResponse sr = Queries.getLatest(Constants.QMLevel.metrics, projectId,  factorID);
        Terms agg = sr.getAggregations().get("IDGroup");
        List<MetricEvaluationDTO> metricsEval = Common.processMetricsBuckets(agg);

        FactorEvaluationDTO factorEvaluationDTO = getSingleEvaluation(projectId, factorID);

        //return new FactorMetricEvaluationDTO(factorID, factorName, projectId, metricsEval);
        return new FactorMetricEvaluationDTO(factorEvaluationDTO, metricsEval);
    }

    /**
     * This method returns the list of metrics associated to the factor evaluation passed as parameter. For each metric,
     * it returns the evaluations belonging to the period defined by the parameters from and to. The evaluation contains
     * the evaluation date and value.
     *
     * @param projectId identifier of the projectId
     * @param factorID identifier of the factor
     * @param from initial date from the range we are querying
     * @param to final date from the range we are querying
     *
     * @return the list of factors' evaluations, for each factor it contains the list of the evaluation of the metrics
     *          used to compute the factor
     * @throws IOException
     */
    public static FactorMetricEvaluationDTO getMetricsEvaluations(String projectId, String factorID, LocalDate from, LocalDate to)
            throws IOException {

        Map<String, String> IDNames = getFactorsIDNames(projectId);
        String factorName = Queries.getStringFromStringMapOrDefault(IDNames, factorID, factorID);


        SearchResponse sr = Queries.getRanged(Constants.QMLevel.metrics, projectId, factorID,
                from, to);
        Terms agg = sr.getAggregations().get("IDGroup");
        List<MetricEvaluationDTO> metricsEval = Common.processMetricsBuckets(agg);

        FactorEvaluationDTO factorEvaluationDTO = getSingleEvaluation(projectId, factorID);
        //return new FactorMetricEvaluationDTO(factorID, factorName, projectId, metricsEval);
        return new FactorMetricEvaluationDTO(factorEvaluationDTO,metricsEval);
    }

    private static Map<String, String> getFactorsIDNames(String projectId) throws IOException {
        if (IDNames == null) {
            IDNames = Common.getIDNames(projectId, Constants.QMLevel.factors);
            return IDNames;
        }
        return IDNames;
    }

    public static void resetFactorsIDNames() {
        IDNames = null;
    }

}
