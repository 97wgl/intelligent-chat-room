//WebScoket初始化
window.CHAT = {
    // serverAddr: "ws://" + window.location.hostname + ":8002/ws",
    serverAddr: "wss://97wgl.cn/ws",
    socket: null,
    username: '',
    headPic: $t.getHeadPic(),
    //将滚动条设置到最顶部，以便能看到最新的消息
    scrollToBottom: function () {
        $(".cy-chat-main").scrollTop($(".cy-chat-main")[0].scrollHeight);
    },
    init: function (username) {
        var message = $("#sendMessage");
        //自动获取焦点
        message.focus();
        //按回车键自动发送
        message.keydown(function (e) {
            //ctrl+回车 发送消息
            if (event.ctrlKey && event.keyCode == 13) {
                CHAT.sendText();
            }
        });


        if (!window.WebSocket) {
            window.WebSocket = window.MozWebSocket;
        }

        if (window.WebSocket) {

            CHAT.socket = new WebSocket(CHAT.serverAddr);

            //接受消息
            CHAT.socket.onmessage = function (e) {
                //将消息追加到面板上
                CHAT.appendToPanel(e.data);
            };

            CHAT.socket.onopen = function () {
                console.log("WebSocket开启");
                //保存当前用户信息
                CHAT.username = username;
                $("#headPic").attr("src", CHAT.headPic);
                //自定义登录命令格式  [LOGIN][时间戳][username]
                CHAT.send("[LOGIN][" + new Date().getTime() + "][" + username + "]");
            };

            CHAT.socket.onclose = function () {
                layui.use('layer', function () {
                    var layer = layui.layer;
                    layer.msg("WebSocket关闭");
                });
                console.log("WebSocket关闭");
            };
            CHAT.socket.onerror = function () {
                layui.use('layer', function () {
                    var layer = layui.layer;
                    layer.msg("WebSocket异常");
                });
                console.log("WebSocket异常");
            };


        } else {
            layui.use('layer', function () {
                var layer = layui.layer;
                layer.msg("你的浏览器不支持WebSocket协议，清欢谷歌或火狐试试！！");
            });

        }
    },
    //发送消息
    send: function (message) {
        if (!window.WebSocket) {
            return;
        }
        if (CHAT.socket.readyState === WebSocket.OPEN) {
            CHAT.socket.send(message);
        } else {
            console.log("WebSocket连接没有建立成功!");
            layui.use('layer', function () {
                var layer = layui.layer;
                layer.confirm("您还未连接上服务器,请重新登录", function () {
                    window.location.href = '/chat/';
                })
            });

        }
    },
    //将消息添加到聊天面板
    appendToPanel: function (message) {
        var regx = /^\[(.*)\](\s\-\s(.*))?/g;
        var group = '', label = "", dialogContent = "", cmd = "", time = 0, name = "", headPic = "";
        while (group = regx.exec(message)) {
            label = group[1];
            dialogContent = group[3];
        }
        var labelArr = label.split("][");
        cmd = labelArr[0];
        time = labelArr[1];

        name = labelArr[2];
        headPic = labelArr[3];
        //如果是系统消息
        if (cmd === "SYSTEM") {
            var total = labelArr[2];
            //在线人数
            $("#onlineCount").html("" + total);
            //系统消息
            CHAT.addSystemTip(dialogContent);
        }
        //如果是聊天信息
        else if (cmd === "CHAT") {
            var parseContent = dialogContent.replaceAll("<br/>", " ").replaceAll("\n", "").replaceAll("\'", "\\'");
            var date = $t.dateFormat(parseInt(time), "yyyy-MM-dd hh:mm:ss");
            //是否是自己
            var isMe = (name === "MY_SELF");
            var _li = "";
            if (isMe) {
                //如果是自己
                _li = [
                    '<li class="cy-chat-mine">',
                    '<div class="cy-chat-user">',
                    '<img src="' + headPic + '">',
                    '<cite><i>' + date + '</i></cite>',
                    ' </div>',
                    ' <div class="cy-chat-text">',
                    dialogContent,
                    '</div>',
                    '</li>'
                ].join("");
            } else if (name.includes("老师") || name.includes("教师")) {
                _li = [
                    '<li>',
                    '<div class="cy-chat-user">',
                    '<img src="' + headPic + '">',
                    '<cite>' + '<font color="red">' + name + '</font>' + '<i>' + date + '</i></cite>',
                    ' </div>',
                    ' <div class="cy-chat-text">',
                    dialogContent,
                    ' <span class="iconfont  icon-bofang" style="cursor: pointer;" title="播放" onclick="CHAT.playContent(\'' + parseContent  +'\')"></span>',
                    '</div>',
                    '</li>'
                ].join("");
                CHAT.playContent(parseContent.replaceAll("\\", ""), "Annie");
            } else if (name.includes("学伴")) {
                _li = [
                    '<li>',
                    '<div class="cy-chat-user">',
                    '<img src="' + headPic + '">',
                    '<cite>' + '<font color="blue">' + name + '</font>' + '<i>' + date + '</i></cite>',
                    ' </div>',
                    ' <div class="cy-chat-text">',
                    dialogContent,
                    ' <span class="iconfont  icon-bofang" style="cursor: pointer;" title="播放" onclick="CHAT.playContent(\'' + parseContent  +'\')"></span>',
                    '</div>',
                    '</li>'
                ].join("");
                CHAT.playContent(parseContent.replaceAll("\\", ""), "Aiwei");
            } else {
                _li = [
                    '<li>',
                    '<div class="cy-chat-user">',
                    '<img src="' + headPic + '">',
                    '<cite>' + name + '<i>' + date + '</i></cite>',
                    ' </div>',
                    ' <div class="cy-chat-text">',
                    dialogContent,
                    ' <span class="iconfont  icon-bofang" style="cursor: pointer;" title="播放" onclick="CHAT.playContent(\'' + parseContent  +'\')"></span>',
                    '</div>',
                    '</li>'
                ].join("");
                CHAT.playContent(parseContent.replaceAll("\\", ""), "sitong");
            }
            $(".cy-chat-main ul").append(_li);
        }
        //如果是鲜花
        else if (cmd == "FLOWER") {
            CHAT.addSystemTip(dialogContent);
            //鲜花特效
            $(document).snowfall('clear');
            $(document).snowfall({
                image: "/img/face/50.gif",
                flakeCount: 60,
                minSize: 20,
                maxSize: 40
            });
            window.flowerTimer = window.setTimeout(function () {
                $(document).snowfall('clear');
                window.clearTimeout(flowerTimer);
            }, 3000);

        }
        //有新的消息过来以后，自动切到最底部
        CHAT.scrollToBottom();

    },
    //添加系统提示
    addSystemTip: function (dialogContent) {
        var _li = '<li class="cy-chat-system"><span>' + dialogContent + '</span></li>'
        $(".cy-chat-main ul").append(_li);
        CHAT.scrollToBottom();
    },
    //发送聊天板消息
    sendText: function () {
        var message = $("#sendMessage");
        this.sendTextTool(message);
        message.val("");
        message.focus();
    },
    //发送聊天消息
    sendTextTool: function (sendContent) {
        //去掉空格
        if (sendContent.val().replace(/\s/ig, "") === "") {
            return;
        }
        if (!window.WebSocket) {
            return;
        }
        if (CHAT.socket.readyState === WebSocket.OPEN) {
            //自定义消息格式 [CHAT][时间戳][username][头像][消息内容]
            var msg = ("[CHAT][" + new Date().getTime() + "]" + "[" + CHAT.username + "][" + CHAT.headPic + "] - " + sendContent.val().replace(/\n/ig, "<br/>"));
            CHAT.send(msg);
        } else {
            layui.use('layer', function () {
                var layer = layui.layer;
                layer.msg("与服务器连接失败");
            });
        }
    },
    //发送鲜花
    sendFlower: function () {
        if (!window.WebSocket) {
            return;
        }
        if (CHAT.socket.readyState == WebSocket.OPEN) {
            var message = ("[FLOWER][" + new Date().getTime() + "]" + "[" + CHAT.username + "]");
            CHAT.send(message);
            $("#send-Message").focus();
        } else {
            layui.use('layer', function () {
                var layer = layui.layer;
                layer.msg("与服务器连接失败");
            });
        }
    },
    //选择表情
    selectFace: function (img) {

        var faceBox = $("#faceBox");
        faceBox.hide();
        faceBox.removeClass("open");
        var i = '<img src="' + img + '" />';
        $("#sendMessage").val($("#sendMessage").val() + i);
        $("#sendMessage").focus();
    },
    //打开表情弹窗
    openFace: function (e) {
        var faceBox = $("#faceBox");
        if (faceBox.hasClass("open")) {
            faceBox.hide();
            faceBox.removeClass("open");
            return;
        }
        faceBox.addClass("open");
        faceBox.show();
        var box = '';
        for (var i = 1; i <= 130; i++) {
            var img = '/img/face/' + i + '.gif';
            box += '<span class="face-item" onclick="CHAT.selectFace(\'' + img + '\');">';
            box += '<img src="' + img + '"/>';
            box += '</span>';
        }
        faceBox.html(box);
    },
    //弹出文件选择框
    chooseFile: function () {
        $("#fileBtn").click();
    },
    //发送图片
    sendPic: function (e) {
        console.info(e.target.files[0]);//图片文件
        var dom = $("#fileBtn")[0];
        console.info(dom.value);//这个是文件的路径
        console.log(e.target.value);//这个也是文件的路径和上面的dom.value是一样的
        var reader = new FileReader();
        reader.onload = (function (file) {
            return function (e) {
                console.info(this.result); //这个就是base64的数据了
                if (!window.WebSocket) {
                    return;
                }
                if (CHAT.socket.readyState === WebSocket.OPEN) {
                    var _img = "<img src='" + this.result + "'>";
                    // if (_img.length > 65536) {
                    //     layui.use('layer', function () {
                    //         var layer = layui.layer;
                    //         layer.msg("文件过大！");
                    //     });
                    //     return;
                    // }
                    //自定义消息格式 [CHAT][时间戳][username][头像][消息内容]
                    var msg = ("[CHAT][" + new Date().getTime() + "]" + "[" + CHAT.username + "][" + CHAT.headPic + "] - " + _img);
                    CHAT.send(msg);
                } else {
                    layui.use('layer', function () {
                        var layer = layui.layer;
                        layer.msg("与服务器连接失败");
                    });
                }
            };
        })(e.target.files[0]);
        reader.readAsDataURL(e.target.files[0]);

    },
    //退出登录
    logout: function () {
        window.location.href = "/login";
    },
    //播放对话内容
    playContent: function (text, type) {

        // if (sessionStorage.getItem(text)) {
        //     var audio = document.createElement("audio");
        //     audio.controls = true;
        //     audio.style.visibility = 'hidden';
        //     //简单利用URL生成播放地址，注意不用了时需要revokeObjectURL，否则霸占内存
        //     audio.src = sessionStorage.getItem(text);
        //     audio.play();
        //     setTimeout(function () {
        //         (window.URL || webkitURL).revokeObjectURL(audio.src);
        //     }, 5000);
        //     return;
        // }
        // // console.log("hi");
        // // dialogContent = dialogContent.replaceAll("<br\/>", "");
        // console.log(text);
        var settings = {
            "url": "/speech/tts",
            "method": "POST",
            "timeout": 0,
            "headers": {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            "data": {
                "text": text,
                "type": type
            }
        };
        $.ajax(settings).done(function (response) {
            // sessionStorage.setItem(text, response);
            var audio = document.createElement("audio");
            audio.controls = true;
            audio.style.visibility = 'hidden';
            //简单利用URL生成播放地址，注意不用了时需要revokeObjectURL，否则霸占内存
            audio.src = response;
            audio.play();
            setTimeout(function () {
                (window.URL || webkitURL).revokeObjectURL(audio.src);
            }, 5000);
        });
        // var utterThis = new window.SpeechSynthesisUtterance(text);
        // window.speechSynthesis.speak(utterThis);
        // $.get("http://localhost:8001/speech/test", {'name' : 'guilin'}, function (data) {
        //     console.log(data);
        // });
    }
};
