package com.dw;

import DTOs.*;
import util.Connection;

import evaluation.Factor;
import evaluation.Metric;
import evaluation.StrategicIndicator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Hello world!
 *
 */
@SuppressWarnings("ALL")
class QueryUtil {

    public static void main(String[] args) throws IOException {
        LocalDate dateFrom = LocalDate.of(2018,03, 01);
        LocalDate dateTo = LocalDate.of(2018, 03, 15);
        String projectId="test";
        String factorCQ = "codequality";
        String strategicIndicatorQ = "productquality";

        String ip = "*"; //Set value before test
        int port = 9200; //Set value before test
        String path= "";
        String prefix = "";
        String username = "";
        String password = "";

        System.err.println("////QUERY TEST UTIL////");
        try {
            //OPEN CONNECTION
            Connection.initConnection(ip, port, path, prefix, username, password);

            //CLASS: PROJECTS
//          System.err.println("-- PROJECT 1 - getProjects");
//          List<String> projects = Project.getProjects();
//          if (projects.size()>0)
//              projectId=projects.get(0);
//          else
//              projectId="default";

            //RELATIONS
            //StrategicIndicator.setStrategicIndicatorFactorRelation(projectId, factorCQ, strategicIndicatorQ, dateTo,
              //      0.7d, 0.75d, null, "0.80");


            StrategicIndicator.setStrategicIndicatorFactorRelation(projectId, "runtimeErrors",
                    "HWReliability", dateTo, 0.0d, 0.4d, "Medium", "High");

            //CLASS: FACTOR
            System.err.println("-- FACTORS 1 - getEvaluations(projectId)");
            List<FactorEvaluationDTO> factorsEvaluationLatest = Factor.getEvaluations(projectId);
            System.err.println("-- FACTORS 2 - getEvaluations(projectId, date, date)");
            List<FactorEvaluationDTO> factorsEvaluationRanged = Factor.getEvaluations(projectId, dateFrom, dateTo);

            System.err.println("-- FACTORS 3 - getMetricsEvaluations(projectId)");
            List<FactorMetricEvaluationDTO> factorsMetricsLatest = Factor.getMetricsEvaluations(projectId);
            System.err.println("-- FACTORS 4 - getMetricsEvaluations(projectId, date, date)");
            List<FactorMetricEvaluationDTO> factorsMetricsRanged = Factor.getMetricsEvaluations(projectId, dateFrom, dateTo);

            System.err.println("-- FACTORS 5 - getMetricsEvaluations(projectId, factor)");
            FactorMetricEvaluationDTO metricsEvaluationLatest = Factor.getMetricsEvaluations(projectId, factorCQ);
            System.err.println("-- FACTORS 6 - getMetricsEvaluations(projectId, factor, date, date)");
            FactorMetricEvaluationDTO metricsEvaluationRanged = Factor.getMetricsEvaluations(projectId, factorCQ, dateFrom, dateTo);

            //CLASS: METRIC
            System.err.println("--  METRIC 1 ");
            List<MetricEvaluationDTO> allMetricsEvaluationLatest = Metric.getEvaluations(projectId);
            System.err.println("-- METRIC 2 ");
            List<MetricEvaluationDTO> allMetricsEvaluationRanged = Metric.getEvaluations(projectId, dateFrom, dateTo);

            //CLASS: STRATEGIC INDICATOR
            System.err.println("-- STRATEGIC INDICATOR 1");
            List<StrategicIndicatorEvaluationDTO> strategicIndicatorsEvaluationLatest = StrategicIndicator.getEvaluations(projectId);
            System.err.println("-- STRATEGIC INDICATOR 2");
            List<StrategicIndicatorEvaluationDTO> strategicIndicatorsEvaluationRanged = StrategicIndicator.getEvaluations(projectId,dateFrom, dateTo);

            System.err.println("-- STRATEGIC INDICATOR 3");
            StrategicIndicatorFactorEvaluationDTO SIfactorsEvaluationLatest = StrategicIndicator.getFactorsEvaluations(projectId,strategicIndicatorQ);
            System.err.println("-- STRATEGIC INDICATOR 4 ");
            StrategicIndicatorFactorEvaluationDTO SIfactorsEvaluationRanged = StrategicIndicator.getFactorsEvaluations(projectId, strategicIndicatorQ, dateFrom, dateTo);

            System.err.println("-- STRATEGIC INDICATOR 5 ");
            List<StrategicIndicatorFactorEvaluationDTO> strategicIndicatorsFactorsLatest = StrategicIndicator.getFactorsEvaluations(projectId);
            System.err.println("-- STRATEGIC INDICATOR 6 ");
            List<StrategicIndicatorFactorEvaluationDTO> strategicIndicatorsFactorsMetricsRanged = StrategicIndicator.getFactorsEvaluations(projectId,dateFrom, dateTo);

            System.err.println("-- STRATEGIC INDICATOR 7 ");
            List<FactorMetricEvaluationDTO> strategicIndicatorFactorsMetricsLatest = StrategicIndicator.getMetricsEvaluations(projectId,strategicIndicatorQ);
            System.err.println("-- STRATEGIC INDICATOR 8");
            List<FactorMetricEvaluationDTO> strategicIndicatorFactorsMetricsRanged = StrategicIndicator.getMetricsEvaluations(projectId, strategicIndicatorQ, dateFrom, dateTo);

            //CLOSE CONNECTION
            System.err.println("-- CLOSE CONNECTION");
            Connection.closeConnection();

        } catch (Exception ex)
        {
            //System.err.println("////QUERY TEST UTIL - ERROR " + ex.toString());
            throw ex;
        }

    }
}

