document.addEventListener('DOMContentLoaded', function() {
    // 表單提交前驗證
    const loginForm = document.querySelector('form');
    
    if (loginForm) {
        loginForm.addEventListener('submit', function(event) {
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value.trim();
            
            if (!username || !password) {
                event.preventDefault();
                alert('請填寫帳號和密碼');
            }
        });
    }
    
    // 自動隱藏提示訊息
    const messages = document.querySelectorAll('.error-message, .logout-message');
    
    if (messages.length > 0) {
        setTimeout(function() {
            messages.forEach(function(message) {
                message.style.opacity = '0';
                message.style.transition = 'opacity 1s';
                
                setTimeout(function() {
                    message.style.display = 'none';
                }, 1000);
            });
        }, 5000);
    }
});