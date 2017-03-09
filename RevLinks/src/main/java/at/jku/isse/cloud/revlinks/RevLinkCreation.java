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

public class RevLinkCreation {
	
	public static final String RL_PREFIX = "RL_";
	
	private static final String OPPOSITE_PROPERTY_KEY = "@opposite";
	
	private static DSConnection conn;
	private static DSRevLink revLink;

	public static void main(String[] args) {
		conn = new DSConnection("dos", "mepwd", "my workspace");
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
	
	public static void createRevLinks(DSConnection connection, Collection<Artifact> artifacts, DSRevLink revLink) {
		artifacts.forEach(artifact -> createRevLinksForArtifact(connection, artifact, revLink));
	}
	
	public static void setOppositeProperties(DSConnection connection, Collection<Artifact> artifacts) {
		artifacts.forEach(artifact -> setOppositePropertyForArtifact(artifact, connection));
	}
	
	private static void createRevLinksForArtifact(DSConnection connection, Artifact artifact, DSRevLink revLink) {
		DSClass sourceModel = new DSClass(connection, artifact.getType(), artifact.getPackage());
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
			DSClass targetModel = new DSClass(connection, target.getType(), targetPkg);
			Package rlPkg = getReverseLinkPackage(connection, targetPkg);
			revLink.createRevLink(sourceModel, targetModel, new DSInstance(connection, artifact), new DSInstance(connection, target), rlPkg,
					entry.getValue().toArray(new String[entry.getValue().size()]));
			System.out.println("Created RLink: " + artifact.getId() + " -> " + target.getId() + " [" + entry.getValue().stream().collect(Collectors.joining(",")) + "]");
		}
	}
	
	private static void setOppositePropertyForArtifact(Artifact artifact, DSConnection connection) {
		DSRevLink revLinkType = connection.getOrCreateReverseLinkClass();
		Package parent = artifact.getPackage();
		Package pkg = getReverseLinkPackage(connection, parent);
		Collection<Artifact> revLinks = connection.getArtifactsOfType(revLinkType, pkg);
		
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
	
	public static Package getReverseLinkPackage(DSConnection conn, Package original) {
		return conn.getOrCreatePackage(getReverseLinkPackageName(original), original.getPackage());
	}
	
	public static String getReverseLinkPackageName(Package original) {
		return RL_PREFIX + original.getId() + "_" + original.getPropertyValue("name");
	}
}
