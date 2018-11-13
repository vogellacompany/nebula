/*******************************************************************************
 * Copyright (c) 2012 Laurent CARON. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.roundedtoolbar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.eclipse.nebula.widgets.opal.commons.AdvancedPath;
import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

/**
 * Instances of this class support the layout of selectable rounded tool bar
 * items.
 * <p>
 * The item children that may be added to instances of this class must be of
 * type <code>RoundedToolItem</code>.
 * </p>
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HIDE_SELECTION</dd>: if this style is selected, the radio button are not drawned
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#toolbar">ToolBar, ToolItem
 *      snippets</a>
 */
public class RoundedToolbar extends Canvas {

	private static final String LAST_TOGGLE_BUTTON_SELECTED = RoundedToolbar.class.toString() + "_LAST_TOGGLE_BUTTON_SELECTED";
	private static final String LAST_TOGGLE_BUTTON_SELECTED_STATE = RoundedToolbar.class.toString() + "_LAST_TOGGLE_BUTTON_SELECTED_STATE";

	private final List<RoundedToolItem> items;
	private int cornerRadius;
	private static Color START_GRADIENT_COLOR_DEFAULT = SWTGraphicUtil.getColorSafely(245, 245, 245);
	private static Color END_GRADIENT_COLOR_DEFAULT = SWTGraphicUtil.getColorSafely(185, 185, 185);
	static Color BORDER_COLOR = SWTGraphicUtil.getColorSafely(66, 66, 66);

	private Color START_GRADIENT_COLOR = START_GRADIENT_COLOR_DEFAULT;
	private Color END_GRADIENT_COLOR = END_GRADIENT_COLOR_DEFAULT;

	/**
	 * Constructs a new instance of this class given its parent and a style
	 * value describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
	 * style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 *
	 * @param parent a composite control which will be the parent of the new
	 *            instance (cannot be null)
	 * @param style the style of control to construct
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 *
	 * @see Widget#getStyle()
	 */
	public RoundedToolbar(final Composite parent, final int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);
		items = new ArrayList<RoundedToolItem>();
		cornerRadius = 2;
		addListeners();
	}

	/**
	 * Constructs a new instance of this class given its parent, a style value
	 * describing its behavior and appearance and colors to specify the start
	 * and end gradient of the rounded corner
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
	 * style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 *
	 * @param parent a composite control which will be the parent of the new
	 *            instance (cannot be null)
	 * @param style the style of control to construct
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 *
	 * @see Widget#getStyle()
	 */
	public RoundedToolbar(final Composite parent, final int style, Color startGradientColor, Color endGradientColor) {
		super(parent, style | SWT.DOUBLE_BUFFERED);
		items = new ArrayList<RoundedToolItem>();
		cornerRadius = 2;
		addListeners();
		START_GRADIENT_COLOR = startGradientColor;
		END_GRADIENT_COLOR = endGradientColor;
	}

	private void addListeners() {
		addListener(SWT.MouseDown, event -> {
			if (event.button != 1) {
				return;
			}

			final Optional<RoundedToolItem> pressedItem = items.stream()//
					.filter(element -> element.getBounds().contains(event.x, event.y) && element.isEnabled()) //
					.findFirst();
			if (!pressedItem.isPresent()) {
				return;
			}
			final RoundedToolItem item = pressedItem.get();
			if (item.isPushButon()) {
				item.forceSelection(true);
			} else {
				if (item.isToogleButon()) {
					setData(LAST_TOGGLE_BUTTON_SELECTED, item);
					setData(LAST_TOGGLE_BUTTON_SELECTED_STATE, item.getSelection());
				}
				item.setSelection(!item.getSelection());
			}

			redraw();
			update();
		});

		addListener(SWT.MouseUp, event -> {
			if (event.button != 1) {
				return;
			}

			final Optional<RoundedToolItem> selectedItem = items.stream()//
					.filter(element -> element.getBounds().contains(event.x, event.y) && element.isEnabled()) //
					.findFirst();
			if (!selectedItem.isPresent()) {
				return;
			}
			final RoundedToolItem item = selectedItem.get();
			if (item.isToogleButon()) {
				// The user clicked on a toogle button and release the button on another button
				final RoundedToolItem lastToggleButtonSelected = (RoundedToolItem) getData(LAST_TOGGLE_BUTTON_SELECTED);
				if (lastToggleButtonSelected != null && lastToggleButtonSelected != item) {
					final Boolean value = (Boolean) getData(LAST_TOGGLE_BUTTON_SELECTED_STATE);
					lastToggleButtonSelected.forceSelection(value);
					clearLastToggleButtonInfo();
					redraw();
					update();
					return;
				}

			}
			clearLastToggleButtonInfo();
			if (item.isPushButon()) {
				item.setSelection(false);
			}
			item.fireSelectionEvent();
			if (item.isRadio()) {
				for (final RoundedToolItem current : items) {
					if (!current.equals(item)) {
						current.setSelection(false);
						current.fireSelectionEvent();
					}
				}
			}
			redraw();
			update();
		});
		addListener(SWT.MouseHover, event -> {
			final Optional<RoundedToolItem> previouslySelectedItem = items.stream()//
					.filter(element -> element.getBounds().contains(event.x, event.y) && element.isEnabled()) //
					.findFirst();
			if (!previouslySelectedItem.isPresent()) {
				return;
			}
			final RoundedToolItem item = previouslySelectedItem.get();

			setToolTipText(item.getTooltipText() == null ? "" : item.getTooltipText());
		});

		addListener(SWT.MouseExit, event -> {
			final Iterator<RoundedToolItem> it = items.iterator();
			boolean needRedraw = false;
			while (it.hasNext()) {
				final RoundedToolItem item = it.next();
				if (item.isPushButon() && item.getSelection()) {
					item.forceSelection(false);
					needRedraw = true;
				}
				// The user clicks on a toogle button, keeps the button press and leave the toolbar: we revert to the previous state
				final RoundedToolItem lastToggleButtonSelected = (RoundedToolItem) getData(LAST_TOGGLE_BUTTON_SELECTED);
				if (lastToggleButtonSelected != null && lastToggleButtonSelected == item) {
					final Boolean value = (Boolean) getData(LAST_TOGGLE_BUTTON_SELECTED_STATE);
					item.forceSelection(value);
					needRedraw = true;
				}

			}
			clearLastToggleButtonInfo();
			if (needRedraw) {
				redraw();
				update();
			}
		});

		addPaintListener(e -> {
			paintControl(e);
		});
	}

	private void clearLastToggleButtonInfo() {
		setData(LAST_TOGGLE_BUTTON_SELECTED, null);
		setData(LAST_TOGGLE_BUTTON_SELECTED_STATE, null);
	}

	/**
	 * Add an item to the toolbar
	 *
	 * @param roundedToolItem roundedToolItem to add
	 */
	void addItem(final RoundedToolItem roundedToolItem) {
		items.add(roundedToolItem);
	}

	/**
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();
		int width = 0, height = 0;
		for (final RoundedToolItem item : items) {
			width += item.getWidth();
			height = Math.max(height, item.getHeight());
		}
		return new Point(Math.max(width, wHint), Math.max(height, hHint));
	}

	/**
	 * @return the corner radius
	 */
	public int getCornerRadius() {
		checkWidget();
		return cornerRadius;
	}

	/**
	 * Returns the item at the given, zero-relative index in the receiver.
	 * Throws an exception if the index is out of range.
	 *
	 * @param index the index of the item to return
	 * @return the item at the given index
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_RANGE - if the index is not between 0
	 *                and the number of elements in the list minus 1
	 *                (inclusive)</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public RoundedToolItem getItem(final int index) {
		checkWidget();
		if (index < 0 || index > items.size()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		return items.get(index);
	}

	/**
	 * Returns the item at the given point in the receiver or null if no such
	 * item exists. The point is in the coordinate system of the receiver.
	 *
	 * @param point the point used to locate the item
	 * @return the item at the given point
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public RoundedToolItem getItem(final Point point) {
		checkWidget();
		final RoundedToolItem item = items.stream()//
				.filter(element -> element.getBounds().contains(point)) //
				.findFirst() //
				.orElse(null);
		return item;
	}

	/**
	 * Returns the number of items contained in the receiver.
	 *
	 * @return the number of items
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getItemCount() {
		checkWidget();
		return items.size();
	}

	/**
	 * Returns an array of <code>RoundedToolItem</code>s which are the items in
	 * the receiver.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain
	 * its list of items, so modifying the array will not affect the receiver.
	 * </p>
	 *
	 * @return the items in the receiver
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public RoundedToolItem[] getItems() {
		checkWidget();
		return items.toArray(new RoundedToolItem[items.size()]);
	}

	/**
	 * Searches the receiver's list starting at the first item (index 0) until
	 * an item is found that is equal to the argument, and returns the index of
	 * that item. If no item is found, returns -1.
	 *
	 * @param item the search item
	 * @return the index of the item
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the tool item is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the tool item has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int indexOf(final RoundedToolItem item) {
		checkWidget();
		return items.indexOf(item);
	}

	/**
	 * Paint the component
	 *
	 * @param e event
	 */
	protected void paintControl(final PaintEvent e) {
		final GC gc = e.gc;
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);

		final int width = getSize().x;
		final int height = getSize().y;

		drawBorders(gc, width, height);
		final Iterator<RoundedToolItem> it = items.iterator();
		int x = 0;
		while (it.hasNext()) {
			final RoundedToolItem item = it.next();
			item.drawButton(gc, x, height, !it.hasNext());
			x += item.getWidth();
		}
	}

	private void drawBorders(final GC gc, final int width, final int height) {
		final AdvancedPath path = new AdvancedPath(getDisplay());
		path.addRoundRectangle(0, 0, width, height, cornerRadius, cornerRadius);
		gc.setClipping(path);

		gc.setForeground(START_GRADIENT_COLOR);
		gc.setBackground(END_GRADIENT_COLOR);
		gc.fillGradientRectangle(0, 0, width, height, true);

		gc.setForeground(BORDER_COLOR);
		gc.drawRoundRectangle(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

		gc.setClipping((Rectangle) null);
	}

	/**
	 * Add an item to the toolbar
	 *
	 * @param roundedToolItem roundedToolItem to add
	 */
	void removeItem(final RoundedToolItem roundedToolItem) {
		checkWidget();
		items.remove(roundedToolItem);
	}

	/**
	 * @param cornerRadius new corner radius
	 */
	public void setCornerRadius(final int cornerRadius) {
		checkWidget();
		this.cornerRadius = cornerRadius;
	}

}
