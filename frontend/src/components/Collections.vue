<template>
    <div id="collections">
        <ul v-if="posts && posts.length">
            <li v-for="post of posts" v-bind:key="post.id">
                <p>{{ post.username }}</p>
            </li>
        </ul>

        <ul v-if="errors && errors.length">
            <li v-for="error of errors" v-bind:key="error.message">
                {{ error.message }}
            </li>
        </ul>
    </div>
</template>

<script>
import axios from 'axios';

export default {
    name: "Collections",

    data() {
        return {
            posts: [],
            errors: []
        }
    },

    // Fetches posts when the component is created.
    async created() {
        try {
            const response = await axios.get(`http://localhost:8080/api/collection`)
            this.posts = response.data
        } catch (e) {
            this.errors.push(e)
        }
    }
}

</script>

<style scoped>

</style>