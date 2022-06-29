<template>
  <div id="app" class="d-flex flex-column h-100">
    <nav-bar />
    <div class="container flex-grow-1">
      <auth-error />
      <div class="mt-5">
        <router-view />
      </div>
    </div>
  </div>
</template>
<script>
import NavBar from "./components/ChatNavBar";
import AuthError from "./components/AuthError";

export default {
  components: {
    NavBar,
    AuthError
  }
};
</script>
<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}
</style>
