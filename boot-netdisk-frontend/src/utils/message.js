import { ElMessage } from 'element-plus';

const showMessage = (msg, callback, type) => {
    ElMessage({
        type: type,
        message: msg,
        duration: 2000,
        onClose: () => {
            if (callback) {
                callback();
            }
        }
    })
}

const message = {
    success: (msg, callback) => {
        showMessage(msg, callback, 'success');
    },
    error: (msg, callback) => {
        showMessage(msg, callback, 'error');
    },
    warning: (msg, callback) => {
        showMessage(msg, callback, 'warning');
    }
}

export default message;