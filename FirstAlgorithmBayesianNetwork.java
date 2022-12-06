

import java.util.ArrayList;
import java.util.Arrays;

public class FirstAlgorithmBayesianNetwork {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		readXmlFile x=new readXmlFile("C:\\Users\\Maya\\OneDrive\\Desktop\\alarm_net.xml");
		String input = "P(B=T|J=T,M=T),1";
		bayesianNetwork bn = new bayesianNetwork(x);
		
		finalCalculation(input,bn);
		
	}
	/**
	 * ======= NEED TO ADD IF CPT IS FOUND IMIDIATLY =============
	 * This function finally returns the probability of the event happening.
	 * @param input the given query as a string
	 * @param bn is the Bayesian Network
	 * @return the probability, the number of additions, the number of multiplications
	 */


	public static int finalCalculation(String input, bayesianNetwork bn) {
		String variable = convert(input).get(0);
		int numOfOutcomesForV =0 ; 
		double keepRecord [] = new double [3];
		double numerator = 0;
		double denominator = 0;
		double answerNumerator = 0;
		double answerDenominator = 0;

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
			keepRecord[1]--;
		}
		double answer = answerNumerator/(answerDenominator);
		keepRecord[0] = answer;

		System.out.println(Arrays.toString(keepRecord));
		return 1;
	}


	/**
	 * This function find the wanted probability of a specific variable.
	 * @param v is the variable that the cpt table is going to be created around.
	 * @param bn is the bayesian network from which we gather our data.
	 * @return the probability found.
	 */
	
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

	/**
	 * This function finds the correct row to recieve from the probability later
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


	//Set wanted outcomes for all
	public static void setWantedOutcomesForDenominator(String input, bayesianNetwork bn, int loopNumber, int vOutcome) {
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
				for (int j = 0 ; j < bn.getBN().get(i).getPossibleOutcomes().size() ; j++) {
					if (bn.getBN().get(i).getPossibleOutcomes().get(j).equals(queryIntoArray[1])) {
						bn.getBN().get(i).getPossibleOutcomes().remove(j);
					}
					bn.getBN().get(i).setWantedOutcome(bn.getBN().get(i).getPossibleOutcomes().get(vOutcome));
				}
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


	// Set wanted outcomes for denominator
	public static void setWantedOutcomesForDenominator2(String input, bayesianNetwork bn, int loopNumber) {
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 2 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);
		//Set given wanted outcome
		ArrayList<Variable> given = new ArrayList<>();
		bn.getBN();
		for (int i = 0 ; i < bn.getBN().size() ; i ++) {
			for (int j = 0; j < query.size() ; j+=2) {
				if (bn.getBN().get(i).getName().equals(query.get(j))) {
					bn.getBN().get(i).setWantedOutcome(query.get(j+1));
					given.add(bn.getBN().get(i));
				}
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
	

	//Create an alternating table for hidden's wanted outcome
	public static String[][] createAlternatingTable(ArrayList<Variable> hidden){
		int rows = 1;
		for (int i = 0 ; i < hidden.size() ; i++) {
			rows *= hidden.get(i).getPossibleOutcomes().size();


		}
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

	//Find hidden for denominator from BN 
	public static ArrayList<Variable> getHiddenForDenominator(String input , bayesianNetwork bn){
		String basic = input.substring(5, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);
		ArrayList <Variable> hidden = new ArrayList<>();
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if (!query.contains(bn.getBN().get(i).getName())) {
				hidden.add(bn.getBN().get(i));
			}
		}
		return hidden;
	}


	//Find hidden from BN 
	public static ArrayList<Variable> getHidden(String input , bayesianNetwork bn){
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		ArrayList <String> query = new ArrayList<>();
		for (int i = 0 ; i < queryIntoArray.length ; i++)
			query.add(queryIntoArray[i]);
		ArrayList <Variable> hidden = new ArrayList<>();
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if (!query.contains(bn.getBN().get(i).getName())) {
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
		return given;
	}

	public static void getVariable(String input, bayesianNetwork bn) {
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			if (bn.getBN().get(i).getName().equals(queryIntoArray[0])) {
				bn.getBN().get(i).setWantedOutcome(queryIntoArray[1]);
			}
		}
	}

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
