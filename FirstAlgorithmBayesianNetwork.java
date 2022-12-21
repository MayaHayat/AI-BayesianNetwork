import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class implements the simple deduction algorithm of the Bayesian Network.
 * This class uses the following classes:
 * 	readXmlFile
 * 	Variable
 * 	CPT
 * 	bayesianNetwork
 * @author Maya
 * @version 1.0
 */

public class FirstAlgorithmBayesianNetwork {

	
	/**
	 * This function finally returns the probability of the event happening.
	 * @param input the given query as a string
	 * @param bn is the Bayesian Network
	 * @return the probability, the number of additions, the number of multiplications
	 */
	public static String finalCalculation(String input, bayesianNetwork bn) {
		String variable = convert(input).get(0);
		int numOfOutcomesForV =0 ; 
		int keepRecord [] = new int [3];
		double numerator = 0;
		double denominator = 0;
		double answerNumerator = 0;
		double answerDenominator = 0;
		String s = "";

		// Find whether we can find directly from CPT
		for (int i = 0; i < bn.getBN().size() ; i++) {
			if (variable.equals(bn.getBN().get(i).getName())) {
				if (bn.getBN().get(i).getParents().equals(getGiven(input, bn))) {
					setWantedOutcomesForAll(input,bn,1);
					s =""+ probs(bn.getBN().get(i), bn) + ", 0, 0";
					return s;
				}
			}
		}		
		for (int i = 1 ; i < createAlternatingTable(getHidden(input, bn)).length; i ++) {
			setWantedOutcomesForAll(input,bn,i);

			numerator =probs(bn.getBN().get(0),bn);
			for (int j = 1 ; j < bn.getBN().size() ; j++) {
				numerator *=probs(bn.getBN().get(j),bn);

				keepRecord[2]++;
			}
			answerNumerator += numerator;
			keepRecord[1]++;
		}
		
		for (int i = 1 ; i < createAlternatingTable(getHiddenForDenominator(input, bn)).length; i ++) {
			setWantedOutcomesForDenominator2(input,bn,i);
			denominator = probs(bn.getBN().get(0),bn);
			for (int j = 1 ; j < bn.getBN().size() ; j++) {
				denominator *=probs(bn.getBN().get(j),bn);
			}
			answerDenominator += denominator;
		}

		double answer = answerNumerator/(answerDenominator);


		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if (bn.getBN().get(i).getName().equals(variable))
				numOfOutcomesForV = bn.getBN().get(i).getPossibleOutcomes().size()-1;
		}
		for (int k = 0 ; k < numOfOutcomesForV ; k++) {
			for (int i = 1 ; i < createAlternatingTable(getHidden(input, bn)).length; i ++) {
				setWantedOutcomesForDenominator(input,bn,i,k);
				for (int j = 1 ; j < bn.getBN().size() ; j++) {
					keepRecord[2]++;
				}
				keepRecord[1]++;
			}
		}
		keepRecord[1]--;
		
		String result = String.format("%.5f", answer);

		s+= "" + result + "," + keepRecord[1] +"," + keepRecord[2];
		return s;
	}





	/**
	 * This function find the wanted probability of a specific variable.
	 * @param v is the variable that the cpt table is going to be created around.
	 * @param bn is the bayesian network from which we gather our data.
	 * @return the probability found.
	 */

	public static double probs(Variable v, bayesianNetwork bn) {
		String [][] cpt = v.createTruthTableByVariable(v);
		ArrayList <Integer> rowNum = new ArrayList<>();
		for (int i = 0; i < cpt[0].length-1 ; i++) {
			if (cpt[0][i].equals(v.getName())) {
				for (int j = 1; j < cpt.length ; j++) {
					if (cpt[j][i].equals(v.getWantedOutcome())) {
						rowNum.add(j);
					}
				}
			}
			for (int k = 0 ; k < v.getParents().size() ; k++) {
				if(cpt[0][i].equals(v.getParents().get(k).getName())) {
					for (int j = 1; j < cpt.length ; j++) {
						if (cpt[j][i].equals(v.getParents().get(k).getWantedOutcome())) {
							rowNum.add(j);
						}
					}
				}

			}
		}
		ArrayList<Integer> rows = new ArrayList<>();
		for (int i = 0 ; i < getRowNumber(rowNum, (cpt[0].length-1)).size() ; i++ )
			if (!rows.contains(getRowNumber(rowNum, (cpt[0].length-1)).get(i)))
				rows.add(getRowNumber(rowNum, (cpt[0].length-1)).get(i));

		double wantedProbabilitiy=0;
		for (int i = 0 ; i < rows.size() ; i++) {
			wantedProbabilitiy = Double.parseDouble(cpt[rows.get(i)][cpt[0].length-1]);
		}

		return wantedProbabilitiy;
	}

	/**
	 * This function finds the correct row to receive from the probability later
	 * @param rowNum
	 * @param numberOfcols to look at in function.
	 * @return a list of all rows that contain the wanted outcomes of each variables.
	 */
	public static ArrayList<Integer> getRowNumber(ArrayList<Integer> rowNum , int numberOfcols){
		ArrayList <Integer> numbers = new ArrayList<>();
		for (int i = 0 ; i < rowNum.size() ; i++) {
			int count = 0;
			for (int j = 0 ; j < rowNum.size() ; j++) {
				if (rowNum.get(i)== rowNum.get(j))
					count++;
			}
			if (count == numberOfcols) {
				numbers.add(rowNum.get(i));
			}
		}
		return numbers;
	}


	/**
	 * This function sets outcomes for all variables in the network.
	 * @param input (the query)
	 * @param bn Bayesian Network
	 * @param loopNumber as the hidden's wanted outcomes change every loop.
	 */
	public static void setWantedOutcomesForAll(String input, bayesianNetwork bn, int loopNumber) {
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);
		//Set given wanted outcome
		ArrayList<Variable> given = getGiven(input,bn);

		//Set Variable wanted outcome
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if (bn.getBN().get(i).getName().equals(queryIntoArray[0])) {
				bn.getBN().get(i).setWantedOutcome(queryIntoArray[1]);
			}
		}
		//set hidden's wanted outcome
		ArrayList <String> variablesquery = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i+=2)
			variablesquery.add(queryIntoArray[i]);

		ArrayList <Variable> hidden = new ArrayList<>();
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if (!variablesquery.contains(bn.getBN().get(i).getName())) {
				hidden.add(bn.getBN().get(i));
			}
		}
		String [][] alternating = createAlternatingTable(hidden);
		for (int j = 1 ; j < alternating.length ; j++) {
			for (int i = 0 ; i < hidden.size() ; i++) {
				hidden.get(i).setWantedOutcome(alternating [loopNumber][i]);
			}
		}

	}


	/**
	 * This function only looks at the other outcomes of the variable and ignore its original wanted outcome.
	 * @param input is the query as the received string from the input.txt
	 * @param bn is out network.
	 * @param loopNumber as we need to iterate over the possibilities of hidden in each row.
	 * @param vOutcome the number of possible outcomes for the variable - the given variable in the query.
	 */
	public static void setWantedOutcomesForDenominator(String input, bayesianNetwork bn, int loopNumber, int vOutcome) {
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);
		//Set given wanted outcome
		ArrayList<Variable> given = getGiven(input,bn);
		//Set Variable wanted outcome
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if (bn.getBN().get(i).getName().equals(queryIntoArray[0])) {
				for (int j = 0 ; j < bn.getBN().get(i).getPossibleOutcomes().size() ; j++) {
					if (bn.getBN().get(i).getPossibleOutcomes().get(j).equals(queryIntoArray[1])) {
						bn.getBN().get(i).getPossibleOutcomes().remove(j);
					}
					bn.getBN().get(i).setWantedOutcome(bn.getBN().get(i).getPossibleOutcomes().get(vOutcome));
				}
			}
		}
		//set hidden's wanted outcome
		ArrayList <Variable> hidden = getHidden(input,bn);
		String [][] alternating = createAlternatingTable(hidden);
		for (int j = 1 ; j < alternating.length ; j++) {
			for (int i = 0 ; i < hidden.size() ; i++) {
				hidden.get(i).setWantedOutcome(alternating [loopNumber][i]);

			}
		}
	}

	/**
	 * This function sets the wanted outcomes for the denominator, note that in the denominator we treat the variable as an hidden as its wanted outcome alternates as well.
	 * @param input the query as a string.
	 * @param bn is our network.
	 * @param loopNumber to rotate over our hidden possibilities in alternating table.
	 */

	// Set wanted outcomes for denominator
	public static void setWantedOutcomesForDenominator2(String input, bayesianNetwork bn, int loopNumber) {
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 2 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);
		//Set given wanted outcome
		ArrayList<Variable> given = getGiven(input,bn);
		
		//set hidden's wanted outcome
		ArrayList <Variable> hidden = getHidden(input,bn);
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if (bn.getBN().get(i).getName().equals(queryIntoArray[0]))
				hidden.add(bn.getBN().get(i));
		}
		String [][] alternating = createAlternatingTable(hidden);
	//	printMat(alternating);
//		for (int j = 1 ; j < alternating.length ; j++) {
			for (int i = 0 ; i < hidden.size() ; i++) {
				
				hidden.get(i).setWantedOutcome(alternating [loopNumber][i]);
			}
		//}

	}

	/**
	 * This function creates an alternating table for the hidden variables as we have to change their wanted outcome in each loop we have to assign them different wanted outcome each time.
	 * @param hidden is the array list of the hidden variables - the variables that aren't in the query.
	 * @return a 2D array of the wanted outcomes so we can iterate through.
	 */

	public static String[][] createAlternatingTable(ArrayList<Variable> hidden){
		int rows = 1;
		for (int i = 0 ; i < hidden.size() ; i++) {
			rows *= hidden.get(i).getPossibleOutcomes().size();
		}
		//Note that the first line is the names of variables, we should know that when accessing table to start loop from row 1.
		String [][] alternating = new String [rows+1][hidden.size()];
		for (int i = 0 ; i < alternating[0].length ; i++) {
			alternating [0][i] = hidden.get(i).getName();	
		}

		int divideT = 1;
		for (int k = 0 ; k < hidden.size(); k++) {
			alternating[0][k] = hidden.get(k).getName();
			int numOutcomesT = hidden.get(k).getPossibleOutcomes().size();
			int colNumT = k;
			divideT *= numOutcomesT;
			for (int j = 0 ; j < divideT-1 ; j ++) {
				for (int i = (j)*alternating.length/divideT+1; i < (j+1)*alternating.length/divideT+1; i++) {
					alternating[i][k] = hidden.get(k).getPossibleOutcomes().get(j%numOutcomesT);
				}
			}

			for (int i = (divideT-1)*alternating.length/divideT+1; i <alternating.length ;i++) {
				alternating[i][k] = hidden.get(k).getPossibleOutcomes().get(numOutcomesT-1);
			}
		}
		return alternating;

	}

	/**
	 * This function gets all the variables that aren't in the given query, note that in this function we treat the main variable as an hidden so its wanted value is changing as well.
	 * @param input is the given string.
	 * @param bn is our network that includes all variables.
	 * @return an ArrayList of all the hidden variables.
	 */
	public static ArrayList<Variable> getHiddenForDenominator(String input , bayesianNetwork bn){
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);
		ArrayList <String> variablesquery = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i+=2)
			variablesquery.add(queryIntoArray[i]);

		ArrayList <Variable> hidden = getHidden(input,bn);
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if(bn.getBN().get(i).getName().equals(queryIntoArray[0]))
				hidden.add(bn.getBN().get(i));
		}
		
		return hidden;
	}


	/**
	 * This function gets all the variables that aren't in the given query.
	 * @param input is the given string.
	 * @param bn is our network that includes all variables.
	 * @return an ArrayList of all the hidden variables.
	 */
	public static ArrayList<Variable> getHidden(String input , bayesianNetwork bn){
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);

		ArrayList <String> variablesquery = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i+=2)
			variablesquery.add(queryIntoArray[i]);

		ArrayList <Variable> hidden = new ArrayList<>();
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if (!variablesquery.contains(bn.getBN().get(i).getName())) {
				hidden.add(bn.getBN().get(i));
			}
		}
		return hidden;
	}


	//Get given as an ArrayList of Variables
	public static ArrayList<Variable> getGiven(String input, bayesianNetwork bn){
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);

		ArrayList <String> variablesquery = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i+=2)
			variablesquery.add(queryIntoArray[i]);

		ArrayList<Variable> given = new ArrayList<>();
		for (int i = 0 ; i < bn.getBN().size() ; i ++) {
			for (int k = 0 ; k < variablesquery.size() ; k++) {
				for (int j = 2; j < query.size() ; j+=2) {
					if (bn.getBN().get(i).getName().equals(variablesquery.get(k)) && bn.getBN().get(i).getName().equals(query.get(j))) {
						bn.getBN().get(i).setWantedOutcome(query.get(j+1));
						given.add(bn.getBN().get(i));
					}
				}
			}
		}
		return given;
	}

	//	public static void getVariable(String input, bayesianNetwork bn) {
	//		String basic = input.substring(2, input.length()-3);
	//		String [] queryIntoArray = basic.split("[\\|=,]");
	//		for (int i = 0 ; i < bn.getBN().size() ; i++) {
	//			if (bn.getBN().get(i).getName().equals(queryIntoArray[0])) {
	//				bn.getBN().get(i).setWantedOutcome(queryIntoArray[1]);
	//			}
	//		}
	//	}

	//Convert from String input to arrayList of all variables
	public static ArrayList<String> convert(String s) {
		String basic = s.substring(2, s.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);
		return query;
	}


	//Print Mat
	public static void printMat(String [][] s) {
		for (int i = 0 ; i < s.length ; i++) {
			for (int j = 0 ; j < s[0].length ; j++) {
				System.out.print(s[i][j] + ", ");
			}
			System.out.println(" ");
		}
	}

}