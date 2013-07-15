package org.hibernate.eclipse.console.wizards.encryption;
/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import java.util.ArrayList;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.hibernate.mapping.PrimaryKey;

/**
 * CheckboxTreeViewer with special behaviour of the checked / gray state on 
 * container (non-leaf) nodes:
 * The grayed state is used to visualize the checked state of its children.
 * Containers are checked and non-gray if all contained leafs are checked. The
 * container is grayed if some but not all leafs are checked.
 * @since 3.1
 */
public class CustomContainerCheckedTreeViewer extends CheckboxTreeViewer {

    /**
     * Constructor for ContainerCheckedTreeViewer.
     * @see CheckboxTreeViewer#CheckboxTreeViewer(Composite)
     */
    public CustomContainerCheckedTreeViewer(Composite parent) {
        super(parent);
        initViewer();
    }

    /**
     * Constructor for ContainerCheckedTreeViewer.
     * @see CheckboxTreeViewer#CheckboxTreeViewer(Composite,int)
     */
    public CustomContainerCheckedTreeViewer(Composite parent, int style) {
        super(parent, style);
        initViewer();
    }

    /**
     * Constructor for ContainerCheckedTreeViewer.
     * @see CheckboxTreeViewer#CheckboxTreeViewer(Tree)
     */
    public CustomContainerCheckedTreeViewer(Tree tree) {
        super(tree);
        initViewer();
    }

    private void initViewer() {
        setUseHashlookup(true);
        addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                doCheckStateChanged(event.getElement());
            }
        });
        addTreeListener(new ITreeViewerListener() {
            public void treeCollapsed(TreeExpansionEvent event) {
            }

            public void treeExpanded(TreeExpansionEvent event) {
                Widget item = findItem(event.getElement());
                if (item instanceof TreeItem) {
                    initializeItem((TreeItem) item);
                }
            }
        });
    }

	/**
	 * Update element after a checkstate change.
	 * @param element
	 */
    protected void doCheckStateChanged(Object element) {

    	Widget item = findExactItem(element);
    	
    	if (item!=null&&item instanceof TreeItem) {
    		TreeItem treeItem = (TreeItem) item;
    		if (element instanceof PrimaryKey||//Disable check if its a primary key or part of one
    				(treeItem.getParentItem()!=null&&(treeItem.getParentItem().getData() instanceof PrimaryKey))) {   
    			treeItem.setChecked(false);
    		}else{
    			
    			treeItem.setGrayed(false);
    			updateChildrenItems(treeItem);
    			updateParentItems(treeItem.getParentItem());
    		}
    	}
    	
    	
    }

	/**
	 * findItem returns the first match if many are found
 	 * we need to verify the exact one so we get all the matches and look for the element passed
 	 * findItem is a final method and can't be override
	 * @param element
	 * @return
	 */
	protected Widget findExactItem(Object element) {
		Widget items[] = findItems(element);//findItem returns the first occurrence based on the content we need to verify the exact one so we get all the matches and look for the element passed
    	for(Widget it: items ){
    		if(it.getData()==element)
    			return it;
    			break;
    		}
		return null;
	}

    /**
     * The item has expanded. Updates the checked state of its children. 
     */
    private void initializeItem(TreeItem item) {
        if (item.getChecked() && !item.getGrayed()) {
            updateChildrenItems(item);
        }
    }

    /**
     * Updates the check state of all created children
     */
    private void updateChildrenItems(TreeItem parent) {
        Item[] children = getChildren(parent);
        boolean state = parent.getChecked();
        for (int i = 0; i < children.length; i++) {
            TreeItem curr = (TreeItem) children[i];
            if (curr.getData() != null 
            		&& !(curr.getData() instanceof PrimaryKey)            				
                    && ((curr.getChecked() != state) || curr.getGrayed())) {
                curr.setChecked(state);
                curr.setGrayed(false);
                updateChildrenItems(curr);
            }
        }
    }

    /**
     * Updates the check / gray state of all parent items
     */
    private void updateParentItems(TreeItem item) {
        if (item != null) {
            Item[] children = getChildren(item);
            boolean containsChecked = false;
            boolean containsUnchecked = false;
            for (int i = 0; i < children.length; i++) {
                TreeItem curr = (TreeItem) children[i];
                containsChecked |= curr.getChecked();
                containsUnchecked |= (!curr.getChecked() || curr.getGrayed());
            }
            item.setChecked(containsChecked);
            item.setGrayed(containsChecked && containsUnchecked);
            updateParentItems(item.getParentItem());
        }
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICheckable#setChecked(java.lang.Object, boolean)
     */
    public boolean setChecked(Object element, boolean state) {
        if (super.setChecked(element, state)) {
            doCheckStateChanged(element);
            return true;
        }
        return false;
    }

 
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CheckboxTreeViewer#setCheckedElements(java.lang.Object[])
     */
    public void setCheckedElements(Object[] elements) {
        super.setCheckedElements(elements);
        for (int i = 0; i < elements.length; i++) {
            doCheckStateChanged(elements[i]);
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTreeViewer#setExpanded(org.eclipse.swt.widgets.Item, boolean)
     */
    protected void setExpanded(Item item, boolean expand) {
        super.setExpanded(item, expand);
        if (expand && item instanceof TreeItem) {
            initializeItem((TreeItem) item);
        }
    }

   
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CheckboxTreeViewer#getCheckedElements()
     */
    public Object[] getCheckedElements() {
        Object[] checked = super.getCheckedElements();
        // add all items that are children of a checked node but not created yet
        ArrayList result = new ArrayList();
        for (int i = 0; i < checked.length; i++) {
            Object curr = checked[i];
            result.add(curr);
            Widget item = findItem(curr);
            if (item != null) {
                Item[] children = getChildren(item);
                // check if contains the dummy node
                if (children.length == 1 && children[0].getData() == null) {
                    // not yet created
                    collectChildren(curr, result);
                }
            }
        }
        return result.toArray();
    }

	/**
	 * Recursively add the filtered children of element to the result.
	 * @param element
	 * @param result
	 */
    private void collectChildren(Object element, ArrayList result) {
        Object[] filteredChildren = getFilteredChildren(element);
        for (int i = 0; i < filteredChildren.length; i++) {
            Object curr = filteredChildren[i];
            result.add(curr);
            collectChildren(curr, result);
        }
    }

}
