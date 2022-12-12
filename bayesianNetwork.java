

import java.util.ArrayList;
/**
 * This class constructs our Bayesian Network, it uses the given xml file in order to set all variables.
 * @author Maya
 *
 */

public class bayesianNetwork {

	private ArrayList<Variable> allVariables;
	private readXmlFile xml;


	/**
	 * This function is a constructor for our network, it reads the data from the readXmlFile class and relates between variables.
	 * @param xml 
	 */
	
	public bayesianNetwork(readXmlFile xml) {
		allVariables = new ArrayList<>();
		ArrayList<String> namesOfVariable = xml.getNames();
		for (int i = 0; i < namesOfVariable.size() ; i++) {
			// In order to turn the arraylist of strings to variables we create a variable by variable from the given list of names.
			//note that we don't include parent's names just yet as we want to to connect between variables.
			Variable temp = new Variable(namesOfVariable.get(i), xml.getOutcomes(namesOfVariable.get(i)), xml.getProbabilities(namesOfVariable.get(i)).get(0).split(" "));
			allVariables.add(temp);
		} 
		// In this next section we connect each variable to it's parents (if exist)
		for (int i = 0 ; i < allVariables.size() ; i++) {
			for (int j = 0 ; j < xml.getParents(allVariables.get(i).getName()).size() ; j++) {
				for (int k = 0 ; k < allVariables.size() ; k++) {
					if (xml.getParents(allVariables.get(i).getName()).get(j).equals(allVariables.get(k).getName())) {
						allVariables.get(i).getParents().add(allVariables.get(k));
					}
				}
			}
		}
	}
	/**
	 * Note that our network is made out of an arraylist of Variables.
	 * @return the Bayesian Network containing all variables from xml file.
	 */
	
	public ArrayList<Variable> getBN() {
		return this.allVariables;
	}
	
	

}
