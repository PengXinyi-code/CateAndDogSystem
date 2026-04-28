import { createWebHistory, createRouter } from 'vue-router'
import Layout from '@/layout'

// 公共路由
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: 'index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: '首页', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: () => import('@/views/userPage/index'),
    redirect: '/user/home',
    hidden: true,
    children: [
      {
        path: 'home',
        component: () => import('@/views/userPage/home'),
        meta: { title: '首页', icon: 'home' }
      },
      {
        path: 'animal',
        component: () => import('@/views/userPage/animal'),
        meta: { title: '动物档案', icon: 'user' }
      },
      {
        path: 'self',
        component: () => import('@/views/system/user/profile/index'),
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

export default router
