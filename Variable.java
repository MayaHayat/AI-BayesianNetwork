

import java.util.ArrayList;
import java.util.Arrays;

public class Variable implements Comparable <Variable>{
	private String name;
	private ArrayList<String> possibleOutcomes;
	private String wantedOutcome;
	private ArrayList<Variable> parents = new ArrayList<Variable>();
	private String [] probabilities;
	private CPT cpt;



	public Variable(String name, ArrayList<String> possibleOutcomes) {
		this.name = name;
		this.possibleOutcomes = possibleOutcomes;
	}

	public Variable(String name, ArrayList<String> possibleOutcomes, String [] probabilities) {
		this.name = name;
		this.possibleOutcomes = possibleOutcomes;
		this.parents = parents;
		this.probabilities = probabilities;
	}

	public Variable() {

	}

	public Variable(readXmlFile x, String name) {
		this.name = name;
		this.parents = new ArrayList<>();
		if (x.getParents(name).equals(null))
			this.parents = null;
		else {
			for (int i = 0 ; i < x.getParents(name).size() ; i++) {
				Variable v = new Variable(x,x.getParents(name).get(i));
				this.parents.add(v) ;
			}
		}

		this.possibleOutcomes = x.getOutcomes(name);
		probabilities = x.getProbabilities(name).get(0).split(" ");
	}

	public Variable(readXmlFile x, String name, String wantedOutcome) {
		this.name = name;
		this.parents = new ArrayList<>();
		if (x.getParents(name).equals(null))
			this.parents = null;
		else {
			for (int i = 0 ; i < x.getParents(name).size() ; i++) {
				Variable v = new Variable(x,x.getParents(name).get(i));
				this.parents.add(v) ;
			}
		}

		this.possibleOutcomes = x.getOutcomes(name);
		probabilities = x.getProbabilities(name).get(0).split(" ");
		this.wantedOutcome = wantedOutcome;
	}


	public Variable(Variable v) {
		this.name = v.name;
		this.parents = new ArrayList<Variable>(v.parents);
		this.possibleOutcomes = v.possibleOutcomes;
		this.probabilities = v.probabilities;
		this.wantedOutcome = v.wantedOutcome;
	}

	public Variable (Variable v, String wantedOutcome) {
		this.name = v.name;
		this.parents = v.getParents();
		this.possibleOutcomes = v.getPossibleOutcomes();
		this.wantedOutcome = wantedOutcome;
	}


	public Variable (String name, String wantedOutcome) {
		this.name = name;
		this.wantedOutcome = wantedOutcome;
	}

	public Variable (String name, ArrayList<Variable> parents, ArrayList<String> possibleOutcomes, ArrayList <String > probabilities) {
		this.name = name;
		this.parents = parents;
		this.possibleOutcomes = possibleOutcomes;
		for (int i = 0 ; i < probabilities.size() ; i ++) {
			this.probabilities[i] = probabilities.get(i);
		}
	}

	public String[] getProbs() {
		return this.probabilities;
	}

	public ArrayList<String> getPossibleOutcomes(){
		return this.possibleOutcomes;
	}

	public boolean hasParent(){
		return parents.size() > 0;
	}


	public ArrayList<Variable> getParents(){
		return this.parents;
	}

	public ArrayList<String> getParentsName(){
		ArrayList<String> p = new ArrayList<>();
		for (int i = 0 ; i < this.getParents().size() ; i++) {
			p.add(this.getParents().get(i).getName());
		}
		return p;
	}


	public String getWantedOutcome() {
		return this.wantedOutcome;
	}

	public void setWantedOutcome(String outcome) {
		this.wantedOutcome = outcome;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	/**
	 * This function creates a truth table for a given variable v.
	 * @param v is the variable that the table is created around
	 * @return a 2D array of strings
	 */

	public String[][] createTruthTableByVariable(Variable v){
		String [][] f = new String[v.getProbs().length+1][v.getParents().size()+2];

		//First we will name the headers, two cases: if there are or aren't any parents
		if (v.getParents().size() > 0) {
			for (int i = 0 ; i < v.getParents().size() ; i++) {
				f[0][i] = v.getParents().get(i).getName();
				f[0][f[0].length-2] = v.getName();
				f[0][f[0].length-1] = "Probs";
			}
		}
		else {
			f[0][f[0].length-2] = v.getName();
			f[0][f[0].length-1] = "Probs";
		}

		//We will deal with cols from left to right, each time the devision increases
		// These initiate the columns of the parents
		int p = v.getParents().size();// number of parents
		int divideT = 1;
		for (int k = 0 ; k < p; k++) {
			f[0][k] = v.getParents().get(k).getName();
			Variable curr = new Variable (v.getParents().get(k));
			int numOutcomesT = v.getParents().get(k).getPossibleOutcomes().size();
			int colNumT = k;
			divideT *= numOutcomesT;
			for (int j = 0 ; j < divideT-1 ; j ++) {
				for (int i = (j)*f.length/divideT+1; i < (j+1)*f.length/divideT+1; i++) {
					f[i][k] = v.getParents().get(k).getPossibleOutcomes().get(j%numOutcomesT);
				}
			}
			for (int i = (divideT-1)*f.length/divideT+1; i <f.length ;i++) {
				f[i][k] = (v.getParents().get(k)).getPossibleOutcomes().get(numOutcomesT-1);
			}
		}

		//variable's col 
		int numOutcomesU = v.getPossibleOutcomes().size();
		int colNumU = p;
		int divideU = (int) Math.pow(numOutcomesU, colNumU+1);
		for (int j = 1 ; j < f.length ; j ++) {
			f[j][colNumU] = v.getPossibleOutcomes().get((j-1)%numOutcomesU);
		}

		//probs col
		//Fill in outcomes one by one
		for (int i = 1 ; i < f.length ; i++) {
			String[] curr = new String [v.getProbs().length];
			curr= v.getProbs();
			f[i][v.getParents().size()+1] = curr[i-1];
		}
		return f;
	}
	
	/**
	 * Create truth table normally
	 * @return
	 */
	
	public String[][] createTruthTable(){
		String [][] f = new String[getProbs().length+1][getParents().size()+2];

		//First we will name the headers, two cases: if there are or aren't any parents
		if (getParents().size() > 0) {
			for (int i = 0 ; i < getParents().size() ; i++) {
				f[0][i] = getParents().get(i).getName();
				f[0][f[0].length-2] = getName();
				f[0][f[0].length-1] = "Probs";
			}
		}
		else {
			f[0][f[0].length-2] = getName();
			f[0][f[0].length-1] = "Probs";
		}

		//We will deal with cols from left to right, each time the devision increases
		// These initiate the columns of the parents
		int p = getParents().size();// number of parents
		int divideT = 1;
		for (int k = 0 ; k < p; k++) {
			f[0][k] = getParents().get(k).getName();
			Variable curr = new Variable (getParents().get(k));
			int numOutcomesT = getParents().get(k).getPossibleOutcomes().size();
			int colNumT = k;
			divideT *= numOutcomesT;
			for (int j = 0 ; j < divideT-1 ; j ++) {
				for (int i = (j)*f.length/divideT+1; i < (j+1)*f.length/divideT+1; i++) {
					f[i][k] = getParents().get(k).getPossibleOutcomes().get(j%numOutcomesT);
				}
			}
			for (int i = (divideT-1)*f.length/divideT+1; i <f.length ;i++) {
				f[i][k] = (getParents().get(k)).getPossibleOutcomes().get(numOutcomesT-1);
			}
		}

		//variable's col 
		int numOutcomesU = getPossibleOutcomes().size();
		int colNumU = p;
		int divideU = (int) Math.pow(numOutcomesU, colNumU+1);
		for (int j = 1 ; j < f.length ; j ++) {
			f[j][colNumU] = getPossibleOutcomes().get((j-1)%numOutcomesU);
		}

		//probs col
		//Fill in outcomes one by one
		for (int i = 1 ; i < f.length ; i++) {
			String[] curr = new String [getProbs().length];
			curr= getProbs();
			f[i][getParents().size()+1] = curr[i-1];
		}
		return f;
	}
	


	public String toString() {
		String s ="";
		s+= "=> Variable: " + this.name + " Parents: " + this.getParentsName() +" Possible outcomes: " + this.possibleOutcomes + " Wanted outcome: " + this.wantedOutcome +  " Probabilities: " + Arrays.toString(this.probabilities) + "\n";


		return s;
	}
	
	
	/**
	 * This function compares between two variable's names
	 * @param o is the other to be compared with.
	 */

	public int compareTo(Variable o) {
		return (this.name.compareTo(o.name));
	}



}
