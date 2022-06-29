import { createRouter as createVueRouter, createWebHashHistory, Router } from "vue-router";
import { createAuthGuard } from "@auth0/auth0-vue";
import { App } from 'vue';
import ChatDashboard from '../views/ChatDashboard.vue'
import Profile from "../views/Profile.vue";

export function createRouter(app: App): Router {
  return createVueRouter({
    routes: [
      {
        path: '/',
        name: 'chat',
        component: ChatDashboard,
      },
      {
        path: "/profile",
        name: "profile",
        component: Profile,
        beforeEnter: createAuthGuard(app)
      }
    ],
    history: createWebHashHistory()
  })
}
