import BootstrapVue3 from 'bootstrap-vue-3'
import PerfectScrollbar from 'vue3-perfect-scrollbar'
import { createAuth0 } from "@auth0/auth0-vue";
import hljs from "vue3-highlightjs";
import "highlight.js/styles/github.css";
import { library } from "@fortawesome/fontawesome-svg-core";
import { faLink, faUser, faPowerOff } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { domain, clientId as client_id } from "../auth_config.json";

import App from './App.vue'
import {createApp, ref} from "vue";
import {createRouter} from './router/index'
import store from './store/index'

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue-3/dist/bootstrap-vue-3.css'
import './assets/css/loading.css'
import './assets/css/loading-btn.css'
import 'vue3-perfect-scrollbar/dist/vue3-perfect-scrollbar.css'

const app = createApp(App);

library.add(faLink, faUser, faPowerOff);

  app
    .use(hljs)
    .use(BootstrapVue3)
    .use(PerfectScrollbar)
    .use(createRouter(app))
    .use(store)
    .use(
        createAuth0({
            domain,
            client_id,
            redirect_uri: window.location.origin,
            audience:"http://localhost:8080/tasks",
            scope:"read:tasks write:tasks"
        })
    )
    .provide("$error", ref(null))
    .component("font-awesome-icon", FontAwesomeIcon)
    .mount("#app");
