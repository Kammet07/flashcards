import Vue from 'vue'
import App from './App.vue'
import './assets/sass/main.scss'
import VueIziToast from "vue-izitoast";
import 'izitoast/dist/css/iziToast.css';

Vue.config.productionTip = false
Vue.use(VueIziToast)

new Vue({
    render: h => h(App),
}).$mount('#app')
