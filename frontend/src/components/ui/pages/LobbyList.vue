<script lang="ts" setup>

/* TODO: lobby interface aus lobbyupdate nehmen (vielleicht anpassen?) */
export interface Lobby {
    id: string,
    name: string,
    //...
}

defineProps<{ lobbies : Lobby[], label: string }>()
const lobbyid = defineModel('lobbyid')

// für filtern https://vuejs.org/guide/essentials/list.html#displaying-filtered-sorted-results
</script>

<template>
    <div class="game-container lobby-container">
        <div class="game-label">{{ label }}</div>
        <div class="lobby-list game-content">
            <template v-for="(lobby) in lobbies" :key="lobby.id">
                <label class="lobby-item">
                    <input class="radio-input" type="radio" :value="lobby.id" v-model="lobbyid" />
                    <div class="radio-content">{{ lobby.id }}</div>
                </label>
            </template>
        </div>
    </div>
</template>

<style scoped>

/** lobby-list **/

.lobby-container {
    min-height: 0;
}

.lobby-list {
    overflow-y: auto;   
    overflow-x: hidden; 
    width: 100%;
    max-height: 100%;
    min-height: 100px;
}

/** lobby-item **/

.lobby-item {
    display: block;
    position: relative;
    cursor: pointer;
    font-size: 22px;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    font-size: 1.2rem;
}

.lobby-item input {
    display: none;
}

.radio-content {
    border-radius: inherit;
    padding: 10px 15px;
    background-color: var(--background-color-forms);
    text-align: center;
}

.radio-content:hover {
    background-color: #ccc;
}

.radio-input:checked~.radio-content {
    background-color: var(--background-color-input-focus);
}
</style>