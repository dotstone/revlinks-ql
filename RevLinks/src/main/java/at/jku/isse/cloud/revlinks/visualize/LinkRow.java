package at.jku.isse.cloud.revlinks.visualize;

import javafx.beans.property.SimpleStringProperty;

public class LinkRow {
	
	private final SimpleStringProperty source;
	private final SimpleStringProperty target;
	private final SimpleStringProperty type;
	private final SimpleStringProperty link;
	
	public LinkRow(String source, String target, String type, String link) {
		this.source = new SimpleStringProperty(source);
		this.target = new SimpleStringProperty(target);
		this.type = new SimpleStringProperty(type);
		this.link = new SimpleStringProperty(link);
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
	
	public String getLink() {
		return link.get();
	}
	
	public void setLink(String link) {
		this.link.set(link);
	}
}