package com.dw;

import java.io.IOException;
import java.util.Collection;

import DTOs.FactorEvaluationDTO;
import simulation.Model;
import simulation.Simulator;
import util.Connection;

public class SimulatorTest {
	// usage example
	public static void main(String[] args)  {

		try {

			Connection.initConnection("", 0, "", "", "", ""); //set correct values before running tests.
			
			
			// create a simulation model for projectId and evaluationDate
			Model model = Simulator.createModel( "modelio38", "2019-01-14");
			
			Simulator.factorPrinter( model.getFactors() );
			
			// simulate effects of changing a metric evaluation value 
			model.setMetric("modelio38-bugcorrection-2019-01-14", 0.5);
			Collection<FactorEvaluationDTO> factors = model.simulate();
			
			Simulator.factorPrinter( factors );
			
			model.setMetric("modelio38-bugcorrection-2019-01-14", 0.0);
			factors = model.simulate();
			
			Simulator.factorPrinter( factors );
			
			
			Connection.closeConnection();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
