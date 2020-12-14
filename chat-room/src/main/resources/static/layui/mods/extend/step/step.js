/**

 @Name : layui.step 基于layui的步骤条面板
 @Author：hsianglee
 @License：MIT

 */

layui.define(["jquery"], function (exports) {
    var $ = layui.jquery;

    function Step(option) {
        this.option = option;     // 获取传入的数据
        this.elem = option.elem;
        // this.methods=option.methods?option.methods:"";
        this.title = option.title ? option.title : [];
        this.description = option.description ? option.description : [];
        this.canIconClick = option.canIconClick ? option.canIconClick : false;
        this.len = 0;   // 页面个数
        this.currentStep = (option.currentStep && option.currentStep >= 1) ? option.currentStep : 1;    // 当前走到第几步

        this.parameterInit();
        this.domRender();
        this.init();
        this.changeStep();
    }

    Step.prototype = {
        constructor: Step,
        // 初始化参数数据
        parameterInit: function () {
            var self = this;
            this.len = $(".layui-step-content-item").length;   // 页面个数
            // 不传title参数
            if (this.title.length <= 0) {
                $(".layui-step-content-item").each(function (index, val) {
                    self.title.push("第" + (index + 1) + "步");
                })
            }
            if (this.len !== this.title.length) {
                throw "title参数长度与页面长度不匹配";
            }
            // 不传description参数
            if (this.description.length <= 0) {
                $(".layui-step-content-item").each(function (index, val) {
                    self.description.push("");
                })
            }
            if (this.len !== this.description.length) {
                throw "description参数长度与页面长度不匹配";
            }
            // 若当前步超过最大步，则默认为最后一步
            this.currentStep = this.currentStep >= this.len ? this.len : this.currentStep;
        },
        domRender: function () {
            var self = this;
            var titleStr = '<div class="layui-step-title layui-clear">' +
                '<div class="layui-step-title-item step-first" style="width: ' + (100 / this.len) + '%;">' +
                '<div class="step-icon">' +
                '<i>1</i>' +
                '</div>' +
                '<div class="step-text">' +
                this.title[0] +
                '</div>' +
                '<div class="step-description">' +
                this.description[0] +
                '</div>' +
                '</div>';
            for (var i = 1; i < this.title.length - 1; i++) {
                titleStr += '<div class="layui-step-title-item" style="width: ' + (100 / this.len) + '%;">' +
                    '<div class="step-icon">' +
                    '<i>' + (i + 1) + '</i>' +
                    '</div>' +
                    '<div class="step-text">' +
                    this.title[i] +
                    '</div>' +
                    '<div class="step-description">' +
                    this.description[i] +
                    '</div>' +
                    '</div>';
            }

            titleStr += '<div class="layui-step-title-item step-last" style="width: ' + (100 / this.len) + '%;">' +
                '<div class="step-icon">' +
                '<i>' + this.len + '</i>' +
                '</div>' +
                '<div class="step-text">' +
                this.title[this.title.length - 1] +
                '</div>' +
                '<div class="step-description">' +
                this.description[this.title.length - 1] +
                '</div>' +
                '</div>' +
                '</div>'
            $(this.elem).prepend(titleStr);

            // 生成三角
            $(".layui-step-content-item").each(function (index, val) {
                $(this).append("<span class='content-item-before' style='left: calc(" + ((100 / (self.len * 2)) + ((100 * index) / self.len)) + "% - 10px);'></span>");
            })
        },
        // 添加样式
        init: function () {
            $(".layui-step-title-item").eq(this.currentStep - 1).addClass("step-current");
            $(".layui-step-content-item").eq(this.currentStep - 1).show();
            if (this.currentStep < 2) return;
            for (var i = this.currentStep - 2; i >= 0; i--) {
                $(".layui-step-title-item").eq(i).addClass("step-finish");
            }
        },
        // 恢复默认样式
        reInit: function () {
            $(".layui-step-title-item").eq(this.currentStep - 1).removeClass("step-current");
            $(".layui-step-content-item").eq(this.currentStep - 1).hide();
            if (this.currentStep < 2) return;
            for (var i = this.currentStep - 2; i >= 0; i--) {
                $(".layui-step-title-item").eq(i).removeClass("step-finish");
            }
        },
        // 给上面的icon添加事件
        changeStep: function () {
            var self = this;
            this.canIconClick ? (function () {
                $(self.elem).on("click", ".layui-step-title-item .step-icon", function () {
                    var index = $(this).parent(".layui-step-title-item").index() + 1;
                    self.goStep(index);
                })
            })() : "";
        },
        // 跳转第几步
        goStep: function (i) {
            this.reInit();
            this.currentStep = (i < 1 || i > this.len) ? (function () {
                throw "goStep函数参数不在范围内";
            })() : i;
            this.init();
        },
        // 跳到第一步
        goFirst: function () {
            this.goStep(1);
        },
        // 跳到最后一步
        goLast: function () {
            this.goStep(this.len);
        },
        // 跳到上一步
        prev: function () {
            this.reInit();
            this.currentStep = this.currentStep <= 1 ? 1 : this.currentStep - 1;
            this.init();
        },
        // 跳到下一部
        next: function () {
            this.reInit();
            this.currentStep = this.currentStep >= this.len ? this.len : this.currentStep + 1;
            this.init();
        },
    }

    var stepObj;    // new的对象，作为内部变量
    var step = {
        option: "",
        currentStep: 1,
        render: function (option) {
            var self = this;
            this.option = option || {};

            this.option.elem ? "" : (function () {
                throw '缺少参数，需要传入elem元素';
            })();

            !$(this.option.elem)[0] ? (function () {
                throw '没有找到' + self.option.elem + '元素';
            })() : "";

            stepObj = new Step(this.option);
            this.currentStep = stepObj.currentStep;
        },
        goStep: function (i) {
            if (typeof i !== "number") {
                throw 'goStep参数不合法';
            }
            stepObj.goStep(i);
            this.currentStep = stepObj.currentStep;
        },
        goFirst: function () {
            stepObj.goFirst();
            this.currentStep = stepObj.currentStep;
        },
        goLast: function () {
            stepObj.goLast();
            this.currentStep = stepObj.currentStep;
        },
        prev: function () {
            stepObj.prev();
            this.currentStep = stepObj.currentStep;
        },
        next: function () {
            stepObj.next();
            this.currentStep = stepObj.currentStep;
        }
    };

    exports('step', step);
});