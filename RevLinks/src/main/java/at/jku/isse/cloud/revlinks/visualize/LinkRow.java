package at.jku.isse.cloud.revlinks.visualize;

import javafx.beans.property.SimpleStringProperty;

public class LinkRow {
	
	private final SimpleStringProperty source;
	private final SimpleStringProperty target;
	private final SimpleStringProperty type;
	
	public LinkRow(String source, String target, String type) {
		this.source = new SimpleStringProperty(source);
		this.target = new SimpleStringProperty(target);
		this.type = new SimpleStringProperty(type);
	}
	
	public String getSource() {
		return source.get();
	}
	
	public void setSource(String source) {
		this.source.set(source);
	}

	public String getTarget() {
		return target.get();
	}
	
	public void setTarget(String target) {
		this.target.set(target);
	}
	
	public String getType() {
		return type.get();
	}
	
	public void setType(String type) {
		this.type.set(type);
	}
}