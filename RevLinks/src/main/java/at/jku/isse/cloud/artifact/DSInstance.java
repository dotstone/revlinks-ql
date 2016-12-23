package at.jku.isse.cloud.artifact;

import static java.util.Objects.requireNonNull;
import at.jku.sea.cloud.Artifact;

public class DSInstance {
	
	private final DSConnection conn;
	private final Artifact artifact;

	public DSInstance(DSConnection conn, Artifact artifact) {
		this.conn = requireNonNull(conn);
		this.artifact = requireNonNull(artifact);
	}
	
	public void setProperty(String key, String value) {
		conn.setArtifactProperty(artifact, key, value);
	}
	
	public void setLinkProperty(DSLink link, DSInstance target) {
		conn.setArtifactProperty(artifact, link.name, target.artifact);
	}

	public void setProperty(String key, DSInstance target) {
		conn.setArtifactProperty(artifact, key, target.artifact);
	}
	
	public void setProperty(String key, DSClass target) {
		conn.setArtifactProperty(artifact, key, target.artifact);
	}
	
	public void setProperty(String key, Artifact target) {
		conn.setArtifactProperty(artifact, key, target);
	}
}
