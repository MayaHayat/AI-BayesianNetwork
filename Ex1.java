import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;


public class Ex1 {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub

		// Open the input file for reading
		try (BufferedReader reader = new BufferedReader(new FileReader("input.txt"))){
			BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));


			String xml = reader.readLine();
			readXmlFile x = new readXmlFile(xml);

			String line;
			while ((line = reader.readLine()) != null) {
				String input = line;
				System.out.println(input);
				String query [] = input.split(",");
				int algoNum = Integer.parseInt(query[query.length-1]);
				switch (algoNum) {

				case 1:
				{
					bayesianNetwork bn = new bayesianNetwork(x);
					String answer = FirstAlgorithmBayesianNetwork.finalCalculation(input, bn);
					System.out.println(answer);
					writer.write(answer + "\n");
					break;
				}

				case 2:
				{
					bayesianNetwork bn = new bayesianNetwork(x);
					String answer = SecondAlgorithm.getProbability(input, bn);
					System.out.println(answer);
					writer.write(answer + "\n");					
					break;

				}

				case 3:
				{
					bayesianNetwork bn = new bayesianNetwork(x);
					String answer = ThirdAlgorithm.getProbability(input, bn);
					System.out.println(answer);
					writer.write(answer + "\n");					
					break;
				}
				}

			}
			writer.close();

		}

		catch (IOException e) {
			System.out.println("There's an error.\n");
			e.printStackTrace();
			// Handle any errors
		}

	}



}
