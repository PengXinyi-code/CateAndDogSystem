import axios from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import { blobValidate } from '@/utils/huacai.js'

// 添加原生下载函数
function nativeSaveAs(blob, filename) {
  // 创建隐藏的下载链接
  const link = document.createElement('a')
  link.style.display = 'none'
  document.body.appendChild(link)
  
  // 创建对象URL
  const url = window.URL.createObjectURL(blob)
  link.href = url
  link.download = filename
  
  // 触发下载
  link.click()
  
  // 清理
  window.URL.revokeObjectURL(url)
  document.body.removeChild(link)
}

const baseURL = import.meta.env.VITE_APP_BASE_API

export default {
  name(name, isDelete = true) {
    var url = baseURL + "/common/download?fileName=" + encodeURIComponent(name) + "&delete=" + isDelete
    axios({
      method: 'get',
      url: url,
      responseType: 'blob',
      headers: { 'Authorization': 'Bearer ' + getToken() }
    }).then((res) => {
      const isBlob = blobValidate(res.data)
      if (isBlob) {
        const blob = new Blob([res.data])
        this.saveAs(blob, decodeURIComponent(res.headers['download-filename']))
      } else {
        this.printErrMsg(res.data)
      }
    })
  },
  saveAs(text, name, opts) {
    nativeSaveAs(text, name, opts)
  },
  async printErrMsg(data) {
    const resText = await data.text()
    const rspObj = JSON.parse(resText)
    const errMsg = rspObj.msg || '下载失败'
    ElMessage.error(errMsg)
  }
}