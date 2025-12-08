<script setup lang="ts">
import BackButton from '@/components/ui/BackButton.vue';
</script>

<template>
    <div class="content">
        <div class="header">
            <div class="backButton">
                <BackButton :to="{ name: 'Homepage' }" />
            </div>

            <div class="header-with-plate2">
                <p>MI'lefiz</p>
            </div>

            <h5>Neues Spiel</h5>
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

.header {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    position: relative;
    margin-top: 2rem;
}

.backButton {
    position: absolute;
    top: 0;
    left: 0;
}

.header-with-plate2 > p {
  transform: translateY(-50%);
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
