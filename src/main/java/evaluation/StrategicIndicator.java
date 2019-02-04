package evaluation;

import DTOs.*;
import util.Common;
import util.Constants;
import util.FormattedDates;
import util.Queries;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class StrategicIndicator {
    private static Map<String, String> IDNames;

    /**
     * This method returns the list of the strategic indicators and the last evaluation. The evaluation contains the
     * evaluation date and value
     *
     * @param projectId identifier of the project
     *
     * @return the list of strategic indicators' evaluations
     *
     * @throws IOException
     */
    public static List<StrategicIndicatorEvaluationDTO> getEvaluations(String projectId) throws IOException {

        List<StrategicIndicatorEvaluationDTO> ret;

        SearchResponse sr = Queries.getLatest(projectId,Constants.QMLevel.strategic_indicators);
        Terms agg = sr.getAggregations().get("IDGroup");
        ret = Common.processStrategicIndicatorsBuckets(agg);

        return ret;
    }

    /**
     * This method returns the list of the strategic indicators and the evaluations belonging to the specific period
     * defined by the parameters from and to. The evaluation contains the evaluation date and value.
     *
     * @param projectId identifier of the project
     * @param from initial date from the range we are querying
     * @param to final date from the range we are querying
     *
     * @return the list of strategic indicators' evaluations
     *
     * @throws IOException
     */
    public static List<StrategicIndicatorEvaluationDTO> getEvaluations(String projectId, LocalDate from, LocalDate to)
            throws IOException {

        List<StrategicIndicatorEvaluationDTO> ret;

        SearchResponse sr = Queries.getRanged(Constants.QMLevel.strategic_indicators, projectId, from, to);
        Terms agg = sr.getAggregations().get("IDGroup");
        ret = Common.processStrategicIndicatorsBuckets(agg);

        return ret;
    }

    /**
     * The external repository have two identifiers for each element, the field used by the repository (hard ID) and the
     * id and evaluation date used by the "users".
     *
     * @param projectId identifier of the project
     * @param strategicIndicatorID identifier of the strategic indicator
     * @param evaluationDate date when the evaluation has been computed
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static String getHardID(String projectId, String strategicIndicatorID, LocalDate evaluationDate)
        throws IOException {

        return strategicIndicatorID + "-" + FormattedDates.formatDate(evaluationDate);
    }

    /**
     * This method updates the value of an strategic indicators in a given date, if it doesn't exist
     * a new strategic indicator is created with the given data.
     *
     * @param projectId identifier of the project
     * @param strategicIndicatorID identifier of the strategic indicator
     * @param strategicIndicatorName name of the strategic indicator
     * @param strategicIndicatorDescription description of the strategic indicator
     * @param value evaluation value
     * @param evaluationDate date when the evaluation has been computed
     * @param estimation    in case we have a estimation (probavilities and probable values), instead of a single value
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static UpdateResponse setStrategicIndicatorEvaluation(String projectId,
                                                                 String strategicIndicatorID,
                                                                 String strategicIndicatorName,
                                                                 String strategicIndicatorDescription,
                                                                 Float value,
                                                                 LocalDate evaluationDate,
                                                                 EstimationEvaluationDTO estimation,
                                                                 List<String> missingFactors,
                                                                 long datesMismatch)
            throws IOException {
        UpdateResponse response;

        // unique id used to identify the entry
        String elastic_entry_ID=getHardID(projectId, strategicIndicatorID, evaluationDate);
        // We store the strategic indicator assessment
        response = Queries.setStrategicIndicatorValue(
                Constants.QMLevel.strategic_indicators,
                elastic_entry_ID,
                projectId,
                strategicIndicatorID,
                strategicIndicatorName,
                strategicIndicatorDescription,
                evaluationDate,
                value,
                estimation,
                missingFactors,
                datesMismatch);

        return response;

    }

    /**
     * This method returns the list of the strategic indicators. For each strategic indicator, it returns the list of
     * factors associated to it and their last evaluation. The evaluation contains the evaluation date and value.
     *
     * @param projectId identifier of the project
     *
     * @return  the list of strategic indicators' evaluations, for each strategic indicator it returns the evaluation
     *          of the factors impacting on this strategic indicator
     *
     * @throws IOException
     */
    public static List<StrategicIndicatorFactorEvaluationDTO> getFactorsEvaluations(String projectId)
            throws IOException {

        List<StrategicIndicatorFactorEvaluationDTO> ret = new ArrayList<>();
        Map<String, String> IDNames = getFactorsIDNames(projectId);

        for (String indicatorID : IDNames.keySet()) {
            StrategicIndicatorFactorEvaluationDTO indicatorFactors = getFactorsEvaluations(projectId, indicatorID);
            ret.add(indicatorFactors);
        }

        resetFactorsIDNames();
        return ret;
    }

    /**
     * This method returns the list of the strategic indicators. For each strategic indicator, it returns the list of
     * factors associated to it and their evaluations belonging to the period defined by the parameters from and to.
     * The evaluation contains the evaluation date and value.
     *
     * @param projectId identifier of the project
     * @param from initial date from the range we are querying
     * @param to final date from the range we are querying
     *
     * @return  the list of strategic indicators' evaluations, for each strategic indicator it returns the evaluation
     *          of the factors impacting on this strategic indicator
     * @throws IOException
     */
    public static List<StrategicIndicatorFactorEvaluationDTO> getFactorsEvaluations(String projectId, LocalDate from, LocalDate to)
            throws IOException {

        List<StrategicIndicatorFactorEvaluationDTO> ret = new ArrayList<>();
        Map<String, String> IDNames = getFactorsIDNames(projectId);

        for (String indicatorID : IDNames.keySet()) {
            StrategicIndicatorFactorEvaluationDTO indicatorFactors = getFactorsEvaluations(projectId, indicatorID, from, to);
            ret.add(indicatorFactors);
        }

        resetFactorsIDNames();
        return ret;
    }

    /**
     * This method returns the list of factors associated to the strategic indicator evaluation passed as a parameter
     * and their last evaluation. The evaluation contains the evaluation date and value.
     *
     * @param projectId identifier of the project
     * @param strategicIndicatorID identifier of the strategic indicator
     *
     * @return  The strategic indicator evaluations, for this strategic indicator it returns the evaluation
     *          of the factors impacting on this strategic indicator
     * @throws IOException
     */
    public static StrategicIndicatorFactorEvaluationDTO getFactorsEvaluations(String projectId, String strategicIndicatorID) throws IOException {

        Map<String, String> IDNames = getFactorsIDNames(projectId);
        String strategicIndicatorName = Queries.getStringFromStringMapOrDefault(IDNames, strategicIndicatorID,
                strategicIndicatorID);

        SearchResponse sr = Queries.getLatest( Constants.QMLevel.factors, projectId, strategicIndicatorID);
        Terms agg = sr.getAggregations().get("IDGroup");
        List<FactorEvaluationDTO> factorsEval = Common.processFactorsBuckets(agg);

        return new StrategicIndicatorFactorEvaluationDTO(strategicIndicatorID, strategicIndicatorName, projectId, factorsEval);
    }

    /**
     * This method returns the list of factors associated to the strategic indicator evaluation passed as parameter and
     * their evaluations belonging to the period defined by the parameters from and to. The evaluation contains the
     * evaluation date and value.
     *
     * @param projectId identifier of the project
     * @param strategicIndicatorID identifier of the strategic indicator
     * @param from initial date from the range we are querying
     * @param to final date from the range we are querying
     *
     * @return  The strategic indicator evaluations, for this strategic indicator it returns the evaluation
     *          of the factors impacting on this strategic indicator
     * @throws IOException
     */
    public static StrategicIndicatorFactorEvaluationDTO getFactorsEvaluations(String projectId, String strategicIndicatorID,
                                                                              LocalDate from, LocalDate to)
            throws IOException {

        Map<String, String> IDNames = getFactorsIDNames(projectId);
        String strategicIndicatorName = Queries.getStringFromStringMapOrDefault(IDNames, strategicIndicatorID,
                strategicIndicatorID);

        SearchResponse sr = Queries.getRanged(Constants.QMLevel.factors, projectId, strategicIndicatorID, from,to);
        Terms agg = sr.getAggregations().get("IDGroup");
        List<FactorEvaluationDTO> factorsEval = Common.processFactorsBuckets(agg);

        return new StrategicIndicatorFactorEvaluationDTO(strategicIndicatorID, strategicIndicatorName, projectId, factorsEval);
    }

    /**
     * This method returns the list of metrics associated to every factor of a strategic indicator passed as parameter
     * and the last metric evaluation. The evaluation contains the evaluation date and value.
     *
     * @param projectId identifier of the project
     * @param strategicIndicatorID identifier of the strategic indicator
     *
     * @return  The list of the evaluation of the factors impacting on this strategic indicator. For each factor,
     *          the evaluation of the metrics used for computing this factor.
     * @throws IOException
     */
    public static List<FactorMetricEvaluationDTO> getMetricsEvaluations(String projectId, String strategicIndicatorID)
        throws IOException {

        List <FactorMetricEvaluationDTO> ret = new ArrayList<>();
        StrategicIndicatorFactorEvaluationDTO siFactors = getFactorsEvaluations(projectId,strategicIndicatorID);

        for (FactorEvaluationDTO factor : siFactors.getFactors()) {
            ret.add(new FactorMetricEvaluationDTO(
                    factor.getID(),
                    factor.getName(),
                    factor.getProject(),
                    Factor.getMetricsEvaluations(
                            factor.getProject(),
                            factor.getID())
                            .getMetrics()));
        }
        return ret;
    }

    /**
     * This method returns the list of metrics associated to every factor of a strategic indicator passed as parameter
     * and their evaluations belonging to the period defined by the parameters from and to. The evaluation contains the
     * evaluation date and value.
     *
     * @param projectId identifier of the project
     * @param strategicIndicatorID identifier of the strategic indicator
     * @param from
     * @param to
     *
     * @return  The list of the evaluation of the factors impacting on this strategic indicator. For each factor,
     *          the evaluation of the metrics used for computing this factor.
     * @throws IOException
     */
    public static List<FactorMetricEvaluationDTO> getMetricsEvaluations(String projectId, String strategicIndicatorID,
                                                                        LocalDate from, LocalDate to)
            throws IOException {

        List <FactorMetricEvaluationDTO> ret = new ArrayList<>();
        StrategicIndicatorFactorEvaluationDTO siFactors = getFactorsEvaluations(projectId,
                                                                                strategicIndicatorID, from, to);

        for (FactorEvaluationDTO factor : siFactors.getFactors()) {
            ret.add(new FactorMetricEvaluationDTO(
                    factor.getID(),
                    factor.getName(),
                    factor.getProject(),
                    Factor.getMetricsEvaluations(
                            factor.getProject(),
                            factor.getID(), from, to)
                            .getMetrics()));
        }
        return ret;
    }

    private static Map<String, String> getFactorsIDNames(String projectId) throws IOException {
        if (IDNames == null) {
            IDNames = Common.getIDNames(projectId, Constants.QMLevel.strategic_indicators);
            return IDNames;
        }
        return IDNames;
    }

    public static void resetFactorsIDNames() {
        IDNames = null;
    }

}
