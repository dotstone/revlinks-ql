package at.jku.isse.cloud.artifact;

import static java.util.Objects.hash;

import java.util.Arrays;

import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.CollectionArtifact;

public class DSRevLink extends DSClass {
	
	public static final String REV_LINK_NAME = "RLink";
	
	public static final String SOURCE_NAME = "source";
	public static final String SOURCE_MODEL_NAME = "sourceModel";
	public static final String TARGET_NAME = "target";
	public static final String TARGET_MODEL_NAME = "targetModel";
	public static final String REL_NAMES_NAME = "relNames";

	public DSRevLink(DSConnection conn) {
		super(conn, REV_LINK_NAME);
		this.withFeatures(SOURCE_NAME, TARGET_NAME, SOURCE_MODEL_NAME, TARGET_MODEL_NAME, REL_NAMES_NAME);
	}
	
	public DSRevLink(DSConnection conn, Artifact artifact) {
		super(conn, artifact);
	}
	
	public void createRevLink(DSClass targetModel, DSClass sourceModel, DSInstance target, DSInstance source, String... types) {
		String rlName = "[RL] " + hash(target, source);
		DSInstance revLink = createInstance(rlName);
		revLink.setProperty(SOURCE_NAME, source);
		revLink.setProperty(SOURCE_MODEL_NAME, sourceModel);
		revLink.setProperty(TARGET_NAME, target);
		revLink.setProperty(TARGET_MODEL_NAME, targetModel);
		
		CollectionArtifact typeCollectionArtifact = 
				conn.createCollectionArtifact(rlName + ".types", Arrays.asList(types));
		revLink.setProperty(REL_NAMES_NAME, typeCollectionArtifact);
	}
}
