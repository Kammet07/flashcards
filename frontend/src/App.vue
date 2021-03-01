<template>
    <div id="app">
        <Authentication/>
        <br>
        <Users/>
        <br>
        <Collections/>
    </div>
</template>

<script>
import Collections from "@/components/Collections";
import Users from "@/components/Users";
import Authentication from "@/components/Authentication";

export default {
    name: 'App',
    components: {
        Collections,
        Users,
        Authentication,
    },
}
</script>

<style>
#app {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: #2c3e50;
    margin-top: 60px;
    margin-left: 20px;
}
</style>
