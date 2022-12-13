import java.util.ArrayList;


public class SecondAlgorithm {

	/**
	 * Turn CPTs into factors according to the evidence.
	 * If a factor is one valued we can disregard it.
	 * While there are still hiddens:
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
		String input = "P(D1=T|C2=v1,C3=F),2";
		//String input = "P(B0=v3|C3=T,B2=F,C2=v3),2";
		//String input = "P(A2=T|C2=v1),1";
		//String input = "P(J=T|B=T),1";
		//String input = "P(B=T|J=T,M=T),1";
		bayesianNetwork bn = new bayesianNetwork(x);
		setWantedOutcomesForGiven(input,bn);
		//System.out.println(bn.getBN());
		//System.out.println(bn.getBN());
		//System.out.println(relevant(input, bn));
		System.out.println(getProbability(input,bn));
		


	}

	
	public static double getProbability(String input, bayesianNetwork bn) {
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
			Factor current = new Factor(bn.getBN().get(i), bn);
			allFactors.add(current);
		}
		
		for (int i = allFactors.size() -1 ; i >=0  ; i--) {
			for (int j = 0 ; j < irrelevant.size() ; j++) {
				if (allFactors.get(i).getFactor().get(0).contains(irrelevant.get(j).getName())) {
					allFactors.remove(i);
				}
			}
		}
		
		//System.out.println(allFactors);
		for (int i = 0 ; i < allFactors.size() ; i++) {
			for (int j = i+1 ; j < allFactors.size() ; j++) {
				if (allFactors.get(i).getFactor().equals(allFactors.get(j).getFactor())) {
					allFactors.remove(j);
				}
			}
		}
//		System.out.println(allFactors);
//		System.out.println("=================");
		hidden.sort(null);
		for (int i = 0 ; i < hidden.size() ; i++) {
//			System.out.println(allFactors);
//			System.out.println(hidden.get(i));
//			System.out.println("=================");

			Factor current = join(hidden.get(i), allFactors, bn);
			current.eliminateVariable(hidden.get(i), bn);
			//System.out.println(current);
			for (int j = allFactors.size()-1 ; j >=0  ; j--) {
				if (allFactors.get(j).getFactor().get(0).contains(hidden.get(i).getName())) {
					allFactors.remove(j);
				}
			}
			System.out.println("---------------");
			System.out.println(allFactors);
		}
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		Variable mainVariable = getMainVariable(input, bn);
		//System.out.println(mainVariable.getWantedOutcome());
		//System.out.println(allFactors);
		Factor finalFactor = join(mainVariable, allFactors, bn);
		
		//System.out.println(finalFactor);
		
		mainVariable.setWantedOutcome(queryIntoArray[1]);
		double sum = 0;
		for (int i = 1 ; i < finalFactor.getFactor().size() ; i++) {
			sum += Double.parseDouble(finalFactor.getFactor().get(i).get(finalFactor.getFactor().get(i).size()-1));
		}
		double alpha = 1/sum;
		double probability = 0;
		for (int i = 1 ; i < finalFactor.getFactor().size() ; i++) {
			if (finalFactor.getFactor().get(i).get(0).equals(mainVariable.getWantedOutcome())) {
				probability = Double.parseDouble(finalFactor.getFactor().get(i).get(finalFactor.getFactor().get(i).size()-1));
			}
		}
		probability*=alpha;
		
		
		return probability;
	}


	
	// IGNORE FOR NOW

	public static double getProbs(String input, bayesianNetwork bn) {
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
		for (int i = 0 ; i < relevant.size() ; i++) {
			ArrayList<Factor> containV = factorsContainV(relevant.get(i), bn);
			for (int j = 0 ; j < containV.size() ; j++) {
				if (!allFactors.contains(containV.get(j))) {
					if (irrelevant.size() > 0) {
						for (int k = 0 ; k < irrelevant.size() ; k ++) {
							if (containV.get(j).getFactor().get(0).contains(irrelevant.get(k).getName())) {
								continue;
							}
							else {
								allFactors.add(containV.get(j));
							}
						}
					}
					else {
						allFactors.add(containV.get(j));
					}

				}
			}
		}
		//System.out.println(allFactors);
		for (int i = 0 ; i < allFactors.size() ; i++) {
			for (int j = i+1 ; j < allFactors.size() ; j++) {
				if (allFactors.get(i).getFactor().equals(allFactors.get(j).getFactor())) {
					allFactors.remove(j);
				}
			}
		}
		
		hidden.sort(null);
		for (int i = 0 ; i < hidden.size() ; i++) {
			//System.out.println(hidden.get(i));
			Factor current = join(hidden.get(i), allFactors, bn);

			current.eliminateVariable(hidden.get(i), bn);
			for (int j = allFactors.size()-1 ; j >=0  ; j--) {
				if (allFactors.get(j).getFactor().get(0).contains(hidden.get(i).getName())) {
					allFactors.remove(j);
				}
			}
			//allFactors.add(current);
		}
		
		//Factor finalCalculation = join();
		String basic = input.substring(2, input.length()-3);
		String [] queryIntoArray = basic.split("[\\|=,]");
		Variable mainVariable = getMainVariable(input, bn);
		//System.out.println(mainVariable.getWantedOutcome());
		Factor finalFactor = join(mainVariable, allFactors, bn);

		//System.out.println(finalFactor);
		mainVariable.setWantedOutcome(queryIntoArray[1]);
		double sum = 0;
		for (int i = 1 ; i < finalFactor.getFactor().size() ; i++) {
			sum += Double.parseDouble(finalFactor.getFactor().get(i).get(finalFactor.getFactor().get(i).size()-1));
		}
		double alpha = 1/sum;
		double probability = 0;
		for (int i = 1 ; i < finalFactor.getFactor().size() ; i++) {
			if (finalFactor.getFactor().get(i).get(0).equals(mainVariable.getWantedOutcome())) {
				probability = Double.parseDouble(finalFactor.getFactor().get(i).get(finalFactor.getFactor().get(i).size()-1));
			}
		}
		probability*=alpha;

		return probability;
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
			factors.remove(factorsThatContainV.get(i));
		}
		for (int i = 0 ; i < factors.size() ; i++) {
			if (factorsThatContainV.get(0).getFactor().equals(factors.get(i))){
				factors.remove(i);
			}
		}
		factors.add(answer);

		return answer;
	}


	//	public static Factor join(Variable v, bayesianNetwork bn) {
	//		ArrayList<Factor> factorsThatContainV = factorsContainV(v, bn);
	//		factorsThatContainV.sort(null);
	//		Factor answer = factorsThatContainV.get(0);
	//		for (int i = 1 ; i < factorsThatContainV.size() ; i++) {
	//			answer = answer.joinTwoFactors(answer, factorsThatContainV.get(i), bn);
	//		}
	//
	//		return answer;
	//	}


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


	public static void printMat(String [][] s) {
		for (int i = 0 ; i < s.length ; i++) {
			for (int j = 0 ; j < s[0].length ; j++) {
				System.out.print(s[i][j] + ", ");
			}
			System.out.println(" ");
		}
	}


}