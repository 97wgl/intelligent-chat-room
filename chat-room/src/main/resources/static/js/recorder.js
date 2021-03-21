const recordBtn = document.querySelector(".record-btn");
const player = document.querySelector(".audio-player");

if (navigator.mediaDevices.getUserMedia) {
    var chunks = [];
    const constraints = {audio: true};
    // var recordLayerMsg;
    var recordState = 0; // 1：正常结束  2：取消
    navigator.mediaDevices.getUserMedia(constraints).then(
        stream => {
            console.log("授权成功！");

            const mediaRecorder = new MediaRecorder(stream);

            recordBtn.onclick = () => {
                console.log(mediaRecorder.state);
                if (mediaRecorder.state === "recording") {
                    mediaRecorder.stop();
                    // recordBtn.textContent = "record";
                    // layer.close(recordLayerMsg);
                    // layer.msg("录音结束", {
                    //     time: 500
                    // });
                    console.log("录音结束");
                } else {
                    mediaRecorder.start();
                    //询问框
                    layer.confirm('录音中...', {
                        btn: ['停止录音','取消'] //按钮
                    }, function() {
                        recordState = 1;
                        mediaRecorder.stop();
                        layer.msg('录音结束', {
                            time: 500
                        });
                    }, function() {
                        recordState = 2;
                        mediaRecorder.stop();
                        layer.msg('取消录音', {
                            time: 500
                        });
                    });
                    // console.log("录音中...");
                    // layer.msg("录音中...");
                    // recordLayerMsg = layer.msg('录音中', {
                    //     icon: 16,
                    //     shade: 0.01,
                    //     time: 10000
                    // });
                    // recordBtn.textContent = "stop";
                }
                console.log("录音器状态：", mediaRecorder.state);
            };

            mediaRecorder.ondataavailable = e => {
                chunks.push(e.data);
            };

            mediaRecorder.onstop = e => {
                var blob = new Blob(chunks, {type: "audio/ogg; codecs=opus"});
                chunks = [];
                if (recordState === 1) {
                    player.src = window.URL.createObjectURL(blob);
                    recordState = 0;
                    CHAT.send(blob);
                }
            };
        },
        () => {
            console.error("授权失败！");
        }
    );
} else {
    console.error("浏览器不支持 getUserMedia");
}
