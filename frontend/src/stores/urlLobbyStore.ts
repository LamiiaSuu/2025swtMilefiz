import { defineStore } from 'pinia'

export const useUrlLobbyStore = defineStore('urlLobby', {
  state: () => ({ urlLobbyId:  undefined as string | undefined }),
  actions: {
    setUrlLobbyId(id: string) {
      this.urlLobbyId = id
    },
    getUrlLobbyId() {
      return this.urlLobbyId
    },
    clear() {
      this.urlLobbyId = undefined
    }
  }
})