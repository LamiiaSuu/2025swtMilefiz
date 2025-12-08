<script setup lang="ts">
import BackButton from '@/components/ui/BackButton.vue';
</script>

<template>
    <div class="content">
        <div class="header-with-plate">
            <p>MI'lefiz</p>
        </div>

        <div class="page-title">
            <h1>Neues Spiel</h1>
        </div>

        <div class="backButton">
            <BackButton :to="{ name: 'Homepage' }" />
        </div>

        <div class="new-game-form">
            <form>
                <div class="form-row">
                    <label>Lobby-ID</label>
                    <input type="text" name="lobby-id" disabled>
                    <button></button>
                </div>

                <div class="form-row">
                    <label>Lobby-Name</label>
                    <input type="text" name="lobby-name">
                </div>

                <div class="form-row">
                    <label>Username</label>
                    <input type="text" name="username">
                </div>

                <div class="form-row">
                    <label>Spieler</label>
                    <input type="text" name="username">
                </div>

                <div class="form-row">
                    <label>Map</label>
                    <div class="map-buttons">
                        <button type="button" class="map-select">Standardmap</button>
                        <button type="button" class="map-select">Importieren</button>
                    </div>
                </div>

                <div class="form-row">
                    <label>Datei</label>
                </div>

                <div class="form-row">
                    <button class="menu-button game-start-button" type="button" @click="$router.push({ name: 'game' })">Spiel Starten</button>
                </div>

            </form>
        </div>
    </div>

</template>

<style scoped>
.content {
    position: relative;
    height: 100vh;
    width: 100vw;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    padding-bottom: 8rem;
}

.content::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url("/backgrounds/BackgroundTest.webp");
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    filter: blur(4px);
    z-index: -1;
}

.backButton {
    position: absolute;
    top: 20vh;
    left: 50vh;
}

.new-game-form {
    padding: 0 25px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
    margin-top: -6vh;
}

.new-game-form form {
    display: flex;
    flex-direction: column;
    gap: 15px;
    width: 100%;
    max-width: 600px;
}

.form-row {
    display: flex;
    align-items: center;
    gap: 10px;
}

.form-row label {
    min-width: 150px;
    width: 150px;
    text-align: right;
    flex-shrink: 0;
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

.menu-button:hover {
    transform: scale(1.05);
    transition: transform 0.2s ease;
}
</style>