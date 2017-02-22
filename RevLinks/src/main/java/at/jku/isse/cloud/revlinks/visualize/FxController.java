package at.jku.isse.cloud.revlinks.visualize;

import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import at.jku.isse.cloud.artifact.DSClass;
import at.jku.isse.cloud.artifact.DSConnection;
import at.jku.isse.cloud.artifact.DSInstance;
import at.jku.isse.cloud.artifact.DSRevLink;
import at.jku.isse.cloud.revlinks.RevLink;
import at.jku.isse.cloud.revlinks.RevLinkCreation;
import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.Package;
import at.jku.sea.cloud.exceptions.WorkspaceEmptyException;
import at.jku.sea.cloud.mmm.MMMTypeProperties;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class FxController implements Initializable {
	
	@FXML private AnchorPane packagePane;
	@FXML private AnchorPane linkPane;
	@FXML private AnchorPane artifactPane;
	
	@FXML private Button createLinksButton;
	@FXML private ListView<String> packagesView;
	@FXML private TextField pkgSearchField;
		
	@FXML private TextField linkSearchField;
	@FXML private MenuButton linkTypeButton;
	@FXML private ListView<String> linkView;
	
	@FXML private RadioButton radioSource;
	@FXML private RadioButton radioTarget;
	
	@FXML private TableView<LinkRow> outgoingView;
	@FXML private TableView<LinkRow> incomingView;
	
	@FXML private TableColumn<LinkRow, String> sourceCol;
	@FXML private TableColumn<LinkRow, String> targetCol;
	@FXML private TableColumn<LinkRow, String> typeCol;
	
	@FXML private TableColumn<LinkRow, String> incSourceCol;
	@FXML private TableColumn<LinkRow, String> incTargetCol;
	@FXML private TableColumn<LinkRow, String> incTypeCol;
	@FXML private TableColumn<LinkRow, String> incLinkCol;
	
	private DSConnection connection;
	private DSRevLink revLink;
	private LinkQuery linkQuery;
	
	private Collection<Package> packages;
	private Map<Artifact, List<RevLink>> rlArtifacts;
	
	private ObservableList<LinkRow> outgoingRows;
	private ObservableList<LinkRow> incomingRows;

	@Override
	public void initialize(URL url, ResourceBundle bundle) {
		assert createLinksButton != null : "fx:id=\"createLinksButton\" was not injected!";
		
		outgoingRows = FXCollections.observableArrayList();
		incomingRows = FXCollections.observableArrayList();
		
		sourceCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getSource()));
		targetCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getTarget()));
		typeCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getType()));
		
		incSourceCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getSource()));
		incTargetCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getTarget()));
		incTypeCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getType()));
		incLinkCol.setCellValueFactory(linkRow -> new SimpleStringProperty(linkRow.getValue().getLink()));
        
        outgoingView.setItems(outgoingRows);
		incomingView.setItems(incomingRows);
		
		Platform.runLater(() -> linkSearchField.requestFocus());
	}
	
	/**
	 * Called after initialize() but before rendering the view
	 * @param linkVisualize
	 * @param conn
	 */
	public void initInterface(LinkQuery linkVisualize, DSConnection conn) {
		this.linkQuery = requireNonNull(linkVisualize);
		this.connection = requireNonNull(conn);
		this.revLink = connection.getOrCreateReverseLinkClass();
		
		this.packages = connection.getPackages();
		
		fillPackagesList();
		this.linkPane.setDisable(true);
		this.artifactPane.setDisable(true);
		this.radioSource.setSelected(true);
		this.createLinksButton.setDisable(true);
		this.linkTypeButton.setText("show all");
	}
	
	private void fillPackagesList() {
		this.packagesView.getItems().clear();
		this.packagesView.getItems().addAll(packages.stream()
				.map(this::getPropertyName)
				.filter(name -> name.toLowerCase().contains(this.pkgSearchField.getText().toLowerCase()))
				.collect(Collectors.toList()));
	}
	
	/**
	 * Called when the input text of the search field in the package pane changes.
	 */
	public void packageSearchFieldChanges() {
		fillPackagesList();
	}

	/**
	 * Called when an element in the package ListView gets selected
	 */
	public void packageSelectionChanged() {		
		if(reverseLinksExist()) {
			this.createLinksButton.setDisable(true);
			enableLinkPane();
		} else if (linkQuery.getArtifactName(getCurrentlySelectedPackage()).endsWith(RevLinkCreation.RL_EXTENSION)) { 
			// selected package is a reverse link package
			this.createLinksButton.setDisable(true);
		} else {
			this.createLinksButton.setDisable(false);
			this.linkPane.setDisable(true);
			this.artifactPane.setDisable(true);
		}
	}
	
	private boolean reverseLinksExist() {
		Package selectedPkg = getCurrentlySelectedPackage();
		if(selectedPkg == null) {
			return false;
		}
		
		return this.packages.stream()
				.anyMatch(p -> linkQuery.getArtifactName(p).equals(linkQuery.getArtifactName(selectedPkg) + RevLinkCreation.RL_EXTENSION));
	}
	
	/**
	 * Called when either the reverse links have been created or if a package was selected that had its links already created.
	 */
	private void enableLinkPane() {		
		this.linkTypeButton.getItems().clear();
		
		// dropdown menu entry for displaying all rev links
		MenuItem itemAll = new MenuItem("show all");
		itemAll.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                fillLinkList();
                linkTypeButton.setText(itemAll.getText());
            }
        });
		this.linkTypeButton.getItems().add(itemAll);
		
		rlArtifacts = linkQuery.getRevLinks(getCurrentlySelectedPackage());
		for(Entry<Artifact, List<RevLink>> rlArtifactGroup: rlArtifacts.entrySet()) {
	
			String sourceModelName = linkQuery.getName(rlArtifactGroup.getKey().getId());
			List<Long> rlTargetModelIds = rlArtifactGroup.getValue().stream()
													.map(a -> a.getTargetModel().getId())
													.distinct()
													.collect(Collectors.toList());
			
			for(Long id : rlTargetModelIds) {
				MenuItem item = new MenuItem(sourceModelName + " (" + rlArtifactGroup.getKey().getId() + ")" + 
															" --> " + linkQuery.getName(id) + " (" + id + ")");
				item.setOnAction(new EventHandler<ActionEvent>() {
		            public void handle(ActionEvent t) {
		                linkTypeSelectionChanged(item.getText());
		                linkTypeButton.setText(item.getText());
		            }
		        });
				this.linkTypeButton.getItems().add(item);
			}
		}
		
		fillLinkList();
		this.linkPane.setDisable(false);
	}
	
	private void fillLinkList() {				
		this.linkView.getItems().clear();
		
		for(Entry<Artifact, List<RevLink>> rlGroup : rlArtifacts.entrySet()) {
			String sourceModelName = linkQuery.getName(rlGroup.getKey().getId());
			
			rlGroup.getValue().stream().forEach(rl -> addRevLinkToLinkView(rl, sourceModelName, linkQuery.getName(rl.getTargetModel().getId())));
		}
 	}
	
	private void fillLinkList(long sourceModelId, long targetModelId) {
		this.linkView.getItems().clear();
		
		Optional<Artifact> sourceModel = rlArtifacts.keySet().stream().
											filter(rl -> rl.getId() == sourceModelId).findFirst();
		if(sourceModel.equals(Optional.empty())) {
			return;
		}
		
		List<RevLink> revLinks = rlArtifacts.get(sourceModel.get()).stream()
											.filter(rl -> rl.getTargetModel().getId() == targetModelId)
											.collect(Collectors.toList());
		
		String sourceModelName = linkQuery.getName(sourceModelId);
		String targetModelName = linkQuery.getName(targetModelId);
		
		revLinks.stream().forEach(rl -> addRevLinkToLinkView(rl, sourceModelName, targetModelName));
	}
	
	private void addRevLinkToLinkView(RevLink rl, String sourceModelName, String targetModelName) {
		this.linkView.getItems().add(sourceModelName + ": " + 
				linkQuery.getArtifactName(rl.getSource()) + 
				" ("+rl.getSource().getId() + ") --> " +
				targetModelName + ": " +
				linkQuery.getArtifactName(rl.getTarget()) +
				" ("+rl.getTarget().getId() + ")");
	}
	
	/**
	 * Called when the according button was clicked.
	 */
	public void createRevLinks() {
		this.createLinksButton.setDisable(true);

		Package selectedPkg = getCurrentlySelectedPackage();
		if(selectedPkg == null) {
			return;
		}
		
		selectedPkg.getArtifacts().stream().
				forEach(a -> RevLinkCreation.createRevLinksForArtifact(a, this.connection, this.revLink));
		
		try {
			connection.commit("");
		} catch (WorkspaceEmptyException e) {
			System.err.println("There is nothing to commit because no reverse links were created.");
		}
		
		packages = connection.getPackages();
		fillPackagesList();
		this.packagesView.getSelectionModel().select(getPropertyName(selectedPkg));
		
		enableLinkPane();
		this.linkSearchField.requestFocus();
	}
	
	/**
	 * Called when the selection menu button for the link type changes
	 */
	public void linkTypeSelectionChanged() {
		fillLinkList();
	}
	
	private void linkTypeSelectionChanged(String linkType) {
		Pattern p = Pattern.compile("[\\D]*\\((\\d*)\\) --> [\\D]*\\((\\d*)\\)");
		Matcher m = p.matcher(linkType);
		m.matches();
		long sourceModelId = Long.parseLong(m.group(1));
		long targetModelId = Long.parseLong(m.group(2));
		fillLinkList(sourceModelId, targetModelId);
	}
	
	/**
	 * Called when an element in the links ListView gets selected
	 */
	public void linkSelectionChanged() {
		this.artifactPane.setDisable(false);
	}
	
	/**
	 * Called when pane is enabled or when the search field text in the links pane changes.
	 */
	public void fillLinksList() {
		Package pkg = getCurrentlySelectedPackage();
		if(pkg == null) {
			return;
		}
		// TODO retrieve all links and reverse links in the selected package and fill out links list.
		this.linkView.getItems().add("Sample Link");
	}
	
	/**
	 * Called when radio button "Source" was clicked
	 */
	public void fillSourceLinks() {
		// TODO (use/adapt fillLinks())
	}
	
	/**
	 * Called when radio button "Target" was clicked
	 */
	public void fillTargetLinks() {
		// TODO (use/adapt fillLinks())
	}
	
	private void fillLinks() {
		outgoingRows.clear();
		incomingRows.clear();
		
		long id;
		try {
			id = Integer.parseInt(linkSearchField.getText());
		} catch(NumberFormatException e) {
			System.err.println("Not a number: " + linkSearchField.getText());
			return;
		}
		
		List<Entry<String, Object>> links = linkQuery.visualizeLinks(id);
		for(Entry<String, Object> link : links) {
			outgoingRows.add(new LinkRow("this (id=" + id + ")", getPropertyName((Artifact)link.getValue()), link.getKey(), ""));
		}
		
		List<RevLink> revLinks = linkQuery.visualizeRevLinks(id);
		for(RevLink link : revLinks) {
			for(String relName : link.getRelNames()) {
				incomingRows.add(new LinkRow(getPropertyName(link.getTarget()) + " - " + getPropertyName(link.getTargetModel()), "this (id=" + id + ")", relName, "id=" + link.getId()));
			}
		}
	}
	
	private Package getCurrentlySelectedPackage() {
		this.linkView.getItems().clear();
		String pkgName = this.packagesView.getSelectionModel().getSelectedItem();
		if(pkgName == null || pkgName.equals("")) {
			return null;
		}
		return this.packages.stream()
				.filter(p -> pkgName.equals(getPropertyName(p)))
				.findAny()
				.orElseThrow(() -> new IllegalStateException("Selected a package that doesn't exist!"));
	}

	private String getPropertyName(Artifact link) {		
		return linkQuery.getArtifactName(link) + " (" + link.getId() + ")";
	}
	
}
