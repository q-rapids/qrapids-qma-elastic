package util;

import DTOs.*;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Common {

    /*
    * NOTE: processMetricsBuckets, processFactorsBuckets and processStrategicIndicatorsBuckets are almost the same, but
    * are separated as in the future there can be changes in the DTOs that require specific processing
    * */

    public static List<MetricEvaluationDTO> processMetricsBuckets(Terms agg) {
        List<MetricEvaluationDTO> ret = new ArrayList<>();

        for (Terms.Bucket entry : agg.getBuckets()) {
            String key = entry.getKey().toString();          // bucket key
            long docCount = entry.getDocCount();            // Doc count
            System.err.println("Key: " + key + ", Doc Count: " + docCount);

            MetricEvaluationDTO metricEval = new MetricEvaluationDTO();
            List<EvaluationDTO> evals = new ArrayList<>();
            // We ask for top_hits for each bucket
            TopHits topHits = entry.getAggregations().get("latest");
            for (SearchHit hit : topHits.getHits().getHits()) {
                System.err.println("ID: " + hit.getId() + ", Source: " + hit.getSourceAsString());

                Map<String, Object> result = hit.getSource();

                EvaluationDTO eval = new EvaluationDTO(
                        hit.getId(),
                        Queries.getStringFromMap(result, Constants.DATA_SOURCE),
                        Queries.getStringFromMap(result, Constants.EVALUATION_DATE),
                        Queries.getStringFromMap(result, Constants.VALUE),
                        Queries.getStringFromMap(result, Constants.RATIONALE));

                evals.add(eval);

                metricEval.setID(Queries.getStringFromMap(result, Constants.METRIC_ID));
                metricEval.setName(Queries.getStringFromMapOrDefault(result, Constants.NAME, metricEval.getID()));
                metricEval.setDescription(Queries.getStringFromMap(result, Constants.DESCRIPTION));
            }
            metricEval.setEvaluations(evals);
            ret.add(metricEval);

            System.err.println();
        }
        return ret;
    }

    public static List<FactorEvaluationDTO> processFactorsBuckets(Terms agg) {
        List<FactorEvaluationDTO> ret = new ArrayList<>();

        for (Terms.Bucket entry : agg.getBuckets()) {
            String key = entry.getKey().toString();          // bucket key
            long docCount = entry.getDocCount();            // Doc count
            System.err.println("Key: " + key + ", Doc Count: " + docCount);

            FactorEvaluationDTO factorEval = new FactorEvaluationDTO();
            List<EvaluationDTO> evals = new ArrayList<>();
            // We ask for top_hits for each bucket
            TopHits topHits = entry.getAggregations().get("latest");
            for (SearchHit hit : topHits.getHits().getHits()) {
                System.err.println("ID: " + hit.getId() + ", Source: " + hit.getSourceAsString());

                Map<String, Object> result = hit.getSource();

                EvaluationDTO eval = new EvaluationDTO(
                        hit.getId(),
                        Queries.getStringFromMap(result, Constants.DATA_SOURCE),
                        Queries.getStringFromMap(result, Constants.EVALUATION_DATE),
                        Queries.getStringFromMap(result, Constants.VALUE),
                        Queries.getStringFromMap(result, Constants.RATIONALE));

                evals.add(eval);

                factorEval.setID(Queries.getStringFromMap(result, Constants.FACTOR_ID));
                factorEval.setName(Queries.getStringFromMapOrDefault(result, Constants.NAME, factorEval.getID()));
                factorEval.setDescription(Queries.getStringFromMapOrDefault(result, Constants.DESCRIPTION, ""));
                factorEval.setProject(Queries.getStringFromMapOrDefault(result, Constants.PROJECT, ""));
                factorEval.setStrategicIndicators(Queries.getArrayListFromMap(result, Constants.ARRAY_STRATEGIC_INDICATORS));
            }
            factorEval.setEvaluations(evals);
            ret.add(factorEval);

            System.err.println();
        }
        return ret;
    }

    public static List<StrategicIndicatorEvaluationDTO> processStrategicIndicatorsBuckets(Terms agg) {
        List<StrategicIndicatorEvaluationDTO> ret = new ArrayList<>();

        for (Terms.Bucket entry : agg.getBuckets()) {
            String key = entry.getKey().toString();          // bucket key
            long docCount = entry.getDocCount();            // Doc count
            System.err.println("Key: " + key + ", Doc Count: " + docCount);

            StrategicIndicatorEvaluationDTO siEval = new StrategicIndicatorEvaluationDTO();
            List<EvaluationDTO> evals = new ArrayList<>();
            List<EstimationEvaluationDTO> estimations = new ArrayList<>();
            // We ask for top_hits for each bucket
            TopHits topHits = entry.getAggregations().get("latest");
            for (SearchHit hit : topHits.getHits().getHits()) {
                System.err.println("ID: " + hit.getId() + ", Source: " + hit.getSourceAsString());

                Map<String, Object> result = hit.getSource();

                EvaluationDTO eval = new EvaluationDTO(
                        hit.getId(),
                        Queries.getStringFromMap(result, Constants.DATA_SOURCE),
                        Queries.getStringFromMap(result, Constants.EVALUATION_DATE),
                        Queries.getStringFromMap(result, Constants.VALUE),
                        Queries.getStringFromMap(result, Constants.RATIONALE)
                        );

                evals.add(eval);

                List<QuadrupletDTO<Integer, String, Float, Float>> estimation = new ArrayList<>();

                ArrayList allEstimations = Queries.getArrayListFromMap(result, Constants.ESTIMATION);
                if (allEstimations != null) {
                    for (Object e : allEstimations) {
                        estimation.add(new QuadrupletDTO<Integer, String, Float, Float>(Queries.getIntFromMap((Map<String, Object>) e, Constants.ESTIMATION_ID), Queries.getStringFromMap((Map<String, Object>) e, Constants.ESTIMATION_LABEL), Queries.getFloatFromMap((Map<String, Object>) e, Constants.ESTIMATION_VALUE), null));
                    }

                    estimations.add(new EstimationEvaluationDTO(estimation));
                } else estimations.add(null);
                siEval.setID(Queries.getStringFromMap(result, Constants.STRATEGIC_INDICATOR_ID));
                siEval.setName(Queries.getStringFromMapOrDefault(result, Constants.NAME, siEval.getID()));
                siEval.setDescription(Queries.getStringFromMap(result, Constants.DESCRIPTION));
                siEval.setProject(Queries.getStringFromMap(result, Constants.PROJECT));
            }
            siEval.setEvaluations(evals);
            siEval.setEstimation(estimations);
            ret.add(siEval);

            System.err.println();
        }
        return ret;
    }

    public static Map<String, String> getIDNames(String projectId, Constants.QMLevel QMLevel) throws IOException {
        Map<String, String> IDNames = new HashMap<>();
        SearchResponse sr = Queries.getLatest(projectId, QMLevel);

        Terms agg = sr.getAggregations().get("IDGroup");
        for (Terms.Bucket entry : agg.getBuckets()) {
            String key = entry.getKey().toString();          // bucket key
            TopHits topHits = entry.getAggregations().get("latest");
            for (SearchHit hit : topHits.getHits().getHits()) {
                Map<String, Object> result = hit.getSource();
                IDNames.putIfAbsent(key, Queries.getStringFromMap(result, "name"));
            }
        }
        return IDNames;
    }

}
