package at.jku.isse.cloud.revlinks;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;

public class PackageSelector {

	public Set<String> select(Predicate<String> verifier) {
		Set<String> packages = new HashSet<>();
		System.out.println("Please input the package names you want to create reverse links for. " + 
				"Use an empty input to continue. If no packages were selected, reverse links will " +
				"be created for every artifact.");
		
		try (Scanner scanner = new Scanner(System.in)) {
			String line;
			do {
				System.out.println("Package name: ");
				line = scanner.nextLine();
				if(!line.equals("")) {
					if(verifier.test(line)) {
						packages.add(line);
					} else {
						System.err.println("Selected package does not exist. Please check your spelling.");
					}
				}
			} while(!line.equals(""));
		}
		return packages;
	}
}
