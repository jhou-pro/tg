package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import java.util.Collections;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import ua.com.fielden.platform.algorithm.search.ITreeNode;
import ua.com.fielden.platform.swing.treewitheditors.domaintree.development.EntitiesTreeModel2.EntitiesTreeUserObject;
import ua.com.fielden.platform.utils.Pair;

public class EntitiesTreeNode2 extends DefaultMutableTreeNode implements ITreeNode<Pair<Class<?>, String>> {

    private static final long serialVersionUID = -1846717362518717357L;

    public EntitiesTreeNode2(final EntitiesTreeUserObject userObject) {
	super(userObject);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ITreeNode<Pair<Class<?>, String>>> daughters() {
	return Collections.list(children());
    }

    @Override
    public Pair<Class<?>, String> state() {
	return getUserObject();
    }

    @Override
    public EntitiesTreeUserObject getUserObject() {
	return (EntitiesTreeUserObject)super.getUserObject();
    }
}
