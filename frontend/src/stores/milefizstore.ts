import { reactive, readonly, computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { Client, type Message } from '@stomp/stompjs'

const wsurl = `ws://${window.location.host}/milefiz`
const DEST = '/topic/milefiz/lobby/'

let stompclient: Client | null = null

export const useMilefizStore = defineStore('milefizstore', () => {
  // Beispiele für Daten
  const gamedata = reactive<{
    lobbyId: string
    playerId: string
    mana: number
    currentDiceRoll?: number
  }>({
    lobbyId: '', // DummyLobby: 271c95db-3737-496f-9081-ae920e8ebbf7
    playerId: '', // UUID vom eigenen Spieler
    mana: 100,
    currentDiceRoll: undefined, //Würfel ergebnis
  })

  function startMilefizLiveUpdate() {
    console.log('Starting Liveupdater for Milefiz')
    // Nur eine Instanz
    if (stompclient != null && stompclient.connected) {
      return
    }

    stompclient = new Client({ brokerURL: wsurl })
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
            handleRollDiceResult(event)
          }
        } catch (err) {
          console.error('Error parsing message:', err)
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
      gamedata.lobbyId = responseMsg.lobbyId
      gamedata.playerId = responseMsg.playerId
      startMilefizLiveUpdate()
    } catch (error_) {
      console.log(error_)
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

  function handleRollDiceResult(event: any) {
    console.log('Dice roll result:', event)

    gamedata.currentDiceRoll = event.number

    console.log(`Player rolled: ${event.number}`)
  }

  return {
    gamedata,
    startMilefizLiveUpdate,
    sendSocketMessage,
    sendRollDice,
    joinLobby,
  }
})
