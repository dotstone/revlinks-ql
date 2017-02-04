package at.jku.isse.cloud.artifact;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import at.jku.isse.cloud.revlinks.RevLinkCreation;
import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.Cloud;
import at.jku.sea.cloud.CollectionArtifact;
import at.jku.sea.cloud.Package;
import at.jku.sea.cloud.Project;
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
	
	public DSConnection(String username, String pwd, String workspace) {
		Cloud cloud = RestCloud.getInstance();
        User user = getOrCreateUser(cloud, username, username, pwd);
        Tool tool = cloud.createTool("RevLinks", "0.1");
        this.ws = cloud.createWorkspace(user.getOwner(), tool, workspace);
	}
	
	public Project createProject(String name) {
		return ws.createProject(name);
	}
	
	public Package getOrCreatePackage(String pkg) {
		return getOrCreatePackage(pkg, null);
	}
	
	public Package getOrCreatePackage(String pkg, Package parent) {
		return ws.getPackages().stream()
				.filter(p -> pkg.equals(p.getPropertyValue("name")))
				.filter(p -> p.getPackage() == null || parent == null || p.getPackage().getId() == parent.getId())
				.findAny()
				.orElseGet(() -> createPackage(pkg, parent));
	}
	
	private Package createPackage(String pkg, Package parent) {
		if(parent == null) {
			return ws.createPackage(pkg);
		} else {
			return ws.createPackage(parent, pkg);	
		}
	}
	
	public Collection<Project> getProjects() {
		return ws.getProjects();
	}
    
	public Artifact createNamedArtifact(String name, Package pkg) {
    	Artifact a = MMMTypesFactory.createComplexType(ws, pkg, name, false, false);
        return a;
    }
	
	public Artifact createInstance(Artifact model, String name, Package pkg) {
		Artifact a = MMMTypesFactory.createComplexTypeInstance(ws, name, model);
		a.setPackage(ws, pkg);
		return a;
	}
	
	public Artifact createFeature(String name) {
		return MMMTypesFactory.createFeature(ws, name, null, false, false, false);
	}
	
	public Artifact createOperation(String name) {
		return MMMTypesFactory.createOperation(ws, name, null, null, false, false, false);
	}
    
	public <T> CollectionArtifact createCollectionArtifact(String name, Collection<T> vals, Package pkg) {
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

	public Collection<Artifact> getArtifactsOfType(DSClass type, Package parent) {
		if(parent == null) {
			// TODO: Get all artifacts without package
			return Collections.emptyList();
		}
		String rlPkgName = parent.getPropertyValue("name").toString() + RevLinkCreation.RL_EXTENSION;
		// Get package with matching name and parent package
		Optional<Package> rlPkg = ws.getPackages().stream().filter(pkg -> rlPkgName.equals(pkg.getPropertyValue("name")) && parentsMatch(pkg, parent)).findAny();
		Collection<Artifact> potentialCandidates = rlPkg.map(pkg -> pkg.getArtifacts()).orElse(Collections.emptyList());
		return potentialCandidates.stream().filter(a -> a.getType().getId() == type.artifact.getId()).collect(Collectors.toList());
	}
	
	private boolean parentsMatch(Package p1, Package p2) {
		if(p1.getPackage() == null || p2.getPackage() == null) {
			return p1.getPackage() == p2.getPackage();
		}
		return p1.getPackage().getId() == p2.getPackage().getId();
	}
	
	public DSRevLink getOrCreateReverseLinkClass() {
		Package pkg = ws.createPackage("RevLinks");
		try {
			return getReverseLinkClass(pkg);
		} catch(IllegalStateException e) {
			return new DSRevLink(this, pkg);
		}
	}
	
	private DSRevLink getReverseLinkClass(Package pkg) {
		return new DSRevLink(this, 
				ws.getArtifacts().stream()
					.filter(this::hasRevLinkName)
					.findAny()
					.orElseThrow(() -> new IllegalStateException("Model for reverse links not found!")), 
				pkg);
	}
	
	private boolean hasRevLinkName(Artifact artifact) {
		Object val = artifact.getPropertyValueOrNull(MMMTypeProperties.NAME);
		return DSRevLink.REV_LINK_NAME.equals(val);
	}

	public void addArtifactToProject(Artifact artifact, Project project) {
		artifact.addToProject(ws, project);
	}

	public Optional<Package> getPackageFromName(String targetPkg) {
		for(Package pkg : ws.getPackages()) {
			if(targetPkg.equals(pkg.getPropertyValue("name"))) {
				return Optional.of(pkg);
			}
		}
		return Optional.empty();
	}
}
