package org.processmining.logfiltering.legacy.plugins.logfiltering.swingx;


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SizeRequirements;

/**
 * LayoutManager which is like a grid, but fixes the sizes of certain rows and
 * columns to their preferred size.
 * 
 * @author R. P. Jagadeesh Chandra 'JC' Bose (adapted from pf aa t)
 * @version 0.1
 * @date 18 December 2009
 */
public class ScrollableGridLayout implements LayoutManager2 {
    // gap between rows, columns
    private int horizontalGap, verticalGap;
    // number of rows, columns
    private int width, height;
    // which rows, columns are fixed size
    private boolean[] fixSizeRow, fixSizeCol;
    // map of where components appear
    private Map<Component, Point> componentPointMap;
    // the target container
    private Container target;
    // sizes of children
    private SizeRequirements[] xChildren, yChildren;
    private SizeRequirements xTotal, yTotal;

    // constructor
    public ScrollableGridLayout(Container target,
        int width, int height, 
        int hgap, int vgap) {
        this.target = target;
        this.width = width;
        this.height = height;
        fixSizeRow = new boolean[height];
        Arrays.fill(fixSizeRow, false);
        fixSizeCol = new boolean[width];
        Arrays.fill(fixSizeCol, false);
        this.horizontalGap = hgap;
        this.verticalGap = vgap;
        componentPointMap = new HashMap<Component, Point>(2 * width * height + 1);
        xChildren = yChildren = null;
        xTotal = yTotal = null;
    }

    // utility
    private void checkContainer(Container target) {
        if (target != this.target)
            throw new RuntimeException("bound to incorrect container");
    }

    // mutators
    public void setRowFixed(int row, boolean b) {
        fixSizeRow[row] = b;
    }

    public void setColumnFixed(int col, boolean b) {
        fixSizeCol[col] = b;
    }

    // set a component position
    public void setPosition(Component comp, int x, int y) {
        componentPointMap.put(comp, new Point(x, y));
        xChildren = yChildren = null;
    }

    // LayoutManager2 interface
    public float getLayoutAlignmentX(Container target) {
        return 0.0f;
    }

    public float getLayoutAlignmentY(Container target) {
        return 0.0f;
    }

    public void invalidateLayout(Container target) {
        checkContainer(target);
        xChildren = null;
        yChildren = null;
        xTotal = null;
        yTotal = null;
    }

    public void addLayoutComponent(Component comp, Object constraints) {
        if (componentPointMap.get(comp) == null)
            throw new RuntimeException("component with unknown "
                    + "location added");
    }

    public void addLayoutComponent(String name, Component comp) {
        if (componentPointMap.get(comp) == null)
            throw new RuntimeException("component with unknown "
                    + "location added");
    }
    
    public void removeLayoutComponent(Component comp) { 
        componentPointMap.remove(comp);
    }

    // check that the row/column sizes have been calculated
    private void checkRequests() {
        if (xChildren == null || yChildren == null) {
            xChildren = new SizeRequirements[width];
            for (int i = xChildren.length - 1; i >= 0; i--)
                xChildren[i] = new SizeRequirements(0, 0, 0, 0.0f);
            yChildren = new SizeRequirements[height];
            for (int i = yChildren.length - 1; i >= 0; i--)
                yChildren[i] = new SizeRequirements(0, 0, 0, 0.0f);

            for (int i = target.getComponentCount() - 1; i >= 0; i--) {
                Component comp = target.getComponent(i);

                if (!comp.isVisible()) 
                    continue;

                Point pos = componentPointMap.get(comp);

                if (pos == null)
                    throw new RuntimeException("component " + comp.toString()
                            + "with unknown "
                            + "location present");
                int x = pos.x;
                int y = pos.y;
                Dimension minimum_d = comp.getMinimumSize();
                Dimension preferred_d = comp.getPreferredSize();
                Dimension maximum_d = comp.getMaximumSize();

                if (fixSizeCol[x]) 
                    maximum_d.width = minimum_d.width = preferred_d.width;
                if (fixSizeRow[y])
                    maximum_d.height = minimum_d.height = preferred_d.height;

                if (minimum_d.width > xChildren[x].minimum)
                    xChildren[x].minimum = minimum_d.width;
                if (minimum_d.height > yChildren[y].minimum)
                    yChildren[y].minimum = minimum_d.height;
                if (preferred_d.width > xChildren[x].preferred)
                    xChildren[x].preferred = preferred_d.width;
                if (preferred_d.height > yChildren[y].preferred)
                    yChildren[y].preferred = preferred_d.height;
                if (maximum_d.width > xChildren[x].maximum)
                    xChildren[x].maximum = maximum_d.width;
                if (maximum_d.height > yChildren[y].maximum)
                    yChildren[y].maximum = maximum_d.height;		
            }

            xTotal = SizeRequirements.getTiledSizeRequirements(xChildren);
            yTotal = SizeRequirements.getTiledSizeRequirements(yChildren);
        }
    }

    // preferred size
    public Dimension preferredLayoutSize(Container target) {
        checkContainer(target);
        checkRequests();
	
        Dimension size = new Dimension(xTotal.preferred, yTotal.preferred);
        Insets insets = target.getInsets();

        size.width = (int) Math.min((long) size.width 
                    + (long) insets.left 
                    + insets.right, 
                    Integer.MAX_VALUE);
        size.height = (int) Math.min((long) size.height 
                    + (long) insets.top 
                    + insets.bottom, 
                    Integer.MAX_VALUE);
        return size;
    }

    // minimum size
    public Dimension minimumLayoutSize(Container target) {
        checkContainer(target);
        checkRequests();
	
        Dimension size = new Dimension(xTotal.minimum, yTotal.minimum);
        Insets insets = target.getInsets();

        size.width = (int) Math.min((long) size.width 
                    + (long) insets.left 
                    + insets.right, 
                    Integer.MAX_VALUE);
        size.height = (int) Math.min((long) size.height 
                    + (long) insets.top 
                    + insets.bottom, 
                    Integer.MAX_VALUE);
        return size;
    }

    // maximum size
    public Dimension maximumLayoutSize(Container target) {
        checkContainer(target);
        checkRequests();
	
        Dimension size = new Dimension(xTotal.maximum, yTotal.maximum);
        Insets insets = target.getInsets();

        size.width = (int) Math.min((long) size.width 
                    + (long) insets.left 
                    + insets.right, 
                    Integer.MAX_VALUE);
        size.height = (int) Math.min((long) size.height 
                    + (long) insets.top 
                    + insets.bottom, 
                    Integer.MAX_VALUE);
        return size;
    }

    // layout the container
    public void layoutContainer(Container target) {
        checkContainer(target);
        checkRequests();

        // determine the child placements
        int[] x_offsets = new int[width];
        int[] x_spans = new int[width];
        int[] y_offsets = new int[height];
        int[] y_spans = new int[height];
        Dimension alloc = target.getSize();
        Insets in = target.getInsets();

        alloc.width -= in.left + in.right;
        alloc.height -= in.top + in.bottom;
        SizeRequirements.calculateTiledPositions(alloc.width, 
            xTotal,
            xChildren, 
            x_offsets,
            x_spans);
        SizeRequirements.calculateTiledPositions(alloc.height, 
            yTotal,
            yChildren, 
            y_offsets,
            y_spans);

        for (int i = target.getComponentCount() - 1; i >= 0; i--) {
            Component comp = target.getComponent(i);
            Point pos = componentPointMap.get(comp);

            if (pos == null)
                throw new RuntimeException("component with unknown "
                        + "location present");
            int x = pos.x;
            int y = pos.y;

            comp.setBounds((int) Math.min((long) in.left 
                    + (long) x_offsets[x], 
                    Integer.MAX_VALUE),
                (int) Math.min((long) in.top 
                    + (long) y_offsets[y], 
                    Integer.MAX_VALUE),
                x_spans[x], 
                y_spans[y]);
        }
    }

	public int getHgap() {
		return horizontalGap;
	}

	public int getVgap() {
		return verticalGap;
	}
	
	public void setHgap(int horizontalGap){
		this.horizontalGap = horizontalGap;
	}
	
	public void setVgap(int verticalGap){
		this.verticalGap = verticalGap;
	}
}
