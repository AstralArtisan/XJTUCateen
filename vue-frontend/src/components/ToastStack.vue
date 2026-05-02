<script setup>
import { useToast } from '../composables/toast'

const toast = useToast()
</script>

<template>
  <div class="toast-stack" aria-live="polite" aria-atomic="true">
    <div v-for="item in toast.items" :key="item.id" class="toast" :class="item.type">
      {{ item.message }}
    </div>
  </div>
</template>
