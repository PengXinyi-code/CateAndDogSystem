import { ElMessage, ElMessageBox, ElNotification, ElLoading } from 'element-plus'

let loadingInstance

export default {
  // 消息提示
  msg(content) {
    ElMessage.info(content)
  },
  // 错误消息
  msgError(content) {
    ElMessage.error(content)
  },
  // 成功消息
  msgSuccess(content) {
    ElMessage.success(content)
  },
  // 确认窗体
  confirm(content) {
    return ElMessageBox.confirm(content, "系统提示", {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: "warning",
    })
  },
  // 打开遮罩层
  loading(content) {
    loadingInstance = ElLoading.service({
      lock: true,
      text: content,
      background: "rgba(0, 0, 0, 0.7)",
    })
  },
  // 关闭遮罩层
  closeLoading() {
    loadingInstance.close()
  }
}
