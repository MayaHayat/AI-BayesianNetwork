import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Factor implements Comparable <Factor>{

	private Variable v;
	private String [][] cpt;
	private ArrayList<ArrayList<String>> factor;
	private bayesianNetwork bn;
	private int length;
	private int height;



	public Factor() {
	}


	/**
	 * Constructor that creates a CPT for a specific variable and turns it into a factor
	 * @param cpt is the CPT we want to convert
	 */

	public Factor(Variable v, bayesianNetwork bn){
		this.v = v;
		this.cpt = v.createTruthTable();
		this.bn = bn;
		ArrayList<ArrayList<String>> factor = twodIntoArrayList(v.createTruthTable());
		ArrayList<Integer> rowsToRemove = new ArrayList<>();
		int deleteCol=-1;
		for (int i = 0 ; i < factor.get(0).size()  ; i++) {
			for (int j = 0 ; j < bn.getBN().size() ; j ++) {
				if (factor.get(0).get(i).equals(bn.getBN().get(j).getName())) {
					if (bn.getBN().get(j).getWantedOutcome()!=(null)) {
						deleteCol = i;
						for (int k = 1 ; k < factor.size() ; k++) {
							if (!factor.get(k).get(i).equals(bn.getBN().get(j).getWantedOutcome())) {
								if (!rowsToRemove.contains(k))
									rowsToRemove.add(k);
							}
						}
						factor = removeCol(factor, deleteCol);
					}
				}
			}
		}
		Collections.sort(rowsToRemove);
		for (int i = rowsToRemove.size()-1 ; i >= 0 ; i--) {
			int remove = rowsToRemove.get(i);
			factor.remove(remove);
		}
		this.factor = factor;
		this.length = factor.get(0).size();
		this.height = factor.size();
	}




	public ArrayList<ArrayList<String>> getFactor() {
		return this.factor;
	}

	public void setFactor(ArrayList<ArrayList<String>> factor) {
		this.factor = factor;
	}
	
	/**
	 * This function removes a specific row from a given 2d arraylist
	 * @param array is the array we want to remove the column from.
	 * @param colRemove is the index of the column we want to remove
	 * @return a new 2d arraylist of strings.
	 */

	public static ArrayList<ArrayList<String>> removeCol(ArrayList<ArrayList<String>> array, int colRemove){
		ArrayList<ArrayList<String>> temp = new ArrayList<>();
		for (int i = 0 ; i < array.size() ; i++) {
			ArrayList<String> tempRow = new ArrayList<>();
			for (int j = 0 ; j < array.get(0).size() ; j++) {
				if (j!= colRemove) {
					tempRow.add(array.get(i).get(j));
				}
			}
			temp.add(tempRow); 
		}
		return temp;
	}

	/**
	 * This function turns a 2D array into a 2D arraylist
	 * @param cpt is the 2D array
	 * @return a 2d arraylist
	 */

	public static ArrayList<ArrayList<String>> twodIntoArrayList(String [][] cpt) {
		ArrayList<ArrayList<String>> factorV = new ArrayList<>();
		for (int i = 0 ; i < cpt.length ; i++) {
			ArrayList<String> temp = new ArrayList<>();
			for (int j = 0 ; j < cpt[0].length ; j++) {
				temp.add(cpt[i][j]);
			}
			factorV.add(temp);
		}
		return factorV;
	}
	
	
	/**
	 * 
	 * @param v
	 * @param bn
	 * @return
	 */
	
	public void eliminateVariable(Variable v, bayesianNetwork bn) {
		ArrayList<ArrayList<String>>  newFactor = new ArrayList<>();
		for (int i = 0 ; i < this.factor.get(0).size() ; i++) {
			if (this.factor.get(0).get(i).equals(v.getName())) {
				this.factor = removeCol(this.factor, i);
			}
		}
		
		newFactor.add(this.factor.get(0));
		
		ArrayList<String> currentRow = new ArrayList<>();
		ArrayList<String> compareToRow = new ArrayList<>();
		double probability = 0;
		for (int i = 1 ; i < this.factor.size(); i++) {
			currentRow = new ArrayList<>();
			for (int j = 0 ; j < this.factor.get(0).size()-1 ; j++) {
				currentRow.add(this.factor.get(i).get(j));
			}
			probability = Double.parseDouble(this.factor.get(i).get(this.factor.get(i).size()-1));
			for(int k = i+1 ; k < this.factor.size() ; k ++) {
				 compareToRow = new ArrayList<>();
				for (int n = 0 ; n < this.factor.get(0).size()-1 ; n++) {
					compareToRow.add(this.factor.get(k).get(n));
				}
				if (currentRow.equals(compareToRow)) {
					probability += Double.parseDouble(this.factor.get(k).get(this.factor.get(0).size()-1));
					this.factor.remove(k);
					
				}
			}
			currentRow.add("" + probability);
			newFactor.add(currentRow);
		}
		this.factor = newFactor;
//		Factor f = new Factor();
//		f.factor = newFactor;
//		return f;
	}



	/**
	 * This function receives two factors and joins them together
	 * @param a is the first factor
	 * @param b is the second factor
	 * @param bn is our network
	 * @return a joint factor
	 */
	
	


	public Factor joinTwoFactors(Factor a, Factor b, bayesianNetwork bn) {
		//System.out.println(a);
		//System.out.println(b);
		this.bn=bn;
		Factor factorMain = new Factor();
		//creating a new factor, add all common variables first then all different
		ArrayList<String> common =commonVariables(a,b);
		ArrayList<String> different = differentVariables(a,b);
		ArrayList<Variable> all = new ArrayList<>();
		ArrayList<Double> probabilities = new ArrayList<>();
		//Adding the common variables (found in both factors)
		for (int i = 0 ; i < bn.getBN().size() ; i ++) {
			for (int j = 0 ; j < common.size() ; j ++) {
				if (bn.getBN().get(i).getName().equals(common.get(j))) {
					all.add(bn.getBN().get(i));
				}
			}
		}
		//Adding all the variables that are found in only one of the factors
		for (int i = 0 ; i < bn.getBN().size() ; i ++) {
			for (int k = 0 ; k < different.size() ; k ++) {
				if (bn.getBN().get(i).getName().equals(different.get(k))) {
					all.add(bn.getBN().get(i));
				}
			}
		}
		//create an alternating table
		factorMain.factor = createAlternatingTable(all);
		//System.out.println(all);
		factorMain.factor.get(0).add("Probs");
		//double [] probs = new double[factorMain.factor.size()-1];
		//System.out.println(factorMain.factor);
		//From here on we use the below help function in order to find the correct row we want to find the probability for.
		for (int i = 1 ; i < factorMain.factor.size() ; i++ ) {
			//We set here the wanted outcomes for each variable as we go down the rows.
			setWantedOutcomeForJoin(all, factorMain.factor.get(i));
			//System.out.println(all);
			//getProbability(a,all, i, factorMain.factor.size()-1);
			//getProbability(b,all, i, factorMain.factor.size()-1);
			//Note that we call the same function twice but at the same time and add the probabilities all together.
			//probabilities.add(getProbability(a,all, i, factorMain.factor.size()-1)*getProbability(b,all, i, factorMain.factor.size()-1));

			probabilities.add(getProbability(a,all)*getProbability(b,all));
			
		}
		for (int i =1 ; i < factorMain.factor.size() ; i++) {
			factorMain.factor.get(i).add("" +probabilities.get(i-1));
		}
		
		return factorMain;
	}
	
	/**
	 * This function is the help function for the join of two factors.
	 * @param smallerFactor is either a or b each time.
	 * @param all is all the variables found in the Main Factor above.
	 * @param row as we iterate through the rows and change them every time.
	 * @param lengthOfMain is the length of the main Factor as we want to compare the rows however we'd like to disregard the probabilities of each row.
	 * @return
	 */


	//public static double getProbability(Factor smallerFactor, ArrayList<Variable> all, int row, int lengthOfMain) {
	public static double getProbability(Factor smallerFactor, ArrayList<Variable> all) {

		ArrayList<Variable> relevent = new ArrayList<>();
		ArrayList<String> releventOutcome = new ArrayList<>(); //the row i want to look for 
		for (int j = 0 ; j < smallerFactor.factor.get(0).size() ; j++) {
			for (int i = 0 ; i < all.size() ; i++) {
				if (smallerFactor.factor.get(0).get(j).equals(all.get(i).getName())) {
					relevent.add(all.get(i));
					releventOutcome.add(all.get(i).getWantedOutcome()); //the correct row
				}
			}
		}
		double probability = 0;
		
		ArrayList<String> currentRow = new ArrayList<>();
		for (int k = 1 ; k < smallerFactor.factor.size() ; k++) {
			currentRow = new ArrayList<>();
			for (int i = 0 ; i < smallerFactor.factor.get(0).size()-1 ; i++) {
				currentRow.add(smallerFactor.factor.get(k).get(i));
			}
		
			if (releventOutcome.equals(currentRow)) {
				probability = Double.parseDouble(smallerFactor.factor.get(k).get(smallerFactor.factor.get(0).size()-1));
				//System.out.println(probability + " " + releventOutcome);
			}
		}

		return probability;
	}


	public static void setWantedOutcomeForJoin(ArrayList<Variable> all, ArrayList<String> row) {
		for (int i = 0 ; i < all.size() ; i++) {
			all.get(i).setWantedOutcome(row.get(i));
		}
	}



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

	public static ArrayList<ArrayList<String>> createAlternatingTable(ArrayList<Variable> variables){
		int rows = 1;
		for (int i = 0 ; i < variables.size() ; i++) {
			rows *= variables.get(i).getPossibleOutcomes().size();
		}
		//Note that the first line is the names of variables, we should know that when accessing table to start loop from row 1.
		String [][] alternating = new String [rows+1][variables.size()];
		for (int i = 0 ; i < alternating[0].length ; i++) {
			alternating [0][i] = variables.get(i).getName();	
		}

		int divideT = 1;
		for (int k = 0 ; k < variables.size(); k++) {
			alternating[0][k] = variables.get(k).getName();
			int numOutcomesT = variables.get(k).getPossibleOutcomes().size();
			divideT *= numOutcomesT;
			for (int j = 0 ; j < divideT-1 ; j ++) {
				for (int i = (j)*alternating.length/divideT+1; i < (j+1)*alternating.length/divideT+1; i++) {
					alternating[i][k] = variables.get(k).getPossibleOutcomes().get(j%numOutcomesT);
				}
			}

			for (int i = (divideT-1)*alternating.length/divideT+1; i <alternating.length ;i++) {
				alternating[i][k] = variables.get(k).getPossibleOutcomes().get(numOutcomesT-1);
			}
		}
		ArrayList<ArrayList<String>> factor = twodIntoArrayList(alternating);
		return factor;

	}

	public static ArrayList<String> commonVariables(Factor a, Factor b){
		ArrayList<String> common = new ArrayList<>();
		for (int i = 0 ; i < a.factor.get(0).size()-1 ; i++) {
			for (int j = 0 ; j < b.factor.get(0).size()-1 ; j ++) {
				if (b.factor.get(0).get(j).equals(a.factor.get(0).get(i))) {
					common.add(a.factor.get(0).get(i));
				}
			}
		}
		return common;
	}

	public static ArrayList<String> differentVariables(Factor a, Factor b){
		ArrayList<String> different = new ArrayList<>();
		ArrayList<String> common = commonVariables(a,b);
		for (int i = 0 ; i < a.factor.get(0).size()-1  ; i++) {
			if (!common.contains(a.factor.get(0).get(i))) {
				different.add(a.factor.get(0).get(i));
			}
		}
		for (int i = 0 ; i < b.factor.get(0).size()-1  ; i++) {
			if (!common.contains(b.factor.get(0).get(i))) {
				different.add(b.factor.get(0).get(i));
			}
		}
		return different;
	}


	public static void printMat(String [][] s) {
		for (int i = 0 ; i < s.length ; i++) {
			for (int j = 0 ; j < s[0].length ; j++) {
				System.out.print(s[i][j] + ", ");
			}
			System.out.println(" ");
		}
	}


	public String toString() {
		String s = "";
		s+= "Factor :" + factor +"\n";
		return s;
	}


	@Override
	public int compareTo(Factor o) {
		if (this.factor.size() > o.factor.size()) {
			return 1;
		}
		else if (o.factor.size()> this.factor.size()) {
			return -1;
		}
		else {
			int ASCII0 = 0;
			int ASCII1 = 0;
			for (int i = 0; i < this.factor.get(0).size()-1 ; i++) {
				ASCII0 += this.factor.get(0).get(i).charAt(0);
			}
			for (int j = 0; j < o.factor.get(0).size()-1 ; j++) {
				ASCII1 += this.factor.get(0).get(j).charAt(0);
			}
			
			if (ASCII0 > ASCII1) return 1;
			return -1;
		}
	}

}
