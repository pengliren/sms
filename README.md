# SMS - Streaming Media Server

SMS is a streaming media server based on Java development, providing comprehensive multimedia streaming capabilities.

## Features

* **RTMP/RTSP/RTP/HLS live streaming** - The RTMP protocol is based on Red5 porting (thanks to the Red5 project)
* **RTMP Video on Demand** - Support for FLV/MP4 files, playing from local filesystem or HTTP
* **Stream relay support** - Distributed streaming with push & pull models
* **Recording capabilities** - Record streams in multiple FLV formats
* **Codec support** - H264/AAC support

## Build Instructions

1. Import the project into Eclipse IDE
2. Execute the build using Ant with the provided [`build.xml`](build.xml)
3. Start the service using the startup scripts:
   - Linux/Mac: [`startup.sh`](startup.sh)
   - Windows: [`startup.bat`](startup.bat)

## Usage Examples

### Video on Demand Setup

1. Create a streams file in the [`webapps/vod`](webapps/vod) directory
2. Add your FLV or MP4 files to this directory

### Streaming URLs

Use VLC or any compatible media player with these URL formats:

```
rtmp://127.0.0.1/vod/file.flv
http://127.0.0.1/vod/flv/file.flv
http://127.0.0.1/vod/file.flv/playlist.m3u8
rtsp://127.0.0.1/vod/file.flv
```

### Live Streaming

Live streaming follows the same URL patterns as Video on Demand.

## Important Notice

⚠️ **This project is primarily intended for educational and learning purposes.** 

For questions or support, please contact: 344867279@qq.com
