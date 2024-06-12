<template>
  <div class="uploader-panel">
    <div class="uploader-title">
      <span>Uploading（{{ finishTaskCount }}/{{ fileList.length }}）</span>
    </div>
    <div class="file-list">
      <div v-if="fileList.length > 0">
        <div v-for="(item, index) in fileList" class="file-item">
          <div class="upload-panel">
            <!-- File name -->
            <div class="file-name">{{ item.fileName }}</div>
            <!-- Upload progress bar -->
            <div class="progress">
              <el-progress
                  :percentage="item.uploadProcess"
                  :show-text="false"
                  v-if="
item.status == STATUS.uploading.value ||
item.status == STATUS.upload_seconds.value ||
item.status == STATUS.upload_finish.value
"
              >
              </el-progress>
            </div>
            <!-- Upload status -->
            <div class="upload-status">
<span
    :class="['iconfont', 'icon-' + STATUS[item.status].icon]"
    :style="{ color: STATUS[item.status].color }"
></span>
              <span
                  class="status"
                  :style="{ color: STATUS[item.status].color }"
              >{{
                  item.status == "fail"
                      ? item.errorMsg
                      : STATUS[item.status].desc
                }}</span
              >
              <!-- Upload size -->
              <span
                  class="upload-info"
                  v-if="item.status == STATUS.uploading.value"
              >
{{ proxy.Utils.size2Str(item.uploadSize) }}/{{
                  proxy.Utils.size2Str(item.totalSize)
                }}
</span>

              <!-- Upload speed -->
              <span class="speed" v-if="item.status == STATUS.uploading.value">
{{ proxy.Utils.size2Str(item.speed) + "/s" }}
</span>
            </div>
          </div>
          <!-- Operation -->
          <div class="op">
            <!-- md5 parsing progress -->
            <el-progress
                type="circle"
                :width="50"
                :percentage="item.md5Progress"
                v-if="item.status == STATUS.init.value"
            ></el-progress>
            <!-- Operation button -->
            <div class="op-btn">
<span v-if="item.status == STATUS.uploading.value">
<Icon
    :width="28"
    class="btn-item"
    iconName="upload"
    title="Upload"
    @click="startUpload(item.uid)"
    v-if="item.pause"
></Icon>
<Icon
    :width="28"
    class="btn-item"
    iconName="pause"
    title="Pause"
    @click="pauseUpload(item.uid)"
    v-else
></Icon>
</span>
              <Icon
                  :width="28"
                  class="del btn-item"
                  iconName="del"
                  title="Delete"
                  @click="delUpload(item.uid, index)"
                  v-if="
item.status != STATUS.init.value &&
item.status != STATUS.upload_finish.value &&
item.status != STATUS.upload_seconds.value
"
              ></Icon>
              <Icon
                  :width="28"
                  class="clean btn-item"
                  iconName="clean"
                  title="Clear"
                  @click="delUpload(item.uid, index)"
                  v-if="
item.status == STATUS.upload_finish.value ||
item.status == STATUS.upload_seconds.value
"
              ></Icon>
            </div>
          </div>
        </div>
        <div class="tips">- Only show this upload task -</div>
      </div>
      <div v-else>
        <NoData msg="No upload task yet"></NoData>
      </div>
    </div>
  </div>
</template>
<script setup>
import {ref, getCurrentInstance, computed} from "vue";
import SparkMD5 from "spark-md5";

const {proxy} = getCurrentInstance();

const api = {
  uploadFile: "/file/uploadFile",
};

// 上传文件列表
const fileList = ref([]);
// 上传删除列表
const delList = ref([]);

// Upload status
const STATUS = {
  empty: {
    value: "empty",
    desc: "File is empty",
    color: "#f75000",
    icon: "close",
  },
  fail: {
    value: "fail",
    desc: "Upload failed",
    color: "#f75000",
    icon: "close",
  },
  init: {
    value: "init",
    desc: "Parsing",
    color: "#e6a23c",
    icon: "clock",
  },
  uploading: {
    value: "uploading",
    desc: "Uploading",
    color: "#409eff",
    icon: "upload",
  },
  upload_finish: {
    value: "upload_finish",
    desc: "Upload completed",
    color: "#67c23a",
    icon: "ok",
  },
  upload_seconds: {
    value: "upload_seconds",
    desc: "Upload in seconds",
    color: "#67c23a",
    icon: "ok",
  },
};
// 文件分片大小
const chunkSize = 1024 * 1024 * 5;

// 添加到文件列表
const addFile = async (file, filePid) => {
  // 文件信息：文件名、文件大小、文件流...
  const fileItem = {
    file: file,
    uid: file.uid, // 文件UID
    md5: null, // 文件md5值
    md5Progress: 0, // md5进度
    fileName: file.name, // 文件名
    status: STATUS.init.value, // 上传状态
    uploadSize: 0, // 已上传大小
    totalSize: file.size, // 文件总大小
    uploadProcess: 0, // 上传进度
    pause: false, // 暂停
    chunkIndex: 0, // 当前分片
    filePid: filePid, // 文件父级ID
    time: 0, // 当前分片上传时间
    speed: 0, // 上传速度
    errorMsg: null, // 错误信息
  };

  fileList.value.unshift(fileItem);

  if (fileItem.totalSize == 0) {
    fileItem.status = STATUS.empty.value;
    return;
  }

  let fileMd5Uid = await computMd5(fileItem);
  if (fileMd5Uid == null) {
    return;
  }

  uploadFile(fileMd5Uid);
};

defineExpose({addFile});

// 计算文件md5
const computMd5 = async (fileItem) => {
  let file = fileItem.file;
  let blobSlice =
      File.prototype.slice ||
      File.prototype.mozSlice ||
      File.prototype.webkitSlice;
  // 分片数量
  let chunks = Math.ceil(file.size / chunkSize);
  let currentChunk = 0;

  let spark = new SparkMD5.ArrayBuffer();
  let fileReader = new FileReader();

  // 分片读取文件
  let loadNext = () => {
    let start = currentChunk * chunkSize;
    let end = start + chunkSize >= file.size ? file.size : start + chunkSize;
    fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
  };

  loadNext();

  // 异步处理
  try {
    return await new Promise((resolve, reject) => {
      let resultFile = getFileByUid(file.uid);
      fileReader.onload = (e) => {
        // 读取文件
        spark.append(e.target.result);
        currentChunk++;

        // 不是最后一个分片
        if (currentChunk < chunks) {
          // 计算md5解析进度
          let percent = Math.floor((currentChunk / chunks) * 100);
          resultFile.md5Progress = percent;

          // 读取下一个分片
          loadNext();
        } else {
          // 最后一个分片，返回md5值
          let md5 = spark.end();
          spark.destroy(); //释放内存

          resultFile.md5Progress = 100;
          resultFile.status = STATUS.uploading.value;
          resultFile.md5 = md5;
          resolve(fileItem.uid);
        }
      };

      fileReader.onerror = () => {
        // 异常处理
        resultFile.md5Progress = -1;
        resultFile.status = STATUS.fail.value;
        resolve(fileItem.uid);
      };
    });
  } catch (error) {
    console.error(error);
    return null;
  }
};

// 根据uid获取文件
const getFileByUid = (uid) => {
  let file = fileList.value.find((item) => {
    return item.file.uid === uid;
  });
  return file;
};

const emit = defineEmits(["uploadCallback"]);

// 执行文件上传
const uploadFile = async (uid, chunkIndex) => {
  chunkIndex = chunkIndex ? chunkIndex : 0;

  let currentFile = getFileByUid(uid);
  const file = currentFile.file;
  const fileSize = currentFile.totalSize;
  // 分片数量
  const chunks = Math.ceil(file.size / chunkSize);

  currentFile.time = new Date().getTime();

  // 分片上传
  for (let i = chunkIndex; i < chunks; i++) {
    // 在删除列表中，不进行上传
    let delIndex = delList.value.indexOf(uid);
    if (delIndex != -1) {
      delList.value.splice(delIndex, 1);
      break;
    }

    currentFile = getFileByUid(uid);
    // 暂停上传
    if (currentFile.pause) {
      break;
    }

    // 截取分片
    let start = i * chunkSize;
    let end = start + chunkSize >= fileSize ? fileSize : start + chunkSize;
    let chunckFile = file.slice(start, end);

    // 进行上传
    let updateResult = await proxy.Request({
      url: api.uploadFile,
      showLoading: false,
      dataType: "file",
      params: {
        file: chunckFile,
        fileName: file.name,
        fileMd5: currentFile.md5,
        chunkIndex: i,
        chunks: chunks,
        fileId: currentFile.fileId,
        filePid: currentFile.filePid,
      },
      showError: false,
      errorCallback: (error) => {
        // 异常回调
        currentFile.status = STATUS.fail.value;
        currentFile.errorMsg = error.msg;
      },
      uploadProgressCallback: (e) => {
        // 上传进度回调
        let loaded = e.loaded;
        if (loaded > fileSize) {
          loaded = fileSize;
        }

        // 上传进度
        currentFile.uploadSize = i * chunkSize + loaded;
        currentFile.uploadProcess = Math.floor(
            (currentFile.uploadSize / fileSize) * 100
        );

        // 上传速度
        let nowTime = new Date().getTime();
        let intervalTime = (nowTime - currentFile.time) / 1000;
        let intervalSize = currentFile.uploadSize;
        currentFile.speed = intervalSize / intervalTime;

        console.log(currentFile.time + "," + nowTime);
        console.log(intervalTime + "," + intervalSize);
      },
    });

    if (updateResult == null) {
      break;
    }

    currentFile.fileId = updateResult.data.fileId;
    let status = updateResult.data.status;
    currentFile.status = STATUS[status].value;
    currentFile.chunkIndex = i;
    // 全部分片上传完成
    if (
        status == STATUS.upload_seconds.value ||
        status == STATUS.upload_finish.value
    ) {
      currentFile.uploadProcess = 100;

      emit("uploadCallback"); // 上传完成回调
      break;
    }
  }
};

// 开始上传
const startUpload = (uid) => {
  let currentFile = getFileByUid(uid);
  currentFile.pause = false;
  uploadFile(uid, currentFile.chunkIndex + 1);
};

// 暂停上传
const pauseUpload = (uid) => {
  let currentFile = getFileByUid(uid);
  currentFile.pause = true;
};

// 删除或清除上传
const delUpload = (uid, index) => {
  let currentFile = getFileByUid(uid);
  if (
      currentFile.status != STATUS.init.value &&
      currentFile.status != STATUS.upload_finish.value &&
      currentFile.status != STATUS.upload_seconds.value
  ) {
    delList.value.push(uid);
  }

  fileList.value.splice(index, 1);
};

const finishTaskCount = computed(() => {
  let finishTaskCount = 0;
  fileList.value.forEach((item) => {
    if (
        item.status == STATUS.upload_finish.value ||
        item.status == STATUS.upload_seconds.value
    ) {
      finishTaskCount++;
    }
  });

  return finishTaskCount;
});
</script>
<style lang="scss" scoped>
.uploader-panel {
  .uploader-title {
    border-bottom: 1px solid #ddd;
    line-height: 40px;
    padding: 0 10px;
    font-size: 15px;
    font-weight: bold;
  }

  .file-list {
    overflow: auto;
    padding: 10px 0;
    min-height: calc(100vh / 2);
    max-height: calc(100vh - 120px);

    .tips {
      font-size: 13px;
      color: rgb(169, 169, 169);
      margin-top: 20px;
      text-align: center;
    }

    .file-item {
      position: relative;
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 3px 10px;
      background-color: #fff;
      border-bottom: 1px solid #ddd;
    }

    .file-item:nth-child(even) {
      background-color: #fcf8f4;
    }

    .upload-panel {
      flex: 1;

      .file-name {
        color: rgb(64, 62, 62);
      }

      .upload-status {
        display: flex;
        align-items: center;
        margin-top: 5px;

        .iconfont {
          margin-right: 3px;
        }

        .status {
          color: red;
          font-size: 13px;
        }

        .upload-info {
          margin-left: 5px;
          font-size: 12px;
          color: rgb(112, 111, 111);
        }

        .speed {
          flex: 1;
          text-align: right;
          color: #409eff;
        }
      }

      .progress {
        height: 10px;
      }
    }

    .op {
      width: 100px;
      display: flex;
      align-items: center;
      justify-content: flex-end;

      .op-btn {
        .btn-item {
          cursor: pointer;
        }

        .del,
        .clean {
          margin-left: 5px;
        }
      }
    }
  }
}
</style>
