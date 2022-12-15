import java.util.ArrayList;


public class SecondAlgorithm {

	/**
	 * Turn CPTs into factors according to the evidence.
	 * If a factor is one valued we can disregard it.
	 * While there are still hidden variables:
	 * 		Pick a hidden variable
	 * 		Join all factors that mention it
	 * 		Eliminate the variable by summing it out
	 * 		If factor becomes one valued disregard it
	 * Join remaining factors.
	 * Normalize.
	 */

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readXmlFile x=new readXmlFile("C:\\Users\\Maya\\OneDrive\\Desktop\\big_net.xml");
		//String input = "P(D1=T|C2=v1,C3=F),2";
		//String input = "P(B0=v3|C3=T,B2=F,C2=v3),2";
		//String input = "P(A2=T|C2=v1),1";
		//String input = "P(J=T|B=T),1";
		//String input = "P(B=T|J=T,M=T),1";
		//String input = "P(D1=T|A1=T,A2=F,A3=T,C1=T,C2=v1,C3=F),2";
//		String input = "P(D1=T|A1=T,A2=F,A3=T,C1=T,C2=v1,C3=F),2"; //supposed to be 0 , 0 
//		String input = "P(D1=T|A1=T,A2=F,A3=T,C1=T,C2=v1),2"; 
//		String input = "P(D1=T|A1=T,A2=F,A3=T,C1=T),2"; 
//		String input = "P(D1=T|A1=T,A2=F,A3=T),2";
//		String input = "P(D1=T|A1=T,A2=F),2";
//		String input ="P(D1=T|A1=T),2";
//		String input ="P(D1=T|C2=v1,C3=F,A1=T,A2=F),2";
//		String input ="P(D1=T|C2=v1,C3=F,A1=T),2";
		String input ="P(D1=T|C2=v1,C3=F),2";
//		String input = "P(C3=T|B1=T,B0=v1),2";
		bayesianNetwork bn = new bayesianNetwork(x);
//		setWantedOutcomesForGiven(input,bn);
		//System.out.println(bn.getBN());
		//System.out.println(bn.getBN());
		//System.out.println(relevant(input, bn));
		System.out.println(getProbability(input,bn));
		//System.out.println(bn.getBN());
		//		Factor f = new Factor(bn.getBN().get(3), bn);
		//		System.out.println(f);

	}
	//DONT FORGET TO ADD IF IS FOUND IN CPT ALREADY


	public static String getProbability(String input, bayesianNetwork bn) {
		setWantedOutcomesForGiven(input,bn);
		String s = "";
		int additions = 0; 
		int multiplications = 0;
		String variable = convert(input).get(0);
		//CHECK if is already found in tables
		for (int i = 0; i < bn.getBN().size() ; i++) {
			if (variable.equals(bn.getBN().get(i).getName())) {
				if (bn.getBN().get(i).getParents().equals(getGiven(input, bn))) {
					setWantedOutcomesForAll(input,bn,1);
					s =""+ probs(bn.getBN().get(i), bn) + ", 0, 0";
					return s;
				}
			}
		}
		
		
		ArrayList <Variable> relevant = relevant(input, bn);
		relevant.sort(null);
		ArrayList<Variable> allHidden = getHidden(input,bn);
		ArrayList<Variable> irrelevant = new ArrayList<>();
		ArrayList <Variable> hidden = new ArrayList<>();
		for (int i = 0 ; i < allHidden.size() ; i ++) {
			if (relevant.contains(allHidden.get(i))) {
				hidden.add(allHidden.get(i));
			}
			else {
				irrelevant.add(allHidden.get(i));
			}
		}
		ArrayList<Factor> allFactors = new ArrayList<>();
		for (int i = 0 ; i < bn.getBN().size() ; i++) {
			Factor temp = new Factor(bn.getBN().get(i), bn);
			allFactors.add(temp);
		}

		for (int i = allFactors.size() -1 ; i >=0  ; i--) {
			for (int j = 0 ; j < irrelevant.size() ; j++) {
				if (allFactors.get(i).getFactor().get(0).contains(irrelevant.get(j).getName())) {
					allFactors.remove(i);
				}
			}
		}
		//get rid of all one valued tables
		for (int i = 0 ; i < allFactors.size() ; i++) {
			for (int j = i+1 ; j < allFactors.size() ; j++) {
				if (allFactors.get(i).getFactor().equals(allFactors.get(j).getFactor())) {
					allFactors.remove(j);
				}
			}
		}
		hidden.sort(null);
		//		System.out.println(allFactors);
		//		System.out.println("========================");
		int multiplyCurrentSize; // how many additions were in each factor
		for (int i = 0 ; i < allFactors.size() ; i++) {
			if (allFactors.get(i).getFactor().get(0).size() < 2) {
				allFactors.remove(i);
				i=0;
			}
		}
		allFactors.sort(null);
		for (int i = 0 ; i < hidden.size() ; i++) {
			multiplyCurrentSize = 0;
			System.out.println(allFactors);
			Factor current = join(hidden.get(i), allFactors, bn);
			System.out.println(hidden.get(i) + "  " + current);
			System.out.println("-------------------");
			//multiplications +=current.getFactor().size()-1; //add multiplications
			multiplications +=current.getMultiplications();

			multiplyCurrentSize = current.getFactor().size()-1; //This is used to calculate the num of additions, find size of factor before elimination
			current.eliminateVariable(hidden.get(i), bn);

			multiplyCurrentSize/= (current.getFactor().size()-1); //This is used to calculate the num of additions, find out by how much the table has shrank.
			additions +=(current.getFactor().size()-1) * (multiplyCurrentSize-1);
			for (int j = 0 ; j< allFactors.size()  ; j++) {
				if (allFactors.get(j).getFactor().get(0).contains(hidden.get(i).getName())) {
					allFactors.remove(j);
					j=-1;	
				}
			}
		}

		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		Variable mainVariable = getMainVariable(input, bn);

		System.out.println("+++++++++++++++++++++++++++");
		for (int i = 0 ; i < allFactors.size() ; i++) {
			if (allFactors.get(i).getFactor().get(0).size() < 2) {
				allFactors.remove(i);
				i=0;
			}
		}
		int allFactorsSize = allFactors.size();
		System.out.println(allFactors);
		Factor finalFactor = join(mainVariable, allFactors, bn);

		System.out.println("2 " + multiplications);
		System.out.println("=====================----");
		System.out.println(finalFactor);
		//multiplications += finalFactor.getFactor().size()-1;
		if (allFactorsSize > 1)
			multiplications += finalFactor.getMultiplications();

		System.out.println("3 " + multiplications);
		mainVariable.setWantedOutcome(queryIntoArray[1]);
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

		s+=""+probability + ", " + additions +", " + multiplications;
		return s;
	}



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

							}
						}
					}
					hidden.remove(i);
				}
			}
			finish = hidden.size();
		}
		for (int i = 0 ; i < query.size() ; i++) {
			hidden.add(query.get(i));
		}

		return hidden;
	}


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

	public static Factor join(Variable v, ArrayList<Factor> factors, bayesianNetwork bn) {
		int multiply = 0;
		if (factors.size() == 1) {
			factors.get(0).setIntMulti(0);
			return factors.get(0);
		}

		ArrayList<Factor> factorsThatContainV = new ArrayList<>();
		for (int i = 0 ; i < factors.size() ; i++) {
			if (factors.get(i).getFactor().get(0).contains(v.getName())) {
				factorsThatContainV.add(factors.get(i));
			}
		}

		factorsThatContainV.sort(null);
		Factor answer = factorsThatContainV.get(0);
		for (int i = 1 ; i < factorsThatContainV.size() ; i++) {
			answer = answer.joinTwoFactors(answer, factorsThatContainV.get(i), bn);
			//System.out.println(answer);
			//trying
			multiply += answer.getFactor().size()-1;

			factors.remove(factorsThatContainV.get(i));
		}

		for (int i = 0 ; i < factors.size() ; i++) {
			if (factorsThatContainV.get(0).getFactor().equals(factors.get(i))){
				factors.remove(i);

			}
		}
		factors.add(answer);
		answer.setIntMulti(multiply);
		System.out.println(multiply);
		return answer;
	}


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


	public static Factor VasFactor(Factor f) {
		ArrayList<ArrayList<String>> current = f.getFactor();
		int numRows = current.size();
		for (int i = 1; i < numRows; i++) {
			ArrayList<String> row = current.get(i);
			int numCols = row.size();
			for (int j = 0; j < numCols - 1; j++) {
				String temp = row.get(j);
				row.set(j, row.get(j + 1));
				row.set(j + 1, temp);
			}
		}
		f.setFactor(current);
		return f;
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


	public static ArrayList<String> convert(String s) {
		String basic = s.substring(2, s.length()-3);
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
		//		//Set Variable wanted outcome
		//		for (int i = 0 ; i < bn.getBN().size() ; i++) {
		//			if (bn.getBN().get(i).getName().equals(queryIntoArray[0])) {
		//				bn.getBN().get(i).setWantedOutcome(queryIntoArray[1]);
		//			}
		//		}

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


	public static void setWantedOutcomesForquery(String input, bayesianNetwork bn) {
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

	}

	// These are used to check whether the answer is already found in CPT // 
	
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
	
	
	public static void printMat(String [][] s) {
		for (int i = 0 ; i < s.length ; i++) {
			for (int j = 0 ; j < s[0].length ; j++) {
				System.out.print(s[i][j] + ", ");
			}
			System.out.println(" ");
		}
	}


}