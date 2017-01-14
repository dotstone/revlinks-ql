package at.jku.isse.cloud.revlinks;

import at.jku.sea.cloud.Artifact;

public class RevLink {

	private final long id;

	private final Artifact source;
	private final Artifact target;
	
	private final Artifact sourceModel;
	private final Artifact targetModel;
	
	private final String[] relNames;
	
	public RevLink(long id, Artifact source, Artifact target, Artifact sourceModel, Artifact targetModel, String... relNames) {
		this.id = id;
		this.source = source;
		this.target = target;
		this.sourceModel = sourceModel;
		this.targetModel = targetModel;
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

	public Artifact getSourceModel() {
		return sourceModel;
	}

	public Artifact getTargetModel() {
		return targetModel;
	}
	
	public String[] getRelNames() {
		return relNames;
	}
}
