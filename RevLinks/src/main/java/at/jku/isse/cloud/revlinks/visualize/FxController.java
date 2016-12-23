package at.jku.isse.cloud.revlinks.visualize;

import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import at.jku.isse.cloud.revlinks.RevLink;
import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.mmm.MMMTypeProperties;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class FxController implements Initializable {
	
	@FXML private Button searchButton;
	@FXML private TextField idField;
	
	@FXML private TableView<LinkRow> outgoingView;
	@FXML private TableView<LinkRow> incomingView;
	
	@FXML private TableColumn<LinkRow, String> sourceCol;
	@FXML private TableColumn<LinkRow, String> targetCol;
	@FXML private TableColumn<LinkRow, String> typeCol;
	
	@FXML private TableColumn<LinkRow, String> incSourceCol;
	@FXML private TableColumn<LinkRow, String> incTargetCol;
	@FXML private TableColumn<LinkRow, String> incTypeCol;
	
	@FXML private Label labelName;
	
	private LinkQuery linkVisualize;
	
	private ObservableList<LinkRow> outgoingRows;
	private ObservableList<LinkRow> incomingRows;

	@Override
	public void initialize(URL url, ResourceBundle bundle) {
		assert searchButton != null : "fx:id=\"searchButton\" was not injected!";
		
		outgoingRows = FXCollections.observableArrayList();
		incomingRows = FXCollections.observableArrayList();
		
		idField.setOnAction(this::search);
		searchButton.setOnAction(this::search);
		
		sourceCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getSource()));
		targetCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getTarget()));
		typeCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getType()));
		
		incSourceCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getSource()));
		incTargetCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getTarget()));
		incTypeCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getType()));
        
        outgoingView.setItems(outgoingRows);
		incomingView.setItems(incomingRows);
		
		Platform.runLater(() -> idField.requestFocus());
	}
	
	public void setLinkVisualize(LinkQuery linkVisualize) {
		this.linkVisualize = linkVisualize;
	}
	
	private void search(ActionEvent event) {
		search();
	}
	
	private void search() {
		outgoingRows.clear();
		incomingRows.clear();
		
		long id;
		try {
			id = Integer.parseInt(idField.getText());
		} catch(NumberFormatException e) {
			System.err.println("Not a number: " + idField.getText());
			return;
		}
		
		this.labelName.setText(linkVisualize.getName(id));
		
		List<Entry<String, Object>> links = linkVisualize.visualizeLinks(id);
		for(Entry<String, Object> link : links) {
			outgoingRows.add(new LinkRow("this (id=" + id + ")", getPropertyName((Artifact)link.getValue()), link.getKey()));
		}
		
		List<RevLink> revLinks = linkVisualize.visualizeRevLinks(id);
		for(RevLink link : revLinks) {
			for(String relName : link.getRelNames()) {
				incomingRows.add(new LinkRow(getPropertyName(link.getTarget()) + " - " + getPropertyName(link.getTargetModel()), "this (id=" + id + ")", relName));
			}
		}
	}

	private String getPropertyName(Artifact link) {
		Object name = link.getPropertyValueOrNull("name");
		if(name == null) {
			name = link.getPropertyValueOrNull(MMMTypeProperties.NAME);
			if(name == null) {
				name = "<Unknown>";
			}
		}
		return name.toString() + " (id=" + link.getId() + ")";
	}
}
