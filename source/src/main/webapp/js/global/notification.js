
function notifyInPage(type, message) {
    Swal.fire({
        toast: true,
        position: 'top-end',
        icon: type, // 'success', 'error', 'warning', 'info', 'question'
        title: message,
        showConfirmButton: false,
        timer: 3000
    });
}

function notifyInline(message, type = 'info', targetQuerySelector, confirm) {
    Swal.fire({
        title: type === 'error' ? 'Error' : 'Info',
        text: message,
        icon: type,
        target: document.querySelector(targetQuerySelector),
        showConfirmButton: true,
        background: 'var(--crb-new-bg)',
        color: 'var(--crb-black-color)'
    });
}

