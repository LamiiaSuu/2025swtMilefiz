<script lang="ts" setup>
import EditorHUD from '@/components/ui/EditorHUD.vue';
import BackButton from '@/components/ui/pages/BackButton.vue'


</script>

<template>
    <div class="mapeditor">
        <EditorHUD style="bottom: 20px;"/>
        <div class="form-row">
            <div class="button-container">
                <BackButton :to="{ name: 'Homepage' }" />
            </div>
        </div>
    </div>
</template>

<style scoped>
.mapeditor {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;

  align-items: center;
  overflow: hidden;
  padding-bottom: 4rem;
}

.mapeditor::before {
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

form {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 40px;
    width: 100%;
    justify-content: center;
    margin-top: -5vh;
}

.form-column {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.form-row {
    display: grid;
    grid-template-columns: 150px 1fr;
    align-items: center;
    gap: 15px;
    z-index: 20;
}

.form-row label {
    text-align: right;
}

.button-container {
  position: absolute;
  bottom: 20px;
  left: 30px;

  display: flex;
  flex-direction: column;
  gap: 2vh;
}

.menu-button:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}
</style>