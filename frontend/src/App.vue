<template>
    <div id="app">
        <Collections/>
    </div>
</template>

<script>
import Collections from "@/components/Collections";

export default {
    name: 'App',
    components: {
        Collections
    }
}
</script>

<style>
#app {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    margin-top: 60px;
}
</style>
