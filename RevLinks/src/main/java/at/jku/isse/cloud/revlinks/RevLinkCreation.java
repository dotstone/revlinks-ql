package at.jku.isse.cloud.revlinks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import at.jku.isse.cloud.artifact.DSClass;
import at.jku.isse.cloud.artifact.DSConnection;
import at.jku.isse.cloud.artifact.DSInstance;
import at.jku.isse.cloud.artifact.DSRevLink;
import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.Package;

/**
 * This class contains the functionality for creating the reverse links.
 * @author Gabriel Schoerghuber
 * @author Dominik Steinbinder
 */
public class RevLinkCreation {
	
	public static final String RL_PREFIX = "RL_";
	
	private static final String OPPOSITE_PROPERTY_KEY = "@opposite";
	
	private static DSConnection conn;
	private static DSRevLink revLink;

	public static void main(String[] args) {
		conn = new DSConnection("RL_user", "RL_pwd", "RL_workspace");
		revLink = conn.getOrCreateReverseLinkClass();
		
		Set<String> pkgNames = new PackageSelector().select(name -> conn.getPackageFromName(name) != null);
		pkgNames.stream()
				.map(conn::getPackageFromName)
				.filter(p -> p.isPresent())
				.map(pkgOpt -> pkgOpt.get())
				.flatMap(pkg -> pkg.getArtifacts().stream())
				.forEach(RevLinkCreation::createRevLinksForArtifact);
		
		System.out.println("Finished.");
		
		/* Queries for the six methodologies:
		 * (2) --L Which car and color correspond to the given link?
		 * (3) -D- Which artifacts are directly linked to a specific color?
		 * (4) -DL Which car is linked to color black?
		 * (5) S-- Which artifacts are directly linked to a specific car?
		 * (6) S-L Which color does a specific car have?
		 * (7) SD- What is the relation between a specific car and a specific color?
		 */
		
		conn.tryCommit("");
	}
	
	private static void createRevLinksForArtifact(Artifact artifact) {
		createRevLinksForArtifact(conn, artifact, revLink);
	}
	
	/**
	 * Creates the reverse link package and the reverse link artifacts for the corresponding artifacts of a given package. 
	 * The reverse link package is only created, if it doesn't exist. The ids of the target artifacts of a reverse link are added to
	 * the collection artifact of the "@opposite" property of the source artifact. 
	 * The id of the given package is added to the ids of packages, for which the reverse links have already been created.
	 * @param connection the Design Space connection
	 * @param pkg the given package that holds the artifacts for which the reverse links will be created
	 * @param revLink the reverse link model
	 */
	public static void createRevLinksAndSetOpposites(DSConnection connection, Package pkg, DSRevLink revLink) {
		Collection<Artifact> artifacts = pkg.getArtifacts();
		if(!artifacts.isEmpty()) {
			createRevLinks(connection, artifacts, revLink);
			setOppositeProperties(connection, artifacts);
		}
		revLink.addRevLinkPackage(pkg);
	}
	
	private static void createRevLinks(DSConnection connection, Collection<Artifact> artifacts, DSRevLink revLink) {
		artifacts.forEach(artifact -> createRevLinksForArtifact(connection, artifact, revLink));
	}
	
	private static void setOppositeProperties(DSConnection connection, Collection<Artifact> artifacts) {
		DSRevLink revLinkType = connection.getOrCreateReverseLinkClass();
		// Just get the first package; as rev links are created for a single package at a time, this doesn't matter
		Package parent = artifacts.iterator().next().getPackage();
		Package pkg = getReverseLinkPackage(connection, parent);
		Collection<Artifact> revLinks = connection.getArtifactsOfType(revLinkType, pkg);
		artifacts.forEach(artifact -> setOppositePropertyForArtifact(artifact, connection, revLinks));
	}
	
	private static void createRevLinksForArtifact(DSConnection connection, Artifact artifact, DSRevLink revLink) {
		DSClass sourceType = new DSClass(connection, artifact.getType(), artifact.getPackage());
		Map<String, Object> props = artifact.getAlivePropertiesMap();
		Multimap<Artifact, String> revLinkRelationNames = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);
		for(String key : props.keySet()) {
			Object val = props.get(key);
			if(val instanceof Artifact) {
				revLinkRelationNames.put((Artifact) val, key);
			}
		}
		for(Map.Entry<Artifact, Collection<String>> entry : revLinkRelationNames.asMap().entrySet()) {
			Artifact target = entry.getKey();
			Package targetPkg = target.getPackage();
			if(targetPkg == null) {
				// Target artifact doesn't have a package. Skip reverse link creation!
				continue;
			}
			DSClass targetType = new DSClass(connection, target.getType(), targetPkg);
			Package rlPkg = getReverseLinkPackage(connection, targetPkg);
			revLink.createRevLink(sourceType, targetType, new DSInstance(connection, artifact), new DSInstance(connection, target), rlPkg,
					entry.getValue().toArray(new String[entry.getValue().size()]));
			System.out.println("Created RLink: " + artifact.getId() + " -> " + target.getId() + " [" + entry.getValue().stream().collect(Collectors.joining(",")) + "]");
		}
	}
	
	private static void setOppositePropertyForArtifact(Artifact artifact, DSConnection connection, Collection<Artifact> revLinks) {
		Set<Artifact> linkedArtifacts = new LinkedHashSet<>();
		for(Artifact revLink : revLinks) {
			Object sourceVal = revLink.getPropertyValueOrNull(DSRevLink.SOURCE_NAME);
			if(sourceVal instanceof Artifact) {
				if(((Artifact) sourceVal).getId() == artifact.getId()) {
					// Found a valid reverse link
					Object target = revLink.getPropertyValueOrNull(DSRevLink.TARGET_NAME);
					if(target instanceof Artifact) {
						linkedArtifacts.add((Artifact)target);
					} else {
						System.err.println("Reverse link with invalid target artifact found! Please review reverse link " + revLink.getId());
					}
				}
			}
		}
		
		Artifact oppositeCollection = connection.createCollectionArtifact(artifact.getId() + ".opposites", linkedArtifacts, artifact.getPackage());
		connection.setPropertyValue(artifact, OPPOSITE_PROPERTY_KEY, oppositeCollection);
		System.out.println("Set Opposite for " + artifact.getId() + " referencing " + linkedArtifacts.size() + " artifacts");
	}
	
	/**
	 * Returns or creates (if the package doesn't exist) the corresponding reverse link package of a given package.
	 * @param conn the Design Space connection
	 * @param original the given package
	 * @return the corresponding reverse link package of a given package 
	 */
	public static Package getReverseLinkPackage(DSConnection conn, Package original) {
		return conn.getOrCreatePackage(getReverseLinkPackageName(original), original.getPackage());
	}
	
	/**
	 * Returns the corresponding reverse link package name of a given package.
	 * @param original the given package
	 * @return the corresponding name of the reverse link package of a given package
	 */
	public static String getReverseLinkPackageName(Package original) {
		return RL_PREFIX + original.getId() + "_" + original.getPropertyValue("name");
	}
}
