package org.eclipse.ui.views.midi;

import java.text.MessageFormat;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

public class NumericValueEditor extends Composite {

	private int value;

	public int getValue() {
		return value;
	}

	public void setValue(int value, boolean callback) {
		value = Math.max(0, Math.min(getMaximumValue(), value));
		this.value = value;
		if(slider.isDisposed()){
			return;
		}
		slider.setSelection(value);
		displayer.setText(MessageFormat.format("{0}/{1}", hooks.display(value), hooks.display(getMaximumValue())));
		if (callback) {
			hooks.valueSet(value);
		}
	}

	public void setValue(int value) {
		setValue(value, true);
	}

	public void resetValue() {
		setValue(defaultValue);
	}

	private int maximumValue;

	public int getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(int maximumValue) {
		this.maximumValue = maximumValue;
		slider.setMaximum(maximumValue + 1);
		slider.setPageIncrement(maximumValue / 100);
		setValue(getValue());
	}

	private final int defaultValue;

	private final Slider slider;

	private final Label displayer;

	private final ValueHooks hooks;

	public NumericValueEditor(Composite parent, String name, ImageDescriptor icon, ImageDescriptor resetterIcon, int maximumValue, int defaultValue, ValueHooks hooks) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout(4, false));

		Label header = new Label(this, SWT.NONE);
		header.setImage(icon.createImage());
		header.setToolTipText(name);

		Button resetter = new Button(this, SWT.FLAT);
		resetter.setImage(resetterIcon.createImage());
		resetter.setToolTipText(MessageFormat.format("Reset to {0}", hooks.display(defaultValue)));
		resetter.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				resetValue();
			}

		});

		slider = new Slider(this, SWT.NONE);
		GridData sliderLayoutData = new GridData();
		sliderLayoutData.horizontalAlignment = SWT.FILL;
		sliderLayoutData.grabExcessHorizontalSpace = true;
		slider.setLayoutData(sliderLayoutData);
		slider.setThumb(1);
		slider.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setValue(slider.getSelection());
			}

		});

		displayer = new Label(this, SWT.CENTER);
		displayer.setLayoutData(new GridData(80, SWT.DEFAULT)); // XXX proper width

		this.hooks = hooks;
		this.setMaximumValue(maximumValue);
		this.defaultValue = defaultValue;
		setValue(defaultValue);
	}

}
