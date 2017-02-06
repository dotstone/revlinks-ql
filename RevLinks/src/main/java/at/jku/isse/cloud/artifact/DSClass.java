package at.jku.isse.cloud.artifact;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.CollectionArtifact;
import at.jku.sea.cloud.Package;
import at.jku.sea.cloud.Project;

/**
 * Contains the model artifact and the package that contains the model artifact. A model artifact is a ComplexType and may contain
 * features, operations, links and a super type. From this model artifact, instance artifacts can be created. The model artifacts can be 
 * added to projects.
 * @author Gabriel Schoerghuber
 * @author Dominik Steinbinder
 */
public class DSClass {
	
	static final String LINK_PROPERTY_NAME = "Links"; 
	
	protected final DSConnection conn;
	protected final Package pkg;
	
	protected final Artifact artifact;
	
	/**
	 * Creates a new DSClass object and an artifact with the specified name in the specified package of the Design Space.
	 * @param conn the Design Space connection (DSConnection object)
	 * @param name the name of the artifact
	 * @param pkg the package that contains the newly created artifact
	 */
	public DSClass(DSConnection conn, String name, Package pkg) {
		this.conn = requireNonNull(conn);
		this.pkg = pkg;
		this.artifact = conn.createNamedArtifact(name, pkg);
	}
	
	/**
	 * Creates a new DSClass object and an artifact with the specified name in the specified package of the Design Space.
	 * The artifact is added to the project.
	 * @param conn the Design Space connection (DSConnection object)
	 * @param name the name of the artifact
	 * @param pkg the package that contains the newly created artifact
	 * @param project the project to which the artifact is added to
	 */
	public DSClass(DSConnection conn, String name, Package pkg, Project project) {
		this.conn = requireNonNull(conn);
		this.pkg = pkg;
		this.artifact = conn.createNamedArtifact(name, pkg);
		addToProject(project);
	}
	
	/**
	 * Creates a new DSClass object that contains the artifact. 
	 * The artifact is contained in the specified package.
	 * @param conn the Design Space connection (DSConnection object)
	 * @param artifact the specified artifact
	 * @param pkg the package that contains the artifact
	 */
	public DSClass(DSConnection conn, Artifact artifact, Package pkg) {
		this.conn = requireNonNull(conn);
		this.pkg = pkg;
		this.artifact = requireNonNull(artifact);
	}
	
	/**
	 * Creates a feature artifact for every feature and adds them to the model artifact of the DSClass object.
	 * @param sFeatures the names of the features
	 * @return the DSClass object
	 */
	public DSClass withFeatures(String... sFeatures) {
		List<Artifact> featureArtifacts = Arrays.stream(sFeatures).map(conn::createFeature).collect(Collectors.toList());
    	featureArtifacts.forEach(feature -> conn.addFeatureToComplexType(artifact, feature));
    	return this;
    }
	
	/**
	 * Creates an operation artifact for every operation and adds them to the model artifact of the DSClass object.
	 * @param operationNames the names of the operations
	 * @return the DSClass object
	 */
	public DSClass withOperations(String... operationNames) {
		List<Artifact> operationArtifacts = Arrays.stream(operationNames).map(conn::createOperation).collect(Collectors.toList());
		operationArtifacts.forEach(operation -> conn.addOperationToComplexType(artifact, operation));
        return this;
    }
	
	/**
	 * Creates a DSLinkArtifact object for each link, which also creates the corresponding artifacts in the Design Space. 
	 * A CollectionArtifact is created that contains all the newly created link artifacts. The created CollectionArtifact is
	 * set as the value of the property with the name "Links" in the model artifact. 
	 * @param links the links that are associated with the model artifact
	 * @return the DSClass object
	 */
	public DSClass withLinks(DSLink... links) {
		Collection<Artifact> linkArtifacts = Arrays.stream(links)
				.map(link -> new DSLinkArtifact(conn, link, pkg))
				.map(artifact -> artifact.artifact)
				.collect(Collectors.toList());
		CollectionArtifact linksArtifact = conn.createCollectionArtifact(LINK_PROPERTY_NAME, linkArtifacts, pkg);
		addProperty(LINK_PROPERTY_NAME, linksArtifact);
		return this;
	}
	
	/**
	 * Adds a property to the artifact.
	 * @param name the name of the property
	 * @param val the value of the property
	 */
	protected void addProperty(String name, Object val) {
		conn.addProperty(artifact, name, val);
	}
	
	/**
	 * Sets an artifact as the super type of the model artifact.
	 * @param target the super type artifact
	 * @return the DSClass object
	 */
	public DSClass withSuperType(DSClass target) {
		conn.addSuperTypeToComplexType(artifact, target.artifact);
		return this;
	}
	
	/**
	 * Creates a new instance artifact of the model artifact and a DSInstance object. This DSInstance object contains the newly created
	 * instance artifact.
	 * @param name the name of the instance artifact
	 * @param pkg the package that contains the newly created instance artifact
	 * @return the newly created DSInstance object
	 */
	public DSInstance createInstance(String name, Package pkg) {
		return new DSInstance(conn, conn.createInstance(this.artifact, name, pkg));
	}
	
	/**
	 * Adds the artifact to the specified project.
	 * @param project the specified project
	 */
	public void addToProject(Project project) {
		conn.addArtifactToProject(this.artifact, project);
	}
}