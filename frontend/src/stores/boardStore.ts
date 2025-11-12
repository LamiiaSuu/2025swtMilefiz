import { defineStore } from 'pinia'
import {ref, computed} from 'vue'
import type { IBoardDTD } from './IBoardDTD'
import type { IFieldDTD } from './IFieldDTD'

export const useBoardStore = defineStore('board', () => {
    const board = ref<IBoardDTD>({fields: []})
})
 