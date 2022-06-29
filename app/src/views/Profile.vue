<template>
  <div class="container">
    <div class="row align-items-center profile-header">
      <div class="col-md-2 mb-3">
        <img
          :src="userInfo?.picture"
          alt="User's profile picture"
          class="rounded-circle img-fluid profile-picture"
        />
      </div>
      <div class="col-md text-center text-md-left">
        <h2>{{ userInfo?.name }}</h2>
        <p class="lead text-muted">{{ userInfo?.email }}</p>
      </div>
    </div>

    <div class="row">
      <pre v-highlightjs><code class="json">{{JSON.stringify(userInfo, null, 2)}}</code></pre>
    </div>
  </div>
</template>

<script>
import { useAuth0 } from '@auth0/auth0-vue';

export default {
  name: "profile",
  setup() {
    const auth0 = useAuth0();

    return {
      userInfo: auth0.user,
    }
  }
};
</script>

