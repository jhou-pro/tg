package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotAddToDistributionTickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.swing.categorychart.AnalysisListDragFromSupport;
import ua.com.fielden.platform.swing.categorychart.AnalysisListDragToSupport;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingEvent;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingListener;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxList;
import ua.com.fielden.platform.swing.checkboxlist.SortingCheckboxListCellRenderer;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.menu.filter.IFilter;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.review.details.AnalysisDetailsData;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.configuration.PivotAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.analysis.view.AnalysisDataEvent;
import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTableModel;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTablePanel;
import ua.com.fielden.platform.swing.utils.DummyBuilder;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * {@link AbstractAnalysisReview} implementation for pivot analysis.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public class PivotAnalysisView<T extends AbstractEntity<?>> extends AbstractAnalysisReview<T, ICentreDomainTreeManagerAndEnhancer, IPivotDomainTreeManager> {

    private static final long serialVersionUID = 8295216779213506230L;

    /**
     * Represents the list of available distribution properties.
     */
    private final SortingCheckboxList<String> distributionList;
    /**
     * Represents the list of available aggregation properties.
     */
    private final SortingCheckboxList<String> aggregationList;
    /**
     * Pivot table that displays the analysis result.
     */
    private final FilterableTreeTablePanel<PivotTreeTable> pivotTablePanel;
    /**
     * Tool bar that contain "configure analysis" button.
     */
    private final JToolBar toolBar;

    /**
     * Initialises {@link PivotAnalysisView} with appropriate model and {@link PivotAnalysisConfigurationView} instance.
     * 
     * @param model
     * @param owner
     */
    public PivotAnalysisView(final PivotAnalysisModel<T> model, final PivotAnalysisConfigurationView<T> owner) {
        super(model, owner);

        this.distributionList = createDistributionList();
        this.aggregationList = createAggregationList();
        this.pivotTablePanel = createPivotTreeTablePanel();
        this.toolBar = createPivotToolBar();

        DnDSupport2.installDnDSupport(distributionList, new AnalysisListDragFromSupport(distributionList),//
                new AnalysisListDragToSupport<T>(distributionList, getModel().getCriteria().getEntityClass(), getModel().adtme().getFirstTick()), true);
        DnDSupport2.installDnDSupport(aggregationList, new AnalysisListDragFromSupport(aggregationList), //
                new PivotListDragToSupport<T>(aggregationList, pivotTablePanel.getTreeTable(), getModel()), true);
        this.addSelectionEventListener(createPivotAnalysisSelectionListener());
        layoutComponents();
    }

    @Override
    public PivotAnalysisModel<T> getModel() {
        return (PivotAnalysisModel<T>) super.getModel();
    }

    @Override
    protected void enableRelatedActions(final boolean enable, final boolean navigate) {
        super.enableRelatedActions(enable, navigate);
        getCentre().getExportAction().setEnabled(enable);
    }

    /**
     * Returns the {@link ISelectionEventListener} that enables or disable appropriate actions when this analysis was selected.
     * 
     * @return
     */
    private ISelectionEventListener createPivotAnalysisSelectionListener() {
        return new ISelectionEventListener() {

            @Override
            public void viewWasSelected(final SelectionEvent event) {
                //Managing the default, design and custom action changer button enablements.
                getCentre().getDefaultAction().setEnabled(getCentre().getCriteriaPanel() != null);
                if (getCentre().getCriteriaPanel() != null && getCentre().getCriteriaPanel().canConfigure()) {
                    getCentre().getCriteriaPanel().getSwitchAction().setEnabled(true);
                }
                if (getCentre().getCustomActionChanger() != null) {
                    getCentre().getCustomActionChanger().setEnabled(true);
                }
                //Managing the paginator's enablements.
                getCentre().getPaginator().setEnableActions(false, false);
                //Managing load and export enablements.
                getCentre().getExportAction().setEnabled(true);
                getCentre().getRunAction().setEnabled(true);
            }
        };
    }

    /**
     * Creates the list of distribution properties. That list supports highlighting of the queried properties and drag&drop.
     * 
     * @return
     */
    private SortingCheckboxList<String> createDistributionList() {
        final DefaultListModel<String> listModel = new DefaultListModel<String>();

        final Class<T> root = getModel().getCriteria().getEntityClass();
        final IPivotAddToDistributionTickManager firstTick = getModel().adtme().getFirstTick();

        for (final String distributionProperty : firstTick.checkedProperties(root)) {
            listModel.addElement(distributionProperty);
        }
        final SortingCheckboxList<String> distributionList = new SortingCheckboxList<String>(listModel, 2);
        distributionList.setCellRenderer(new SortingCheckboxListCellRenderer<String>(distributionList) {

            private static final long serialVersionUID = 7712966992046861840L;

            private final EntityDescriptor ed = new EntityDescriptor(getModel().getCriteria().getManagedType(), firstTick.checkedProperties(root));

            @Override
            public Component getListCellRendererComponent(final JList<? extends String> list, final String value, final int index, final boolean isSelected, final boolean cellHasFocus) {

                final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                final Pair<String, String> titleAndDesc = ed.getTitleAndDesc(value.toString());
                defaultRenderer.setText(titleAndDesc.getKey());
                setToolTipText(titleAndDesc.getValue());
                return rendererComponent;
            }

            @Override
            public boolean isSortingAvailable(final String element) {
                return false;
            }
        });

        getModel().getRowDistributionCheckingModel().addListCheckingListener(new ListCheckingListener<String>() {

            @Override
            public void valueChanged(final ListCheckingEvent<String> e) {
                aggregationList.repaint();
            }
        });
        distributionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        distributionList.setCheckingModel(getModel().getRowDistributionCheckingModel(), 0);
        distributionList.setCheckingModel(getModel().getColumnDistributionCheckingModel(), 1);
        distributionList.setSortingModel(null);
        return distributionList;
    }

    /**
     * Creates the list of checked aggregation properties. That list supports Drag&Drop and highlighting of queried properties.
     * 
     * @return
     */
    private SortingCheckboxList<String> createAggregationList() {
        final DefaultListModel<String> listModel = new DefaultListModel<String>();

        final Class<T> root = getModel().getCriteria().getEntityClass();
        final IPivotAddToAggregationTickManager secondTick = getModel().adtme().getSecondTick();

        for (final String aggregationProperty : secondTick.checkedProperties(root)) {
            listModel.addElement(aggregationProperty);
        }
        final SortingCheckboxList<String> aggregationList = new SortingCheckboxList<String>(listModel, 1);
        aggregationList.setCellRenderer(new SortingCheckboxListCellRenderer<String>(aggregationList) {

            private static final long serialVersionUID = -6751336113879821723L;

            private final EntityDescriptor ed = new EntityDescriptor(getModel().getCriteria().getManagedType(), secondTick.checkedProperties(root));

            @Override
            public Component getListCellRendererComponent(final JList<? extends String> list, final String value, final int index, final boolean isSelected, final boolean cellHasFocus) {

                final Component rendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                final Pair<String, String> titleAndDesc = ed.getTitleAndDesc(value);
                defaultRenderer.setText(titleAndDesc.getKey());
                setToolTipText(titleAndDesc.getValue());
                return rendererComponent;
            }

            @Override
            public boolean isSortingAvailable(final String element) {
                return aggregationList.isValueChecked(element, 0) && aggregationList.isSortable(element);
            }
        });
        aggregationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        aggregationList.setCheckingModel(getModel().getAggregationCheckingModel(), 0);
        aggregationList.setSortingModel(getModel().getAggregationSortingModel());

        return aggregationList;
    }

    private FilterableTreeTablePanel<PivotTreeTable> createPivotTreeTablePanel() {
        final PivotTreeTable treeTable = new PivotTreeTable(new FilterableTreeTableModel(getModel().getPivotModel()));
        final FilterableTreeTablePanel<PivotTreeTable> pivotTablePanel = new FilterableTreeTablePanel<PivotTreeTable>(treeTable, createPivotFilter(), "find item");
        getModel().getPivotModel().addPivotDataLoadedListener(new PivotDataLoadedListener() {

            @Override
            public void pivotDataLoaded(final PivotDataLoadedEvent event) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        pivotTablePanel.getFilterControl().refresh();
                    }
                });
            }
        });
        treeTable.addMouseListener(createDoubleClickListener(treeTable));
        refreshPivotTable(treeTable);
        return pivotTablePanel;
    }

    private IFilter createPivotFilter() {
        return new WordFilter() {
            @Override
            public boolean filter(final Object value, final String valuefilterCrit) {
                return super.filter(((PivotTreeTableNode) value).getValueAt(0), valuefilterCrit);
            }
        };
    }

    private MouseListener createDoubleClickListener(final PivotTreeTable treeTable) {
        return new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    final TreePath rowPath = generatePathWithoutRoot(treeTable.getRowPathForLocation(e.getX(), e.getY()));
                    final TreePath columnPath = generatePathWithoutRoot(treeTable.getColumnPathForLocation(e.getX(), e.getY()));
                    final List<Pair<String, Object>> choosenProperty = createChoosenProperty(rowPath, columnPath);
                    performCustomAction(new AnalysisDataEvent<>(PivotAnalysisView.this, choosenProperty));
                }
            }

            /**
             * Returns the tree path without root node.
             * 
             * @param treePath
             * @return
             */
            private TreePath generatePathWithoutRoot(final TreePath treePath) {
                if (treePath.getPathCount() > 1) {
                    TreePath newPath = new TreePath(treePath.getPathComponent(1));
                    for (int index = 2; index < treePath.getPathCount(); index++) {
                        newPath = newPath.pathByAddingChild(treePath.getPathComponent(index));
                    }
                    return newPath;
                }
                return null;
            }

            /**
             * Returns the list of property name - value pairs of the selected value.
             * 
             * @param rowPath
             *            - the path for selected row.
             * @param columnPath
             *            - the path for selected column.
             * @return
             */
            private List<Pair<String, Object>> createChoosenProperty(final TreePath rowPath, final TreePath columnPath) {
                final List<Pair<String, Object>> choosenItems = new ArrayList<>(createChoosenProperty(rowPath, getModel().getPivotModel().rowCategoryProperties()));
                choosenItems.addAll(createChoosenProperty(columnPath, getModel().getPivotModel().columnCategoryProperties()));
                return choosenItems;
            }

            /**
             * Returns the List of selected property name and it's value.
             * 
             * @param path
             *            - path of tree nodes with values.
             * @param distributions
             *            - the list of distribution properties.
             * @return
             */
            private List<Pair<String, Object>> createChoosenProperty(final TreePath path, final List<String> distributions) {
                final List<Pair<String, Object>> choosenItems = new ArrayList<>();
                for (int index = 0; path != null && index < path.getPathCount(); index++) {
                    final DefaultMutableTreeNode node = getUnderlyingNode(path.getPathComponent(index));
                    final String distributionProperty = distributions.get(index);
                    final Object value = PivotTreeTableNode.NULL_USER_OBJECT.equals(node.getUserObject()) ? null : node.getUserObject();
                    choosenItems.add(new Pair<>(distributionProperty, value));
                }
                return choosenItems;
            }

            /**
             * Returns the underlying node for the specified node.
             * 
             * @param node
             * @return
             */
            private DefaultMutableTreeNode getUnderlyingNode(final Object node) {
                if (node instanceof PivotTreeTableNode) {
                    return (DefaultMutableTreeNode) ((PivotTreeTableNode) node).getUserObject();
                } else {
                    return (DefaultMutableTreeNode) node;
                }
            }

        };
    }

    /**
     * Creates the tool bar with "configure analysis" button.
     * 
     * @return
     */
    private JToolBar createPivotToolBar() {
        final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        getConfigureAction().putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/configure.png"));
        getConfigureAction().putValue(Action.SHORT_DESCRIPTION, "Configure analysis");

        toolBar.add(getConfigureAction());
        return toolBar;
    }

    //////////////////////Refactor code below//////////////////////////////////
    private void layoutComponents() {
        removeAll();
        setLayout(new MigLayout("fill, insets 0", "[fill,grow]", "[fill,grow]"));
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        final JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        addLoadListener(new ILoadListener() {

            @Override
            public void viewWasLoaded(final LoadEvent event) {
                leftPane.setDividerLocation(0.5);
            }
        });

        //Configuring controls those allows to choose distribution properties.
        final JPanel leftTopPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,grow]", "[][grow,fill]"));
        final JLabel distributionLabel = DummyBuilder.label("Distribution properties");
        leftTopPanel.add(distributionLabel, "wrap");
        leftTopPanel.add(new JScrollPane(distributionList));

        //Configuring controls those allows to choose aggregation properties.
        final JPanel leftDownPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,grow]", "[][fill,grow]"));
        final JLabel aggregationLabel = DummyBuilder.label("Aggregation properties");
        leftDownPanel.add(aggregationLabel, "wrap");
        leftDownPanel.add(new JScrollPane(aggregationList));

        //Configuring controls for pivot tree table.
        final JPanel rightPanel = new JPanel(new MigLayout("fill, insets 3", "[fill,grow]", "[][fill,grow]"));
        rightPanel.add(toolBar, "wrap");
        rightPanel.add(pivotTablePanel);

        //Configuring left panel with distribution and aggregation list properties.
        leftPane.setOneTouchExpandable(true);
        leftPane.setTopComponent(leftTopPanel);
        leftPane.setBottomComponent(leftDownPanel);

        //Configuring main view panel.
        splitPane.setOneTouchExpandable(true);
        splitPane.setLeftComponent(leftPane);
        splitPane.setRightComponent(rightPanel);

        add(splitPane);
    }

    @SuppressWarnings("unchecked")
    private void performCustomAction(final AnalysisDataEvent<?> clickedData) {
        final ICentreDomainTreeManagerAndEnhancer baseCdtme = getModel().getCriteria().getCentreDomainTreeManagerAndEnhnacerCopy();
        baseCdtme.setRunAutomatically(true);
        final AnalysisDetailsData<T> analysisDetailsData = new AnalysisDetailsData<>(//
        getModel().getCriteria().getEntityClass(), //
        getOwner().getOwner().getModel().getName(), //
        getOwner().getModel().getName(), //
        baseCdtme, //
        getModel().adtme(),//
        (List<Pair<String, Object>>) clickedData.getData());
        getOwner().showDetails(analysisDetailsData, AnalysisDetailsData.class);
    }

    /**
     * Refreshes the pivot tree table.
     * 
     * @param treeTable
     */
    private static void refreshPivotTable(final PivotTreeTable treeTable) {
        final TreePath selectedPath = treeTable.getPathForRow(treeTable.getSelectedRow());
        ((AbstractTableModel) treeTable.getModel()).fireTableStructureChanged();
        treeTable.getSelectionModel().setSelectionInterval(0, treeTable.getRowForPath(selectedPath));
    }
}
