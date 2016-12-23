package at.jku.isse.cloud.revlinks;

import at.jku.isse.cloud.artifact.DSClass;
import at.jku.isse.cloud.artifact.DSConnection;
import at.jku.isse.cloud.artifact.DSInstance;
import at.jku.isse.cloud.artifact.DSLink;

public class CarExample {

	public static void main(String[] args) {
		DSConnection conn = new DSConnection("dos", "mepwd", "my workspace", "some other package");
    	
		// Create Meta Model
		DSClass colorModel = new DSClass(conn, "Color").withFeatures("colorName");
		DSClass countryModel = new DSClass(conn, "Country").withFeatures("name");
		DSLink brandOrigin = new DSLink("brand_country", countryModel, 0, -1, 1, 1);
		DSClass brandModel = new DSClass(conn, "Brand").withFeatures("brandName").withLinks(brandOrigin);
		DSLink hasColor = new DSLink("has_color", colorModel, 0, -1, 1, 1);
		DSLink interiorColor = new DSLink("interior_color", colorModel, 0, -1, 1, 1);
		DSLink brand = new DSLink("brand", brandModel, 0, -1, 1, 1);
		DSClass carModel = new DSClass(conn, "Car").withLinks(hasColor, brand, interiorColor);
		
		// Instantiate
		DSInstance black = colorModel.createInstance("Black");
		black.setProperty("colorName", "black");
		DSInstance honda = brandModel.createInstance("Honda");
		honda.setProperty("brandName", "Honda");
		DSInstance renault = brandModel.createInstance("Renault");
		renault.setProperty("brandName", "Renault");
		DSInstance japan = countryModel.createInstance("Japan");
		japan.setProperty("name", "Japan");
		DSInstance france = countryModel.createInstance("France");
		france.setProperty("name", "France");
		
		DSInstance blackHonda = carModel.createInstance("My black Honda");
		DSInstance blackRenault = carModel.createInstance("My black Renault");
		
		// Create Links
		blackHonda.setLinkProperty(hasColor, black);
		blackHonda.setLinkProperty(brand, honda);
		blackHonda.setLinkProperty(interiorColor, black);
		honda.setLinkProperty(brandOrigin, japan);
		
		blackRenault.setLinkProperty(hasColor, black);
		blackRenault.setLinkProperty(brand, renault);
		blackRenault.setLinkProperty(interiorColor, black);
		renault.setLinkProperty(brandOrigin, france);

		conn.commit("");
	}
}
