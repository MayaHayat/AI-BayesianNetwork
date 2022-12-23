import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This class implements the variable elimination in a different variable sort algorithm of the Bayesian Network.
 * This class uses the following classes:
 * 	readXmlFile
 * 	Variable
 * 	CPT
 * 	bayesianNetwork
 * @author Maya
 * @version 1.0
 */


public class ThirdAlgorithm {

	/**
	 * THE ALGORITHM IS DIVIDED INTO A COUPLE OF STEPS:
	 * Turn CPTs into factors according to the evidence (deleting all irrelevant columns and rows).
	 * If a factor is one valued we can disregard it.
	 * While there are still hidden variables:
	 * 		Pick a hidden variable
	 * 		Join all factors that mention it
	 * 		Eliminate the variable by summing it out
	 * 		If factor becomes one valued disregard it
	 * Join remaining factors.
	 * Normalize.
	 */


	/**
	 * This function calculates the probability, number of additions and multiplications of the given query.
	 * @param input represents the query
	 * @param bn is the network
	 * @return a string that contains probability, number of additions and multiplications.
	 */

	public static String getProbability(String input, bayesianNetwork bn) {
		ArrayList<Variable> hiddenSorted = new ArrayList<>();
		String s = "";
		int additions = 0; 
		int multiplications = 0;
		String variable = convert(input).get(0);

		//CHECK if is already found in tables
		for (int i = 0; i < bn.getBN().size() ; i++) {
			if (variable.equals(bn.getBN().get(i).getName())) {
				if (bn.getBN().get(i).getParents().equals(getGiven(input, bn))) {
					setWantedOutcomesForAll(input,bn,1);
					s =""+ probs(bn.getBN().get(i), bn) + ",0,0";
					return s;
				}
			}
		}


		//Find if probability == 0
		if (isZero(input, bn)) {
			s="0.00000,0,0";
			return s;
		}

		ArrayList <Variable> relevant = relevant(input, bn); 
		relevant.sort(null);
		ArrayList<Variable> allHidden = getHidden(input,bn); // Find all hidden variables.
		ArrayList<Variable> irrelevant = new ArrayList<>(); // is all variables we need to remove from calculations
		ArrayList <Variable> hidden = new ArrayList<>(); // all relevant hidden variables.
		for (int i = 0 ; i < allHidden.size() ; i ++) {
			if (relevant.contains(allHidden.get(i))) {
				hidden.add(allHidden.get(i));
			}
			else {
				irrelevant.add(allHidden.get(i));
			}
		}

		// Add all factors from network to an arraylist of factors.
		ArrayList<Factor> allFactors = new ArrayList<>();
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			Factor temp = new Factor(bn.getBN().get(i), bn);
			allFactors.add(temp);
		}
		// Remove all factors that contain irrelevant variables in them.
		for (int i = 0 ; i < allFactors.size() ; i++) {
			for (int j = 0 ; j < irrelevant.size() ; j++) {
				if (allFactors.get(i).getFactor().get(0).contains(irrelevant.get(j).getName())) {
					allFactors.remove(i);
					i=0;
				}
			}
		}

		//Get rid of all one valued tables, this affects the number of calculations.
		for (int i = 0 ; i < allFactors.size() ; i++) {
			for (int j = i+1 ; j < allFactors.size() ; j++) {
				if (allFactors.get(i).getFactor().equals(allFactors.get(j).getFactor())) {
					allFactors.remove(j);
					j--;
				}
			}
		}

		// Sort the variables.

		int multiplyCurrentSize; // how many additions were in each factor
		for (int i = 0 ; i < allFactors.size() ; i++) {
			if (allFactors.get(i).getFactor().get(0).size() < 2) {

				allFactors.remove(i);
				i=0;
			}
		}
		allFactors.sort(null); // Sort all factors according to size.

		hidden = sort(hidden, allFactors);
		//Start the joining and eliminating process.
		for (int i = 0 ; i < hidden.size() ; i++) {
			multiplyCurrentSize = 0;
			hidden = sort(hidden, allFactors);
			Factor current = join(hidden.get(i), allFactors, bn);
			multiplications +=current.getMultiplications();

			multiplyCurrentSize = current.getFactor().size()-1; //This is used to calculate the num of additions, find size of factor before elimination
			current.eliminateVariable(hidden.get(i), bn);

			multiplyCurrentSize/= (current.getFactor().size()-1); //This is used to calculate the num of additions, find out by how much the table has shrank.
			additions +=(current.getFactor().size()-1) * (multiplyCurrentSize-1);
			for (int j = 0 ; j< allFactors.size()  ; j++) {
				if (allFactors.get(j).getFactor().get(0).contains(hidden.get(i).getName())) {
					allFactors.remove(j);
					j=0;	
				}
			}
		}
		// Make sure the variable has to correct wanted outcome as it might change in the elimination function.
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		Variable mainVariable = getMainVariable(input, bn);
		// Make sure we only have factors that aren't one valued.
		for (int i = 0 ; i < allFactors.size() ; i++) {
			if (allFactors.get(i).getFactor().get(0).size() < 2) {
				allFactors.remove(i);
				i=0;
			}
		}
		int allFactorsSize = allFactors.size();
		Factor finalFactor = join(mainVariable, allFactors, bn);

		if (allFactorsSize > 1)
			multiplications += finalFactor.getMultiplications();

		mainVariable.setWantedOutcome(queryIntoArray[1]);
		// The normalization part
		double sum = 0;
		for (int i = 1 ; i < finalFactor.getFactor().size() ; i++) {
			sum += Double.parseDouble(finalFactor.getFactor().get(i).get(finalFactor.getFactor().get(i).size()-1));
			additions++;
		}
		additions+=-1;
		double alpha = 1/sum;
		double probability = 0;
		for (int i = 1 ; i < finalFactor.getFactor().size() ; i++) {
			if (finalFactor.getFactor().get(i).get(0).equals(mainVariable.getWantedOutcome())) {
				probability = Double.parseDouble(finalFactor.getFactor().get(i).get(finalFactor.getFactor().get(i).size()-1));
			}
		}
		probability*=alpha;
		//multiplications ++;

		String result = String.format("%.5f", probability);


		s+=""+result + "," + additions +"," + multiplications;
		return s;
	}



	/**
	 * This function finds out if the probability is 0
	 * @param input String query
	 * @param bn is the network
	 * @return whether the probability is 0
	 */

	public static boolean isZero(String input, bayesianNetwork bn) {
		ArrayList<Variable> given = getGiven(input, bn);
		ArrayList<String> queryAll = convert(input);
		for (int i = 0 ; i < given.size() ; i++) {
			if (given.get(i).getName().equals(queryAll.get(0)) && !given.get(i).getWantedOutcome().equals(queryAll.get(1))) {
				return true;
			}
		}
		return false;
	}


	/**
	 * This function sorts the variables according the heuristic chosen
	 * @param hidden
	 * @param allFactors
	 * @return a sorted arraylist of variables
	 */

	public static ArrayList<Variable> sort(ArrayList <Variable> hidden, ArrayList <Factor> allFactors) {
		//ArrayList<Variable> sorted = new ArrayList<>();
		HashMap <Variable, Integer> countAppearance = new HashMap<>();
		int count = 0;
		for (int i = 0 ; i < hidden.size() ; i++) {
			count = 0;
			for (int j = 0 ; j < allFactors.size() ; j++) {

				if (allFactors.get(j).getFactor().get(0).contains(hidden.get(i).getName())) {
					count+= allFactors.get(j).getFactor().size()-1;
				}
				countAppearance.put(hidden.get(i), count);
			}
		}
		Map<Variable, Integer> sortedMap = countAppearance.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
						(oldValue, newValue) -> oldValue, LinkedHashMap::new));	

		Set<Variable> keySet =  sortedMap.keySet();
		ArrayList<Variable> sorted = new ArrayList<>(keySet);
		Collections.reverse(sorted);

		return sorted;
	}



	/**
	 * This function finds all relevant variables for the calculation
	 * @param input the query sting
	 * @param bn is the network
	 * @return all the variables we don't want to eliminate
	 */

	public static ArrayList<Variable> relevant(String input, bayesianNetwork bn){
		ArrayList<Variable> hidden = getHidden(input, bn);
		ArrayList<Variable> query = new ArrayList<>();
		for (int i = 0; i < bn.getBN().size() ; i++) {
			if (!hidden.contains(bn.getBN().get(i))) {
				query.add(bn.getBN().get(i));
			}
		}
		int start = 0;
		int finish = Integer.MAX_VALUE;

		ArrayList <Factor> allFactors = new ArrayList<>();
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			Factor current = new Factor(bn.getBN().get(i), bn);
			allFactors.add(current);
		}

		while (finish!= start) {
			start = hidden.size();
			for (int i = 0 ; i < hidden.size() ; i++) {
				if (leaf(allFactors, hidden.get(i)) && !query.contains(hidden.get(i))) { // CHECK IF THE && IS CORRECT
					ArrayList<Factor> containV = factorsContainV(hidden.get(i), bn);
					for (int j = 0 ; j <allFactors.size() ; j++) {
						for (int k = 0 ; k < containV.size() ; k++) {
							if (allFactors.get(j).getFactor().equals(containV.get(k).getFactor())) {
								allFactors.remove(j);
								j=0; // CHECKKKKKKKK

							}
						}
					}
					hidden.remove(i);
					//i = 0; CHECKKKKK
				}
			}
			finish = hidden.size();
		}
		for (int i = 0 ; i < query.size() ; i++) {
			hidden.add(query.get(i));
		}

		return hidden;
	}

	/**
	 * This function helps us remove all irrelevant hidden variables from the calculation.
	 * @param allFactors is the current list of factors.
	 * @param v is the variable we want to check whether is relevant.
	 * @return true if is a leaf, else false.
	 */


	public static boolean leaf(ArrayList<Factor> allFactors, Variable v) {
		int count = 0;
		for (int i = 0 ; i < allFactors.size() ; i++) {
			for (int j = 0 ; j < allFactors.get(i).getFactor().get(0).size()-1 ; j++) {
				if (allFactors.get(i).getFactor().get(0).get(j).equals(v.getName())) {
					count++;
				}

			}
		}
		if (count == 1) return true;
		return false;
	}

	/**
	 * This function joins all factors that contain a certain variable, this function also helps us count the number of multiplications.
	 * @param v is the variable that all factors must contain in order to be joined in this round.
	 * @param factors is an arraylist of all factors currently found, from that list we only join the relevant ones.
	 * @param bn is out total network.
	 * @return a factor which is a joint factor of all factors that contain v.
	 */


	public static Factor join(Variable v, ArrayList<Factor> factors, bayesianNetwork bn) {
		int multiply = 0;
		// If there are no factors to join immediately return the factor, and set the number of multiplications to 0. 
		if (factors.size() == 1) {
			factors.get(0).setIntMulti(0);
			return factors.get(0);
		}

		// find all factors that contain variable v.
		ArrayList<Factor> factorsThatContainV = new ArrayList<>();
		for (int i = 0 ; i < factors.size() ; i++) {
			if (factors.get(i).getFactor().get(0).contains(v.getName())) {
				factorsThatContainV.add(factors.get(i));
			}
		}
		if (factorsThatContainV.size()==1) {
			return factorsThatContainV.get(0);
		}

		// Sort the factors according to their size to minimize number of multiplications.
		factorsThatContainV.sort(null);
		Factor answer = factorsThatContainV.get(0);
		for (int i = 1 ; i < factorsThatContainV.size() ; i++) {
			answer = answer.joinTwoFactors(answer, factorsThatContainV.get(i), bn);
			// note that the number of multiplications is the size of the factor after joining.
			multiply += answer.getFactor().size()-1;
			// We need to remove the factor that we just joined.
			factors.remove(factorsThatContainV.get(i));
		}
		for (int i = 0 ; i < factors.size() ; i++) {
			if (factorsThatContainV.get(0).getFactor().equals(factors.get(i))){
				factors.remove(i);

			}
		}
		factors.add(answer);
		answer.setIntMulti(multiply);
		return answer;
	}

	/**
	 * This function goes over the network and finds all factors that contain the specific variable.
	 * @param v is the variable we want to find all factors that contain it.
	 * @param bn is the network.
	 * @return an arraylist of factors that contain the variable.
	 */

	public static ArrayList<Factor> factorsContainV(Variable v, bayesianNetwork bn){
		ArrayList<Factor> factorsList = new ArrayList<>();
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			for (int j = 0 ; j < bn.getBN().get(i).createTruthTable()[0].length ; j ++) {
				if (bn.getBN().get(i).createTruthTable()[0][j].equals(v.getName())) {
					Factor temp = new Factor(bn.getBN().get(i),bn);
					if (!factorsList.contains(temp)) {
						factorsList.add(temp);
					}
				}
			}
		}		
		return factorsList;
	}


	/**
	 * This function finds the correct row to keep for elimination process
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
	 * This function convert the input string into an arraylist.
	 * @param input is the input string
	 * @return the query as an arraylist.
	 */


	public static ArrayList<String> convert(String input) {
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);
		return query;
	}



	/**
	 * This function finds the correct row to receive from the probability later
	 * @param rowNum
	 * @param numberOfcols to look at in function.
	 * @return a list of all rows that contain the wanted outcomes of each variables.
	 */

	public static void setWantedOutcomesForGiven(String input, bayesianNetwork bn) {
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);
		//Set given wanted outcome
		ArrayList<Variable> given = new ArrayList<>();
		bn.getBN();
		for (int i = 0 ; i < bn.getBN().size() ; i ++) {
			for (int j = 2; j < query.size() ; j+=2) {
				if (bn.getBN().get(i).getName().equals(query.get(j))) {
					bn.getBN().get(i).setWantedOutcome(query.get(j+1));
					given.add(bn.getBN().get(i));
				}
			}
		}
	}

	/**
	 * This function goes over the input string and firstly converts it into an array, next it iterates through the BN.
	 * @param input string which is the query.
	 * @param bn represents out network
	 * @return an arraylist of hidden variables.
	 */

	public static Variable getMainVariable (String input, bayesianNetwork bn) {
		Variable v = new Variable();
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		for (int i  = 0 ; i < bn.getBN().size() ; i++) {
			if (bn.getBN().get(i).getName().equals(queryIntoArray[0]))
				v = bn.getBN().get(i);
		}
		v.setWantedOutcome(queryIntoArray[1]);
		return v;
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


	public static void setWantedOutcomesForquery(String input, bayesianNetwork bn) {
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

	}

	//=============================================================================//

	// These are used to check whether the answer is already found in CPT // 

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



	public static void setWantedOutcomesForAll(String input, bayesianNetwork bn, int loopNumber) {
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);
		//Set given wanted outcome
		ArrayList<Variable> given = new ArrayList<>();
		bn.getBN();
		for (int i = 0 ; i < bn.getBN().size() ; i ++) {
			for (int j = 2; j < query.size() ; j+=2) {
				if (bn.getBN().get(i).getName().equals(query.get(j))) {
					bn.getBN().get(i).setWantedOutcome(query.get(j+1));
					given.add(bn.getBN().get(i));
				}
			}
		}
		//Set Variable wanted outcome
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if (bn.getBN().get(i).getName().equals(queryIntoArray[0])) {
				bn.getBN().get(i).setWantedOutcome(queryIntoArray[1]);
			}
		}
		//set hidden's wanted outcome
		ArrayList <Variable> hidden = new ArrayList<>();
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if (!query.contains(bn.getBN().get(i).getName())) {
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




	public static double probs(Variable v, bayesianNetwork bn) {
		String [][] cpt = v.createTruthTableByVariable(v);
		//printMat(cpt);
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

	// Only relevant for self check
	public static void printMat(String [][] s) {
		for (int i = 0 ; i < s.length ; i++) {
			for (int j = 0 ; j < s[0].length ; j++) {
				System.out.print(s[i][j] + ", ");
			}
			System.out.println(" ");
		}
	}




}

