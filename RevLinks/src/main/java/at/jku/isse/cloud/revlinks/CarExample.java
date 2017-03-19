package at.jku.isse.cloud.revlinks;

import at.jku.isse.cloud.artifact.DSClass;
import at.jku.isse.cloud.artifact.DSConnection;
import at.jku.isse.cloud.artifact.DSInstance;
import at.jku.isse.cloud.artifact.DSLink;
import at.jku.sea.cloud.Package;
import at.jku.sea.cloud.Project;

public class CarExample {

	public static void main(String[] args) {
		DSConnection conn = new DSConnection("Car_user", "Car_pwd", "car_workspace");
		Package uml = conn.getOrCreatePackage("Cars_UML");
		Package instances = conn.getOrCreatePackage("Cars_Instances");
		Project project = conn.createProject("Cars");
    	
		// Create Meta Model
		DSClass colorModel = new DSClass(conn, "Color", uml, project).withFeatures("colorName", "@opposite");
		DSClass countryModel = new DSClass(conn, "Country", uml, project).withFeatures("name");
		DSLink brandOrigin = new DSLink("brand_country", countryModel, 0, -1, 1, 1);
		DSClass brandModel = new DSClass(conn, "Brand", uml, project).withFeatures("brandName", "@opposite").withLinks(brandOrigin);
		DSLink hasColor = new DSLink("has_color", colorModel, 0, -1, 1, 1);
		DSLink interiorColor = new DSLink("interior_color", colorModel, 0, -1, 1, 1);
		DSLink brand = new DSLink("brand", brandModel, 0, -1, 1, 1);
		
		DSClass engineModel = new DSClass(conn, "Engine", uml, project).withFeatures("power", "displacement", "cylinders", "torque");
		DSClass dieselModel = new DSClass(conn, "Diesel", uml, project).withSuperType(engineModel);
		DSClass petrolModel = new DSClass(conn, "Petrol", uml, project).withSuperType(engineModel);
		DSLink hasEngine = new DSLink("has_engine", engineModel, 0, -1, 1, 1);
		
		DSClass transmissionModel = new DSClass(conn, "Transmission", uml, project);
		DSClass automaticModel = new DSClass(conn, "Automatic", uml, project).withSuperType(transmissionModel);
		DSClass manualModel = new DSClass(conn, "Manual", uml, project).withFeatures("gears").withSuperType(transmissionModel);
		DSLink hasTransmission = new DSLink("has_transmission", transmissionModel, 0, -1, 1, 1);
		
		DSClass carModel = new DSClass(conn, "Car", uml, project).withLinks(hasColor, brand, interiorColor, hasEngine, hasTransmission);
		
		// Instantiate
		
		// Colors
		DSInstance black = colorModel.createInstance("Black", instances);
		black.setProperty("colorName", "black");
		DSInstance blue = colorModel.createInstance("Blue", instances);
		blue.setProperty("colorName", "blue");
		DSInstance grey = colorModel.createInstance("Grey", instances);
		grey.setProperty("colorName", "grey");
		
		// Brands
		DSInstance honda = brandModel.createInstance("Honda", instances);
		honda.setProperty("brandName", "Honda");
		DSInstance renault = brandModel.createInstance("Renault", instances);
		renault.setProperty("brandName", "Renault");
		DSInstance bmw = brandModel.createInstance("BMW", instances);
		bmw.setProperty("brandName", "BMW");
		DSInstance porsche = brandModel.createInstance("Porsche", instances);
		porsche.setProperty("brandName", "Porsche");
		
		// Countries
		DSInstance japan = countryModel.createInstance("Japan", instances);
		japan.setProperty("name", "Japan");
		DSInstance france = countryModel.createInstance("France", instances);
		france.setProperty("name", "France");
		DSInstance germany = countryModel.createInstance("Germany", instances);
		germany.setProperty("name", "Germany");
		
		// Engines
		DSInstance diesel = dieselModel.createInstance("2.2 i-DTEC", instances);
		diesel.setProperty("power", "150");
		diesel.setProperty("displacement", "2199");
		diesel.setProperty("cylinders", "4");
		diesel.setProperty("torque", "350");
		DSInstance petrol = petrolModel.createInstance("2.0 16V WT", instances);
		petrol.setProperty("power", "136");
		petrol.setProperty("displacement", "1998");
		petrol.setProperty("cylinders", "4");
		petrol.setProperty("torque", "191");
		DSInstance bmwDiesel = dieselModel.createInstance("525D", instances);
		bmwDiesel.setProperty("power", "177");
		bmwDiesel.setProperty("displacement", "2497");
		bmwDiesel.setProperty("cylinders", "6");
		bmwDiesel.setProperty("torque", "400");
		DSInstance porschePetrol = petrolModel.createInstance("911 S Turbo", instances);
		bmwDiesel.setProperty("power", "580");
		bmwDiesel.setProperty("displacement", "3800");
		bmwDiesel.setProperty("cylinders", "6");
		bmwDiesel.setProperty("torque", "700");
		
		// Transmissions
		DSInstance hondaTransmission = automaticModel.createInstance("Honda Automatic", instances);
		DSInstance renaultTransmission = manualModel.createInstance("Renault Manual", instances);
		renaultTransmission.setProperty("gears", "5");
		DSInstance bmwTransmission = manualModel.createInstance("BMW Manual", instances);
		bmwTransmission.setProperty("gears", "6");
		DSInstance porscheTransmission = manualModel.createInstance("Porsche Manual", instances);
		porscheTransmission.setProperty("gears", "7");
		
		// Cars
		DSInstance blackHonda = carModel.createInstance("My black Honda", instances);
		DSInstance blackRenault = carModel.createInstance("My black Renault", instances);
		DSInstance blueHonda = carModel.createInstance("My blue Honda", instances);
		DSInstance greyBmw = carModel.createInstance("My grey BMW", instances);
		DSInstance myPorsche = carModel.createInstance("My Porsche", instances);
		
		// Create Links
		blackHonda.setLinkProperty(hasColor, black);
		blackHonda.setLinkProperty(brand, honda);
		blackHonda.setLinkProperty(interiorColor, black);
		blackHonda.setLinkProperty(hasEngine, diesel);
		blackHonda.setLinkProperty(hasTransmission, hondaTransmission);
		
		blackRenault.setLinkProperty(hasColor, black);
		blackRenault.setLinkProperty(brand, renault);
		blackRenault.setLinkProperty(interiorColor, black);
		blackRenault.setLinkProperty(hasEngine, petrol);
		blackRenault.setLinkProperty(hasTransmission, renaultTransmission);
		
		blueHonda.setLinkProperty(hasColor, blue);
		blueHonda.setLinkProperty(brand, honda);
		blueHonda.setLinkProperty(interiorColor, grey);
		blueHonda.setLinkProperty(hasEngine, diesel);
		blueHonda.setLinkProperty(hasTransmission, hondaTransmission);
		
		greyBmw.setLinkProperty(hasColor, grey);
		greyBmw.setLinkProperty(brand, bmw);
		greyBmw.setLinkProperty(interiorColor, grey);
		greyBmw.setLinkProperty(hasEngine, bmwDiesel);
		greyBmw.setLinkProperty(hasTransmission, bmwTransmission);
		
		myPorsche.setLinkProperty(hasColor, grey);
		myPorsche.setLinkProperty(brand, porsche);
		myPorsche.setLinkProperty(interiorColor, black);
		myPorsche.setLinkProperty(hasEngine, porschePetrol);
		myPorsche.setLinkProperty(hasTransmission, porscheTransmission);
		
		honda.setLinkProperty(brandOrigin, japan);
		renault.setLinkProperty(brandOrigin, france);
		bmw.setLinkProperty(brandOrigin, germany);
		porsche.setLinkProperty(brandOrigin, germany);
		
		conn.commit("");
		System.out.println("Finished!");
	}
}
