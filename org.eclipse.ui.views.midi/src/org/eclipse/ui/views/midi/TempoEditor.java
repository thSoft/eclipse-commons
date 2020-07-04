package org.eclipse.ui.views.midi;

import java.text.MessageFormat;

import javax.sound.midi.Sequencer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

public class TempoEditor extends Composite {

	private int value;
	private int maximumValue=200;

	private int getValue() {
		return value;
	}

	private void setValue(int value, boolean callback) {
		value = Math.max(0, Math.min(maximumValue, value));
		this.value = value;
		if(slider.isDisposed()){
			return;
		}
		slider.setSelection(value);
		displayer.setText(displayTempo(computeTempoFactor(value)));
		if (callback) {
			sequencer.setTempoFactor(computeTempoFactor(value));
		}
	}

	public void setValue(int value) {
		setValue(value, true);
	}

	private void resetValue() {
		setValue(defaultValue);
	}

	private void initMaximumValue() {
		slider.setMaximum(maximumValue + 1);
		slider.setPageIncrement(maximumValue / 10);
		slider.setIncrement(maximumValue / 100);
		setValue(getValue(), false);
	}

	private final int defaultValue=100;

	private final Slider slider;

	private final Label displayer;
	private final Button resetter;

	private final Sequencer sequencer;

	public TempoEditor(Composite parent, Sequencer sequencer) {
		super(parent, SWT.NONE);
		this.sequencer = sequencer;
		setLayout(new GridLayout(4, false));

		Label header = new Label(this, SWT.NONE);
		header.setImage( Activator.getImageDescriptor(MidiViewPage.ICON_PATH + "Tempo.png").createImage()); //$NON-NLS-1$
		header.setToolTipText("Tempo"); //$NON-NLS-1$

		resetter = new Button(this, SWT.FLAT);
		resetter.setImage(Activator.getImageDescriptor(MidiViewPage.ICON_PATH + "Reset.png").createImage());
		setResetterValue(defaultValue);
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

		initMaximumValue();
	}

	void setResetterValue(int value) {
		if(resetter!=null && !resetter.isDisposed()) {
			resetter.setToolTipText(MessageFormat.format("Reset to {0}", displayTempo(computeTempoFactor(value))));
		}
	}

	private String displayTempo(float tempo) {
		return MessageFormat.format("{0,number,0.00}x", tempo);
	}

	private float computeTempoFactor(int value) {
		return value / (float)100;
	}
}