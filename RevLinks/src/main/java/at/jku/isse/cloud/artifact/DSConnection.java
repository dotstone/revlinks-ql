package at.jku.isse.cloud.artifact;

import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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
import at.jku.sea.cloud.stream.Context.Path;
import at.jku.sea.cloud.stream.Contexts;
import at.jku.sea.cloud.stream.QueryFactory;
import at.jku.sea.cloud.stream.predicate.Predicate;
import at.jku.sea.cloud.stream.predicate.PredicateProvider;

/**
 * Represents a connection to the Design Space and provides methods for creating and modifying artifacts in the workspace.
 * @author Gabriel Schoerghuber
 * @author Dominik Steinbinder
 */
public class DSConnection {
	
	private static final String PATH = "";
	
	private final Cloud cloud;
	private final Workspace ws;
	private final QueryFactory queryFactory;
	
	private final Predicate<Artifact> isRevLinkArtifact;
	
	private DSRevLink revLinkClass;
	
	/**
	 * Creates a new DSConnection object, a user (if it doesn't exist) and a workspace with the given tool.
	 * @param username the name of the user
	 * @param pwd the password of the user
	 * @param toolId Artifact ID of the tool.
	 * @param workspace the identifier for the workspace
	 */
	public DSConnection(String username, String pwd, int toolId, String workspace) {
		this.cloud = RestCloud.getInstance();
        User user = getOrCreateUser(cloud, username, username, pwd);
        Tool tool = cloud.getTool(toolId);
        this.ws = cloud.createWorkspace(user.getOwner(), tool, workspace);
        
        queryFactory = cloud.queryFactory();
		Path path = Contexts.of(PATH);
		PredicateProvider predicates = queryFactory.predicateProvider();
		isRevLinkArtifact = predicates.hasProperty(path, MMMTypeProperties.NAME)
				.and(predicates.hasPropertyValue(path, MMMTypeProperties.NAME, DSRevLink.REV_LINK_NAME));
	}
	
	/**
	 * Creates a new DSConnection object, a user (if it doesn't exist), a tool with the name "RevLinks" in the version 0.1 and a workspace.
	 * @param username the name of the user
	 * @param pwd the password of the user
	 * @param workspace the identifier for the workspace
	 */
	public DSConnection(String username, String pwd, String workspace) {
		this.cloud = RestCloud.getInstance();
        User user = getOrCreateUser(cloud, username, username, pwd);
        Tool tool = getOrCreateTool(cloud, "RevLinks", "0.1");
        this.ws = cloud.createWorkspace(user.getOwner(), tool, workspace);
        
        queryFactory = cloud.queryFactory();
		Path path = Contexts.of(PATH);
		isRevLinkArtifact = queryFactory.predicateProvider().hasProperty(path, MMMTypeProperties.NAME)
				.and(queryFactory.predicateProvider().hasPropertyValue(path, MMMTypeProperties.NAME, DSRevLink.REV_LINK_NAME));
	}
	
	/**
	 * Creates and returns a new project.
	 * @param name the name of the project
	 * @return the newly created project
	 */
	public Project createProject(String name) {
		return ws.createProject(name);
	}
	
	/**
	 * Returns a collection of all visible packages.
	 * @return the collection of packages
	 */
	public Collection<Package> getPackages() {
		return ws.getPackages();
	}
	
	/**
	 * Returns the package with the specified name. If it doesn't exist, then a new package is created and returned.
	 * @param pkg the name of the package
	 * @return the found or newly created package
	 */
	public Package getOrCreatePackage(String pkg) {
		return getOrCreatePackage(pkg, null);
	}
	
	/**
	 * Returns the package with the specified name. If it doesn't exist, then a new package is created and returned. 
	 * The parent package can also be specified. 
	 * @param pkg the name of the package
	 * @param parent the parent package
	 * @return the found or newly created package
	 */
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
	
	private Tool getOrCreateTool(Cloud cloud, String name, String toolVersion) {
		return cloud.getTools().stream()
				.filter(t -> t.getName().equals(name) && t.getToolVersion().equals(toolVersion))
				.findAny().orElseGet(() -> cloud.createTool(name, toolVersion));
	}
	
	/**
	 * Returns the projects of the workspace.
	 * @return the projects of the workspace
	 */
	public Collection<Project> getProjects() {
		return ws.getProjects();
	}
    
	/**
	 * Creates an artifact with the specified name in the specified package. The type of the artifact is ComplexType.
	 * @param name the name of the artifact
	 * @param pkg the package that contains the artifact
	 * @return the newly created artifact
	 */
	public Artifact createNamedArtifact(String name, Package pkg) {
    	Artifact a = MMMTypesFactory.createComplexType(ws, pkg, name, false, false);
        return a;
    }
	
	/**
	 * Creates an instance artifact from the ComplexType artifact.
	 * @param model the model artifact, which is a ComplexType
	 * @param name the name of the instance artifact
	 * @param pkg the package that contains the instance artifact
	 * @return the newly created instance artifact
	 */
	public Artifact createInstance(Artifact model, String name, Package pkg) {
		Artifact a = MMMTypesFactory.createComplexTypeInstance(ws, name, model);
		a.setPackage(ws, pkg);
		return a;
	}
	
	/**
	 * Creates a feature artifact with the specified name.
	 * @param name the name of the feature artifact
	 * @return the newly created feature artifact
	 */
	public Artifact createFeature(String name) {
		return MMMTypesFactory.createFeature(ws, name, null, false, false, false);
	}
	
	/**
	 * Creates an operation artifact with the specified name.
	 * @param name the name of the operation artifact
	 * @return the newly created operation artifact
	 */
	public Artifact createOperation(String name) {
		return MMMTypesFactory.createOperation(ws, name, null, null, false, false, false);
	}
    
	/**
	 * Creates a collection artifact with the specified name in the specified package. 
	 * @param name the specified name of the collection artifact
	 * @param vals the values that are added to the collection artifact
	 * @param pkg the package that contains the newly created collection artifact
	 * @return the newly created collection artifact
	 */
	public <T> CollectionArtifact createCollectionArtifact(String name, Collection<T> vals, Package pkg) {
    	CollectionArtifact a = ws.createCollection(false, pkg);
    	addProperty(a, "name", name);
    	a.addElements(ws, vals);
    	return a;
    }
	
	/**
	 * Adds a property to the artifact.
	 * @param artifact the artifact to which the property is added
	 * @param name the name of the property
	 * @param val the value of the property
	 */
	public void addProperty(Artifact artifact, String name, Object val) {
		Property prop = artifact.createProperty(ws, name);
        prop.setValue(ws, val);
	}
	
	/**
	 * Adds a feature artifact to a ComplexType.
	 * @param complexType the ComplexType artifact
	 * @param feature the feature artifact
	 */
	public void addFeatureToComplexType(Artifact complexType, Artifact feature) {
		MMMTypesFactory.addFeatureToComplexType(ws, complexType, feature);
	}
	
	/**
	 * Adds an operation artifact to a ComplexType.
	 * @param complexType the ComplexType artifact
	 * @param operation the operation artifact
	 */
	public void addOperationToComplexType(Artifact complexType, Artifact operation) {
		MMMTypesFactory.addOperationToComplexType(ws, complexType, operation);
	}
	
	/**
	 * Adds an artifact as a super type of a ComplexType.
	 * @param complexType the ComplexType artifact
	 * @param superType the super type artifact
	 */
	public void addSuperTypeToComplexType(Artifact complexType, Artifact superType) {
		MMMTypesFactory.addSuperTypeToComplexType(ws, complexType, superType);
	}
	
	/**
	 * Sets the value of the property. 
	 * @param artifact the artifact in which the property is contained
	 * @param name the name of the property
	 * @param value the value of the property
	 */
	public void setArtifactProperty(Artifact artifact, String name, String value) {
		artifact.setPropertyValue(ws, name, value);
	}
	
	/**
	 * Sets an artifact as a value of the property.
	 * @param artifact the artifact in which the property is contained
	 * @param name the name of the artifact
	 * @param value the value of the property, which is an artifact
	 */
	public void setArtifactProperty(Artifact artifact, String name, Artifact value) {
		artifact.setPropertyValue(ws, name, value);
	}
	
	/**
	 * Returns the artifact with the specified id from the workspace.
	 * If there exists no artifact with the specified id, then an empty optional instance is returned.
	 * @param id the specified id of the artifact
	 * @return the artifact with the specified id or an empty optional instance, if no artifact with the specified id exists
	 */
	public Optional<Artifact> getArtifactById(long id) {
		try {
			return Optional.of(ws.getArtifact(id));
		} catch(ArtifactDoesNotExistException e) {
			return Optional.empty();
		}
	}
	
	/**
	 * Commits (and publishes) the contents of the workspace.
	 * @param msg the commit message, can be set to null
	 */
	public void commit(String msg) {
		ws.commitAll(msg);
	}
	
	/**
	 * Commits (and publishes the contents of the workspace. If the commit fails, then the stack trace is printed out.
	 * @param msg the commit message, can be set to null
	 * @return true, if the commit is successful, otherwise false
	 */
	public boolean tryCommit(String msg) {
		try {
			commit(msg);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
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

	public Collection<Artifact> getArtifactsOfType(DSClass type, Package parent) {
		if(parent == null) {
			return Collections.emptyList();
		}
		// Get package with matching name and parent package
		return parent.getArtifacts().stream()
				.filter(a -> a.getType().getId() == type.artifact.getId())
				.collect(Collectors.toList());
	}
	
	/**
	 * Gets the package for the reverse link model artifact with the name "RevLinks" and returns the reverse link model artifact. 
	 * If the reverse link model artifact or/and the package don't exist, then they will be created first.
	 * This method caches the class to speedup further calls.
	 * @return the newly created or existing reverse link model artifact
	 */
	public DSRevLink getOrCreateReverseLinkClass() {
		if(revLinkClass != null) {
			return revLinkClass;
		}
		Package pkg = getOrCreatePackage("RevLinks");
		try {
			revLinkClass = getReverseLinkClass(pkg);
			return revLinkClass;
		} catch(IllegalStateException e) {
			return new DSRevLink(this, pkg);
		}
	}
	
	private DSRevLink getReverseLinkClass(Package pkg) {			
		try {
			Artifact artifact = queryFactory.streamProvider().of(ws.getArtifacts()).find(PATH, isRevLinkArtifact);
			return new DSRevLink(this, artifact, pkg);
		} catch(NoSuchElementException e) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Adds the artifact to the specified project.
	 * @param artifact the artifact to be added to the project
	 * @param project the specified project
	 */
	public void addArtifactToProject(Artifact artifact, Project project) {
		artifact.addToProject(ws, project);
	}

	/**
	 * Returns the package with the specified name from the workspace.
	 * If there exists no package with the specified name, then an empty optional instance is returned.
	 * @param targetPkg the specified name of the package
	 * @return the package with the specified name or an empty optional instance, if no package with the specified name exists
	 */
	public Optional<Package> getPackageFromName(String targetPkg) {
		for(Package pkg : ws.getPackages()) {
			if(targetPkg.equals(pkg.getPropertyValue("name"))) {
				return Optional.of(pkg);
			}
		}
		return Optional.empty();
	}

	/**
	 * Sets the value of a property of an artifact.
	 * @param artifact the artifact that holds the property
	 * @param propertyKey the name of the property
	 * @param value the value that will be set to the property
	 */
	public <T> void setPropertyValue(Artifact artifact, String propertyKey, T value) {
		artifact.setPropertyValue(ws, propertyKey, value);
	}

	public void addValueToCollection(CollectionArtifact collArtifact, long id) {
		collArtifact.addElement(ws, id);
	}
}
