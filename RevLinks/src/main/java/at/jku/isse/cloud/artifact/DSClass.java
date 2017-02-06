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

public class DSClass {
	
	static final String LINK_PROPERTY_NAME = "Links"; 
	
	protected final DSConnection conn;
	protected final Package pkg;
	
	protected final Artifact artifact;
	
	public DSClass(DSConnection conn, String name, Package pkg) {
		this.conn = requireNonNull(conn);
		this.pkg = pkg;
		this.artifact = conn.createNamedArtifact(name, pkg);
	}
	
	public DSClass(DSConnection conn, String name, Package pkg, Project project) {
		this.conn = requireNonNull(conn);
		this.pkg = pkg;
		this.artifact = conn.createNamedArtifact(name, pkg);
		addToProject(project);
	}
	
	public DSClass(DSConnection conn, Artifact artifact, Package pkg) {
		this.conn = requireNonNull(conn);
		this.pkg = pkg;
		this.artifact = requireNonNull(artifact);
	}
	
	public DSClass withFeatures(String... sFeatures) {
		List<Artifact> featureArtifacts = Arrays.stream(sFeatures).map(conn::createFeature).collect(Collectors.toList());
    	featureArtifacts.forEach(feature -> conn.addFeatureToComplexType(artifact, feature));
    	return this;
    }
	
	public DSClass withOperations(String... operationNames) {
		List<Artifact> operationArtifacts = Arrays.stream(operationNames).map(conn::createOperation).collect(Collectors.toList());
		operationArtifacts.forEach(operation -> conn.addOperationToComplexType(artifact, operation));
        return this;
    }
	
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
	
	public DSClass withSuperType(DSClass target) {
		conn.addSuperTypeToComplexType(artifact, target.artifact);
		return this;
	}
	
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