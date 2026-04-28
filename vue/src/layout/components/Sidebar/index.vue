<template>
  <div>
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="false"
        background-color="#ffffff"
        text-color="#303133"
        :unique-opened="false"
        active-text-color="#409EFF"
        :collapse-transition="false"
        mode="vertical"
        class="sidebar-menu"
      >
        <sidebar-item
          v-for="(route, index) in sidebarRouters"
          :key="route.path  + index"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import SidebarItem from './SidebarItem.vue'
import usePermissionStore from '@/store/modules/permission'
import {useRoute} from "vue-router";

const route = useRoute()
const permissionStore = usePermissionStore()

const sidebarRouters = computed(() => permissionStore.sidebarRouters)

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  background-color: #ffffff;
  transition: all 0.3s ease;

  .scrollbar-wrapper {
    background-color: #ffffff;
  }

  .sidebar-menu {
    border: none;
    height: 100%;
    width: 100% !important;
    padding: 8px 0;

    :deep(.el-menu-item), :deep(.el-sub-menu__title) {
      margin: 2px 10px;
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        background-color: rgba(0, 0, 0, 0.02) !important;
        transform: translateY(-2px);
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
      }

      .svg-icon {
        margin-right: 5px;
        margin-left: -5px;
        font-size: 25px;
        vertical-align: middle;
        transition: all 0.3s ease;
      }

      span {
        font-size: 14px;
        vertical-align: middle;
        transition: all 0.1s ease;
      }
    }

    :deep(.el-menu-item) {
      color: #606266;
      height: 55px;
      line-height: 55px;

      &.is-active {
        background: var(--el-color-primary) !important;
        color: #fff !important;
        position: relative;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        border-radius: 8px;

        &::before {
          content: "";
          position: absolute;
          left: 0;
          top: 0;
          height: 100%;
          width: 4px;
          background-color: #ffffff80;
          border-radius: 0 4px 4px 0;
        }

        &:hover {
          background: var(--el-color-primary) !important;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        }

        .svg-icon {
          transform: scale(1.1);
        }
      }
    }

    :deep(.el-sub-menu__title) {
      color: #606266;
      height: 55px;
      line-height: 55px;

      .svg-icon {
        margin-right: 5px;
        margin-left: -5px;
      }
    }

    :deep(.el-sub-menu) {
      &.is-active {
        > .el-sub-menu__title {
          background-color: rgba(0, 0, 0, 0.02);
          color: var(--el-color-primary) !important;
          font-weight: 500;
          position: relative;
          border-radius: 8px;

          &::before {
            content: "";
            position: absolute;
            left: 0;
            top: 0;
            height: 100%;
            width: 3px;
            background-color: var(--el-color-primary);
            border-radius: 0 2px 2px 0;
          }

          .svg-icon {
            transform: scale(1.1);
          }
        }
      }
    }

    :deep(.el-menu--inline) {
      background-color: #ffffff;

      .el-menu-item {
        margin: 2px 10px 2px 20px;
        height: 55px;
        line-height: 55px;
        padding-left: 40px !important;
        border-radius: 6px;

        &.is-active {
          background: var(--el-color-primary) !important;
          color: #fff !important;
          position: relative;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
          border-radius: 6px;

          &::before {
            content: "";
            position: absolute;
            left: 0;
            top: 0;
            height: 100%;
            width: 4px;
            background-color: #ffffff80;
            border-radius: 0 4px 4px 0;
          }
        }
      }
    }
  }
}

// 添加侧边栏顶部圆角效果
.sidebar-container.has-logo {
  border-top-left-radius: 12px;
}

// 添加侧边栏底部圆角效果
.sidebar-container {
  border-bottom-left-radius: 12px;
}
</style>
