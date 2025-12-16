import { reactive, readonly } from 'vue'

const errorState = reactive({
  show: false,
  message: '',
  type: 'error' as 'error' | 'warning' | 'info',
  autoHide: true,
})

let autoHideTimer: number | null = null

export function useErrorHandler() {
  function showError(
    message: string,
    type: 'error' | 'warning' | 'info' = 'error', //error ist default Wert
    autoHide = true,
  ) {
    // Clear existing timer
    if (autoHideTimer) {
      clearTimeout(autoHideTimer)
      autoHideTimer = null
    }

    errorState.show = true
    errorState.message = message
    errorState.type = type
    errorState.autoHide = autoHide

    if (autoHide) {
      let timeout = 5000 // Default 5 Sekunden
      //Verschiedene Timings je nach Type:
      switch (type) {
        case 'info':
          timeout = 4000 // Success: 4 Sekunden
          break
        case 'warning':
          timeout = 2000 //Warning: 2 Sekunden
          break
        case 'error':
          timeout = 4000 // Error: 4 Sekunden
          break
      }
      autoHideTimer = setTimeout(() => {
        if (errorState.show) hideError()
      }, timeout)
    }
  }

  function hideError() {
    if (autoHideTimer) {
      clearTimeout(autoHideTimer)
      autoHideTimer = null
    }

    errorState.show = false
    errorState.message = ''
  }

  function showSuccess(message: string) {
    showError(message, 'info', true)
  }

  function showWarning(message: string, autoHide = true) {
    showError(message, 'warning', autoHide)
  }

  function showCriticalError(message: string) {
    showError(message, 'error', false)
  }
  return {
    errorState: readonly(errorState),
    showError,
    hideError,
    showSuccess,
    showWarning,
    showCriticalError,
  }
}
