package at.jku.isse.cloud.artifact;


public class DSLinkArtifact extends DSClass {
	
	static final String TARGET_NAME = "target";
	static final String SRC_MIN_NAME = "srcMin";
	static final String SRC_MAX_NAME = "srcMax";
	static final String DST_MIN_NAME = "dstMin";
	static final String DST_MAX_NAME = "dstMax";

	public DSLinkArtifact(DSConnection conn, DSLink link) {
		super(conn, link.name);
		addProperty(TARGET_NAME, link.target.artifact);
		addProperty(SRC_MIN_NAME, link.srcMin);
		addProperty(SRC_MAX_NAME, link.srcMax);
		addProperty(DST_MIN_NAME, link.dstMin);
		addProperty(DST_MAX_NAME, link.dstMax);
	}
}