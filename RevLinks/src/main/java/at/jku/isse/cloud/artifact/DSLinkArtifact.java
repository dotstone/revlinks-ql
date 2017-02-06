package at.jku.isse.cloud.artifact;

import at.jku.sea.cloud.Package;

/**
 * Represents the artifact for the link of a DSLink object. The artifact contains 
 * the properties "target", "srcMin", "srcMax", "dstMin" and "dstMax".
 * @author Gabriel Schoerghuber
 * @author Dominik Steinbinder
 */
public class DSLinkArtifact extends DSClass {
	
	static final String TARGET_NAME = "target";
	static final String SRC_MIN_NAME = "srcMin";
	static final String SRC_MAX_NAME = "srcMax";
	static final String DST_MIN_NAME = "dstMin";
	static final String DST_MAX_NAME = "dstMax";

	/**
	 * Creates a new DSLinkArtifact object and an artifact for the link in the Design Space.
	 * @param conn the design space connection (DSConnection object)
	 * @param link the DSLink object of the link
	 * @param pkg the package that contains the newly created artifact
	 */
	public DSLinkArtifact(DSConnection conn, DSLink link, Package pkg) {
		super(conn, link.name, pkg);
		addProperty(TARGET_NAME, link.target.artifact);
		addProperty(SRC_MIN_NAME, link.srcMin);
		addProperty(SRC_MAX_NAME, link.srcMax);
		addProperty(DST_MIN_NAME, link.dstMin);
		addProperty(DST_MAX_NAME, link.dstMax);
	}
}