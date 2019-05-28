package com.sms.server.stream;

import java.util.Random;

import com.sms.server.api.stream.IPlaylist;
import com.sms.server.api.stream.IPlaylistController;

/**
 * Simple playlist controller implementation
 */
public class SimplePlaylistController implements IPlaylistController {
	
	/** {@inheritDoc} */
    public int nextItem(IPlaylist playlist, int itemIndex) {
		if (itemIndex < 0) {
			itemIndex = -1;
		}
		
		if (playlist.isRepeat()) {
			return itemIndex;
		}
		
		if (playlist.isRandom()) {
			int lastIndex = itemIndex;
			if (playlist.getItemSize() > 1) {
				// continuously generate a random number
				// until you get one that was not the last...
				Random rand = new Random();
				while (itemIndex == lastIndex) {
					itemIndex = rand.nextInt(playlist.getItemSize());
				}
			}
			return itemIndex;
		}
		
		int nextIndex = itemIndex + 1;
		
		if (nextIndex < playlist.getItemSize()) {
			return nextIndex;
		} else if (playlist.isRewind()) {
			return playlist.getItemSize() > 0 ? 0 : -1;
		} else {
			return -1;
		}
	}

	/** {@inheritDoc} */
    public int previousItem(IPlaylist playlist, int itemIndex) {
    	
		if (itemIndex > playlist.getItemSize()) {
			return playlist.getItemSize() - 1;
		}
		
		if (playlist.isRepeat()) {
			return itemIndex;
		}

		if (playlist.isRandom()) {
			Random rand = new Random();
			int lastIndex = itemIndex;
			// continuously generate a random number
			// until you get one that was not the last...
			while (itemIndex == lastIndex) {
				itemIndex = rand.nextInt(playlist.getItemSize());
			}
			lastIndex = itemIndex;
			return itemIndex;
		}
		
		int prevIndex = itemIndex - 1;
		
		if (prevIndex >= 0) {
			return prevIndex;
		} else if (playlist.isRewind()) {
			return playlist.getItemSize() - 1;
		} else {
			return -1;
		}
	}

}