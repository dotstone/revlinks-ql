package at.jku.isse.cloud.artifact;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.Cloud;
import at.jku.sea.cloud.CollectionArtifact;
import at.jku.sea.cloud.Package;
import at.jku.sea.cloud.Property;
import at.jku.sea.cloud.Tool;
import at.jku.sea.cloud.User;
import at.jku.sea.cloud.Workspace;
import at.jku.sea.cloud.exceptions.ArtifactDoesNotExistException;
import at.jku.sea.cloud.exceptions.CredentialsException;
import at.jku.sea.cloud.mmm.MMMTypeProperties;
import at.jku.sea.cloud.mmm.MMMTypesFactory;
import at.jku.sea.cloud.rest.client.RestCloud;

public class DSConnection {
	
	private final Workspace ws;
	private final Package pkg;
	
	public DSConnection(String username, String pwd, String workspace, String pkgName) {
		Cloud cloud = RestCloud.getInstance();
        User user = getOrCreateUser(cloud, username, username, pwd);
        Tool tool = cloud.createTool("MyTool", "0.1");
        this.ws = cloud.createWorkspace(user.getOwner(), tool, workspace);
        this.pkg = ws.createPackage(pkgName);
	}
    
	public Artifact createNamedArtifact(String name) {
    	Artifact a = MMMTypesFactory.createComplexType(ws, pkg, name, false, false);
        return a;
    }
	
	public Artifact createInstance(Artifact model, String name) {
		return MMMTypesFactory.createComplexTypeInstance(ws, name, model);
	}
	
	public Artifact createFeature(String name) {
		Artifact a = MMMTypesFactory.createFeature(ws, name, null, false, false, false);
        return a;
	}
	
	public Artifact createOperation(String name) {
		Artifact a = MMMTypesFactory.createOperation(ws, name, null, null, false, false, false);
        return a;
	}
    
	public <T> CollectionArtifact createCollectionArtifact(String name, Collection<T> vals) {
    	CollectionArtifact a = ws.createCollection(false, pkg);
    	addProperty(a, "name", name);
    	a.addElements(ws, vals);
    	return a;
    }
	
	public void addProperty(Artifact artifact, String name, Object val) {
		Property prop = artifact.createProperty(ws, name);
        prop.setValue(ws, val);
	}
	
	public void addFeatureToComplexType(Artifact complexType, Artifact feature) {
		MMMTypesFactory.addFeatureToComplexType(ws, complexType, feature);
	}
	
	public void addOperationToComplexType(Artifact complexType, Artifact operation) {
		MMMTypesFactory.addOperationToComplexType(ws, complexType, operation);
	}
	
	public void addSuperTypeToComplexType(Artifact complexType, Artifact superType) {
		MMMTypesFactory.addSuperTypeToComplexType(ws, complexType, superType);
	}
	
	public void setArtifactProperty(Artifact artifact, String name, String value) {
		artifact.setPropertyValue(ws, name, value);
	}
	
	public void setArtifactProperty(Artifact artifact, String name, Artifact value) {
		artifact.setPropertyValue(ws, name, value);
	}
	
	public Object getArtifactProperty(Artifact artifact, String name) {
		return artifact.getPropertyValue(name);
	}
	
	public Optional<Artifact> getArtifactById(long id) {
		try {
			return Optional.of(ws.getArtifact(id));
		} catch(ArtifactDoesNotExistException e) {
			return Optional.empty();
		}
	}
	
	public void commit(String msg) {
		ws.commitAll(msg);
	}
    
	private User getOrCreateUser(Cloud cloud, String name, String login, String pwd) {
    	try {
    		return cloud.getUserByCredentials(login, pwd);
    	} catch(CredentialsException e) {
    		try {
    			return cloud.createUser(name, login, pwd);
    		} catch(CredentialsException e2) {
    			throw new RuntimeException("Failed to create User " + name + "! Wrong password?", e2);
    		}
    	}
    }

	public Collection<Artifact> getAllArtifacts() {
		return ws.getArtifacts();
	}

	public Collection<Artifact> getArtifactsOfType(DSClass type) {
		return ws.getArtifacts().stream().filter(a -> a.getType().getId() == type.artifact.getId()).collect(Collectors.toList());
	}
	
	public DSRevLink getOrCreateReverseLinkClass() {
		try {
			return getReverseLinkClass();
		} catch(IllegalStateException e) {
			return new DSRevLink(this);
		}
	}
	
	private DSRevLink getReverseLinkClass() {
		return new DSRevLink(this, 
				ws.getArtifacts().stream()
					.filter(this::hasRevLinkName)
					.findAny()
					.orElseThrow(() -> new IllegalStateException("Model for reverse links not found!")));
	}
	
	private boolean hasRevLinkName(Artifact artifact) {
		Object val = artifact.getPropertyValueOrNull(MMMTypeProperties.NAME);
		return DSRevLink.REV_LINK_NAME.equals(val);
	}
}
