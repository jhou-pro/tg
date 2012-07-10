package ua.com.fielden.platform.swing.menu;

import java.util.EventListener;
import java.util.EventObject;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.model.DefaultUiModel;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.factory.IEntityCentreBuilder;
import ua.com.fielden.platform.swing.review.report.events.CentreConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ICentreConfigurationEventListener;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

/**
 * Ad hoc report wrapper. See {@link BaseNotifPanel} for more information.
 *
 * @author TG Team
 *
 */
public class DynamicReportWrapper<T extends AbstractEntity<?>> extends BaseNotifPanel<DefaultUiModel> {

    private static final long serialVersionUID = 1655601830703524962L;

    private final EventListenerList listenerList = new EventListenerList();

    //Menu item related properties.
    private final TreeMenuWithTabs<?> treeMenu;
    private final String description;

    //Entity centre related properties.
    private final IEntityCentreBuilder<T> centreBuilder;
    private final CentreConfigurationView<T, ?> entityCentreConfigurationView;
    private final Class<? extends MiWithConfigurationSupport<T>> menuItemClass;

    /**
     * Creates new {@link DynamicReportWrapper} for the given {@link DynamicCriteriaModelBuilder} and with specified title and information about the wrapped report.
     *
     * @param caption
     * @param description
     * @param modelBuilder
     */
    @SuppressWarnings("unchecked")
    public DynamicReportWrapper(
    //Menu item related parameters
    final String caption,//
	    final String description,//
	    final TreeMenuWithTabs<?> treeMenu,//
	    //Entity centre related parameters
	    final String name,//
	    final Class<? extends MiWithConfigurationSupport<T>> menuItemClass,//
	    final IEntityCentreBuilder<T> centreBuilder) {
	super(caption, new DefaultUiModel(true));
	this.description = description;
	this.treeMenu = treeMenu;
	this.centreBuilder = centreBuilder;
	this.menuItemClass = menuItemClass;
	//Create and configure entity centre;
	final BlockingIndefiniteProgressLayer progressLayer = new BlockingIndefiniteProgressLayer(null, "");
	this.entityCentreConfigurationView = centreBuilder.createEntityCentre(menuItemClass, name, progressLayer);
	this.entityCentreConfigurationView.addCentreConfigurationEventListener(createContreConfigurationListener());
	progressLayer.setView(entityCentreConfigurationView);
	add(progressLayer);
	getModel().setView(this);

    }

    @Override
    public void buildUi() {
	treeMenu.updateSelectedMenuItem();
	entityCentreConfigurationView.open();
    }

    @Override
    public String getInfo() {
	return description;
    }

    @Override
    public ICloseGuard canClose() {
	return entityCentreConfigurationView.canClose();
    }

    @Override
    public void close() {
	entityCentreConfigurationView.close();
	fireCentreClosingEvent(new CentreClosingEvent(this));
    }

    /**
     * Returns the list of non principle entity centre names.
     *
     * @return
     */
    public Set<String> getNonPrincipleEntityCentreNames() {
	return entityCentreConfigurationView.getModel().getNonPrincipleEntityCentreList();
    }

    /**
     * Returns the non null entity centre manager for this {@link DynamicReportWrapper} instance.
     *
     * @return
     */
    public ICentreDomainTreeManager getEntityCentreManager() {
	final ICentreDomainTreeManager cdtm = entityCentreConfigurationView.getModel().getEntityCentreManager();
	if(cdtm == null){
	    entityCentreConfigurationView.getModel().initEntityCentreManager();
	}
	return entityCentreConfigurationView.getModel().getEntityCentreManager();
    }

    /**
     * Returns the {@link CentreConfigurationView} instance wrapped by this {@link DynamicReportWrapper}.
     *
     * @return
     */
    public CentreConfigurationView<T, ?> getCentreConfigurationView() {
	return entityCentreConfigurationView;
    }

    @Override
    public boolean canLeave() {
	return true;
    }

    public final Class<? extends MiWithConfigurationSupport<T>> getMenuItemClass() {
	return menuItemClass;
    }

    public TreeMenuWithTabs<?> getTreeMenu() {
	return treeMenu;
    }

    public IEntityCentreBuilder<T> getCentreBuilder() {
	return centreBuilder;
    }

    private ICentreConfigurationEventListener createContreConfigurationListener() {
	return new ICentreConfigurationEventListener() {

	    @Override
	    public boolean centerConfigurationEventPerformed(final CentreConfigurationEvent event) {
		switch (event.getEventAction()) {
		case POST_SAVE_AS:
		    final MiWithConfigurationSupport<T> principleEntityCentreMenuItem = getPrincipleEntityCentreMenuItem();
		    final MiSaveAsConfiguration<T> newTreeMenuItem = new MiSaveAsConfiguration<T>(//
		    principleEntityCentreMenuItem,//
		    event.getSaveAsName());
		    principleEntityCentreMenuItem.addItem(newTreeMenuItem);
		    treeMenu.getModel().getOriginModel().reload(principleEntityCentreMenuItem);
		    treeMenu.activateOrOpenItem(newTreeMenuItem);
		    break;
		case POST_REMOVE:
		    if (event.getSource().getModel().getName() != null) {
			treeMenu.closeCurrentTab();
			treeMenu.getModel().getOriginModel().removeNodeFromParent(getAssociatedTreeMenuItem());
		    } else {
			throw new IllegalStateException("The principle tree menu item can not be removed!");
		    }
		    break;
		case REMOVE_FAILED:
		    JOptionPane.showMessageDialog(DynamicReportWrapper.this, event.getException().getMessage(), "Information", JOptionPane.INFORMATION_MESSAGE);
		    break;
		}
		return true;
	    }

	};
    }

    @SuppressWarnings("unchecked")
    private MiWithConfigurationSupport<T> getPrincipleEntityCentreMenuItem() {
	final TreeMenuItem<?> associatedTreeMenuItem = getAssociatedTreeMenuItem();
	if (associatedTreeMenuItem instanceof MiWithConfigurationSupport) {
	    return (MiWithConfigurationSupport<T>) associatedTreeMenuItem;
	} else if (associatedTreeMenuItem instanceof MiSaveAsConfiguration) {
	    return (MiWithConfigurationSupport<T>) associatedTreeMenuItem.getParent();
	} else {
	    throw new IllegalStateException("The associated or parent item must be instance of MiWithConfigurationSupport");
	}
    }

    public void addCentreClosingListener(final CentreClosingListener l) {
	listenerList.add(CentreClosingListener.class, l);
    }

    public void removeCentreClosingListener(final CentreClosingListener l) {
	listenerList.remove(CentreClosingListener.class, l);
    }

    protected void fireCentreClosingEvent(final CentreClosingEvent event) {
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == CentreClosingListener.class) {
		((CentreClosingListener) listeners[i + 1]).centreClosing(event);
	    }
	}
    }

    /**
     * Contract for anything that is interested in receiving {@link CentreClosingEvent}.
     *
     * @author TG Team
     *
     */
    public static interface CentreClosingListener extends EventListener {

	/**
	 * Invoked after centre was closed to perform custom additional task.
	 *
	 * @param event
	 */
	void centreClosing(final CentreClosingEvent event);
    }

    /**
     * Represents centre closing event.
     *
     * @author TG Team
     *
     */
    public static class CentreClosingEvent extends EventObject {

	private static final long serialVersionUID = 7671961023819217439L;

	/**
	 * Constructs centre closing event.
	 *
	 * @param source
	 */
	public CentreClosingEvent(final Object source) {
	    super(source);
	}

    }
}
