package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.egi.EgiPanel;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.review.OrderingArrow;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.verticallabel.DefaultTableHeaderCellRenderer;
import ua.com.fielden.platform.swing.verticallabel.MouseDefaultHeaderHandler;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class GridAnalysisView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReview<T, CDTME, IAbstractAnalysisDomainTreeManagerAndEnhancer, IPage<T>> {

    private static final long serialVersionUID = 8538099803371092525L;

    private final EgiPanel<T> egiPanel;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public GridAnalysisView(final GridAnalysisModel<T, CDTME> model, final GridConfigurationView<T, CDTME> owner) {
	super(model, owner);
	this.egiPanel = new EgiPanel(getModel().getGridModel(), false);
	this.addSelectionEventListener(createGridAnalysisSelectionListener());

	//Set this analysis view for model.
	model.setAnalysisView(this);

	configureEgiWithOrdering();
	layoutView();
    }

    public EgiPanel<T> getEgiPanel() {
	return egiPanel;
    }

    @Override
    public GridAnalysisModel<T, CDTME> getModel() {
	return (GridAnalysisModel<T, CDTME>) super.getModel();
    }

    @Override
    protected void enableRelatedActions(final boolean enable, final boolean navigate) {
	if(getModel().getCriteria().isDefaultEnabled()){
	    getCentre().getDefaultAction().setEnabled(enable);
	}
	if(!navigate){
	    getCentre().getPaginator().setEnableActions(enable, !enable);
	}
	getCentre().getExportAction().setEnabled(enable);
	getCentre().getRunAction().setEnabled(enable);
    }

    @Override
    protected ConfigureAction createConfigureAction() {
	return null;
    }

    protected void layoutView() {
	setLayout(new MigLayout("fill, insets 0","[fill, grow]","[fill, grow]"));
	add(this.egiPanel);
    }

    /**
     * Determines the number of rows in the table those must be shown on the page using the size of the content panel as the basis.
     * If the calculated size is zero then value of 25 is returned.
     * This is done to handle cases where calculation happens prior to panel resizing takes place.
     *
     * @return
     */
    final int getPageSize() {
	double pageSize = egiPanel.getEgiScrollPane().getSize().getHeight() / egiPanel.getEgi().getRowHeight();
	if (getOwner().getOwner().getCriteriaPanel() != null) {
	    pageSize += getOwner().getOwner().getCriteriaPanel().getSize().getHeight() / egiPanel.getEgi().getRowHeight();
	}
	final int pageCapacity = (int) Math.floor(pageSize);
	return pageCapacity > 1 ? pageCapacity : 1;
    }

    //    /**
    //     * Enables or disables the paginator's actions without enabling or disabling blocking layer.
    //     *
    //     * @param enable
    //     */
    //    private void enablePaginatorActionsWithoutBlockingLayer(final boolean enable){
    //	getOwner().getPaginator().getFirst().setEnabled(enable, false);
    //	getOwner().getPaginator().getPrev().setEnabled(enable, false);
    //	getOwner().getPaginator().getNext().setEnabled(enable, false);
    //	getOwner().getPaginator().getLast().setEnabled(enable, false);
    //	if(getOwner().getPaginator().getFeedback() != null){
    //	    getOwner().getPaginator().getFeedback().enableFeedback(false);
    //	}
    //    }

    /**
     * Returns the {@link ISelectionEventListener} that enables or disable appropriate actions when this analysis was selected.
     * 
     * @return
     */
    private ISelectionEventListener createGridAnalysisSelectionListener() {
	return new ISelectionEventListener() {

	    @Override
	    public void viewWasSelected(final SelectionEvent event) {
		//Managing the default, design and custom action changer button enablements.
		getCentre().getDefaultAction().setEnabled(getModel().getCriteria().isDefaultEnabled());
		if (getCentre().getCriteriaPanel() != null && getCentre().getCriteriaPanel().canConfigure()) {
		    getCentre().getCriteriaPanel().getSwitchAction().setEnabled(true);
		}
		if(getCentre().getCustomActionChanger() != null){
		    getCentre().getCustomActionChanger().setEnabled(true);
		}
		//Managing the paginator's enablements.
		getCentre().getPaginator().setEnableActions(true, false);
		//Managing load and export enablements.
		getCentre().getExportAction().setEnabled(true);
		getCentre().getRunAction().setEnabled(true);
	    }
	};
    }

    /**
     * Configures the analysis entity grid inspector with ordering facility.
     */
    private void configureEgiWithOrdering(){
	final Class<T> root = getModel().getCriteria().getEntityClass();
	final IAddToResultTickManager tickManager = getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer().getSecondTick();
	final EntityGridInspector<T> egi = this.egiPanel.getEgi();
	egi.getColumnModel().addColumnModelListener(createColumnSwapModelListener(root, tickManager));
	egi.getTableHeader().addMouseListener(createTableHeaderClickMouseListener(root, tickManager, egi));
	for (int columnIndex = 0; columnIndex < egi.getColumnCount(); columnIndex++) {
	    final TableColumn column =  egi.getColumnModel().getColumn(columnIndex);
	    column.setHeaderRenderer(new SortableTableHeaderCellRenderer(root, tickManager));
	    column.addPropertyChangeListener(createColumnWidthChangeListener(root, tickManager, egi));
	}
	final MouseDefaultHeaderHandler mouseHandler = new MouseDefaultHeaderHandler();
	egi.getTableHeader().addMouseMotionListener(mouseHandler);
	egi.getTableHeader().addMouseListener(mouseHandler);
    }

    /**
     * Creates the mouse listener that listens the table header mouse click events.
     * @param tickManager
     * @param root
     * 
     * @param egi
     * @return
     */
    private MouseListener createTableHeaderClickMouseListener(final Class<T> root, final IAddToResultTickManager tickManager, final EntityGridInspector<T> egi) {
	return new MouseAdapter() {
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		final TableColumnModel columnModel = egi.getColumnModel();
		final int viewColumn = columnModel.getColumnIndexAtX(e.getX());
		if (e.getClickCount() == 1 && viewColumn >= 0 && (e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
		    final String property = tickManager.checkedProperties(root).get(viewColumn);
		    tickManager.toggleOrdering(root, property);
		    egi.getTableHeader().repaint();
		}
	    }

	};
    }

    /**
     * Creates {@link TableColumnModelListener} instance that listens the column moved events.
     * 
     * @param root
     * @param tickManager
     * @return
     */
    private TableColumnModelListener createColumnSwapModelListener(final Class<T> root, final IAddToResultTickManager tickManager) {
	return new TableColumnModelListener() {

	    @Override
	    public void columnSelectionChanged(final ListSelectionEvent e) { }

	    @Override
	    public void columnRemoved(final TableColumnModelEvent e) { }

	    @Override
	    public void columnMarginChanged(final ChangeEvent e) { }

	    @Override
	    public void columnAdded(final TableColumnModelEvent e) { }

	    @Override
	    public void columnMoved(final TableColumnModelEvent e) {
		if(e.getFromIndex() != e.getToIndex()){
		    final List<String> checkedProperties = tickManager.checkedProperties(root);
		    final String fromProperty = checkedProperties.get(e.getFromIndex());
		    final String toProperty = checkedProperties.get(e.getToIndex());
		    tickManager.swap(root, fromProperty, toProperty);
		}

	    }
	};
    }

    /**
     * Creates {@link PropertyChangeListener} that listens the column width change events.
     * 
     * @param root
     * @param tickManager
     * @param egi
     * @return
     */
    private PropertyChangeListener createColumnWidthChangeListener(final Class<T> root, final IAddToResultTickManager tickManager, final EntityGridInspector<T> egi) {
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("width")) {
		    final TableColumn tableColumn = (TableColumn)evt.getSource();
		    final String columnProperty = tickManager.checkedProperties(root).get(egi.getColumnModel().getColumnIndex(tableColumn.getIdentifier()));
		    tickManager.setWidth(root, columnProperty, ((Integer)evt.getNewValue()).intValue());
		}
	    }
	};
    }

    private AbstractEntityCentre<T, CDTME> getCentre(){
	return getOwner().getOwner();
    }

    /**
     * Table header cell renderer that draws sorting arrows for the concrete table column.
     * 
     * @author TG Team
     *
     */
    private class SortableTableHeaderCellRenderer extends DefaultTableHeaderCellRenderer{

	private static final long serialVersionUID = -6294136148685562497L;

	/**
	 * The ordering arrow to be drawn on the table column.
	 */
	private final OrderingIcon orderingIcon = new OrderingIcon();

	/**
	 * The entity type for which this analysis was created.
	 */
	private final Class<T> root;

	/**
	 * The {@link IAddToResultTickManager} instance needed to determine checked properties and ordering properties.
	 */
	private final IAddToResultTickManager tickManager;

	private final static int ICON_LEFT_INSETS = 3;
	private final static int ICON_TOP_INSETS = 3;
	private final static int ICON_RIGHT_INSETS = -3;
	private final static int ICON_BOTTOM_INSETS = 3;

	/**
	 * Creates new table header cell renderer with ordering icon
	 * @param tickManager
	 * @param root
	 */
	public SortableTableHeaderCellRenderer(final Class<T> root, final IAddToResultTickManager tickManager) {
	    this.root = root;
	    this.tickManager = tickManager;
	    setHorizontalAlignment(LEFT);
	    setHorizontalTextPosition(RIGHT);
	}

	@Override
	protected Icon getIcon(final JTable table, final int column) {
	    final String property = tickManager.checkedProperties(root).get(column);

	    final List<Pair<String, Ordering>> sortKeys = tickManager.orderedProperties(root);
	    orderingIcon.setSortOrder(SortOrder.UNSORTED);
	    for (int counter = 0; counter < sortKeys.size(); counter++) {
		if (EntityUtils.equalsEx(property, sortKeys.get(counter).getKey())) {
		    orderingIcon.setOrder(counter + 1);
		    switch(sortKeys.get(counter).getValue()){
		    case ASCENDING:
			orderingIcon.setSortOrder(SortOrder.ASCENDING);
			break;
		    case DESCENDING:
			orderingIcon.setSortOrder(SortOrder.DESCENDING);
			break;
		    }
		}
	    }
	    return orderingIcon;

	}

	/**
	 * Icon that represents ordering arrow on the table header
	 *
	 * @author TG Team
	 * 
	 */
	private class OrderingIcon implements Icon {

	    private final OrderingArrow orderingArrow;

	    /**
	     * Creates {@link OrderingIcon} and {@link OrderingArrow}
	     */
	    public OrderingIcon() {
		orderingArrow = new OrderingArrow();
	    }

	    @Override
	    public int getIconHeight() {
		return (int) Math.ceil(orderingArrow.getActualHeight(getGraphics()) + ICON_TOP_INSETS + ICON_BOTTOM_INSETS);
	    }

	    @Override
	    public int getIconWidth() {
		return (int) Math.ceil(orderingArrow.getActualWidth(getGraphics()) + ICON_LEFT_INSETS + ICON_RIGHT_INSETS);
	    }

	    @Override
	    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		g.translate(x + ICON_LEFT_INSETS, y + ICON_TOP_INSETS);
		orderingArrow.paintComponent(g);
		g.translate(-x - ICON_LEFT_INSETS, -y - ICON_TOP_INSETS);
	    }

	    /**
	     * Set the {@link SortOrder} for the {@link OrderingArrow}
	     *
	     * @param sortOrder
	     */
	    public void setSortOrder(final SortOrder sortOrder) {
		orderingArrow.setSortOrder(sortOrder);
	    }

	    /**
	     * Set the order value for the {@link OrderingArrow} associated with this {@link OrderingIcon}
	     *
	     * @param order
	     */
	    public void setOrder(final int order) {
		orderingArrow.setOrder(order);
	    }

	    /**
	     * Set the indicator that determines whether {@link OrderingArrow} is highlighted or not
	     *
	     * @param mouseOver
	     */
	    public void setMouseOver(final boolean mouseOver) {
		orderingArrow.setMouseOver(mouseOver);
	    }

	}

	@Override
	public Dimension getPreferredSize() {
	    final Dimension labelDimension = super.getPreferredSize();
	    return new Dimension(labelDimension.width, orderingIcon.getIconHeight());
	}

	@Override
	public void setMouseOver(final boolean mouseOver) {
	    super.setMouseOver(mouseOver);
	    orderingIcon.setMouseOver(mouseOver);
	}

	@Override
	public Insets getInsets() {
	    return new Insets(3, 3, 3, 3 + (getIcon() != null ? ICON_LEFT_INSETS : 0));
	}

	@Override
	public Insets getInsets(final Insets insets) {
	    return getInsets();
	}
    }
}
