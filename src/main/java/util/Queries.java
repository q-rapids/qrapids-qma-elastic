package util;

import DTOs.EstimationEvaluationDTO;
import DTOs.EvaluationDTO;
import DTOs.FactorEvaluationDTO;
import DTOs.QuadrupletDTO;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class Queries {
    public static String getStringFromMap(Map<String, Object> map, String k) {
        return String.valueOf(map.get(k));
    }

    public static ArrayList getArrayListFromMap(Map<String, Object> map, String k) {
        return (ArrayList)map.get(k);
    }

    public static Integer getIntFromMap(Map<String, Object> map, String k) {
        return (Integer)map.get(k);
    }

    public static Float getFloatFromMap(Map<String, Object> map, String k) {
        System.out.println(map.get(k));
        return Float.valueOf(String.valueOf(map.get(k)));
    }

    public static String getStringFromMapOrDefault(Map<String, Object> map, String k, String def) {
        String valueOfmap = String.valueOf(map.get(k));
        if (valueOfmap.equals("null")) {
            return def;
        } else {
            return valueOfmap;
        }
    }

    public static String getStringFromStringMapOrDefault(Map<String, String> map, String k, String def) {
        String valueOfmap = String.valueOf(map.get(k));
        if (valueOfmap.equals("null")) {
            return def;
        } else {
            return valueOfmap;
        }
    }

    private static String getIndexPath(String element_index_name, String projectId) {
        String index=Constants.PATH + Constants.INDEX_PREFIX + element_index_name;
        if (projectId!=null && !projectId.isEmpty() && !projectId.equalsIgnoreCase("EMPTY")&&
                !projectId.equalsIgnoreCase("\"\""))
            index=index.concat("."+projectId);
        return index;
    }
    private static String getFactorsIndex(String projectId) {
        return getIndexPath(Constants.INDEX_FACTORS, projectId);
    }

    private static String getStrategicIndicatorsIndex(String projectId) {
        return getIndexPath(Constants.INDEX_STRATEGIC_INDICATORS, projectId);
    }
    private static String getMetricsIndex(String projectId)
    {
        return getIndexPath(Constants.INDEX_METRICS, projectId);
    }

    public static SearchResponse getLatest(Constants.QMLevel QMLevel, String projectId,  String parent) throws IOException {
        RestHighLevelClient client = Connection.getConnectionClient();
        return client.search(new SearchRequest(getIndex(projectId, QMLevel))
                .source(new SearchSourceBuilder()
                        .query(getLatestParentQueryBuilder(parent, QMLevel))
                        .size(0)
                        .aggregation(
                                AggregationBuilders.terms("IDGroup").field(getIDtoGroup(QMLevel)).size(10000)
                                        .subAggregation(
                                                AggregationBuilders.topHits("latest")
                                                        .sort(Constants.EVALUATION_DATE, SortOrder.DESC)
                                                        .explain(true)
                                                        .size(1)
                                        )
                        )
                )

        );
    }

    public static SearchResponse getLatest(String projectId, Constants.QMLevel QMLevel) throws IOException {
        return getLatest(QMLevel, projectId,"all");
    }

    private static String getIndex(String projectId, Constants.QMLevel QMLevel) {
        String index="";
        switch (QMLevel) {
            case strategic_indicators:
                index = getStrategicIndicatorsIndex(projectId);break;
            case factors:
                index = getFactorsIndex(projectId);break;
            case metrics:
                index = getMetricsIndex(projectId);break;
        }
//        System.out.println("GET INDEX: " + index);
        return index;
    }

    // These two functions could be transformed into two Hashmaps
    private static String getIDtoGroup(Constants.QMLevel QMLevel) {
        String group;
        switch (QMLevel) {
            case strategic_indicators:
                group = Constants.STRATEGIC_INDICATOR_ID;break;
            case factors:
                group = Constants.FACTOR_ID; break;
            case metrics:
                group = Constants.METRIC_ID; break;
            default:
                group="";
        }
        System.out.println("IDto GROUP: " + group);
        return group;
    }


    // parent means that you are searching elements that are related to other, e.g. the factors for a specific SI
    private static QueryBuilder getLatestParentQueryBuilder(String parent, Constants.QMLevel QMLevel) {
        QueryBuilder query=null;

        if (parent.equals("all")) {
            query = QueryBuilders.matchAllQuery();
        }
        else {
            if (QMLevel==Constants.QMLevel.metrics) {
                query = QueryBuilders
                        .boolQuery()
                        .must(new TermQueryBuilder(Constants.ARRAY_FACTORS, parent));
            }
            else {
                // The strategic indicators in the factors index contains the evaluation date --> we need to use wildcards
                query = QueryBuilders.
                        boolQuery()
                        .must( new WildcardQueryBuilder(Constants.ARRAY_STRATEGIC_INDICATORS, parent + "*"));
            }
        }
        return query;
    }

    private static QueryBuilder getRangedParentQueryBuilder(String parent, Constants.QMLevel QMLevel,
                                                            LocalDate dateFrom, LocalDate dateTo) {
        String from, to;
        from = FormattedDates.formatDate(dateFrom);
        to = FormattedDates.formatDate(dateTo);

        QueryBuilder query=null;
        if (parent.equals("all")) {
            query = QueryBuilders
                    .rangeQuery(Constants.EVALUATION_DATE)
                    .gte(from)
                    .lte(to);
        }
        else  {
            if (QMLevel==Constants.QMLevel.metrics) {
                query = QueryBuilders
                        .boolQuery()
                        .must(new TermQueryBuilder(Constants.ARRAY_FACTORS, parent))
                        .filter(QueryBuilders
                                .rangeQuery(Constants.EVALUATION_DATE)
                                .gte(from)
                                .lte(to));
            }
            else {
                query = QueryBuilders
                        .boolQuery()
                        .must(new WildcardQueryBuilder(Constants.ARRAY_STRATEGIC_INDICATORS, parent + "*"))
                        .filter(QueryBuilders
                                .rangeQuery(Constants.EVALUATION_DATE)
                                .gte(from)
                                .lte(to));
            }
            return query;
        }
        return query;
    }

    public static SearchResponse getRanged(Constants.QMLevel QMLevel, String projectId , String parent,
                                           LocalDate dateFrom, LocalDate dateTo) throws IOException {

        RestHighLevelClient client = Connection.getConnectionClient();
        return client.search(new SearchRequest(getIndex(projectId, QMLevel))
                .searchType(SearchType.QUERY_THEN_FETCH)
                .source(
                        new SearchSourceBuilder()
                                .size(0)
                                .query(getRangedParentQueryBuilder(parent, QMLevel, dateFrom, dateTo))
                                .aggregation(
                                        AggregationBuilders.terms("IDGroup").field(getIDtoGroup(QMLevel)).size(10000)
                                                .subAggregation(
                                                        AggregationBuilders.topHits("latest")
                                                                .sort(Constants.EVALUATION_DATE, SortOrder.ASC)
                                                                .explain(true)
                                                                .size(10000)
                                                )
                                )
                )
        );
    }

    public static SearchResponse getRanged(Constants.QMLevel QMLevel, String projectId, LocalDate dateFrom, LocalDate dateTo)
            throws IOException {
        return getRanged(QMLevel, projectId,"all", dateFrom, dateTo);
    }

    public static UpdateResponse setStrategicIndicatorValue(Constants.QMLevel QMLevel,
                                                            String hardID,
                                                            String projectId,
                                                            String strategicIndicatorID,
                                                            String strategicIndicatorName,
                                                            String strategicIndicatorDescription,
                                                            LocalDate evaluationDate,
                                                            Float value,
                                                            EstimationEvaluationDTO estimation,
                                                            List<String> missingFactors,
                                                            long datesMismatch)
            throws IOException {

        XContentBuilder indexReqObj = jsonBuilder()
                .startObject()
                .field(Constants.PROJECT, projectId)
                .field(Constants.STRATEGIC_INDICATOR_ID, strategicIndicatorID)
                .field(Constants.EVALUATION_DATE, evaluationDate)
                .field(Constants.DATA_SOURCE, "QRapids Dashboard")
                .field(Constants.NAME, strategicIndicatorName)
                .field(Constants.DESCRIPTION, strategicIndicatorDescription);

        // If the entry already exists, we update the information about the assessment
        XContentBuilder updateReqObj = jsonBuilder().startObject();

        updateReqObj.field(Constants.VALUE, value);
        indexReqObj.field(Constants.VALUE, value);
        updateReqObj.field(Constants.MISSING_FACTORS, missingFactors);
        indexReqObj.field(Constants.MISSING_FACTORS, missingFactors);
        updateReqObj.field(Constants.DATES_MISMATCH, datesMismatch);
        indexReqObj.field(Constants.DATES_MISMATCH, datesMismatch);

        if (estimation != null) {
            updateReqObj.startArray(Constants.ESTIMATION);
            indexReqObj.startArray(Constants.ESTIMATION);
            for (QuadrupletDTO<Integer, String, Float, Float> e : estimation.getEstimation()) {
                indexReqObj.startObject();
                updateReqObj.startObject();
                indexReqObj.field(Constants.ESTIMATION_ID, e.getFirst());
                updateReqObj.field(Constants.ESTIMATION_ID, e.getFirst());
                indexReqObj.field(Constants.ESTIMATION_LABEL, e.getSecond());
                updateReqObj.field(Constants.ESTIMATION_LABEL, e.getSecond());
                indexReqObj.field(Constants.ESTIMATION_VALUE, e.getThird());
                updateReqObj.field(Constants.ESTIMATION_VALUE, e.getThird());
                indexReqObj.field(Constants.ESTIMATION_UPPER_THRESHOLD, e.getFourth());
                updateReqObj.field(Constants.ESTIMATION_UPPER_THRESHOLD, e.getFourth());
                indexReqObj.endObject();
                updateReqObj.endObject();
            }
            updateReqObj.endArray();
            indexReqObj.endArray();
        }
        updateReqObj.endObject();
        indexReqObj.endObject();

        RestHighLevelClient client = Connection.getConnectionClient();
        IndexRequest indexRequest = new IndexRequest(getIndex(projectId, QMLevel),
                                                    Constants.INDEX_STRATEGIC_INDICATORS,
                                                    hardID)
                .source(indexReqObj);

        UpdateRequest updateRequest = new UpdateRequest(getIndex(projectId, QMLevel),
                                                        Constants.INDEX_STRATEGIC_INDICATORS,
                                                        hardID)
                .doc(updateReqObj)
                .upsert(indexRequest);
        return client.update(updateRequest);
    }

    // Funtion that updates the factors' index with the information of the strategic indicators using
    // a concrete factor evaluation. These entries already exist in the factors' index.
    public static UpdateResponse setFactorStrategicIndicatorRelation(FactorEvaluationDTO factor)
            throws IOException {

        String index_name = getIndex(factor.getProject(), Constants.QMLevel.factors);

        UpdateResponse response = new UpdateResponse();
        RestHighLevelClient client = Connection.getConnectionClient();

        String factorID;
        int index=0;

        if (factor.getEvaluations().isEmpty()) {
        }
        else {
            for (EvaluationDTO eval: factor.getEvaluations())
            {
                factorID = factor.getFactorEntryID(index++);

                IndexRequest indexReq = new IndexRequest(
                        index_name,
                        Constants.FACTOR_TYPE,
                        factorID)
                        .source(jsonBuilder()
                                .startObject()
                                .endObject());

                UpdateRequest updateReq = new UpdateRequest (
                        index_name,
                        Constants.FACTOR_TYPE,
                        factorID)
                        .doc(jsonBuilder()
                                .startObject()
//                                .array(Constants.ARRAY_STRATEGIC_INDICATORS, factor.getStrategicIndicators())
                                .field(Constants.ARRAY_STRATEGIC_INDICATORS, factor.getStrategicIndicators())
                                .endObject())
                        .upsert(indexReq);
                response = client.update(updateReq);
            }

        }

        return response;
    }


    public static Response getIndexes() throws IOException {
        RestClient client = Connection.getLowLevelConnectionClient();
        Map<String, String> params = Collections.emptyMap();

        String query = "/" + Constants.PATH +"_cat/indices?format=json";

        System.out.println(query);
        return client.performRequest("GET",query, params);
    }

}



