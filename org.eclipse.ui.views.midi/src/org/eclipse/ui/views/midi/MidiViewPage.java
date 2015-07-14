package org.eclipse.ui.views.midi;

import java.io.IOException;
import java.text.MessageFormat;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.util.midi.MidiUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class MidiViewPage extends ScrolledComposite {

	public static final String ICON_PATH = "icons/"; //$NON-NLS-1$

	private final Composite content;

	private final Sequencer sequencer;

	private final Synthesizer synthesizer;

	public MidiViewPage(Composite parent, IFile file) throws MidiUnavailableException, InvalidMidiDataException, IOException {
		super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		setExpandHorizontal(true);
		setExpandVertical(true);
		content = new Composite(this, SWT.NONE);
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
		addTracks(content);
		for (Control child : content.getChildren()) {
			GridData layoutData = new GridData();
			layoutData.horizontalAlignment = SWT.FILL;
			layoutData.grabExcessHorizontalSpace = true;
			child.setLayoutData(layoutData);
		}

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
		tracks.setInput(sequencer.getSequence());
		layoutColumns();
		setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	public void reload() throws InvalidMidiDataException, IOException {
		setFile(getFile());
	}

	public void closeFile() {
		pause();
		synthesizer.close();
		sequencer.close();
		content.dispose();
		this.dispose();
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
		if(sequencer.isOpen()){
			sequencer.stop();
			getPlaybackAction().setPlaying(false);
		}
	}

	public boolean isPlaying() {
		return sequencer.isRunning();
	}
	
	public void togglePlayback() {
		if (isPlaying()) {
			pause();
		} else {
			play();
		}
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

	private void addVolume(Composite parent) {
		new NumericValueEditor(parent, "Volume", Activator.getImageDescriptor(ICON_PATH + "Volume.png"), Activator.getImageDescriptor(ICON_PATH + "Reset.png"), MAX_VOLUME, MAX_VOLUME / 2, new ValueHooks() { //$NON-NLS-2$ //$NON-NLS-3$

			@Override
			public String display(int value) {
				return MessageFormat.format("{0}%", value);
			}

			@Override
			public void valueSet(int value) {
				int volume = (value * 127) / 100;
				MidiUtils.setVolume(synthesizer, volume);
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

	private TableViewer tracks;

	private void addTracks(Composite parent) {
		tracks = new TableViewer(parent, SWT.BORDER | SWT.NO_SCROLL);
		for (TrackColumn trackColumn : TrackColumn.values()) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tracks, SWT.NONE);
			tableViewerColumn.setEditingSupport(new TrackEditingSupport(tracks, trackColumn));
			TableColumn tableColumn = tableViewerColumn.getColumn();
			tableColumn.setText(trackColumn.name);
			tableColumn.setImage(Activator.getImageDescriptor(ICON_PATH + trackColumn.iconFilename).createImage());
			tableColumn.setAlignment(SWT.CENTER);
		}
		Table table = tracks.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tracks.setContentProvider(new TrackContentProvider());
		tracks.setLabelProvider(new TrackLabelProvider());
		addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				layoutColumns();
			}

		});
	}

	private void layoutColumns() {
		layout();
		Table table = tracks.getTable();
		int widthOfOtherColumns = 0;
		for (int i = 1; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumn(i);
			column.pack();
			widthOfOtherColumns += column.getWidth();
		}
		table.getColumn(0).setWidth(table.getSize().x - widthOfOtherColumns - table.getBorderWidth() - 2);
	}

	private enum TrackColumn {
		NAME("Track name", "Name.png") { //$NON-NLS-2$

			@Override
			public String getColumnText(Sequencer sequencer, Track track) {
				String result = MidiUtils.getTrackName(track);
				return ((result == null) || result.isEmpty() ? "(untitled)" : result);
			}

			@Override
			public CellEditor getCellEditor(Composite parent) {
				return null;
			}

			@Override
			public Object getValue(Sequencer sequencer, int trackNumber) {
				return null;
			}

			@Override
			public void setValue(Sequencer sequencer, int trackNumber, Object value) {
			}

		},
		MUTE("Mute", "Mute.png") { //$NON-NLS-2$

			@Override
			public String getColumnText(Sequencer sequencer, Track track) {
				return getCheckLabel(sequencer, track);
			}

			@Override
			public CellEditor getCellEditor(Composite parent) {
				return new CheckboxCellEditor(parent);
			}

			@Override
			public Object getValue(Sequencer sequencer, int trackNumber) {
				return sequencer.getTrackMute(trackNumber);
			}

			@Override
			public void setValue(Sequencer sequencer, int trackNumber, Object value) {
				sequencer.setTrackMute(trackNumber, (Boolean)value);
			}

		},
		SOLO("Solo", "Solo.png") { //$NON-NLS-2$

			@Override
			public String getColumnText(Sequencer sequencer, Track track) {
				return getCheckLabel(sequencer, track);
			}

			@Override
			public CellEditor getCellEditor(Composite parent) {
				return new CheckboxCellEditor(parent);
			}

			@Override
			public Object getValue(Sequencer sequencer, int trackNumber) {
				return sequencer.getTrackSolo(trackNumber);
			}

			@Override
			public void setValue(Sequencer sequencer, int trackNumber, Object value) {
				sequencer.setTrackSolo(trackNumber, (Boolean)value);
			}

		};

		private TrackColumn(String name, String iconFilename) {
			this.name = name;
			this.iconFilename = iconFilename;
		}

		private final String name;

		private final String iconFilename;

		public abstract String getColumnText(Sequencer sequencer, Track track);

		public abstract CellEditor getCellEditor(Composite parent);

		public abstract Object getValue(Sequencer sequencer, int trackNumber);

		public abstract void setValue(Sequencer sequencer, int trackNumber, Object value);

		protected String getCheckLabel(Sequencer sequencer, Track track) { // XXX workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=285121
			return (Boolean)getValue(sequencer, MidiUtils.getTrackNumber(sequencer, track)) ? "✓" : "";
		}

	}

	private class TrackContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return ((Sequence)inputElement).getTracks();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	public class TrackLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return TrackColumn.values()[columnIndex].getColumnText(sequencer, (Track)element);
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

	}

	public class TrackEditingSupport extends EditingSupport {

		private final TrackColumn column;

		private final CellEditor cellEditor;

		public TrackEditingSupport(TableViewer viewer, TrackColumn column) {
			super(viewer);
			this.column = column;
			cellEditor = column.getCellEditor(viewer.getTable());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return cellEditor != null;
		}

		@Override
		protected Object getValue(Object element) {
			int trackNumber = MidiUtils.getTrackNumber(sequencer, (Track)element);
			return column.getValue(sequencer, trackNumber);
		}

		@Override
		protected void setValue(Object element, Object value) {
			int trackNumber = MidiUtils.getTrackNumber(sequencer, (Track)element);
			column.setValue(sequencer, trackNumber, value);
			getViewer().update(element, null);
		}

	}

}
