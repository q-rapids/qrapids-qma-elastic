package simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import DTOs.ElemenEvaluationtDTO;
import DTOs.EvaluationDTO;
import DTOs.FactorEvaluationDTO;
import DTOs.MetricEvaluationDTO;

public class Model {
	
	Logger log = Logger.getLogger(this.getClass().getName());
	
	// map factorIds (<projectId>-<factorId>-<evaluationDate>) to FactorEvaluationDTO
	private Map<String, FactorEvaluationDTO> mapFactorIdDTO = new HashMap<>();
	
	// map metricIds (<projectId>-<metricId>-<evaluationDate>) to MetricEvaluationDTO
	private Map<String, MetricEvaluationDTO> mapMetricIdDTO = new HashMap<>();
	
	// unique metricIds and factorIds contained in relations (used for consistency check)
	private Set<String> relationMetricIdSet = new HashSet<>();
	private Set<String> relationFactorIdSet = new HashSet<>();
	
	// map metricId -> Set<FactorsId> 
	// records which factorIds (unique) are influenced by a metricId
	private Map<String, Set<String>> influencedFactors = new HashMap<>();
	
	// factorId -> metricId -> weight
	Map< String, Map< String, Double > > impacts = new HashMap<>();
	
	// set of affected factors
	Set<String> changeFactors = new HashSet<String>();
	
	
	/**
	 * Create Model on metrics / factors (DTOs), and relations SearchResponse (qr-eval)
	 * Assumption: MetricEvaluationDTOs and FactorEvaluationDTOs contain exactly one EvaluationDTO for a specific evaluationDate
	 * 
	 * @param metrics List of MetricEvaluationDTOs for evaluationDate and projectId
	 * @param factors List of FactorEvaluationDTOs for evaluationDate and projectId
	 * @param relations SearchResponse of relation query for metric-factor relations, evaluationDate, and projectId
	 */
	public Model( List<MetricEvaluationDTO> metrics, List<FactorEvaluationDTO> factors, SearchResponse relations ) {
		
		for ( MetricEvaluationDTO medto : metrics ) {
			this.mapMetricIdDTO.put( medto.getEvaluations().get(0).getID(), medto );
		}
		
		for ( FactorEvaluationDTO fedto : factors ) {
			this.mapFactorIdDTO.put( fedto.getEvaluations().get(0).getID(), fedto );
		}
		
		readRelations(relations);
	}

	
	/**
	 * Simulate change of a metric value:
	 * - lookup MetricEvaluationDTO in mapMetricIdDTO
	 * - change EvaluationDTO to simulated value
	 * - updates the list of changeFactors
	 * 
	 * @param metricId The metricId
	 * @param value The simulated value
	 */	
	public void setMetric( String metricId, Double value ) {	
		// change metric evaluation to simulation value
		MetricEvaluationDTO medto = mapMetricIdDTO.get(metricId);
		
		if ( medto == null ) {
			throw new IllegalArgumentException( "MetricEvaluationDTO not found: " + metricId );
		}
		
		setEvaluationDTO(value, medto);

		// factors influenced by the metric
		this.changeFactors.addAll(influencedFactors.get(metricId));		
	}
	
	/**
	 * Simulate change of for a list of metrics:
	 * - lookup MetricEvaluationDTO in mapMetricIdDTO
	 * - change EvaluationDTO to simulated value
	 * - updates the list of changeFactors
	 * @param metrics
	 */
	public void setMetrics (Map <String,Double> metrics){
		for (Map.Entry<String, Double> entry : metrics.entrySet())
		{
		    setMetric(entry.getKey(), entry.getValue());
		}		
	}
	
	
	
	/**
	 * Simulate change of a metric value:
	 * - re-evaluate influenced factors
	 * 
	 * @return Recomputed factors
	 */
	public Collection<FactorEvaluationDTO> simulate() {	
		// for all changeFactors

		for ( String factorId : changeFactors ) {
			
			Double sumWeights = 0.0;
			Double sumValues = 0.0;
			
			for ( String sourceMetricId : impacts.get(factorId).keySet() ) {
				
				MetricEvaluationDTO source = mapMetricIdDTO.get(sourceMetricId);
				Double metricValue = source.getEvaluations().get(0).getValue().doubleValue();
				Double weight = impacts.get(factorId).get(sourceMetricId);
				sumWeights+= weight;
				sumValues+= metricValue * weight;
			}
			
			FactorEvaluationDTO fedto = mapFactorIdDTO.get(factorId);
			setEvaluationDTO( sumValues / sumWeights, fedto );

		}
		
		
		return mapFactorIdDTO.values();
		
	}
	
	/**
	 * Return the actual Collectino of FactorEvaluationDTOs
	 * @return
	 */
	public Collection<FactorEvaluationDTO> getFactors() {
		return mapFactorIdDTO.values();
	}
	
	/**
	 * Read relations 
	 * - build Maps influencedFactors and impact
	 * - build sets relationMetricIdSet and relationFactorIdSet
	 * @param relations
	 */
	private void readRelations(SearchResponse relations) {
		
		// process hits of relations search
		for ( SearchHit hit : relations.getHits().getHits() ) {
			
			String metricId = (String) hit.getSource().get("sourceId");
			String factorId = (String) hit.getSource().get("targetId");
			Double weight = getWeight( hit.getSource().get("weight") );
			
			// record source and target ids
			relationMetricIdSet.add(metricId);
			relationFactorIdSet.add(factorId);
			
			// fill map influencedFactors: metricId -> Set<factorId>
			if ( influencedFactors.containsKey(metricId) ) {
				influencedFactors.get(metricId).add(factorId);
			} else {
				Set<String> factorSet = new HashSet<String>();
				factorSet.add(factorId);
				influencedFactors.put(metricId, factorSet);
			}
			
			if ( !mapMetricIdDTO.containsKey(metricId) ) {
				// relation between metric and factor, but no metricId in mapMetricIdDTO. skipped
				log.warning("Incosistent relation: metricId " + metricId + " referenced by relation, but not read from API. relation-factorId: " + factorId );
				continue;
			}
			
			if ( !mapFactorIdDTO.containsKey(factorId) ) {
				// relation between metric and factor, but no factorId  in mapFactorIdDTO. skipped.
				log.warning("Incosistent relation: factor " + factorId + " referenced by relation-target, but not read from API. relation-metricId: " + metricId );
				continue;
			}

			if ( impacts.containsKey( factorId ) ) {
				
				Map<String,Double> innerMap = impacts.get( factorId );
				
				if ( innerMap.containsKey(metricId) ) {
					// more than one relation between metric and factor
					log.warning("relation: " + metricId + "->" + factorId + " alreaady added to impacts. skipped.");
				} else {
					innerMap.put(metricId, weight);
				}
				
			} else {
				
				Map<String, Double> innerMap = new HashMap<String, Double>();
				innerMap.put(metricId, weight);
				impacts.put(factorId, innerMap);
				
			}
			
		}
		
		validate();
	}

	/**
	 * In some cases relations weight is an Integer instead of a Double
	 * @param weight
	 * @return
	 */
	private double getWeight(Object weight) {
		if ( weight instanceof Integer ) {
			return ( (Integer) weight ).doubleValue();
		} else {
			return ( Double ) weight;
		}
	}

	private void validate() {
		
		// number of metrics/factors derived from relations are also delivered by Metric/Factor API
		if ( relationMetricIdSet.size() != mapMetricIdDTO.size() ) {
			log.warning( "metric count in relations: " + relationMetricIdSet.size() + ", in API: " + mapMetricIdDTO.size());
		}
		
		if ( relationFactorIdSet.size() != mapMetricIdDTO.size() ) {
			log.warning("factor count in relations: " + relationFactorIdSet.size() + ", in API: " + mapFactorIdDTO.size());
		}
		
		// metricId (source of a relation) is contained in mapMetricIdDTO
		for ( String metricId : relationMetricIdSet ) {
			if ( !mapMetricIdDTO.containsKey(metricId) ) {
				log.warning("Metric in relations, but not delivered by Metric API: " + metricId);
			}
		}
		
		// factorId (target of a relation) is contained in relationFactorIdSet
		for ( String factorId : relationFactorIdSet ) {
			if ( !mapFactorIdDTO.containsKey(factorId) ) {
				log.warning("Factor in relations, but not delivered by Factor API: " + factorId);
			}
		}
	}
	


	private void setEvaluationDTO(Double value, ElemenEvaluationtDTO elementEvaluationDTO) {

		EvaluationDTO edto = elementEvaluationDTO.getEvaluations().get(0);
		EvaluationDTO simulated = new EvaluationDTO(edto.getID(), edto.getDatasource(), edto.getEvaluationDate(), value.floatValue(), edto.getRationale() );
		List<EvaluationDTO> ledto = new ArrayList<>();
		ledto.add(simulated);
		elementEvaluationDTO.setEvaluations(ledto);
		
	}
	
}
