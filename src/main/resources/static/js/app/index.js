var main = {
    init : function () {
        var _this = this;
        $('#btn-sendMessage').on('click', function () {
            _this.sendMessage();
        });

        $('#btn-save').on('click', function () {
            _this.save();
        });
    },

    sendMessage : function() {
        var data = {
            email: $('#email').val()
        }
        $("#email").attr("readonly",true);
        alert('메일 전송을 시작합니다.');

        $.ajax({
            type: 'POST',
            url: '/api/email/authentication',
            dataType: 'text',
            contentType:'application/json;',
            data: JSON.stringify(data)
        }).done(function() {
            alert("메일 전송이 완료되었습니다.");
        }).fail(function (error) {
            $("#email").attr("readonly",false);
            alert(JSON.stringify(error));
        });
    },

    save : function () {
        var data = {
            name: $('#name').val(),
            email: $('#email').val(),
            nickname: $('#nickname').val(),
            password: $('#password').val(),
            profileImage: $('#profileImage').val(),
            authenticationNumber: $('#authenticationNumber').val()
        };

        $.ajax({
            type: 'POST',
            url: '/api/users',
            dataType: 'json',
            contentType:'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function() {
            alert('회원가입이 완료되었습니다.');
            window.location.href = '/';
        }).fail(function (request) {
            alert(request.responseText);
        });
    }
};

main.init();
