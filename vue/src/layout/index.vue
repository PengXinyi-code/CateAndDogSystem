<template>
    <div :class="classObj" class="app-wrapper" :style="{ '--current-color': '#409EFF' }">
        <div class="logo-header">
            <div class="logo-container">
                <img :src="logo" class="logo-img" alt=""/>
                <span class="logo-title">{{ title }}</span>
            </div>
            <div class="header-navbar">
                <breadcrumb id="breadcrumb-container" class="breadcrumb-container"/>

                <div class="right-menu">
                    <div class="welcome-container right-menu-item">
                        <span>您好, {{ nickName }}</span>
                    </div>
                    <el-button type="text" @click.native="logout" class="right-menu-item logout-btn">
                        <svg-icon icon-class="logout" style="font-size: 15px" class-name='custom-class'/>
                        <el-link type="primary">退出登录</el-link>
                    </el-button>
                    <div class="avatar-container">
                        <el-dropdown @command="handleCommand" class="right-menu-item hover-effect" trigger="click">
                            <div class="avatar-wrapper">
                                <img :src="userStore.avatar" class="user-avatar" alt=""/>
                                <el-icon>
                                    <CaretBottom/>
                                </el-icon>
                            </div>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <router-link to="/user/profile">
                                        <el-dropdown-item>个人中心</el-dropdown-item>
                                    </router-link>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </div>
            </div>
        </div>

        <sidebar class="sidebar-container"/>
        <div class="main-containers">
            <app-main/>
        </div>
    </div>
</template>

<script setup>
import Sidebar from './components/Sidebar/index.vue'
import {AppMain} from './components'
import Breadcrumb from '@/components/Breadcrumb'
import logo from '@/assets/logo/logo.png'
import {ElMessageBox} from 'element-plus'
import useUserStore from '@/store/modules/user'
import {getUser} from "@/api/system/user.js";
import {CaretBottom} from "@element-plus/icons-vue";

const title = import.meta.env.VITE_APP_TITLE
const userStore = useUserStore()
const nickName = ref(null)

function handleCommand(command) {
    switch (command) {
        case "logout":
            logout()
            break
        default:
            break
    }
}

function logout() {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        userStore.logOut().then(() => {
            location.href = '/index'
        })
    }).catch(() => {
    })
}

const device = 'desktop'

const classObj = computed(() => ({
    hideSidebar: false,
    openSidebar: true,
    withoutAnimation: false,
    mobile: device.value === 'mobile'
}))

watch(() => device.value, () => {

})

onMounted(() => {
    getUser(userStore.id).then(res => {
        nickName.value = res.data.nickName
    })
})
</script>

<style lang="scss" scoped>
@import "@/assets/styles/mixin.scss";

.app-wrapper {
  @include clearfix;
  position: relative;
  height: 100vh; // 100vh确保占满整个视口高度
  width: 100%;
  transition: all 0.3s ease;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}

.logo-header {
  height: 50px;
  background: var(--navbar-bg);
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 11; // 确保logo在最上层

  .logo-container {
    display: flex;
    align-items: center;

    .logo-img {
      width: 40px;
      height: 40px;
      vertical-align: middle;
      margin-right: 12px;
      border-radius: 4px;
    }

    .logo-title {
      color: var(--navbar-text);
      font-weight: 600;
      font-size: 25px;
      white-space: nowrap;
    }
  }

  .header-navbar {
    flex: 1;
    height: 100%;
    display: flex;
    align-items: center;
    margin-left: 20px;

    .breadcrumb-container {
      flex: 1;
      height: 100%;
      display: flex;
      align-items: center;
    }

    .topmenu-container {
      flex: 1;
      height: 100%;
      position: static;
      margin-left: 20px;
    }

    .right-menu {
      height: 100%;
      display: flex;
      align-items: center;

      &:focus {
        outline: none;
      }

      .right-menu-item {
        display: flex;
        align-items: center;
        padding: 6px 10px;
        height: 36px;
        font-size: 16px;
        color: var(--navbar-text);
        vertical-align: text-bottom;
        border-radius: 6px;
        margin: 0 2px;
        transition: all 0.3s ease;
        position: relative;

        &.hover-effect {
          cursor: pointer;

          &:hover {
            background: rgba(0, 0, 0, 0.05);
            transform: translateY(-1px);
          }
        }

        &.theme-switch-wrapper {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 36px;
          height: 36px;
          padding: 0;
          border-radius: 50%;

          svg {
            transition: all 0.3s ease;
            font-size: 18px;

            &:hover {
              transform: scale(1.15) rotate(10deg);
            }
          }
        }
      }

      .welcome-container {
        font-size: 14px;
        padding: 0 12px;
        background: linear-gradient(90deg, var(--el-color-primary-light-9), var(--el-color-primary-light-8));
        border-radius: 18px;
        margin-right: 8px;
        font-weight: 500;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
      }

      .logout-btn {
        padding: 6px 12px;
        border-radius: 18px;
        margin: 0 8px;
        background: linear-gradient(90deg, var(--el-color-primary-light-9), var(--el-color-primary-light-8));
        border: 1px solid var(--el-color-primary-light-5);
        transition: all 0.3s ease;

        &:hover {
          background: linear-gradient(90deg, var(--el-color-primary-light-8), var(--el-color-primary-light-7));
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          transform: translateY(-1px);
        }

        :deep(.el-link) {
          font-size: 14px;
          font-weight: 500;
        }
      }

      .avatar-container {
        margin-left: 15px;
        margin-right: 10px;

        .avatar-wrapper {
          display: flex;
          align-items: center;
          position: relative;
          cursor: pointer;
          padding: 2px;

          .user-avatar {
            cursor: pointer;
            width: 36px;
            height: 36px;
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid #fff;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;

            &:hover {
              transform: scale(1.05);
              box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
            }
          }

          i {
            cursor: pointer;
            position: absolute;
            right: -10px;
            top: 50%;
            transform: translateY(-50%);
            font-size: 12px;
            color: #999;
            transition: all 0.3s ease;
          }

          &:hover {
            i {
              color: var(--el-color-primary);
              right: -8px;
            }
          }
        }
      }
    }
  }
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

.main-containers {
  min-height: calc(100vh - 50px); // 减去顶部logo区域的高度
  transition: margin-left .28s;
  margin-left: 240px;
  position: relative;
  top: 50px; // 使用top而不是margin-top来定位，避免增加整体高度
  box-shadow: none;
}

.hideSidebar .main-containers {
  margin-left: 54px;
}

.sidebarHide .main-containers {
  margin-left: 0;
}

.mobile .main-containers {
  margin-left: 0;
  top: 50px;
  box-shadow: none;
}
</style>
