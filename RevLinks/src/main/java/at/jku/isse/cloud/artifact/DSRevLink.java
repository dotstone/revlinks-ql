package at.jku.isse.cloud.artifact;

import static java.util.Objects.hash;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.CollectionArtifact;
import at.jku.sea.cloud.Container.Filter;
import at.jku.sea.cloud.Package;

/**
 * Represents the model for the reverse links and provides an operation to create instances of reverse links. 
 * The reverse link artifacts have the following properties:
 * <li> name: "RLink"
 * <li> source: the source artifact
 * <li> source model: the model artifact of the source
 * <li> target: the target artifact
 * <li> target model: the model artifact of the target
 * <li> relNames: a collection artifact that contains the name of the links
 * @author Gabriel Schoerghuber
 * @author Dominik Steinbinder
 */
public class DSRevLink extends DSClass {
	
	public static final String REV_LINK_NAME = "RLink";
	public static final String LINKED_PACKAGES_ARTIFACT_NAME = "Packages";
	
	public static final String SOURCE_NAME = "source";
	public static final String SOURCE_TYPE_NAME = "sourceType";
	public static final String TARGET_NAME = "target";
	public static final String TARGET_TYPE_NAME = "targetType";
	public static final String REL_NAMES_NAME = "relNames";
	
	private final CollectionArtifact linkedPackagesArtifact;

	/**
	 * Creates a DSRevLink object and the reverse link model artifact in the Design Space.
	 * @param conn the Design Space connection (DSConnection object)
	 * @param pkg the package that contains the newly created reverse link model artifact
	 */
	public DSRevLink(DSConnection conn, Package pkg) {
		super(conn, REV_LINK_NAME, pkg);
		this.withFeatures(SOURCE_NAME, TARGET_NAME, SOURCE_TYPE_NAME, TARGET_TYPE_NAME, REL_NAMES_NAME);
		linkedPackagesArtifact = conn.createCollectionArtifact(LINKED_PACKAGES_ARTIFACT_NAME, Collections.emptyList(), pkg);
	}
	
	/**
	 * Creates a DSRevLink object using the existing reverse link model artifact.
	 * @param conn the Design Space connection (DSConnection object)
	 * @param artifact the reverse link model artifact
	 * @param pkg the package that contains the reverse link model artifact
	 */
	public DSRevLink(DSConnection conn, Artifact artifact, Package pkg) {
		super(conn, artifact, pkg);
		Collection<Artifact> collArtifacts = pkg.getArtifactsWithProperty("name", LINKED_PACKAGES_ARTIFACT_NAME, true, new Filter());
		if(collArtifacts.size() != 1) {
			throw new IllegalStateException("Invalid package setup for RevLinks! No or multiple linked packages collection artifacts found: " + collArtifacts.size());
		}
		linkedPackagesArtifact = (CollectionArtifact) collArtifacts.iterator().next();
	}
	
	/**
	 * Creates instances (reverse links) from the reverse link model artifact. 
	 * The name of the reverse links are a combination of the prefix "[RL]" and
	 * the hash of the target and the source DSInstance objects.
	 * The collection artifact, that contains the names of the links, is put into the same package
	 * as the reverse link. The name of the collection artifact results from the name of 
	 * the reverse link and the extension ".types".
	 * @param targetType the type artifact of the target
	 * @param sourceType the type artifact of the source
	 * @param target the target artifact
	 * @param source the source artifact
	 * @param instPkg the package that contains the newly created reverse link
	 * @param types the name of the links
	 */
	public void createRevLink(DSClass targetType, DSClass sourceType, DSInstance target, DSInstance source, Package instPkg, String... types) {
		String rlName = "[RL] " + hash(target, source);
		DSInstance revLink = createInstance(rlName, instPkg);
		revLink.setProperty(SOURCE_NAME, source);
		revLink.setProperty(SOURCE_TYPE_NAME, sourceType);
		revLink.setProperty(TARGET_NAME, target);
		revLink.setProperty(TARGET_TYPE_NAME, targetType);
		CollectionArtifact typeCollectionArtifact = 
				conn.createCollectionArtifact(rlName + ".types", Arrays.asList(types), instPkg);
		revLink.setProperty(REL_NAMES_NAME, typeCollectionArtifact);
	}
	
	/**
	 * Adds the specified package to the linked package collection artifact marking it as 
	 * analyzed (i.e. reverse links have been created for this package).
	 * @param pkg Package for which reverse links have been created
	 */
	public void addRevLinkPackage(Package pkgToAdd) {
		conn.addValueToCollection(linkedPackagesArtifact, pkgToAdd.getId());
	}

	/**
	 * Returns true if the specified package has reverse links created for its artifacts.
	 * Note that it is necessary to make use of the addRevLinkPackage method for this check to work. 
	 * @param selectedPkg
	 * @return
	 */
	public boolean containsPackage(Package selectedPkg) {
		return linkedPackagesArtifact.existsElement(selectedPkg.getId());
	}
}
