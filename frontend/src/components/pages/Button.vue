<template>
    <button class="button"><slot>Button_Text</slot></button>
</template>

<style scoped>
    .button {
        font-family: "MainFont", sans-serif;
        background-image: url("/backgrounds/ButtonPlate.png");
        background-color: transparent;
        background-size: contain;
        background-repeat: no-repeat;
        background-position: center;
        border: none;
        cursor: pointer;

        flex: 1 1 0;
        width: 0;

        align-items: center;
        justify-content: center;
        text-align: center;

        color: white;
        font-size: 3.5vh;
        min-width: 35vw;
        min-height: 9vh;
        outline: none;

        padding-bottom: 1vh;

        -webkit-text-stroke: 1px black;
        text-shadow:
            3px 3px 6px rgba(0, 0, 0, 0.8),
            0 0 10px rgba(0, 0, 0, 0.5);

        &:hover {
            transform: scale(1.05);
            transition: transform 0.2s ease;
        }
    }
</style>