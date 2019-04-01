package evaluation;

import DTOs.EstimationEvaluationDTO;
import DTOs.FactorEvaluationDTO;
import DTOs.FactorMetricEvaluationDTO;
import DTOs.MetricEvaluationDTO;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import util.Common;
import util.Constants;
import util.FormattedDates;
import util.Queries;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class Factor {
    private static Map<String, String> IDNames;

    public static Map<List<String>, Integer> getCommonConfigurations(String projectId, String[] factors, String[] categories,
                                                                     float[] ranges, LocalDate from, LocalDate to)
            throws IOException {
        //construir diccionari
        List<String> factorsArray = Arrays.asList(factors);
        Map<List<String>, Integer> observedCombinations = new HashMap<>();

        for (LocalDate currentDay = from; !currentDay.isAfter(to); currentDay = currentDay.plusDays(1)) {
            SearchResponse sr = Queries.getFilteredDay(projectId, Constants.QMLevel.factors, currentDay, factors);
            SearchHits hits = sr.getHits();
            String[] combination = new String[factors.length];
            for (SearchHit hit : hits) {
                //processar factor i correspondencia value - categoria
                Map<String, Object> hitSource = hit.getSource();
                String factor = Queries.getStringFromMap(hitSource, Constants.FACTOR_ID);
                float value = Float.parseFloat(Queries.getStringFromMap(hitSource, Constants.VALUE));
                // construir combinatoria
                int factorIndex = factorsArray.indexOf(factor);
                combination[factorIndex] = discretize(value, ranges, categories);// combinatoria construida
            }
            observedCombinations.merge(Arrays.asList(combination), 1, Integer::sum);
        }
        return observedCombinations;
    }

    public static Map<String, Map<String, Float>> getFrequencyQuantification(String projectId, String[] factors,
                                                                             float[] ranges, LocalDate from,
                                                                             LocalDate to) throws IOException {
        Map<String, Map<String, Float>> ret = new LinkedHashMap<>();
        for (String factor : factors) {
            SearchResponse sr = Queries.getFrequencies(projectId, Constants.QMLevel.factors, factor, from, to, ranges);
            long total = sr.getHits().totalHits;
            Range rangeAggregation = sr.getAggregations().get("categoryranges");
            Map<String, Float> dictFrequenciesFactor = new LinkedHashMap<>();
            for (Range.Bucket rangebucket : rangeAggregation.getBuckets()) {
                String key = String.format("%.2f",rangebucket.getFrom()) + "-" + String.format("%.2f",rangebucket.getTo());//rangebucket.getKeyAsString();          // bucket key
                long docCount = rangebucket.getDocCount();            // Doc count
                dictFrequenciesFactor.put(key, (float)docCount/total);
                System.err.println("Key: " + key + ", Doc Count: " + docCount);
            }
            ret.put(factor, dictFrequenciesFactor);
        }
        return ret;
    }

    private static String discretize(float value, float[] ranges, String[] categories) {
        int i = 1;
        if (value < ranges[0]) return categories[0];
        else if (value >= ranges[ranges.length-1]) return categories[categories.length-1];
        else {
            while (i < ranges.length) {
                if (value < ranges[i]) break;
                i++;
            }
        }
        return categories[i];
    }

    public static double[] makeEqualWidthIntervals(int numBins) {
        double min = 0d;
        double max = 1d;

        double width = (max - min) / numBins;

        double[] intervals = new double[numBins * 2];
        intervals[0] = min;
        intervals[1] = min + width;
        for (int i = 2; i < intervals.length - 1; i += 2) {
            intervals[i] = intervals[i - 1];
            intervals[i + 1] = intervals[i] + width;
        }

        return intervals;
    }

    public static double[] makeEqualFrequencyIntervals(int numBins, String project, String[] factors, LocalDate from,
                                                       LocalDate to) throws IOException {
        double min = 0d;
        double max = 1d;
        /**/
        //numBins = 3;
        /**/
        double[] intervals = new double[numBins * 2];

        SearchResponse sr = Queries.getFactorsAggregations(project, factors, from, to);
        SearchHits hits = sr.getHits();
        int totalHits = (int) hits.getTotalHits();
        /**/
        //totalHits = 9;
        /**/
        Float[] sortedValues = new Float[totalHits];
        int index = 0;
        for (SearchHit hit : hits.getHits()) {
            float value = Float.parseFloat(Queries.getStringFromMap(hit.getSourceAsMap(), Constants.VALUE));
            sortedValues[index] = value;
            index++;
        }
        /**/
        //sortedValues = new Float[]{0F,4F,12F,16F,16F,18F,24F,26F,28F};
        /**/
        int groupsSize = (int) (sortedValues.length / numBins);
        intervals[0] = min;
        intervals[1] = mean(sortedValues[groupsSize-1], sortedValues[groupsSize]);
        for (int i = 2, indexBins = 2; i <= numBins; i += 2, indexBins++) {
            intervals[i] = intervals[i - 1];
            intervals[i + 1] = mean(sortedValues[(groupsSize * indexBins) - 1], sortedValues[groupsSize * indexBins]);
        }
        intervals[intervals.length - 2] = intervals[intervals.length - 3];
        intervals[intervals.length - 1] = max;
        return intervals;
    }

    private static double mean(double d1, double d2) {
        return (d1 + d2) / 2;
    }



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

        return new FactorMetricEvaluationDTO(factorID, factorName, projectId, metricsEval);
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

        return new FactorMetricEvaluationDTO(factorID, factorName, projectId, metricsEval);
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
