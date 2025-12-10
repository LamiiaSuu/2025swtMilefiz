<script lang="ts" setup>

/* TODO: vielleicht lobby interface aus lobbyupdate (vielleicht anpassen?) */
export interface Lobby {
    id: string,
    name: string,
    //...
}

defineProps<{ lobbies : Lobby[]}>()
const lobbyid = defineModel('lobbyid')

// für filtern https://vuejs.org/guide/essentials/list.html#displaying-filtered-sorted-results
</script>

<template>
    <div class="lobby-list-container">
        <label>Lobbys</label>
        <div class="lobby-list">
            <select v-model="lobbyid" multiple>
                  <option :value="lobby.id" class="lobby-item" v-for="(lobby) in lobbies" :key="lobby.id" >
                        {{ lobby.id }} / {{ lobby.name }}
                  </option>
            </select>
        </div>
    </div>
</template>

<style scoped>
    .component-list {
        display: flex;
        height: 50%;
        flex-direction: column;
        flex-wrap: wrap;
        align-items: center;
        gap: 2vh;
    }

    .component-list > * {
        flex: 1;
    }
</style>