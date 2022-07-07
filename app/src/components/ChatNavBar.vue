<template>
  <div class="nav-container mb-3">
    <nav class="navbar navbar-expand-md navbar-light bg-light">
      <div class="container">
        <div class="navbar-brand">
          <img alt="Zilla icon" src="../assets/icon.png">
        </div>
        <button
            class="navbar-toggler"
            type="button"
            data-toggle="collapse"
            data-target="#navbarNav"
            aria-controls="navbarNav"
            aria-expanded="false"
            aria-label="Toggle navigation"
        >
          <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" >
          <ul class="navbar-nav mr-auto">
            <li class="nav-item">
              <router-link to="/" class="nav-link">Dashboard</router-link>
            </li>
          </ul>
          <ul class="navbar-nav d-none d-md-block">
            <li v-if="!isAuthenticated && !isLoading" class="nav-item">
              <button
                  id="qsLoginBtn"
                  class="btn btn-primary btn-margin"
                  @click.prevent="login"
              >Login</button>
            </li>

            <li class="nav-item dropdown" v-if="isAuthenticated">
              <a
                  class="nav-link dropdown-toggle"
                  href="#"
                  id="profileDropDown"
                  data-toggle="dropdown"
              >
                <img
                    :src="user.picture"
                    alt="User's profile picture"
                    class="nav-user-profile rounded-circle"
                    width="50"
                />
              </a>
              <div class="dropdown-menu dropdown-menu-right">
                <div class="dropdown-header">{{ user.name }}</div>
                <router-link to="/profile" class="dropdown-item dropdown-profile">
                  <font-awesome-icon class="mr-3" icon="user" />Profile
                </router-link>
                <a id="qsLogoutBtn" href="#" class="dropdown-item" @click.prevent="logout">
                  <font-awesome-icon class="mr-3" icon="power-off" />Log out
                </a>
              </div>
            </li>
          </ul>

          <ul class="navbar-nav d-md-none" v-if="!isAuthenticated && !isLoading">
            <button id="qsLoginBtn" class="btn btn-primary btn-block" @click="login">Log in</button>
          </ul>

          <ul
              id="mobileAuthNavBar"
              class="navbar-nav d-md-none d-flex"
              v-if="isAuthenticated"
          >
            <li class="nav-item">
              <span class="user-info">
                <img
                    :src="user.picture"
                    alt="User's profile picture"
                    class="nav-user-profile d-inline-block rounded-circle mr-3"
                    width="50"
                />
                <h6 class="d-inline-block">{{ user.name }}</h6>
              </span>
            </li>
            <li>
              <font-awesome-icon icon="user" class="mr-3" />
              <router-link to="/profile">Profile</router-link>
            </li>

            <li>
              <font-awesome-icon icon="power-off" class="mr-3" />
              <a id="qsLogoutBtn" href="#" class @click.prevent="logout">Log out</a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  </div>
</template>

<script>
import { useAuth0 } from '@auth0/auth0-vue';
import {mapActions} from "vuex";
import {default as axios} from "axios";
import jwt_decode from "jwt-decode";

export default {
  name: "NavBar",
  setup() {
    const auth0 = useAuth0();
    return {
      auth0: auth0,
      isAuthenticated: auth0.isAuthenticated,
      isLoading: auth0.isLoading,
      user: auth0.user
    }
  },
  methods: {
    ...mapActions([
        'unload'
    ]),
    async logout() {
      const accessToken = await this.auth0.getAccessTokenSilently();
      const decodedToken = jwt_decode(accessToken);
      const userId = decodedToken.sub.split("|")[1];
      const userInfo = this.auth0.user.value;
      const status = "offline";

      const currentUser = {
        id: `${userId}`,
        username: userInfo.nickname,
        name: userInfo.name,
        status: status
      };

      await axios.put(`http://localhost:8080/users/${userId}`, {
        username: currentUser.username,
        name: currentUser.name,
        status: status
      }, {
        headers: {
          Authorization: `Bearer ${accessToken}`
        }
      });

      await this.unload();
      this.auth0.logout({
        returnTo: window.location.origin
      });
    },
    login() {
      this.unload();
      this.auth0.loginWithRedirect();
    }
  }
};
</script>

<style>
#mobileAuthNavBar {
  min-height: 125px;
  justify-content: space-between;
}

img {
  width: 50px;
  height: 50px;
}

.navbar .router-link-exact-active {
  color: #041433;
  border-bottom: 3px solid #0d9b76;
}

.btn-primary {
  background-color: #0d9b76 !important;
  border: 1px solid #0d9b76 !important;
}

.btn-primary:hover {
  color: #fff;
  background-color: #0d9b76;
  border-color: #0d9b76;
}

.navbar .router-link-exact-active {
  color: #0d9b76;
  border-bottom: 3px solid #0d9b76;
}

a {
  color: #0d9b76;
}

.d-md-block {
  display: block !important;
  margin-left: auto;
}
</style>
