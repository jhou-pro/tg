package ua.com.fielden.platform.javafx.dashboard;

import static ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeRepresentation.GREEN;
import static ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeRepresentation.RED;
import static ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeRepresentation.YELLOW;

import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;

/**
 * A swing container to contain javaFx dashboard.
 *
 * @author TG Team
 *
 */
public class DashboardPanel<T extends AbstractEntity<?>> extends JFXPanel {
    private static final long serialVersionUID = 9202827128855362320L;

    private final IGlobalDomainTreeManager globalManager;
    private final ICriteriaGenerator criteriaGenerator;
    private final IEntityMasterManager masterManager;
    private TableView<DashboardRow<T>> table = new TableView<DashboardRow<T>>();
    private ObservableList<DashboardRow<T>> data;
    private final TreeMenuWithTabs<?> treeMenu;

    private final String webLightGrey = "d6d9df";
    private final Color lightGrey = Color.web(webLightGrey);

    /**
     * Creates a swing container with javaFx dashboard.
     *
     * @return
     */
    public DashboardPanel(final IGlobalDomainTreeManager globalManager, final ICriteriaGenerator criteriaGenerator, final IEntityMasterManager masterManager, final TreeMenuWithTabs<?> treeMenu) {
	this.globalManager = globalManager;
	this.criteriaGenerator = criteriaGenerator;
	this.masterManager = masterManager;
	this.treeMenu = treeMenu;

	this.data = createData();
	Platform.runLater(new Runnable() {
	    @Override
	    public void run() {
	        // This method is invoked on the JavaFX thread
	        final Scene scene = createScene();
	        setScene(scene);
	    }
	});
    }

    private ObservableList<DashboardRow<T>> createData() {
	final ObservableList<DashboardRow<T>> data = FXCollections.observableArrayList();
	final List<Class<?>> mmiTypes = globalManager.entityCentreMenuItemTypes();
	for (final Class<?> mmiType : mmiTypes) {
	    final Set<String> centreNames = globalManager.entityCentreNames(mmiType);
	    for (final String centreName : centreNames) {
		if (globalManager.getEntityCentreManager(mmiType, centreName) == null) {
		    globalManager.initEntityCentreManager(mmiType, centreName); // TODO this operation consumes a lot of time / memory during load
		}
		final ICentreDomainTreeManagerAndEnhancer centreManager = globalManager.getEntityCentreManager(mmiType, centreName);
		final List<String> analysisKeys = centreManager.analysisKeys();
		for (final String analysisName : analysisKeys) {
		    final IAbstractAnalysisDomainTreeManager analysis = centreManager.getAnalysisManager(analysisName);
		    if (analysis instanceof ISentinelDomainTreeManager) {
			data.add(new DashboardRow<T>(treeMenu, criteriaGenerator, globalManager, masterManager, mmiType, centreName, analysisName));
		    }
		}
	    }
	}
	return data;
    }

    public class TrafficLightsCell extends TableCell<DashboardRow<T>, Integer> {
	@Override
	protected void updateItem(final Integer arg0, final boolean arg1) {
	    super.updateItem(arg0, arg1);

	    if (getIndex() <= table.getItems().size() - 1) {
		final DashboardRow<T> sentinel = table.getItems().get(getIndex());
		setGraphic(new TrafficLights(sentinel.getModel(),
			new IAction() { @Override public void action() { sentinel.invokeDetails(RED); }}, //
			new IAction() { @Override public void action() { sentinel.invokeDetails(YELLOW); }}, //
			new IAction() { @Override public void action() { sentinel.invokeDetails(GREEN); }} //
			));
	    }
	}
    }

    public class RefreshCell extends TableCell<DashboardRow<T>, Void> {
	@Override
	protected void updateItem(final Void arg0, final boolean arg1) {
	    super.updateItem(arg0, arg1);

	    if (getIndex() <= table.getItems().size() - 1) {
		final DashboardRow<T> sentinel = table.getItems().get(getIndex());
		final Button button = new Button("Refresh");
		button.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(final ActionEvent arg0) {
			sentinel.runAndUpdate();
		        sort();
		    };
		});
		setGraphic(button);
	    }
	}
    }

    private Scene createScene() {
	final BorderPane borderPane = new BorderPane();
	borderPane.setStyle("-fx-base: #" + webLightGrey + "; -fx-background: #" + webLightGrey + ";");
	final Scene scene = new Scene(borderPane, lightGrey);
        table.setEditable(true);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);

        final TableColumn<DashboardRow<T>, String> sentinelCol = new TableColumn<>("Sentinel rule");
        sentinelCol.setMinWidth(100);
        sentinelCol.setCellValueFactory(
                new PropertyValueFactory<DashboardRow<T>, String>("sentinelTitle"));

        final TableColumn<DashboardRow<T>, Integer> resultCol = new TableColumn<>("Status");
        resultCol.setCellFactory(new Callback<TableColumn<DashboardRow<T>, Integer>, TableCell<DashboardRow<T>, Integer>>()  {
            @Override
            public TableCell<DashboardRow<T>, Integer> call(final TableColumn<DashboardRow<T>, Integer> arg0) {
        	return new TrafficLightsCell();
            }
        });
        resultCol.setCellValueFactory(
                new PropertyValueFactory<DashboardRow<T>, Integer>("countOfBad"));
        final double resultColWidth = 97;
        resultCol.setMinWidth(resultColWidth);
        resultCol.setPrefWidth(resultColWidth);
        resultCol.setMaxWidth(resultColWidth);

        final TableColumn<DashboardRow<T>, Void> refreshCol = new TableColumn<>("Action");
        refreshCol.setCellFactory(new Callback<TableColumn<DashboardRow<T>, Void>, TableCell<DashboardRow<T>, Void>>()  {
            @Override
            public TableCell<DashboardRow<T>, Void> call(final TableColumn<DashboardRow<T>, Void> arg0) {
        	return new RefreshCell();
            }
        });
        final double refreshColWidth = 79;
        refreshCol.setMinWidth(refreshColWidth);
        refreshCol.setPrefWidth(refreshColWidth);
        refreshCol.setMaxWidth(refreshColWidth);

        sentinelCol.prefWidthProperty().bind(table.widthProperty().subtract(resultColWidth + refreshColWidth + 2));

        table.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
        	if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
        	    table.getSelectionModel().getSelectedItem().openAnalysis();
        	}
            }
        });
        table.getColumns().addAll(resultCol, sentinelCol, refreshCol);
        table.setItems(data);

        for (final DashboardRow<T> sentinel : data) {
            sentinel.runAndUpdate();
        }
        sort();

        borderPane.setCenter(table);

        final VBox vbButtons = new VBox();
        // vbButtons.setSpacing(10);
        vbButtons.setPadding(new Insets(4, 0, 4, 0));

        final Button refreshAll = new Button("Refresh");
        refreshAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
        	refreshAll();
            }
        });
        vbButtons.getChildren().add(refreshAll);
        borderPane.setBottom(vbButtons);
        refreshAll.translateXProperty().bind(table.widthProperty().subtract(refreshAll.widthProperty().add(5.0)));
        return (scene);
    }

    public void refreshAll() {
	table.getSortOrder().clear();
	table.getItems().clear();
	table.getItems().addAll(createData());
        for (final DashboardRow sentinel : table.getItems()) {
            sentinel.runAndUpdate();
        }
        sort();
    }

    private void sort() {
	table.getSortOrder().clear();
	final int index = 0;
	table.getSortOrder().add(table.getColumns().get(index));
        table.getColumns().get(index).setSortType(TableColumn.SortType.DESCENDING);
        table.getColumns().get(index).setSortable(true); // This performs a sort
    }
}