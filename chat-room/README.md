# 对话教学系统


## 软件环境 
- SpringBoot 2.0 +
- JDK 1.8
- MySQL 5.7
- Netty
- 阿里语音合成sdk
- IBM Watson sdk

## 项目结构 
```
│  **chat-room**│  
└─com.hust  
│  AdminApplication.java  （服务启动）
│  Generate.java  （mybatis-plus代码自动生成）
│    
├─common  
│      ClassOneData.java  （课堂对话数据，后期应改成读取数据库）
│      SessionResponseCounterPair.java  （对话逻辑控制基本数据结构）
│        
├─config  
│      AlibabaSpeechConfig.java  （阿里语音服务配置，可忽略）
│      ClassFirstConfig.java  （watson配置，后期应读取数据库动态获取）
│      HttpsConfig.java  （https请求转发配置）
│      WatsonConfig.java  （watson配置，可忽略）
│      WebConfiguration.java  （静态资源映射配置）
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
│  │      MsgProcessor.java  （消息接收和处理，包括学伴任务）
│  │        
│  ├─protocol  
│  │      IMDecoder.java  （消息解码）
│  │      IMEncoder.java  （消息编码）
│  │      IMMessage.java  （消息格式）
│  │      IMP.java  （消息类型）
│  │        
│  └─server  
│      │  ChatServer.java  （Netty启动）
│      │  NettyListener.java  （Netty监听器）
│      │    
│      └─handler  
│              ImIdleHandler.java  （心跳检测）
│              WebSocketHandler.java  （服务端websocket处理） 
│                
├─service  
│  │  ApiConfigService.java  （阿里语音配置）
│  │  DialogLibraryService.java  
│  │  SpeechRecognizerRestfulService.java （阿里语音识别） 
│  │  SpeechSynthesizerRestfulService.java （阿里语音合成）  
│  │  SpeechTokenService.java  （阿里语音获取token）
│  │  WatsonService.java  （Watson调用）
│  │    
│  └─impl  
│       ApiConfigServiceImpl.java  
│       DialogLibraryServiceImpl.java  
│            
└─util  
    HttpUtil.java  
    SpringContextUtil.java  
```
                
