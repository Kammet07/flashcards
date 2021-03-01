<template>
    <div class="content" id="users">
        <h2>Users</h2>
        <ul v-if="users && users.length">
            <li v-for="user of users" v-bind:key="user.id">
                <p>{{ user.username }}</p>
            </li>
        </ul>
    </div>
</template>

<script>
import axios from "axios";
import {url} from "@/assets/mixins";

export default {
    name: "Users",
    data() {
        return {
            users: [],
            errors: []
        }
    },

    async created() {
        try {
            const response = await axios.get(`http://${url}:8080/api/user`, {withCredentials: true})
            this.users = response.data
        } catch (e) {
            this.errors.push(e)
        }
    }
}
</script>

<style scoped>

</style>