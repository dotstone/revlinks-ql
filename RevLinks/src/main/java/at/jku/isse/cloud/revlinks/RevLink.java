package at.jku.isse.cloud.revlinks;

import at.jku.sea.cloud.Artifact;

public class RevLink {

	private final long id;

	private final Artifact source;
	private final Artifact target;
	
	private final Artifact sourceType;
	private final Artifact targetType;
	
	private final String[] relNames;
	
	public RevLink(long id, Artifact source, Artifact target, Artifact sourceType, Artifact targetType, String... relNames) {
		this.id = id;
		this.source = source;
		this.target = target;
		this.sourceType = sourceType;
		this.targetType = targetType;
		this.relNames = relNames;
	}
	
	public long getId() {
		return id;
	}

	public Artifact getSource() {
		return source;
	}

	public Artifact getTarget() {
		return target;
	}

	public Artifact getSourceType() {
		return sourceType;
	}

	public Artifact getTargetType() {
		return targetType;
	}
	
	public String[] getRelNames() {
		return relNames;
	}
}
