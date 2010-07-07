package org.eclipse.ui.views.midi;

import java.io.IOException;
import java.text.MessageFormat;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class MidiViewPage extends ScrolledComposite {

	public static final String ICON_PATH = "icons/"; //$NON-NLS-1$

	private final Sequencer sequencer;

	private final Synthesizer synthesizer;

	public MidiViewPage(Composite parent, IFile file) throws MidiUnavailableException, InvalidMidiDataException, IOException {
		super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		setExpandHorizontal(true);
		setExpandVertical(true);
		Composite content = new Composite(this, SWT.NONE);
		content.setLayout(new GridLayout(1, true));
		setContent(content);

		sequencer = MidiSystem.getSequencer();
		sequencer.open();
		synthesizer = MidiSystem.getSynthesizer();
		synthesizer.open();
		sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());

		addTime(content);
		addVolume(content);
		addTempo(content);
		for (Control child : content.getChildren()) {
			GridData layoutData = new GridData();
			layoutData.horizontalAlignment = SWT.FILL;
			layoutData.grabExcessHorizontalSpace = true;
			child.setLayoutData(layoutData);
		}
		setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		setFile(file);
	}

	// File handling

	/**
	 * The open MIDI file.
	 */
	private IFile file;

	public IFile getFile() {
		return file;
	}

	public void setFile(IFile file) throws InvalidMidiDataException, IOException {
		this.file = file;
		sequencer.setSequence(MidiSystem.getSequence(file.getRawLocation().toFile()));
		time.setMaximumValue((int)sequencer.getMicrosecondLength());
	}

	public void reload() throws InvalidMidiDataException, IOException {
		setFile(getFile());
	}

	public void closeFile() {
		sequencer.close();
	}

	// Playback

	private MidiViewToolbarManager.PlaybackAction playbackAction;

	public MidiViewToolbarManager.PlaybackAction getPlaybackAction() {
		return playbackAction;
	}

	public void setPlaybackAction(MidiViewToolbarManager.PlaybackAction playbackAction) {
		this.playbackAction = playbackAction;
	}

	public void play() {
		if (isFinished()) {
			sequencer.setMicrosecondPosition(0);
		}
		sequencer.start();
		getPlaybackAction().setPlaying(true);
		Display.getDefault().timerExec(0, new Updater());
	}

	public void pause() {
		sequencer.stop();
		getPlaybackAction().setPlaying(false);
	}

	public boolean isPlaying() {
		return sequencer.isRunning();
	}

	// Time

	private NumericValueEditor time;

	private void addTime(Composite parent) {
		time = new NumericValueEditor(parent, "Time", Activator.getImageDescriptor(ICON_PATH + "Time.png"), Activator.getImageDescriptor(ICON_PATH + "Rewind.png"), 0, 0, new ValueHooks() { //$NON-NLS-2$ //$NON-NLS-3$

			@Override
			public String display(int value) {
				return getDuration(value);
			}

			@Override
			public void valueSet(int value) {
				sequencer.setMicrosecondPosition(value);
			}

			private String getDuration(long microseconds) {
				long seconds = microseconds / 1000000;
				final int secondsInMinute = 60;
				return MessageFormat.format("{0}:{1,number,00}", seconds / secondsInMinute, seconds % secondsInMinute);
			}

		});
	}

	private boolean isFinished() {
		return sequencer.getMicrosecondPosition() >= sequencer.getMicrosecondLength();
	}

	private class Updater implements Runnable {

		@Override
		public void run() {
			time.setValue((int)sequencer.getMicrosecondPosition(), false);
			if (isFinished()) {
				pause();
			} else {
				final int millisecondsPerSecond = 1000;
				final int framesPerSecond = 25;
				Display.getDefault().timerExec(millisecondsPerSecond / framesPerSecond, this);
			}
		}

	}

	// Volume

	private static final int MAX_VOLUME = 100;

	private static final int CHANNEL_VOLUME = 7;

	private void addVolume(Composite parent) {
		new NumericValueEditor(parent, "Volume", Activator.getImageDescriptor(ICON_PATH + "Volume.png"), Activator.getImageDescriptor(ICON_PATH + "Reset.png"), MAX_VOLUME, MAX_VOLUME / 2, new ValueHooks() { //$NON-NLS-2$ //$NON-NLS-3$

			@Override
			public String display(int value) {
				return MessageFormat.format("{0}%", value);
			}

			@Override
			public void valueSet(int value) {
				int volume = value * 127 / 100;
				MidiChannel[] channels = synthesizer.getChannels();
				for (MidiChannel channel : channels) {
					channel.controlChange(CHANNEL_VOLUME, volume);
				}
			}

		});
	}

	// Tempo

	private static final int MAX_TEMPO_FACTOR = 200;

	private void addTempo(Composite parent) {
		new NumericValueEditor(parent, "Tempo", Activator.getImageDescriptor(ICON_PATH + "Tempo.png"), Activator.getImageDescriptor(ICON_PATH + "Reset.png"), MAX_TEMPO_FACTOR, MAX_TEMPO_FACTOR / 2, new ValueHooks() { //$NON-NLS-2$ //$NON-NLS-3$

			@Override
			public String display(int value) {
				return MessageFormat.format("{0,number,0.00}x", computeFactor(value));
			}

			@Override
			public void valueSet(int value) {
				sequencer.setTempoFactor(computeFactor(value));
			}

			private float computeFactor(int value) {
				return value / (float)100;
			}

		});
	}

	// Tracks

	// TODO

}
