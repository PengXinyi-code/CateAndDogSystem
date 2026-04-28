<template>
  <section class="app-main">
    <router-view v-slot="{ Component, route }">
      <el-card class="app-content-card">
        <transition name="fade-transform" mode="out-in">
          <div>
            <component v-if="!route.meta.link" :is="Component" :key="route.path"/>
          </div>
        </transition>
      </el-card>
    </router-view>
  </section>
</template>

<script setup>
import {useRoute} from "vue-router";

const route = useRoute()
</script>

<style lang="scss" scoped>
.app-main {
  /* 50= navbar  50  */
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: hidden;
  background-color: #f5f7f9;
  transition: all 0.3s ease;
  padding: 16px;
}

.fixed-header + .app-main {
  padding-top: 50px;
}

// 主内容区域圆角和阴影
.app-main {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

// 内容卡片样式优化
.app-content-card {
  min-height: calc(100vh - 85px);
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  :deep(.el-card__body) {
    padding: 20px;
    height: 100%;
  }


}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 6px;
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background-color: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background-color: #c0c0c0;
  border-radius: 3px;
  transition: all 0.3s ease;

  &:hover {
    background-color: #a0a0a0;
  }
}
</style>