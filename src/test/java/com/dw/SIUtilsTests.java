package com.dw;

import evaluation.Factor;
import util.BayesUtils;
import util.Connection;
import util.Constants;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SIUtilsTests {

    public static void main(String[] args) throws IOException {
        LocalDate dateFrom = LocalDate.of(2019,01, 01);
        LocalDate dateTo = LocalDate.of(2019, 05, 30);
        String projectId="";

        String ip = ""; //Set value before test
        int port = 9200; //Set value before test
        String path= "";
        String prefix = "";
        String username = "";
        String password = "";

        System.err.println("////QUERY TEST UTIL////");
        try {
            //OPEN CONNECTION
            Connection.initConnection(ip, port, path, prefix, username, password);

            //SI UTILS
            System.err.println("getObservedConfigurations");
            //String[] factors = {"activitycompletion", "knownremainingdefects", "productstability"};
            //String[] elements = {"developmenttaskcompletion", "specificationtaskcompletion", "posponedissuesratio",
              //      "buildstability", "criticalissuesratio", "testsuccess"};
            String[] elements = {"posponedissuesratio"};
            Constants.QMLevel level = Constants.QMLevel.metrics;
            String[] categories = {"Very Low", "Low", "Medium", "High", "Very High"};
            String[] three_categories = {"Low", "Medium", "High"};
            int desiredIntervals = 5;
            double[] intervals = {0.4f, 0.8f};//{0.4f, 0.6f, 0.85f, 0.95f};

            //DISCRETIZATION
//            double[] equalWidthIntervals = BayesUtils.makeEqualWidthIntervals(desiredIntervals);
//            double[] equalFrequencyIntervals = BayesUtils.makeEqualFrequencyIntervals(desiredIntervals, projectId,
//                    level, elements, dateFrom, dateTo);

            //OBSERVED SCENARIOS
//            Map<List<String>, Integer> observedCombinations = BayesUtils.getCommonConfigurations(projectId, level,
//                    elements, categories, equalWidthIntervals, dateFrom, dateTo);
//            System.out.println(Arrays.toString(elements) + " : findings");
//            observedCombinations.forEach((key, value) -> System.out.println(key + " : " + value));

            //FREQUENCY QUANTIFICATION
            Map<String, Map<String, Float>> elmenentFrequencies = BayesUtils.getFrequencyQuantification(projectId, level,
                    elements, intervals, dateFrom, dateTo);
            elmenentFrequencies.forEach((element, mapElementFreq) -> {
                System.out.println("ELEMENT " + element + " freqüency quantification:");
                mapElementFreq.forEach((interval, percentage) -> System.out.println(interval + " : " + percentage));
            });
        } catch (Exception e) {
            throw e;
        }
        finally {
            Connection.closeConnection();
        }
        }}
