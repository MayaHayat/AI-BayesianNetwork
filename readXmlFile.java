
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** 
 * This class imports all the information from the given xml file.
 * We will extract information in order to later create out variables and network.
 * @author Maya
 *
 */

public class readXmlFile {

	private Document doc;


	public readXmlFile(String xmlName) {
		try {
			File xmlDoc = new File (xmlName);
			DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuild = dbFact.newDocumentBuilder();
			doc = dBuild.parse(xmlDoc);
			doc.getDocumentElement();
		}

		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function gets all names from xml in order to create the list of variables in out network.
	 * @return a list of all given names (all variables names).
	 */

	public ArrayList<String> getNames() {

		// Note that the names are found under the VARIABLE line, therefore we need to access that first
		NodeList namesNode = doc.getElementsByTagName("VARIABLE");
		ArrayList<String> names = new ArrayList<String>();

		//Go through all names found in file
		for(int i=0; i < namesNode.getLength(); i++){
			Node name = namesNode.item(i);
			if(name.getNodeType() == Node.ELEMENT_NODE){ 
				Element eName = (Element) name;
				//Then we will access the NAME lines
				String vName = eName.getElementsByTagName("NAME").item(0).getTextContent();
				names.add(vName);

			}
		}
		return names;
	}
	/**
	 * This function goes through the xml file in order to find the possible outcomes of each variable.
	 * @param variable 
	 * @return an arraylist of all variable's possible outcomes.
	 */

	public ArrayList<String> getOutcomes(String variable) {
		
		//Note in file that the possible outcomes are also found underneath VARIABLE
		NodeList outcomeNode = doc.getElementsByTagName("VARIABLE");
		ArrayList<String> outcomes = new ArrayList<String>();

		for(int i=0; i < outcomeNode.getLength(); i++){
			Node outcome = outcomeNode.item(i);
			if(outcome.getNodeType() == Node.ELEMENT_NODE){
				Element eOutcomes = (Element) outcome;
				String vOutcome = eOutcomes.getElementsByTagName("NAME").item(0).getTextContent();
				//find the outcomes of a given variable
				if(variable.equals(vOutcome)) {
					NodeList outcomesOfVariable = eOutcomes.getElementsByTagName("OUTCOME");
					for (int j = 0 ; j < outcomesOfVariable.getLength(); j++) 
						outcomes.add(outcomesOfVariable.item(j).getTextContent());

				}
			}
		}
		return outcomes;
	}
	/**
	 * This function goes through the xml file in order to find the all the probabilities of each variable.
	 * @param variable
	 * @return an arraylist of probabilities of a specific variable
	 * Note that we will need to convert it later from string to double.
	 */

	public ArrayList<String> getProbabilities(String variable) {
		
		//Note that the probabilities(TABLE) are in the DEFINITION section
		NodeList probNode = doc.getElementsByTagName("DEFINITION");
		ArrayList<String> probabilities = new ArrayList<String>();
		
		for(int i=0; i < probNode.getLength(); i++){
			Node prob = probNode.item(i);
			if(prob.getNodeType() == Node.ELEMENT_NODE){
				Element eProbs = (Element) prob;
				//TABLE is found in the same level as FOR
				String vProb = eProbs.getElementsByTagName("FOR").item(0).getTextContent();
				//Find the probabilities of a given variable by String.
				if(variable.equals(vProb)) {
					NodeList probabilitiessOfVariable = eProbs.getElementsByTagName("TABLE");
					for (int j = 0 ; j < probabilitiessOfVariable.getLength(); j++) 
						probabilities.add(probabilitiessOfVariable.item(j).getTextContent());

				}
			}
		}
		//note that this returns all probabilities as a string in the first cell of the arraylist, we need to split later
		return probabilities;
	}


	/**
	 * This function goes through the xml file in order to find the all the parents of each variable.
	 * @param variable
	 * @return an arraylist of parents of a specific variable
	 */

	public ArrayList<String> getParents(String child) {

		//Note that the parents(GIVEN) are in the DEFINITION section
		NodeList parentsNode = doc.getElementsByTagName("DEFINITION");
		ArrayList<String> parents = new ArrayList<String>();

		for(int i=0; i < parentsNode.getLength(); i++){
			Node parent = parentsNode.item(i);
			if(parent.getNodeType() == Node.ELEMENT_NODE){
				Element eParents = (Element) parent;
				//we need to use the next line as it is found in the same level as FOR
				String vParent = eParents.getElementsByTagName("FOR").item(0).getTextContent();
				if(child.equals(vParent)) {
					NodeList parentsOfVariable = eParents.getElementsByTagName("GIVEN");
					for (int j = 0 ; j < parentsOfVariable.getLength(); j++) 
						parents.add(parentsOfVariable.item(j).getTextContent());
				}

			}
		}
		return parents;

	}
}


