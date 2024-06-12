export default {
    size2Str: (fileSize) => {
        var size = "";
        if (fileSize < 0.1 * 1024) {
            size = fileSize.toFixed(2) + "B";
        } else if (fileSize < 0.1 * 1024 * 1024) {
            size = (fileSize / 1024).toFixed(2) + "KB";
        } else if (fileSize < 0.1 * 1024 * 1024 * 1024) {
            size = (fileSize / (1024 * 1024)).toFixed(2) + "MB";
        } else {
            size = (fileSize / (1024 * 1024 * 1024)).toFixed(2) + "GB";
        }

        var sizeStr = size + "";
        // 如果小数点后两位是00则去除
        var index = sizeStr.indexOf(".");
        var point = sizeStr.substr(index + 1, 2);
        if (point == "00") {
            return sizeStr.substring(0, index) + sizeStr.substr(index + 3, 2)
        }
        return sizeStr;
    },
};
