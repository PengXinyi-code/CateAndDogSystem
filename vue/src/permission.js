import router from './router'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import usePermissionStore from '@/store/modules/permission'

const whiteList = ['/login', '/register']

const isWhiteList = (path) => {
  return whiteList.includes(path)
}

router.beforeEach((to, from, next) => {
  if (getToken()) {
    to.meta.title
    /* has token*/
    if (to.path === '/login') {
      next({ path: '/' })
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      if (useUserStore().roles.length === 0) {
        isRelogin.show = true
        // 判断当前用户是否已拉取完user_info信息
        useUserStore().getInfo().then(() => {
          isRelogin.show = false
          usePermissionStore().generateRoutes().then(accessRoutes => {
            // 根据roles权限生成可访问的路由表
            accessRoutes.forEach(route => {
              if (route.path.indexOf('http://') === -1 && route.path.indexOf('https://') === -1) {
                router.addRoute(route) // 动态添加可访问路由表
              }
            })
            next({ ...to, replace: true }) // hack方法 确保addRoutes已完成
          })
        }).catch(err => {
          useUserStore().logOut().then(() => {
            ElMessage.error(err)
            next({ path: '/' })
          })
        })
      } else {
        next()
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else {
      next(`/login`) // 否则全部重定向到登录页
    }
  }
})
