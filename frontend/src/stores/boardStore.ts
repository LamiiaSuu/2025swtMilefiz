import { defineStore } from 'pinia'
import type { IBoardDTD } from './IBoardDTD'
import { ref } from 'vue'
import { useMilefizStore } from './milefizstore'
import type { Player } from '../types/lobbyupdate'
import type { Lobby } from '../types/lobbyupdate'
import type { IFieldDTD } from './IFieldDTD'
import type { ITreeDTD } from './ITreeDTD'

const gameBoardTiles = ref<IFieldDTD[]>()
const gameTrees = ref<ITreeDTD[]>()
// Pending update buffers (non-reactive) to batch frequent updates
const _pendingPositionUpdates: Record<string, string> = {}
const _pendingRotationUpdates: Record<string, number> = {}
let _flushScheduled = false
let _lastFlushAt = 0
const _FLUSH_THROTTLE_MS = 33 // ~30 FPS maximum for store flushes

function scheduleFlush(store: any) {
  if (_flushScheduled) return
  const now = (typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now()
  const elapsed = now - _lastFlushAt

  const doFlush = () => {
    _flushScheduled = false
    _lastFlushAt = (typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now()

    // Apply pending positions in a single reactive burst
    const positionKeys = Object.keys(_pendingPositionUpdates)
    for (const id of positionKeys) {
      const newField = _pendingPositionUpdates[id]
      const prev = store.meeplePositions[id] ?? null
      if (prev === newField) continue
      if (prev) store.lastFields[id] = prev
      store.meeplePositions[id] = newField
    }
    // clear applied positions
    for (const k of positionKeys) delete _pendingPositionUpdates[k]

    // Apply pending rotations
    const rotKeys = Object.keys(_pendingRotationUpdates)
    for (const id of rotKeys) {
      const newRot = _pendingRotationUpdates[id]
      if (newRot === undefined) continue
      const prev = store.meepleRotations[id]
      if (prev !== undefined && Math.abs(prev - newRot) < 1e-4) continue
      store.meepleRotations[id] = newRot
    }
    for (const k of rotKeys) delete _pendingRotationUpdates[k]
  }

  // If enough time has passed since last flush, run on next animation frame.
  if (elapsed >= _FLUSH_THROTTLE_MS) {
    _flushScheduled = true
    requestAnimationFrame(doFlush)
    return
  }

  // Otherwise schedule a delayed flush to hit the throttle boundary.
  _flushScheduled = true
  const delay = Math.max(1, Math.ceil(_FLUSH_THROTTLE_MS - elapsed))
  setTimeout(() => {
    // ensure we flush on an animation frame for smoother visuals
    requestAnimationFrame(doFlush)
  }, delay)
}
/**
 *
 * Pinia Store für das Spielbrett.
 * Lädt Spielfelddaten vom Backend und verwaltet den Zustand.
 */
export const useBoardStore = defineStore('board', {
  state: () => ({
    /** Status ob das Board erfolgreich geladen wurde */
    ok: false,
    /** Das komplette Spielbrett mit allen Feldern */
    board: null as IBoardDTD | null,
    /** meeple positionen */
    meeplePositions: {} as Record<string, string>,
    //testMeepleId: '123e4567-e89b-12d3-a456-426614174000' as string,
    lastFields: {} as Record<string, string | null>,
    /** meeple rotationen */
    meepleRotations: {} as Record<string, number>,
    /** cached barrier positions to avoid allocating new arrays on each getter access */
    barriersWithPositions: [] as Array<{ fieldId: string; position: [number, number, number] }>,
  }),
  actions: {
    /**
     * Lädt das Spielbrett vom Backend.
     * Setzt ok=true bei Erfolg, leert bei Fehler den State.
     */
    async getBoard() {
      // guarded log — avoid noisy output in production
      if (import.meta && import.meta.env && import.meta.env.DEV) console.log('Start receiving Gameboard Data...')
      try {
        const milefizStore = useMilefizStore()
        const lobbyId = milefizStore.gamedata.lobby?.id
        if (!lobbyId) {
          console.error("Keine Lobby ID - Board kann nicht geladen werden")
          return
        }
        const resp = await fetch(`/api/lobby/${lobbyId}/board/get`)
        if (!resp.ok) {
          console.error('Error while recieving Data:\n', resp.statusText)
          throw new Error(resp.statusText)
        }
        this.board = (await resp.json()) as IBoardDTD
        gameBoardTiles.value = this.board.fields
        gameTrees.value = this.board.trees

        this.ok = true

        if (import.meta && import.meta.env && import.meta.env.DEV) console.log('GameBoard successfully loaded')

        // cache barrier positions once when board is loaded to provide a stable
        // reference for UI consumers instead of recomputing on every access
        this.barriersWithPositions = this.board.fields
          .filter((f) => f.barrier)
          .map((field) => ({ fieldId: field.id, position: [field.position.x, 0, field.position.y] as [number, number, number] }))

        // noch zum testen
        if (this.board) {

          const milefizStore = useMilefizStore()
          const lobby: Lobby | null = milefizStore.gamedata.lobby
          if (lobby) {
            const players: Player[] = lobby.players
            for (const player of players) {
              for (const meeple of player.meeples) {
                if (meeple.currentFieldId) {
                  this.meeplePositions[meeple.id] = meeple.currentFieldId
                }
                this.lastFields[meeple.id] = null
                this.meepleRotations[meeple.id] ??= 0
              }
              // Debug: Meeple Positionen loggen nach assignment (guarded)
              if (import.meta && import.meta.env && import.meta.env.DEV) console.log('boardStore.getBoard: meeplePositions after init:', JSON.stringify(this.meeplePositions))
            }
          }
        }
      } catch (error_) {
        console.log(error_)
        this.ok = false
        this.board = null
      }
    },

    // Variablen zurücksetzen
    resetBoardStore() {
      this.ok = false
      this.board = null
      this.meeplePositions = {}
      this.lastFields = {}
      this.meepleRotations = {}
      this.barriersWithPositions = []
    },

    // meeple bewegen und letztes Feld merken
    updateMeeplePosition(meepleId: string, fieldId: string) {
      // Buffer the update and flush in rAF to collapse many rapid updates
      const previousField = this.meeplePositions[meepleId] ?? null
      if (previousField === fieldId) return
      _pendingPositionUpdates[meepleId] = fieldId
      scheduleFlush(this)
    },

    updateMeepleRotation(meepleId: string, rotation: number) {
      // Buffer rotation updates and flush in rAF
      const prev = this.meepleRotations[meepleId]
      if (prev !== undefined && Math.abs(prev - rotation) < 1e-4) return
      _pendingRotationUpdates[meepleId] = rotation
      scheduleFlush(this)
    },

    updateBarrierPosition(barrierId: string, currentFieldId: string, targetFieldId: string) {
      if (!this.board) return;

      //Alte Barriere entfernen
      const oldField = this.board.fields.find(f => f.id === currentFieldId);
      if (oldField) {
        oldField.barrier = false;
      }

      //Neue Barriere setzen
      const newField = this.board.fields.find(f => f.id === targetFieldId);
      if (newField) {
        newField.barrier = true;
      } else {
        console.warn(`Barrier target field ${targetFieldId} not found.`);
        return;
      }

      //Re-render erzwingen 
      this.board = {
        ...this.board,
        fields: [...this.board.fields],
      };

      console.log(`Barrier moved to field ${targetFieldId}`);

      // update cached barrier positions to keep a stable array reference for consumers
      this.barriersWithPositions = this.board.fields
        .filter((f) => f.barrier)
        .map((field) => ({ fieldId: field.id, position: [field.position.x, 0, field.position.y] as [number, number, number] }))
    }
  },
  // Getter um alle Barriere-Meeple ans Frontend zu übergeben
  getters: {
    /**
     * Gibt alle Felder zurück, die eine Barriere haben
     */
    barrierFields: (state) => {
      return state.board?.fields.filter((f) => f.barrier) || []
    },

    /**
     * Prüft ob ein bestimmtes Feld eine Barriere hat
     * @param fieldId - ID des zu prüfenden Feldes
     * @returns true wenn Barriere vorhanden, sonst false
     */
    hasBarrier: (state) => (fieldId: string) => {
      return state.board?.fields.find((f) => f.id === fieldId)?.barrier || false
    },

    /**
    /** Returns cached barrier positions (stable array reference) */
    barriersWithPositions: (state) => {
      return state.barriersWithPositions || []
    },
  },
})
