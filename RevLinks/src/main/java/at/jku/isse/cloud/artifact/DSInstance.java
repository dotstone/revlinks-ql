package at.jku.isse.cloud.artifact;

import static java.util.Objects.requireNonNull;
import at.jku.sea.cloud.Artifact;

/**
 * The DSInstance contains a design space instance artifact. The methods enable to set the values of the properties of the instance artifact.
 * @author Gabriel Schoerghuber 
 * @author Dominik Steinbinder
 */
public class DSInstance {
	
	private final DSConnection conn;
	private final Artifact artifact;

	/**
	 * Creates a new DSInstance object. 
	 * @param conn the design space connection (DSConnection object)
	 * @param artifact the instance artifact
	 */
	public DSInstance(DSConnection conn, Artifact artifact) {
		this.conn = requireNonNull(conn);
		this.artifact = requireNonNull(artifact);
	}
	
	/**
	 * Sets a string value of a property.
	 * @param key the name of the property
	 * @param value the string value to be set
	 */
	public void setProperty(String key, String value) {
		conn.setArtifactProperty(artifact, key, value);
	}
	
	/**
	 * Sets the artifact of the DSInstance object as the value of a link property.
	 * @param link the DSLink object of the link
	 * @param target the DSInstance object, which contains the artifact that is set as the value of the link property
	 */
	public void setLinkProperty(DSLink link, DSInstance target) {
		conn.setArtifactProperty(artifact, link.name, target.artifact);
	}

	/**
	 * Sets the artifact of the DSInstance object as the value of a property.
	 * @param key the name of the property
	 * @param target the DSInstance object, which contains the artifact that is set as the value of the property
	 */
	public void setProperty(String key, DSInstance target) {
		conn.setArtifactProperty(artifact, key, target.artifact);
	}
	
	/**
	 * Sets the artifact of the DSClass object as the value of a property.
	 * @param key the name of the property
	 * @param target the DSClass object, which contains the artifact that is set as the value of the property
	 */
	public void setProperty(String key, DSClass target) {
		conn.setArtifactProperty(artifact, key, target.artifact);
	}
	
	/**
	 * Sets an artifact as the value of a property.
	 * @param key the name of the property
	 * @param target the artifact that is set as the value of the property
	 */
	public void setProperty(String key, Artifact target) {
		conn.setArtifactProperty(artifact, key, target);
	}
}
