package at.jku.isse.cloud.revlinks.visualize;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import at.jku.isse.cloud.artifact.DSClass;
import at.jku.isse.cloud.artifact.DSConnection;
import at.jku.isse.cloud.artifact.DSRevLink;
import at.jku.isse.cloud.revlinks.RevLink;
import at.jku.isse.cloud.revlinks.RevLinkCreation;
import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.Package;
import at.jku.sea.cloud.mmm.MMMTypeProperties;
import at.jku.sea.cloud.navigator.NavigatorProvider;
import at.jku.sea.cloud.rest.client.RestCloud;

/**
 * This class contains the functionality to retrieve links and reverse links for a given source artifact. 
 * The reverse links of a specific package can be retrieved and grouped by the type of the source artifacts of the reverse links.
 * @author Gabriel Schoerghuber
 * @author Dominik Steinbinder
 */
public class LinkQuery {
	
	private final DSConnection conn;
	private final DSClass revLinkModel;
	private final NavigatorProvider navigatorProvider;

	/**
	 * Creates a LinkQuery object for the given Design Space connection.
	 * @param conn the Design Space connection
	 */
	public LinkQuery(DSConnection conn) {
		this.conn = requireNonNull(conn);
		this.revLinkModel = conn.getOrCreateReverseLinkClass();
		navigatorProvider = RestCloud.getInstance().queryFactory().navigatorProvider();
	}
	
	/**
	 * Returns the result of {@link #getArtifactName(Artifact)} for the artifact that has the given id. If there is no artifact with this specific id,
	 * then the String "Artifact does not exist" is returned.
	 * @param id the id of the artifact
	 * @return if an artifact with the given id exists, then the result of {@link #getArtifactName(Artifact)} is returned, 
	 * otherwise "Artifact does not exist" is returned.
	 */
	public String getName(long id) {
		Optional<Artifact> artifact = conn.getArtifactById(id);
		return artifact.map(this::getArtifactName).orElse("Artifact does not exist");
	}
	
	/**
	 * Returns the name of the artifact. This is basically the value of the property 
	 * {@link at.jku.sea.cloud.mmm.MMMTypeProperties#NAME} or the property "name". If the artifact doesn't have
	 * either of the two properties, then the String "&ltUnknown&gt" is returned.
	 * @param artifact 
	 * @return the name of the artifact, or "&ltUnknown&gt", if it doesn't have a name property
	 */
	public String getArtifactName(Artifact artifact) {
		Object name = artifact.getPropertyValueOrNull(MMMTypeProperties.NAME);
		if(name == null) {
			name = artifact.getPropertyValueOrNull("name");
			if(name == null) {
				name = "<Unknown>";
			}
		}
		return name.toString();
	}
	
	/**
	 * Returns the links, where the given artifact is the source of the links.
	 * @param id the id of the source artifact of the links
	 * @return a list of links
	 */
	public List<Map.Entry<String, Object>> visualizeLinks(long id) {
		Optional<Artifact> artifact = conn.getArtifactById(id);
		return artifact.map(this::visualizeLinks).orElse(Collections.emptyList());
	}
	
	/**
	 * Returns the reverse links, where the given artifact is the source of the reverse links.
	 * @param id the id of the source artifact of the reverse links
	 * @return a list of reverse links
	 */
	public List<RevLink> visualizeRevLinks(long id) {
		Optional<Artifact> artifact = conn.getArtifactById(id);
		return artifact.map(this::visualizeRevLinks).orElse(Collections.emptyList());
	}
	
	private List<Map.Entry<String, Object>> visualizeLinks(Artifact artifact) {
		return artifact.getAlivePropertiesMap().entrySet().stream()
				.filter(e -> e.getValue() instanceof Artifact)
				.collect(Collectors.toList());
	}
	
	private List<RevLink> visualizeRevLinks(Artifact artifact) {		
		Artifact sourceType = artifact.getType();
		Package rlPkg = RevLinkCreation.getReverseLinkPackage(conn, artifact.getPackage());
		Collection<Artifact> revLinks = conn.getArtifactsOfType(revLinkModel, rlPkg);
		return revLinks.stream()
				.filter(revLink -> getSourceTypeIdOrZero(revLink) == sourceType.getId())
				.filter(revLink -> getSourceIdOrZero(revLink) == artifact.getId())
				.map(revLink -> new RevLink(revLink.getId(), getSource(revLink), getTarget(revLink), getSourceType(revLink), getTargetType(revLink), getRelNames(revLink)))
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns the reverse links of the artifacts of a given package. The reverse links are grouped by the type of the source artifacts.
	 * @param pkg the given package
	 * @return the reverse links, grouped by the type of the source artifacts
	 */
	public Map<Artifact, List<RevLink>> getRevLinks(Package pkg) {	
		Package rlPkg = RevLinkCreation.getReverseLinkPackage(conn, pkg);
		Collection<Artifact> rlArtifacts = conn.getArtifactsOfType(revLinkModel, rlPkg);
		return rlArtifacts.stream()
				.map(revLink -> new RevLink(revLink.getId(), getSource(revLink), getTarget(revLink), getSourceType(revLink), getTargetType(revLink), getRelNames(revLink)))
				.collect(Collectors.groupingBy(RevLink::getSourceType));

	}
	
	private long getSourceIdOrZero(Artifact revLink) {
		try {
			return getSource(revLink).getId();
		} catch(IllegalArgumentException e) {
			return 0;
		}
	}
	
	private long getSourceTypeIdOrZero(Artifact revLink) {
		try {
			return getSourceType(revLink).getId();
		} catch(IllegalArgumentException e) {
			return 0;
		}
	}
	
	private Artifact getSource(Artifact revLink) {
		return getArtifactByProperty(revLink, DSRevLink.SOURCE_NAME);
	}
	
	private Artifact getSourceType(Artifact revLink) {
		return getArtifactByProperty(revLink, DSRevLink.SOURCE_TYPE_NAME);
	}
	
	private Artifact getTarget(Artifact revLink) {
		return getArtifactByProperty(revLink, DSRevLink.TARGET_NAME);
	}
	
	private Artifact getTargetType(Artifact revLink) {
		return getArtifactByProperty(revLink, DSRevLink.TARGET_TYPE_NAME);
	}
	
	private String[] getRelNames(Artifact revLink) {
		 Collection<String> relNames = (Collection<String>) navigatorProvider
				 .from(revLink).toCollection(DSRevLink.REL_NAMES_NAME).get().getElements();
		 return relNames.toArray(new String[relNames.size()]);
	}
	
	private Artifact getArtifactByProperty(Artifact artifact, String property) {
		return navigatorProvider.from(artifact).to(property).get();
	}
}
