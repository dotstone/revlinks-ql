package at.jku.isse.cloud.artifact;

import at.jku.sea.cloud.Package;

public class DSLinkArtifact extends DSClass {
	
	static final String TARGET_NAME = "target";
	static final String SRC_MIN_NAME = "srcMin";
	static final String SRC_MAX_NAME = "srcMax";
	static final String DST_MIN_NAME = "dstMin";
	static final String DST_MAX_NAME = "dstMax";

	public DSLinkArtifact(DSConnection conn, DSLink link, Package pkg) {
		super(conn, link.name, pkg);
		addProperty(TARGET_NAME, link.target.artifact);
		addProperty(SRC_MIN_NAME, link.srcMin);
		addProperty(SRC_MAX_NAME, link.srcMax);
		addProperty(DST_MIN_NAME, link.dstMin);
		addProperty(DST_MAX_NAME, link.dstMax);
	}
}