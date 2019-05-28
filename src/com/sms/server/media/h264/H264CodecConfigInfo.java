package com.sms.server.media.h264;

/**
 * H264 Codec Config Info
 * @author pengliren
 *
 */
public class H264CodecConfigInfo {

	public int profileIDC = 0;
	public int levelIDC = 0;
	public int spsID = 0;
	public int chromaFormatIDC = 0;
	public int residualColorTransformFlag = 0;
	public int bitDepthLumaMinus8 = 0;
	public int bitDepthChromaMinus8 = 0;
	public int transformBypass = 0;
	public int scalingMatrixFlag = 0;
	public int log2MaxFrameNum = 0;
	public int pocType = 0;
	public int log2MaxPocLSB = 0;
	public int deltaPicOrderAlwaysZeroFlag = 0;
	public int offsetForNonRefPic = 0;
	public int offsetForTopToBottomField = 0;
	public int pocCycleLength = 0;
	public int[] offsetForRefFrame = null;
	public int refFrameCount = 0;
	public int gapsInFrameNumAllowedFlag = 0;
	public int mbWidth = 0;
	public int mbHeight = 0;
	public int frameMBSOnlyFlag = 0;
	public int mbAFF = 0;
	public int adjWidth = 0;
	public int adjHeight = 0;
	public int direct8x8InferenceFlag = 0;
	public int crop = 0;
	public int cropLeft = 0;
	public int cropRight = 0;
	public int cropTop = 0;
	public int cropBottom = 0;
	public int vuiParametersPresentFlag = 0;
	public int videoSignalTypePresentFlag = 0;
	public int videoFormat = 0;
	public int videoFullRange = 0;
	public long timingNumUnitsInTick = 0L;
	public long timingTimescale = 0L;
	public int timingFixedFrameRateFlag = 0;
	public double frameRate = 0.0D;
	public int sarNum = 0;
	public int sarDen = 0;
	public int aspectRatioIDC = 0;
	public int aspectRatioInfoPresentFlag = 0;
	public int height = 0;
	public int width = 0;
	public int displayHeight = 0;
	public int displayWidth = 0;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{H264CodecConfigInfo: ");
		sb.append("profile: ").append(H264Utils.profileIDCToString(this.profileIDC)).append(", ");
		sb.append("level: ").append(H264Utils.levelIDCToString(this.levelIDC)).append(", ");
		sb.append("frameSize: ").append(this.width).append("x").append(this.height).append(", ");
		sb.append("displaySize: ").append(this.displayWidth).append("x").append(this.displayHeight).append(", ");
		if ((this.sarNum > 0) && (this.sarDen > 0))
			sb.append("PAR: ").append(this.sarNum).append(":").append(this.sarDen).append(", ");
		if ((this.cropLeft > 0) || (this.cropRight > 0) || (this.cropTop > 0) || (this.cropBottom > 0))
			sb.append("crop: l:").append(this.cropLeft).append(" r:")
			.append(this.cropRight).append(" t:").append(this.cropTop).append(" b:")
			.append(this.cropBottom).append(", ");
		if (this.mbAFF > 0)
			sb.append("mbAFF: true, ");
		if (this.frameRate > 0.0D)
			sb.append("frameRate: ").append(this.frameRate);
		sb.append("}");
		return sb.toString();
	}
}
