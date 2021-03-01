<template>
    <div id="auth" class="content">

        <h1>Login</h1>
        <p>
            <label>
                <input v-model="username" class="input" placeholder="username" type="text" value="trsrsssstasr">
                <input v-model="password" class="input" placeholder="password" type="text" value="tars32p">
                <button class="button" @click="login">send</button>
            </label>
        </p>

        <h1>Registration</h1>
        <p>
            <label>
                <input v-model="usernameReg" class="input" placeholder="username" type="text">
                <input v-model="passwordReg" class="input" placeholder="password" type="text">
                <input v-model="mail" class="input" placeholder="mail" type="email">
                <button class="button" @click="register">register</button>
            </label>
        </p>
        <button class="button" @click="logout">logout</button>

        <ul v-if="errors && errors.length">
            <li v-for="error of errors" v-bind:key="error.message">
                {{ error.status }}
            </li>
        </ul>

<!--        <button class="button" v-on:click="$toast.success('hello', 'dudue', {})">tsrsat</button>-->

        <p>username: {{ username }}</p>
        <p>password: {{ password }}</p>
        <p>{{ user }}</p>
    </div>
</template>

<script>
import axios from "axios";
import {axiosErrorHandler, user, url} from "@/assets/mixins";

export default {
    name: "Authentication",
    data() {
        return {
            username: '',
            password: '',
            usernameReg: '',
            passwordReg: '',
            mail: '',
            errors: {},
        }
    },
    mixins: [axiosErrorHandler, user, url],
    methods: {
        async login() {
            // try {
            //     const response = await axios.post(`http://${url}:8080/api/authentication`, {
            //         username: this.username,
            //         password: this.password
            //     }, {withCredentials: true})
            //     this.user = response.status
            // } catch (error) {
            //     this.errors = e
            // }

            await axios.post(`http://${url}:8080/api/authentication`, {
                username: this.username,
                password: this.password
            }, {withCredentials: true})
                .then(response => {
                    user = response.data
                    this.$toast.success('Logged in', 'OK', {})
                })
                .catch(function (error) {
                    const errorResponse = this.handleError(error) // if you want to customize the error message, you can send it as second parameter
                    if (errorResponse.isValidationError) {
                        this.formErrors = errorResponse.errors
                        this.$toast.error("error", errorResponse.message, {})
                    } else {
                        this.$toast.error("error", errorResponse.message, {})
                    }
                })

        },
        async register() {
            await axios.post(`http://${url}:8080/api/user`, {})

            try {
                const response = await axios.post(`http://${url}:8080/api/user`, {
                    username: this.username,
                    password: this.password,
                    mail: this.mail
                })
                user = response.data
            } catch (e) {
                this.errors = e
            }
        },
        async logout() {
            try {
                await axios.delete(`http://${url}:8080/api/authentication`)
                user = Object
            } catch (e) {
                this.errors = e
            }
        },
    }
}
</script>

<style scoped>
#auth {
    width: 300px;
}
</style>