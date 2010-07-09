package javax.util.midi;

import java.util.Arrays;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

public class MidiUtils {

	private MidiUtils() {
	}

	public static final int CHANNEL_VOLUME = 7;

	public static void setVolume(Synthesizer synthesizer, int volume) {
		MidiChannel[] channels = synthesizer.getChannels();
		for (MidiChannel channel : channels) {
			channel.controlChange(CHANNEL_VOLUME, volume);
		}
	}

	public static int getTrackNumber(Sequencer sequencer, Track track) {
		return Arrays.asList(sequencer.getSequence().getTracks()).indexOf(track);
	}

	public static final int TRACK_NAME = 3;

	public static String getTrackName(Track track) {
		for (int i = 0; i < track.size(); i++) {
			MidiEvent event = track.get(i);
			MidiMessage message = event.getMessage();
			if (message instanceof MetaMessage) {
				MetaMessage metaMessage = (MetaMessage)message;
				if (metaMessage.getType() == TRACK_NAME) {
					return new String(metaMessage.getData());
				}
			}
		}
		return null;
	}

}
