import { reactive } from 'vue'

const state = reactive({ items: [] })
let nextId = 1

export const useToast = () => {
  const push = (message, type = 'info') => {
    const id = nextId++
    state.items.push({ id, message, type })
    window.setTimeout(() => {
      const index = state.items.findIndex((item) => item.id === id)
      if (index >= 0) state.items.splice(index, 1)
    }, 2600)
  }

  return {
    items: state.items,
    info: (message) => push(message, 'info'),
    success: (message) => push(message, 'success'),
    error: (message) => push(message, 'error'),
  }
}
