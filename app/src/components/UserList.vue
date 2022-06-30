<template>
  <div class="user-list">
    <h4>Members</h4>
    <hr>
    <b-list-group>
      <b-list-group-item v-for="user in users" :key="user.username">
        {{ user.name }}
        <b-badge v-if="user.status"
        :variant="statusColor(user.status)"
        pill>
        {{ user.status }}</b-badge>
      </b-list-group-item>
    </b-list-group>
  </div>
</template>

<script>
import {mapMutations, mapState} from 'vuex'
import {useAuth0} from "@auth0/auth0-vue";
import {watch} from "vue";

export default {
  name: 'user-list',
  setup() {
    const auth0 = useAuth0();
    return {
      auth0: auth0
    }
  },
  computed: {
    ...mapState([
      'loading',
      'users',
      'activeChannel'
    ]),
    ...mapMutations([
        'addUser'
    ])
  },
  async mounted() {
    const auth0 = this.auth0;
    let channelMembers;

    async function subscriberToChannelMembers(channelId) {
      const accessToken = await auth0.getAccessTokenSilently();
      channelMembers?.close();
      channelMembers = new EventSource(`http://localhost:8080/channels/${channelId}/members?access_token=${accessToken}`);
      channelMembers.onmessage = function (event) {
        const user = JSON.parse(event.data);
        this.addUser({
          id: user.id,
          username: user.username,
          name: user.name,
          status: user.status
        });
      };
    }
    watch(this.activeChannel, subscriberToChannelMembers)
  },
  methods: {
    statusColor(status) {
      return status === 'online' ? 'success' : 'warning'
    }
  }
}
</script>
