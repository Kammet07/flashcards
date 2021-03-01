<template>
    <div class="content" id="collections">
        <h2>Collections</h2>
        <ul class="" v-if="collections && collections.length">
            <li v-for="collection of collections" v-bind:key="collection.id">
                <p>{{ collection.category }} by {{ collection.creatorId }}</p>
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
import {url} from "@/assets/mixins";

export default {
    name: "Collections",

    data() {
        return {
            collections: [],
            errors: []
        }
    },

    // Fetches posts when the component is created.
    async created() {
        try {
            const response = await axios.get(`http://${url}:8080/api/collection`)
            this.collections = response.data
        } catch (e) {
            this.errors.push(e)
        }
    }
}

</script>

<style scoped>

</style>