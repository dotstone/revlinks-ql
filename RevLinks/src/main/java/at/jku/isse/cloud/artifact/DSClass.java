package at.jku.isse.cloud.artifact;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.CollectionArtifact;

public class DSClass {
	
	static final String LINK_PROPERTY_NAME = "Links"; 
	
	protected final DSConnection conn;
	
	protected final Artifact artifact;
	
	public DSClass(DSConnection conn, String name) {
		this.conn = requireNonNull(conn);
		this.artifact = conn.createNamedArtifact(name);
	}
	
	public DSClass(DSConnection conn, Artifact artifact) {
		this.conn = requireNonNull(conn);
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
				.map(link -> new DSLinkArtifact(conn, link))
				.map(artifact -> artifact.artifact)
				.collect(Collectors.toList());
		CollectionArtifact linksArtifact = conn.createCollectionArtifact(LINK_PROPERTY_NAME, linkArtifacts);
		addProperty(LINK_PROPERTY_NAME, linksArtifact);
		return this;
	}
	
	protected void addProperty(String name, Object val) {
		conn.addProperty(artifact, name, val);
	}
	
	public DSClass withSuperType(DSClass target) {
		conn.addSuperTypeToComplexType(artifact, target.artifact);
		return this;
	}
	
	public DSInstance createInstance(String name) {
		return new DSInstance(conn, conn.createInstance(this.artifact, name));
	}
}