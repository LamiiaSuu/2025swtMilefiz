import { reactive, readonly, computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { Client, type Message } from '@stomp/stompjs'
import type { Direction, MovementCommand } from "@/types/movement";
import { useBoardStore } from "./boardStore"

const wsurl = `ws://${window.location.host}/milefiz`
const DEST = '/topic/milefiz/lobby/'

let stompclient: Client | null = null

export const useMilefizStore = defineStore('milefizstore', () => {

  //Cooldown für das Würfelsystem
  const cooldown = reactive({
    remainingMs: 0,
    active: false,
  })

  // Beispiele für Daten
  const gamedata = reactive<{ lobbyId: string, playerId: string; mana: number }>({
    lobbyId: "", // DummyLobby: 271c95db-3737-496f-9081-ae920e8ebbf7
    playerId: "", // UUID vom eigenen Spieler
    mana: 100,
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
      stompclient.subscribe((DEST + gamedata.lobbyId), (message) => {
        console.log('Message received: ' + message + "\nBody:\n" + message.body)
        // const eventobjekt: IZutatDTD = JSON.parse(message.body)
        // console.log(JSON.stringify(eventobjekt))
        // if (eventobjekt.type === 'DOENER') {
        // updateDoenerListe()
        // }
        // Callback: Nachricht auf DEST empfangen
        // empfangene Nutzdaten in message.body abrufbar,
        // ggf. mit JSON.parse(message.body) zu JS konvertieren

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
          boardStore.updateMeeplePosition(event.meepleId, event.targetField)
        }

      })

      /**
      * Abonniert das STOMP-Topic für Bewegungs-Updates (`/topic/move`).
      * 
      * Wenn der Server eine Bewegung eines Meeples sendet, 
      * wird die Nachricht hier empfangen, verarbeitet und an den `BoardStore` 
      * weitergereicht, um die Spielfeld-Position lokal zu aktualisieren.
      * 
      * Ablauf:
      * 1. Empfang des JSON-Nachrichtentexts über `message.body`.
      * 2. Umwandlung in ein JS-Objekt (`event`).
      * 3. Prüfung auf Fehlermeldungen (z. B. `"CANNOT_CHANGE_DIRECTION"`).  
      * 4. Aktualisierung der Spielfigur-Position im `BoardStore`.
      */
      stompclient.subscribe(DEST + gamedata.lobbyId + "/move", (message) => {
        console.log("movement update:", message.body)
        const event = JSON.parse(message.body)
        const boardStore = useBoardStore()
        if (event.type === "MOVE_ERROR") {
          console.warn("Move rejected:", event)
          return
        }
        if (event.type === "MOVE") {
          console.log("move angekommen")
          console.log(event.id, event.targetField)
          boardStore.updateMeeplePosition(event.id, event.targetField)
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
      console.error("Cannot send message: STOMP client not connected.")
      return
    }
    const DEST_APP = '/app/milefiz/lobby/' + gamedata.lobbyId
    const body = JSON.stringify(payload)

    try {
      stompclient.publish({
        destination: "/app/milefiz/lobby",
        body,
      })
      console.log("Message sent to /app/milefiz/lobby/: " + body)
    } catch (err) {
      console.error("Error sending message:", err)
    }
  }

  async function joinLobby(lobbyId: string = "random") {
    console.log('Start receiving Gameboard Data...')
    try {
      if (lobbyId == null) lobbyId = "random"
      const resp = await fetch('/api/lobby/join/' + lobbyId)
      if (!resp.ok) {
        console.error('Error while recieving Data:\n', resp.statusText)
        throw new Error(resp.statusText)
      }
      let responseMsg = await resp.json()
      console.log(responseMsg.msg)
      gamedata.lobbyId = responseMsg.lobbyId;
      gamedata.playerId = responseMsg.playerId;
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
   * und veröffentlicht es über den STOMP-Endpunkt `/app/move`.
   * 
   * Ablauf:
   * 1. Verbindung prüfen – Abbruch, falls STOMP-Client nicht verbunden ist.  
   * 2. Move-Daten serialisieren (`JSON.stringify`).  
   * 3. Nachricht an den Server senden.  
   * 
   * @param meepleId - Eindeutige ID der Spielfigur, die bewegt werden soll  
   * @param direction - Bewegungsrichtung (z. B. "NORTH", "SOUTH", "EAST", "WEST")
   */
  function sendMove(meepleId: string, direction: Direction) {
    if (!stompclient || !stompclient.connected) {
      console.error("Cannot send move: STOMP client not connected.")
      return
    }

    const moveCmd: MovementCommand = { meepleId, direction };

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

  return {
    gamedata,
    startMilefizLiveUpdate,
    sendSocketMessage,
    joinLobby,
    cooldown,
    sendMove
  }
})
