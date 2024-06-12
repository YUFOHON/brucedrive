import { ElMessageBox } from "element-plus";

const confirm = (message, okFun) => {
    ElMessageBox.confirm(message, 'prompt', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'info'
    }).then(() => {
        okFun();
    }).catch(() => { });
}

export default confirm;