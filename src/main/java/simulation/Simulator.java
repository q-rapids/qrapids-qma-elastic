package simulation;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.elasticsearch.action.search.SearchResponse;

import DTOs.FactorEvaluationDTO;
import DTOs.MetricEvaluationDTO;
import evaluation.Factor;
import evaluation.Metric;
import util.Connection;
import util.Queries;

public class Simulator {
	
	Logger log = Logger.getLogger(this.getClass().getName());
	

	
	// convert String date (like 2019-01-15) into LocalDate
	final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	/**
	 * Fetch data for Model creation, check data is available, and create Model
	 * @param projectId
	 * @param evaluationDate
	 * @return Model
	 * @throws IOException
	 */
	public static Model createModel( String projectId, String evaluationDate ) throws IOException {
		
		LocalDate localEvaluationDate = LocalDate.parse(evaluationDate, dtf);
		
		List<MetricEvaluationDTO> metrics = Metric.getEvaluations( projectId, localEvaluationDate, localEvaluationDate );
		List<FactorEvaluationDTO> factors = Factor.getEvaluations( projectId, localEvaluationDate, localEvaluationDate );
		SearchResponse relations = Queries.getFactorMetricsRelations(  projectId, evaluationDate );
		
		if ( metrics.size() == 0 ) {
			throw new IllegalArgumentException("No metrics found for projectId " + projectId + ", evaluationDate " + evaluationDate);
		}
		
		if ( factors.size() == 0 ) {
			throw new IllegalArgumentException("No factors found for projectId " + projectId + ", evaluationDate " + evaluationDate);
		}
		
		if ( relations.getHits().totalHits  == 0 ) {
			throw new IllegalArgumentException("No relations found for projectId " + projectId + ", evaluationDate " + evaluationDate);
		}
		
		return new Model(metrics, factors, relations);

	}
	
	

	/**
	 * Helper: print factor values
	 * @param factors
	 */
	public static void factorPrinter( Collection<FactorEvaluationDTO> factors ) {
		
		for ( FactorEvaluationDTO fedto : factors ) {
			System.out.println(fedto.getFactorEntryID(0) + ": " + fedto.getEvaluations().get(0).getValue());
		}
		
		System.out.println();
	}
	

	 

}
