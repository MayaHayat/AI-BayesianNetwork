

import java.util.ArrayList;

/**
 * This class is our CPT class to which the Variable class has access to.
 * @author Maya
 *
 */

public class CPT {
	
	String name;
	ArrayList<Variable> parents;
	double [] probabilities;
	String [][] cpt;


	// empty constructor
	public CPT() {
		this.name = null;
		this.parents = null;
		this.probabilities = null;
		this.cpt = null;
	}


	// copy constructor
	public CPT(CPT cpt) {
		this.name = cpt.name;
		this.parents = cpt.parents;
		this.probabilities = cpt.probabilities;
		this.cpt = cpt.cpt;
	}

	public CPT(String [][] cpt) {
		this.cpt = cpt;
	}


	public String[][] createAlternatingTable(ArrayList<Variable> hidden){
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

	public static String printMat(String [][] s) {
		String cur = "";
		for (int i = 0 ; i < s.length ; i++) {
			for (int j = 0 ; j < s[0].length ; j++) {
				cur+= s[i][j] + ", ";
			}
			cur += "\n";
		}
		return cur;
	}

//	public String toString() {
//		String s = "";
//		s+= "Variable: " + v.getName() + "\nParents:" + v.getParentsName() + "\nProbabilities: " + 
//				Arrays.toString(v.getProbs()) + "\nTruth table: \n" +  printMat(this.cpt)+ "\n" ;	
//
//		return s;
//	}

}
