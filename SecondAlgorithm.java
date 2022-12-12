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
		String input = "P(B0=v3|C3=T,B2=F,C2=v3),2";
		bayesianNetwork bn = new bayesianNetwork(x);
		setWantedOutcomesForGiven(input,bn);
		//System.out.println(bn.getBN());
//		printMat(bn.getBN().get(2).createTruthTable());
		//System.out.println(bn.getBN().get(2));
		//Factor f = new Factor(bn.getBN().get(1),bn);
		//System.out.println(f);
//		System.out.println(f);
		//System.out.println(f);
		//System.out.println(bn.getBN());
		//		for (int i = 0 ; i < bn.getBN().size() ; i++) {
		//			System.out.println(	CPTtoFactor(bn.getBN().get(i).createTruthTableByVariable(bn.getBN().get(i)),bn));
		//			System.out.println("================");
		//		}
//		printMat(bn.getBN().get(2).createTruthTable());
		//ArrayList<Factor> containV = factorsContainV(bn.getBN().get(1),bn);
		//Factor a = new Factor(bn.getBN().get(9),bn);
		//System.out.println(a);
		//System.out.println(containV);
		//System.out.println(containV );
		//System.out.println(bn.getBN().get(5));
		//Factor f = new Factor(bn.getBN().get(5), bn);
		//System.out.println(f);
		System.out.println("=============");
//		System.out.println("=============");
		
//		Factor joined = new Factor();
//		Factor f = joined.joinFactors(containV.get(1), containV.get(2));
//		System.out.println(f);
//		Factor g = joined.joinFactors(containV.get(0), f);
//		//System.out.println(g);
		//f.join(containV.get(0), containV.get(1), bn);
		//Factor c = f.joinTwoFactors(containV.get(0), containV.get(2), bn);
		//System.out.println(c);
		//System.out.println(f.join(containV.get(0), c, bn));
		//System.out.println();
		Factor current = join(bn.getBN().get(2),bn);
		//System.out.println(current);
		Factor n = current.eliminateVariable(bn.getBN().get(1),bn);
		//System.out.println(n);
		//System.out.println(isLeaf(bn.getBN().get(3),bn));
		System.out.println(isRelevent(input, bn));
		
	}
	
	
//	public static double getProbability(String input, bayesianNetwork bn) {
//		
//	}
//	
//	
	public static ArrayList<Variable> isRelevent(String input, bayesianNetwork bn){
		ArrayList<Variable> hidden = getHidden(input, bn);
		for (int i = hidden.size()-1 ; i >= 0 ; i--) {
			if (isLeaf(hidden.get(i),bn) == true) {
				hidden.remove(i);
			}
		}
		return hidden;
	}
	
	public static boolean isLeaf(Variable v, bayesianNetwork bn) {
		System.out.println(factorsContainV(v, bn));
		ArrayList <Factor> allVariables = factorsContainV(v, bn);
		if ( allVariables.size() == 1)
			return true;
		return false;
		
	}

	public static Factor join(Variable v, bayesianNetwork bn) {
		ArrayList<Factor> factorsThatContainV = factorsContainV(v, bn);
		factorsThatContainV.sort(null);
		Factor answer = factorsThatContainV.get(0);
		for (int i = 1 ; i < factorsThatContainV.size() ; i++) {
		answer = answer.joinTwoFactors(answer, factorsThatContainV.get(i), bn);
		}
		
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

	public static void printMat(String [][] s) {
		for (int i = 0 ; i < s.length ; i++) {
			for (int j = 0 ; j < s[0].length ; j++) {
				System.out.print(s[i][j] + ", ");
			}
			System.out.println(" ");
		}
	}


}
