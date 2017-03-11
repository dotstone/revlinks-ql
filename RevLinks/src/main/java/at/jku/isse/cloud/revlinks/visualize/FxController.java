package at.jku.isse.cloud.revlinks.visualize;

import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import at.jku.isse.cloud.artifact.DSConnection;
import at.jku.isse.cloud.artifact.DSRevLink;
import at.jku.isse.cloud.revlinks.RevLink;
import at.jku.isse.cloud.revlinks.RevLinkCreation;
import at.jku.sea.cloud.Artifact;
import at.jku.sea.cloud.Package;
import at.jku.sea.cloud.Property;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
	
	private static final Pattern LINK_PATTERN = Pattern.compile("[^\\n\\r]*\\((\\d*)\\) --> [^\\n\\r]*\\((\\d*)\\)");

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
	public void packageSearchFieldChanges(KeyEvent keyEvent) {
		if(KeyCode.ENTER.equals(keyEvent.getCode())) {
			fillPackagesList();
		}
	}

	/**
	 * Called when an element in the package ListView gets selected
	 */
	public void packageSelectionChanged() {		
		this.artifactPane.setDisable(true);
		this.incomingRows.clear();
		this.outgoingRows.clear();
		
		Package currentlySelected = getCurrentlySelectedPackage();
		if(currentlySelected == null) {
			this.createLinksButton.setDisable(true);
			this.linkPane.setDisable(true);
			return;
		}
		
		if(reverseLinksExist()) {
			this.createLinksButton.setDisable(true);
			enableLinkPane();
		} else if (linkQuery.getArtifactName(getCurrentlySelectedPackage()).startsWith(RevLinkCreation.RL_PREFIX)) { 
			// selected package is a reverse link package
			this.createLinksButton.setDisable(true);
		} else {
			this.createLinksButton.setDisable(false);
			this.linkPane.setDisable(true);
		}
	}
	
	private boolean reverseLinksExist() {
		Package selectedPkg = getCurrentlySelectedPackage();
		if(selectedPkg == null) {
			return false;
		}
		
		String revLinkPkgName = RevLinkCreation.getReverseLinkPackageName(selectedPkg);
		return this.packages.stream()
				.anyMatch(p -> linkQuery.getArtifactName(p)
				.equals(revLinkPkgName));
	}
	
	/**
	 * Called when either the reverse links have been created or if a package was selected that had its links already created.
	 */
	private void enableLinkPane() {		
		this.linkTypeButton.getItems().clear();
		this.linkTypeButton.setText("show all");
		
		// dropdown menu entry for displaying all rev links
		MenuItem itemAll = new MenuItem("show all");
		itemAll.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
            	fillLinkListNoFilter();
            	linkTypeButton.setText(itemAll.getText());
            }
        });
		this.linkTypeButton.getItems().add(itemAll);
		
		rlArtifacts = linkQuery.getRevLinks(getCurrentlySelectedPackage());
		for(Entry<Artifact, List<RevLink>> rlArtifactGroup: rlArtifacts.entrySet()) {
	
			String sourceModelName = linkQuery.getName(rlArtifactGroup.getKey().getId());
			
			for(RevLink rl : rlArtifactGroup.getValue()) {
				for(String relation : rl.getRelNames()) {
					String itemText = relation + ": " + sourceModelName + " (" + rlArtifactGroup.getKey().getId() + ")" + 
							" --> " + linkQuery.getName(rl.getTargetModel().getId()) + " (" + rl.getTargetModel().getId() + ")";
					if(this.linkTypeButton.getItems().stream().filter(item -> item.getText().equals(itemText)).findAny().isPresent()) {
						// This link is already in the list
						continue;
					}
					MenuItem item = new MenuItem(itemText);
					item.setMnemonicParsing(false);
					item.setOnAction(new EventHandler<ActionEvent>() {
						public void handle(ActionEvent t) {
							fillLinkList(item.getText());
							linkTypeButton.setText(item.getText());
						}
					});
					this.linkTypeButton.getItems().add(item);
				}
			}
		}
		
		fillLinkListNoFilter();
		this.linkPane.setDisable(false);
	}
	
	private void fillLinkList() {
		if(this.linkTypeButton.getText().equals("show all")) {
			fillLinkListNoFilter();
		} else {
			fillLinkList(this.linkTypeButton.getText());
		}
	}
	
	private void fillLinkListNoFilter() {
		fillLinkList(s -> true, rl -> true);
	}
	
	private void fillLinkList(String linkType) {
		Matcher m = LINK_PATTERN.matcher(linkType);
		m.matches();
		long sourceModelId = Long.parseLong(m.group(1));
		long targetModelId = Long.parseLong(m.group(2));
		fillLinkList(source -> source.getId() == sourceModelId, rl -> rl.getTargetModel().getId() == targetModelId);
 	}
	
	private void fillLinkList(Predicate<Artifact> sourcePredicate, Predicate<RevLink> linkPredicate) {
		this.linkView.getItems().clear();
		
		for(Entry<Artifact, List<RevLink>> rlGroup : rlArtifacts.entrySet()) {
			String sourceModelName = linkQuery.getName(rlGroup.getKey().getId());
			if(sourcePredicate.test(rlGroup.getKey())) {
				rlGroup.getValue().stream()
						.filter(linkPredicate)
						.forEach(rl -> addRevLinkToLinkView(rl, sourceModelName, linkQuery.getName(rl.getTargetModel().getId())));
			}
			
		}
	}
	
	private void addRevLinkToLinkView(RevLink rl, String sourceModelName, String targetModelName) {
		if(!matchesSearchText(rl)) {
			return;
		}
		this.linkView.getItems().add(sourceModelName + ": " + 
				linkQuery.getArtifactName(rl.getSource()) + 
				" ("+rl.getSource().getId() + ") --> " +
				targetModelName + ": " +
				linkQuery.getArtifactName(rl.getTarget()) +
				" ("+rl.getTarget().getId() + ")");
	}
	
	private boolean matchesSearchText(RevLink rl) {
		return hasAttributeMatching(rl.getSource(), this.linkSearchField.getText()) || 
				hasAttributeMatching(rl.getTarget(), this.linkSearchField.getText());
	}

	private boolean hasAttributeMatching(Artifact target, String text) {
		if(text == null || text.equals("")) {
			return true;
		}
		text = text.toLowerCase();
		boolean isNumber = text.matches("\\d+");
		if(isNumber && target.getId() == Integer.parseInt(text)) {
			return true;
		}
		for(Property property : target.getAliveProperties()) {
			Object value = property.getValue();
			if(value instanceof String) {
				if(((String)value).toLowerCase().contains(text)) {
					return true;
				}
			}
			if(isNumber) {
				if(value instanceof Number) {
					if(text.equals(String.valueOf(value))) {
						return true;
					}
				}
			}
		}
		return false;
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
		
		Collection<Artifact> artifacts = selectedPkg.getArtifacts(); 
		RevLinkCreation.createRevLinks(this.connection, artifacts, this.revLink);
		RevLinkCreation.setOppositeProperties(connection, artifacts);
		connection.tryCommit("");
		
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
	
	/**
	 * Called when an element in the links ListView gets selected
	 */
	public void linkSelectionChanged() {
		this.artifactPane.setDisable(false);
		if(this.radioSource.isSelected()) {
			fillSourceLinks();
		} else {
			fillTargetLinks();
		}
	}
	
	/**
	 * Called when pane is enabled or when the search field text in the links pane changes.
	 */
	public void searchTextChanged(KeyEvent keyEvent) {
		if(KeyCode.ENTER.equals(keyEvent.getCode())) {
			fillLinkList();
		}
	}
	
	/**
	 * Called when radio button "Source" was clicked
	 */
	public void fillSourceLinks() {
		if(this.linkView.getSelectionModel().getSelectedItem() != null) {
			long sourceId = Long.parseLong(getLinkMatcher().group(1));
			fillLinks(sourceId);
		}
	}
	
	/**
	 * Called when radio button "Target" was clicked
	 */
	public void fillTargetLinks() {
		if(this.linkView.getSelectionModel().getSelectedItem() != null) {
			long targetId = Long.parseLong(getLinkMatcher().group(2));
			fillLinks(targetId);
		}
	}
	
	private Matcher getLinkMatcher() {
		Matcher m = LINK_PATTERN.matcher(this.linkView.getSelectionModel().getSelectedItem());
		m.matches();
		return m;
	}
	
	private void fillLinks(long id) {
		outgoingRows.clear();
		incomingRows.clear();
		
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
