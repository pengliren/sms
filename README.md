# SMS is a streaming media server based on java development.

### Features
* RTMP/RTSP/RTP/HLS live streaming, 
  the RTMP protocol is based on red5 porting, thanks to the red5 project

* RTMP Video on demand FLV/MP4,
  playing from local filesystem or HTTP

* Stream relay support for distributed
  streaming: push & pull models

* Recording streams in multiple FLVs

* H264/AAC support

### Build
* Import the project into eclipse and execute the build with ant
* Start the service with startup

### Example
* Create a streams file in webapps\vod, add a flv file or mp4 file，
* Use vlc for network streaming，
  rtmp://127.0.0.1/vod/file.flv,
  http://127.0.0.1/vod/flv/file.flv, 
  http://127.0.0.1/vod/file.flv/playlist.m3u8,
  rtsp://127.0.0.1/vod/file.flv
  rtp://xxx
  
* Live stream is the same as above
  
  ### Warning
  * This project is mainly used for learning. If you have any questions, please send an email to 344867279@qq.com
