<!DOCTYPE html>
<html>
<head>
    <title>登录页面</title>
    <meta charset="utf-8">
    <#include "../header.ftl"/>
    <link rel="stylesheet" href="/css/login.css" media="all">
</head>
<body>
<img class="video_mask" src="/img/bg.jpg">

<div class="login">
    <h1>欢迎进入课堂</h1>
    <form class="layui-form">
        <div class="layui-form-item">
            <input class="layui-input" name="username" id="username" placeholder="请输入昵称" maxlength="10" value=""
                   type="text"
                   autocomplete="off">
        </div>
        <span class="layui-btn chat_login_btn right" lay-submit="" lay-filter="login">立即进入</span>

    </form>
</div>

<script>
    layui.use(['form', 'layer'], function () {
        var form = layui.form;
        var layer = layui.layer;
        form.on("submit(login)", function (data) {
            if (data.field.username.trim() === "") {
                layer.tips('进入前请先输入昵称', $("#username"), {
                    tips: [3, '#3595CC'],
                    time: 4000
                });
                return;
            }
            window.location.href = "/chat?username=" + data.field.username;
        });

    });

</script>
</body>
</html>