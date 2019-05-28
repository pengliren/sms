package com.sms.io.flv;


import java.io.Serializable;
import java.util.Arrays;

/**
 * Analyzes key frame data.
 */
public interface IKeyFrameDataAnalyzer {

    /**
     * Analyze and return keyframe metadata.
	 *
     * @return           Metadata object
     */
    public KeyFrameMeta analyzeKeyFrames();

    /**
     * Keyframe metadata.
     */
    public static class KeyFrameMeta implements Serializable {
		private static final long serialVersionUID = 5436632873705625365L;
		/**
		 * Video codec id.
		 */
		public int videoCodecId = -1;

		/**
		 * Audio codec id.
		 */
		public int audioCodecId = -1;
		/**
		 * Duration in milliseconds
		 */
		public long duration;
		/**
		 * Only audio frames?
		 */
		public boolean audioOnly;
        /**
         * Keyframe timestamps
         */
		public int timestamps[];
        /**
         * Keyframe positions
         */
		public long positions[];
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "KeyFrameMeta [videoCodecId=" + videoCodecId + ", audioCodecId=" + audioCodecId + ", duration=" + duration + ", audioOnly=" + audioOnly + ", timestamps="
					+ Arrays.toString(timestamps) + ", positions=" + Arrays.toString(positions) + "]";
		}
	}
}
