/*
 * @(#)AbstractListIntelliHints.java 7/24/2005
 *
 * Copyright 2002 - 2005 JIDE Software Inc. All rights reserved.
 */
package ua.com.fielden.platform.swing.components.smart.autocompleter.development;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;

import ua.com.fielden.platform.swing.components.smart.AbstractIntelliHints;
import ua.com.fielden.platform.swing.components.smart.Hover;

import com.jidesoft.swing.JideScrollPane;

/**
 * <code>AbstractListIntelliHints</code> extends AbstractIntelliHints and further implement most of the methods in interface {@link com.jidesoft.hints.IntelliHints}. In this class,
 * it assumes the hints can be represented as a JList, so it used JList in the hints popup.
 * 
 * @author Santhosh Kumar T - santhosh@in.fiorano.com
 * @author JIDE Software, Inc.
 */
public abstract class AbstractListIntelliHints extends AbstractIntelliHints {
    private JList _list;
    protected KeyStroke[] _keyStrokes;
    private JideScrollPane _scroll;

    /**
     * Creates a Completion for JTextComponent
     * 
     * @param textComponent
     */
    public AbstractListIntelliHints(final JTextComponent textComponent, final ListCellRenderer cellRenderer) {
	super(textComponent, cellRenderer);
    }

    public JComponent createHintsComponent(final ListCellRenderer cellRenderer) {
	final JPanel panel = new JPanel(new BorderLayout());

	_list = createList(cellRenderer);
	Hover.install(_list);
	_scroll = new JideScrollPane(getList());
	getList().setFocusable(false);

	_scroll.setHorizontalScrollBarPolicy(JideScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	_scroll.setBorder(BorderFactory.createEmptyBorder());
	_scroll.getVerticalScrollBar().setFocusable(false);
	_scroll.getHorizontalScrollBar().setFocusable(false);

	panel.add(_scroll, BorderLayout.CENTER);
	return panel;
    }

    /**
     * Creates the list to display the hints. By default, we create a JList using the code below. <code><pre>
     * return new JList() {
     *     public int getVisibleRowCount() {
     *         int size = getModel().getSize();
     *         return size < super.getVisibleRowCount() ? size : super.getVisibleRowCount();
     *     }
     * <p/>
     *     public Dimension getPreferredScrollableViewportSize() {
     *         if (getModel().getSize() == 0) {
     *             return new Dimension(0, 0);
     *         }
     *         else {
     *             return super.getPreferredScrollableViewportSize();
     *         }
     *     }
     * };
     * </pre></code>
     * 
     * @return the list.
     */
    protected JList createList(final ListCellRenderer cellRenderer) {
	return new JList() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public int getVisibleRowCount() {
		final int size = getModel().getSize();
		return size < super.getVisibleRowCount() ? size : super.getVisibleRowCount();
	    }

	    @Override
	    public Dimension getPreferredScrollableViewportSize() {
		if (getModel().getSize() == 0) {
		    return new Dimension(0, 0);
		} else {
		    return super.getPreferredScrollableViewportSize();
		}
	    }
	};
    }

    /**
     * Returns the {@link ListCellRenderer} associated with hints pop up component.
     * 
     * @return
     */
    protected ListCellRenderer getHintsCellRenderer() {
	return _list.getCellRenderer();
    }

    /**
     * Gets the list.
     * 
     * @return the list.
     */
    protected JList getList() {
	return _list;
    }

    /**
     * Sets the list data.
     * 
     * @param objects
     */
    protected void setListData(final Object[] objects) {
	resetSelection();
	getList().setListData(objects);

	// update the view so that isViewSizeSet flag in JViewport is reset to false
	if (_scroll != null) {
	    _scroll.setViewportView(getList());
	}

    }

    /**
     * Sets the list data.
     * 
     * @param objects
     */
    protected void setListData(final Vector<?> objects) {
	resetSelection();
	getList().setListData(objects);
	// update the view so that isViewSizeSet flag in JViewport is reset to false
	if (_scroll != null) {
	    _scroll.setViewportView(getList());
	}
    }

    protected void resetSelection() {
	getList().getSelectionModel().setAnchorSelectionIndex(-1); // has to call setAnchor first
	getList().getSelectionModel().setLeadSelectionIndex(-1);
	getList().getSelectionModel().clearSelection();
    }

    public Object getSelectedHint() {
	return getList().getSelectedValue();
    }

    @Override
    public JComponent getDelegateComponent() {
	return getList();
    }

    /**
     * Gets the delegate keystrokes. Since we know the hints popup is a JList, we return eight keystrokes so that they can be delegate to the JList. Those keystrokes are DOWN, UP,
     * PAGE_DOWN, PAGE_UP, HOME and END.
     * 
     * @return the keystokes that will be delegated to the JList when hints popup is visible.
     */
    @Override
    public KeyStroke[] getDelegateKeyStrokes() {
	if (_keyStrokes == null) {
	    _keyStrokes = new KeyStroke[6];
	    _keyStrokes[0] = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
	    _keyStrokes[1] = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
	    _keyStrokes[2] = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
	    _keyStrokes[3] = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0);
	    _keyStrokes[4] = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0);
	    _keyStrokes[5] = KeyStroke.getKeyStroke(KeyEvent.VK_END, 0);
	}
	return _keyStrokes;
    }
}