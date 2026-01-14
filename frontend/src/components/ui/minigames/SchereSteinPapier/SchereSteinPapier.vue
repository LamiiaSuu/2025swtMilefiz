<template>
    <div class="rps-card no-select">
        <h2 class="rps-title">
            {{ tUI('MINIGAME_RPS_TITLE') }}
        </h2>

        <!-- COUNTDOWN -->
        <CountdownBar :seconds="duel.timeOut" />

        <div class="players">
            <!-- Spieler 1 -->
            <div class="player">
                <div class="move-badge" :class="{ pending: !duel.state?.moveP1 }">
                    {{ renderMoveFor(duel.state?.moveP1, isMe(duel.getP1 ?? duel.state?.p1 ?? duel.player1)) }}
                </div>
                <h3 :style="{ color: getPlayerColorByMeeple(duel.firstMeeple) }">
                    {{ getPlayerNameByMeeple(duel.firstMeeple) }}
                </h3>
            </div>

            <!-- Spieler 2 -->
            <div class="player">
                <div class="move-badge" :class="{ pending: !duel.state?.moveP2 }">
                    {{ renderMoveFor(duel.state?.moveP2, isMe(duel.getP2 ?? duel.state?.p2 ?? duel.player2)) }}
                </div>
                <h3 :style="{ color: getPlayerColorByMeeple(duel.secondMeeple) }">
                    {{ getPlayerNameByMeeple(duel.secondMeeple) }}
                </h3>
            </div>
        </div>

        <!-- Buttons -->
        <div v-if="!duel.state?.finished" class="choices">
            <button class="choice-btn" :disabled="waiting === duel.duelId || hasAlreadyChosen()"
                @click="choose('SCISSORS')">
                ✌️
            </button>

            <button class="choice-btn" :disabled="waiting === duel.duelId || hasAlreadyChosen()"
                @click="choose('ROCK')">
                ✊
            </button>

            <button class="choice-btn" :disabled="waiting === duel.duelId || hasAlreadyChosen()"
                @click="choose('PAPER')">
                ✋
            </button>
        </div>

        <!-- Ergebnis -->
        <div v-if="duel.state?.finished" class="winner-big">
            <span v-if="isWinner()" class="winner-text">
                {{ tUI('DUEL_WON') }}
            </span>
            <span v-else class="loser-text">
                {{ tUI('DUEL_LOST') }}
            </span>

            <!-- optional: Unentschieden -->
            <div v-if="duel.state?.winner === null" class="draw-text">
                {{ tUI('RPS_DRAW')}}
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from "vue"
import { useMilefizStore } from "@/stores/milefizstore"
import { tUI } from "@/i18n"
import CountdownBar from "../CountdownBar.vue"

const props = defineProps<{ duel: any }>()
const emit = defineEmits<{ (e: "close"): void }>()

const store = useMilefizStore()
const waiting = ref<string | null>(null)

// winner check
function isWinner() {
    return props.duel.state?.winner === store.gamedata.playerId
}

// optional: wenn dein Event p1/p2 nicht enthält, kannst du das auch über Meeple->Player machen.
// hier minimal: "isMe" kann auch einfach immer false sein, renderMoveFor nutzt es nur für "?" logic.
function isMe(playerId: string | undefined) {
    if (!playerId) return false
    return playerId === store.gamedata.playerId
}

// falls du deinen eigenen Move schon im state hast: keine Mehrfachwahl
function hasAlreadyChosen() {
    const myId = store.gamedata.playerId
    // wir erkennen "ich bin p1 oder p2" über winner? -> schwierig. simplest:
    // wenn dein Backend im Update p1/p2 mitsendet, kannst du das sauberer machen.
    // pragmatisch: wenn moveP1 oder moveP2 schon gesetzt ist UND dein Name dem Spieler gehört, ok.
    // Hier: wir erlauben nur einmal klicken pro Duel-Overlay (waiting).
    return waiting.value === props.duel.duelId
}

function choose(move: "SCISSORS" | "ROCK" | "PAPER") {
    waiting.value = props.duel.duelId

    // Backend erwartet hier aktuell: Payload = String (z.B. "SCHERE")
    store.sendLobbyMessage(
        `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/rockpaperscissors/choose`,
        move
    )
}

function renderMoveFor(move: string | null | undefined, reveal: boolean) {
    // Wenn nicht gewählt:
    if (!move) return "…"

    // Optional: Gegnerwahl bis Ende verstecken (nice UX)
    if (!props.duel.state?.finished && !reveal) return "?"

    switch (move) {
        case "SCISSORS": return "✌️"
        case "ROCK": return "✊"
        case "PAPER": return "✋"
        default: return String(move)
    }
}

function getPlayerNameByMeeple(meepleId: string) {
    const lobby = store.gamedata.lobby
    if (!lobby) return "?"

    for (const player of lobby.players) {
        if (player.meeples?.some((m: any) => m.id === meepleId)) {
            return player.playerName ?? "?"
        }
    }
    return "?"
}

function getPlayerColorByMeeple(meepleId: string) {
    const lobby = store.gamedata.lobby
    if (!lobby) return "#ffffff"

    for (const player of lobby.players) {
        if (player.meeples?.some((m: any) => m.id === meepleId)) {
            return player.color || "#ffffff"
        }
    }
    return "#ffffff"
}

// auto close nachdem es fertig ist
watch(
    () => props.duel.state?.finished,
    finished => {
        if (finished) setTimeout(() => emit("close"), 1500)
    }
)
</script>

<style scoped>
.rps-card {
    border-radius: 14px;
    padding: 14px;
}

.rps-title {
    font-family: "Acme", sans-serif;
    font-size: 1.6rem;
    font-weight: 900;
    text-align: center;
    margin: 0 0 8px 0;
}

.players {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    margin: 0 0 12px;
}

.player {
    text-align: center;
}

.player h3 {
    font-size: 1.8rem;
    font-weight: 900;
    margin: 6px 0 0 0;
    font-family: "Acme", sans-serif;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    text-shadow:
        0 0 1px rgba(0, 0, 0, .95),
        1px 0 1px rgba(0, 0, 0, .9),
        -1px 0 1px rgba(0, 0, 0, .9),
        0 1px 1px rgba(0, 0, 0, .9),
        0 -1px 1px rgba(0, 0, 0, .9);
}

.move-badge {
    width: 110px;
    height: 110px;
    margin: 10px auto 0;
    border-radius: 16px;
    display: grid;
    place-items: center;
    font-size: 3rem;
    font-weight: 900;
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.12);
}

.move-badge.pending {
    opacity: 0.7;
}

.choices {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 10px;
}

.choice-btn {
    width: 74px;
    height: 74px;
    border-radius: 14px;

    background: rgba(255, 255, 255, 0.08);

    border: 1px solid rgba(255, 255, 255, 0.14);

    display: grid;
    place-items: center;

    cursor: pointer;
    font-family: "Acme", sans-serif;
    font-weight: 900;
    font-size: 2.2rem;


    padding: 0;

    -webkit-appearance: none;

    appearance: none;
}

.choice-btn:disabled {
    opacity: 0.45;
    cursor: not-allowed;
}

.choice-btn:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.12);
}

.winner-big {
    margin-top: 14px;
    text-align: center;
    font-size: 1.8rem;
    font-weight: 900;
    font-family: "Acme", sans-serif;
}

.draw-text {
    margin-top: 6px;
    opacity: 0.9;
    font-size: 1.2rem;
}

.no-select {
    -webkit-user-select: none;
    -ms-user-select: none;
    user-select: none;
    -webkit-user-drag: none;
}
</style>
