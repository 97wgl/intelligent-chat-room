# 对话教学系统


## 软件环境 
- SpringBoot 2.0 +
- JDK 1.8
- MySQL 5.7
- Netty
- 阿里语音合成sdk
- IBM Watson sdk

## 项目结构 
│  **chat-room**│  
└─com.hust
│  AdminApplication.java
│  Generate.java
│  
├─common
│      ClassOneData.java
│      SessionResponseCounterPair.java
│      
├─config
│      AlibabaSpeechConfig.java
│      ClassFirstConfig.java
│      HttpsConfig.java
│      WatsonConfig.java
│      WebConfiguration.java
│      
├─controller
│      ApiConfigController.java
│      DialogLibraryController.java
│      IndexController.java
│      SpeechController.java
│      
├─entity
│      ApiConfig.java
│      ASRResponse.java
│      DialogLibrary.java
│      
├─mapper
│      ApiConfigMapper.java
│      DialogLibraryMapper.java
│      
├─netty
│  ├─process
│  │      MsgProcessor.java
│  │      
│  ├─protocol
│  │      IMDecoder.java
│  │      IMEncoder.java
│  │      IMMessage.java
│  │      IMP.java
│  │      
│  └─server
│      │  ChatServer.java
│      │  NettyListener.java
│      │  
│      └─handler
│              ImIdleHandler.java
│              WebSocketHandler.java
│              
├─service
│  │  ApiConfigService.java
│  │  DialogLibraryService.java
│  │  SpeechRecognizerRestfulService.java
│  │  SpeechSynthesizerRestfulService.java
│  │  SpeechTokenService.java
│  │  WatsonService.java
│  │  
│  └─impl
│          ApiConfigServiceImpl.java
│          DialogLibraryServiceImpl.java
│          
└─util
└─comonent
HttpUtil.java
SpringContextUtil.java
                
