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
import at.jku.sea.cloud.CollectionArtifact;
import at.jku.sea.cloud.Package;
import at.jku.sea.cloud.Project;
import at.jku.sea.cloud.mmm.MMMTypeProperties;
import at.jku.sea.cloud.rest.client.RestCloud;

public class LinkQuery {
	
	private final DSConnection conn;
	private final DSClass revLinkModel;

	public LinkQuery(DSConnection conn, Project project) {
		this.conn = requireNonNull(conn);
		this.revLinkModel = conn.getOrCreateReverseLinkClass();
	}
	
	public String getName(long id) {
		Optional<Artifact> artifact = conn.getArtifactById(id);
		return artifact.map(this::getArtifactName).orElse("Artifact does not exist");
	}
	
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
	
	public List<Map.Entry<String, Object>> visualizeLinks(long id) {
		Optional<Artifact> artifact = conn.getArtifactById(id);
		return artifact.map(this::visualizeLinks).orElse(Collections.emptyList());
	}
	
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
		Object val = revLink.getPropertyValueOrNull(DSRevLink.REL_NAMES_NAME);
		if(val instanceof CollectionArtifact) {
			Collection<String> relNames = (Collection<String>) ((CollectionArtifact)val).getElements();
			return relNames.toArray(new String[relNames.size()]);
		}
		throw new IllegalStateException("RevLink " + revLink.getId() + " does not contain a rel name collection!");
	}
	
	private Artifact getArtifactByProperty(Artifact artifact, String property) {
		return RestCloud.getInstance().queryFactory().navigatorProvider().from(artifact).to(property).get();
	}
}
