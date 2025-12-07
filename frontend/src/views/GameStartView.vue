<script setup lang="ts">
import BackButton from '@/components/ui/BackButton.vue';
</script>

<template>
    <div class="content">
        <div class="header">
            <BackButton />

            <div class="titles">
                <h1>MI'lefiz</h1>
                <h5>Neues Spiel</h5>
            </div>
        </div>

        <div class="new-game-form">
            <form>
                <div class="form-row">
                    <label>
                        Lobby-ID
                        <input type="text" name="lobby-id" disabled>
                        <button></button>
                    </label>
                </div>

                <div class="form-row">
                    <label>
                        Lobby-Name
                        <input type="text" name="lobby-name">
                    </label>
                </div>

                <div class="form-row">
                    <label>Username
                        <input type="text" name="username">
                    </label>
                </div>

                <div class="form-row">
                    <label>Spieler
                        <input type="text" name="username">
                    </label>
                </div>

                <div class="form-row">
                    <label>Map
                        <div class="map-buttons">
                            <button type="button" class="map-select">Standardmap</button>
                            <button type="button" class="map-select">Importieren</button>
                        </div>
                    </label>
                </div>

                <div class="form-row">
                    <label>Datei</label>
                </div>

                <div class="form-row">
                    <button class="game-start" @click="$router.push({ name: 'game' })">Spiel Starten</button>

                </div>

            </form>
        </div>
    </div>

</template>

<style>
.content {
    background: white;
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.header {
    position: relative;
    text-align: center;
    padding: 10px 25px;
    color: var(--color-text);
}

.header>*:first-child {
    position: absolute;
    left: 10px;
    top: 50%;
    transform: translateY(-50%);
}

.titles {
    margin: 0 auto;
    text-align: center;
}

.new-game-form {
    padding: 0 25px;
    display: flex;
    flex-direction: column;
    gap: 15px;
}


.form-row {
    display: flex;
    align-items: center;
    gap: 10px;
}

.form-row label {
    min-width: 120px;
    text-align: right;
}

.form-row input {
    flex: 1;
    padding: 5px;
}

.map-select {
  padding: 6px 10px;
  border: 1px solid #234420;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

</style>
