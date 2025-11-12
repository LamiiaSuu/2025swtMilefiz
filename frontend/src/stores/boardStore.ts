import { defineStore } from 'pinia'
import {ref, computed} from 'vue'
import type { IBoardDTD } from './IBoardDTD'
import type { IFieldDTD } from './IFieldDTD'

export const useBoardStore = defineStore('board', {
    state: () =>({
        board: {
            ok: false,
            fieldList: [] as IFieldDTD[]
        },
    }),
    actions: {
        async getBoard() {
            console.log('Start receiving Gameboard Data...')
            try{
                const resp = await fetch('/api/game/getBoard')
                if(!resp.ok){
                    console.error('Error while recieving Data:\n', resp.statusText)
                    throw new Error(resp.statusText)
                }
                const fieldList = await resp.json() as IFieldDTD[]

                this.board.ok = true
                this.board.fieldList = fieldList

                console.log('GameBoard successfully loaded')
            }catch(reason){
                console.log(reason)
                this.board.ok = false
                this.board.fieldList = []
            }
        }
    }
})
 