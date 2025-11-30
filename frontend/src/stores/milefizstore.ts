import { reactive, readonly, computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { Client, type Message } from '@stomp/stompjs'
import type { Direction, MovementCommand } from "@/types/movement";
import { useBoardStore } from "./boardStore"

const wsurl = `ws://${window.location.host}/milefiz`
const DEST = '/topic/milefiz/lobby/'

let stompclient: Client | null = null

export const useMilefizStore = defineStore('milefizstore', () => {

  /**
   * Cooldown für das Würfelsystem
   * cooldown
   * @prop {long} remainingMs - Beschreibt verbleibende Millisekunden des Würfelcooldowns.
   * @prop {boolean} active - Wenn 'true', dann läuft gerade aktiv ein Cooldown herunter. Wenn 'false' steht der Cooldown auf 0 und es läuft gerade kein Timer.
   */
  const cooldown = reactive({
    remainingMs: 0,
    active: false,
  })
  // Beispiele für Daten
  const gamedata = reactive<{
    lobbyId: string
    playerId: string
    playerToken: string,
    mana: number
    currentDiceRoll?: number
  }>({
    lobbyId: '', // DummyLobby: 271c95db-3737-496f-9081-ae920e8ebbf7
    playerId: '', // UUID vom eigenen Spieler
    playerToken: "",
    mana: 100,
    currentDiceRoll: undefined, //Würfel ergebnis
  })

  function startMilefizLiveUpdate() {
    console.log('Starting Liveupdater for Milefiz')
    // Nur eine Instanz
    if (stompclient != null && stompclient.connected) {
      return
    }

    stompclient = new Client({
      brokerURL: wsurl,
      connectHeaders: {
        "player-token": gamedata.playerToken
      }
    })
    stompclient.onWebSocketError = (event) => {
      console.error(event)
      /* WS-Fehler */
    }
    stompclient.onStompError = (frame) => {
      console.error(frame)
      /* STOMP-Fehler */
    }

    stompclient.onConnect = (frame) => {
      console.log('Connected')

      if (stompclient == null) {
        console.error('Geht nicht')
        return
      }
      // Callback: erfolgreicher Verbindugsaufbau zu Broker
      stompclient.subscribe(DEST + gamedata.lobbyId, (message) => {
        console.log('Message received: ' + message + '\nBody:\n' + message.body)

        try {
          const event = JSON.parse(message.body)

          if (event.type === 'ROLL_DICE') {
            console.log(`Player ${event.playerId} rolled: ${event.number}`)
            gamedata.currentDiceRoll = event.number
          }
        } catch (err) {
          console.error('Error parsing message:', err)
        }

        // Fängt die JSON message ab und bildet die Schnittstelle des Front- und Backends für den Cooldown des Würfelns
        const event = JSON.parse(message.body)
        const boardStore = useBoardStore()
        if (event.type === 'COOLDOWN_STARTED') {
          cooldown.active = true
          cooldown.remainingMs = event.remainingMs
        }

        if (event.type === 'COOLDOWN_UPDATE') {
          cooldown.remainingMs = event.remainingMs
        }

        if (event.type === 'COOLDOWN_READY') {
          cooldown.active = false
          cooldown.remainingMs = 0
        }
        if (event.type === "MOVE_ERROR") {
          console.warn("Move rejected:", event.msg)
          return
        }
        if (event.type === "MOVE") {
          boardStore.updateMeeplePosition(event.id, event.targetField)
          gamedata.currentDiceRoll = event.remainingMoves
        }

      })
    }
    stompclient.onDisconnect = () => {
      /* Verbindung abgebaut*/
      console.log('Disconnected')
    }
    // Verbindung zum Broker aufbauen
    stompclient.activate()
  }

  function sendSocketMessage(payload: any) {
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot send message: STOMP client not connected.')
      return
    }
    const DEST_APP = '/app/milefiz/lobby/' + gamedata.lobbyId
    const body = JSON.stringify(payload)

    try {
      stompclient.publish({
        destination: '/app/milefiz/lobby',
        body,
      })
      console.log('Message sent to /app/milefiz/lobby/: ' + body)
    } catch (err) {
      console.error('Error sending message:', err)
    }
  }

  async function joinLobby(lobbyId: string = 'random') {
    console.log('Start receiving Gameboard Data...')
    try {
      if (lobbyId == null) lobbyId = 'random'
      const resp = await fetch('/api/lobby/join/' + lobbyId)
      if (!resp.ok) {
        console.error('Error while recieving Data:\n', resp.statusText)
        throw new Error(resp.statusText)
      }
      let responseMsg = await resp.json()
      console.log(responseMsg.msg)
      gamedata.lobbyId = responseMsg.lobbyId;
      gamedata.playerId = responseMsg.playerId;
      gamedata.playerToken = responseMsg.playerToken;
      startMilefizLiveUpdate();
    } catch (error_) {
      console.log(error_)
    }
  }

  /**
   * Sendet eine Bewegungsaktion (Move) an den Spielserver.
   *
   * Wird aufgerufen, wenn der Spieler im Frontend eine Bewegung
   * durchführt.
   *
   * Erstellt ein `MovementCommand`-Objekt mit Meeple-ID und Bewegungsrichtung
   * und veröffentlicht es über den STOMP-Endpunkt `/app/milefiz/lobby/{lobbyId}`.
   *
   * Ablauf:
   * 1. Verbindung prüfen – Abbruch, falls STOMP-Client nicht verbunden ist.
   * 2. Move-Daten serialisieren (`JSON.stringify`).
   * 3. Nachricht an den Server senden.
   *
   * @param meepleId - Eindeutige ID der Spielfigur, die bewegt werden soll
   * @param direction - Bewegungsrichtung (z. B. "NORTH", "SOUTH", "EAST", "WEST")
   * @param remainingMoves - Schritte die der Spieler noch tätigen kann
   */
  function sendMove(meepleId: string, direction: Direction, remainingMoves?: number) {
    if (!stompclient || !stompclient.connected) {
      console.error("Cannot send move: STOMP client not connected.")
      return
    }

    const moveCmd: MovementCommand = { meepleId, direction,  remainingMoves};

    const body = JSON.stringify(moveCmd)

    const DEST_APP = '/app/milefiz/lobby/' + gamedata.lobbyId

    try {
      stompclient.publish({
        destination: DEST_APP + "/move",
        body,
      })
      console.log("Move sent:", body)
    } catch (err) {
      console.error("Error sending move:", err)
    }
  }

  function sendRollDice() {
    if (!stompclient || !stompclient.connected) {
      console.error('Cannot roll dice: STOMP client not connected.')
      return
    }

    if (!gamedata.lobbyId || !gamedata.playerId) {
      console.error('Cannot roll dice: Missing lobbyId or playerId')
      return
    }

    const rollDiceCommand = {
      playerId: gamedata.playerId,
    }

    try {
      stompclient.publish({
        destination: `/app/milefiz/lobby/${gamedata.lobbyId}/rollDice`,
        body: JSON.stringify(rollDiceCommand),
      })
      console.log('Roll dice command sent for player:', gamedata.playerId)
    } catch (err) {
      console.error('Error sending roll dice command:', err)
    }
  }

  return {
    gamedata,
    startMilefizLiveUpdate,
    sendSocketMessage,
    sendRollDice,
    joinLobby,
    cooldown,
    sendMove
  }
})
