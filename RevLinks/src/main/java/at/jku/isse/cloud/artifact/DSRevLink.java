package at.jku.isse.cloud.artifact;

import static java.util.Objects.hash;

import java.util.Arrays;

import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.CollectionArtifact;
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
	
	public static final String SOURCE_NAME = "source";
	public static final String SOURCE_MODEL_NAME = "sourceModel";
	public static final String TARGET_NAME = "target";
	public static final String TARGET_MODEL_NAME = "targetModel";
	public static final String REL_NAMES_NAME = "relNames";

	/**
	 * Creates a DSRevLink object and the reverse link model artifact in the Design Space.
	 * @param conn the Design Space connection (DSConnection object)
	 * @param pkg the package that contains the newly created reverse link model artifact
	 */
	public DSRevLink(DSConnection conn, Package pkg) {
		super(conn, REV_LINK_NAME, pkg);
		this.withFeatures(SOURCE_NAME, TARGET_NAME, SOURCE_MODEL_NAME, TARGET_MODEL_NAME, REL_NAMES_NAME);
	}
	
	/**
	 * Creates a DSRevLink object using the existing reverse link model artifact.
	 * @param conn the Design Space connection (DSConnection object)
	 * @param artifact the reverse link model artifact
	 * @param pkg the package that contains the reverse link model artifact
	 */
	public DSRevLink(DSConnection conn, Artifact artifact, Package pkg) {
		super(conn, artifact, pkg);
	}
	
	/**
	 * Creates instances (reverse links) from the reverse link model artifact. 
	 * The name of the reverse links are a combination of the prefix "[RL]" and
	 * the hash of the target and the source DSInstance objects.
	 * The collection artifact, that contains the names of the links, is put into the same package
	 * as the reverse link. The name of the collection artifact results from the name of 
	 * the reverse link and the extension ".types".
	 * @param targetModel the model artifact of the target
	 * @param sourceModel the model artifact of the source
	 * @param target the target artifact
	 * @param source the source artifact
	 * @param instPkg the package that contains the newly created reverse link
	 * @param types the name of the links
	 */
	public void createRevLink(DSClass targetModel, DSClass sourceModel, DSInstance target, DSInstance source, Package instPkg, String... types) {
		String rlName = "[RL] " + hash(target, source);
		DSInstance revLink = createInstance(rlName, instPkg);
		revLink.setProperty(SOURCE_NAME, source);
		revLink.setProperty(SOURCE_MODEL_NAME, sourceModel);
		revLink.setProperty(TARGET_NAME, target);
		revLink.setProperty(TARGET_MODEL_NAME, targetModel);
		CollectionArtifact typeCollectionArtifact = 
				conn.createCollectionArtifact(rlName + ".types", Arrays.asList(types), instPkg);
		revLink.setProperty(REL_NAMES_NAME, typeCollectionArtifact);
	}
}
