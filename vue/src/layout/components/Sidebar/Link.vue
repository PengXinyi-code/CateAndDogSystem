<!-- 用于在应用内部进行页面导航而不重新加载整个页面 -->
<template>
  <!-- 绑定 to 属性到 router-link 组件:to 是 v-bind:to 的简写，用来动态绑定路由地址 -->
  <router-link :to="to">
    <!-- slot 插槽，用于显示包裹在组件标签内的内容 -->
    <slot />
  </router-link>
</template>

<script setup>
// 定义组件接收的属性(props)
// 这个组件只接收一个 to 属性，用于指定要跳转的路由路径
defineProps({
  // to 属性：表示链接的目标地址
  to: {
    // required: true 表示这个属性是必需的
    // 如果父组件没有传递 to 属性，会在控制台报错提示
    required: true
  }
})
</script>
