<template>
  <div class="user-list">
    <h4>Members</h4>
    <hr>
    <b-list-group>
      <b-list-group-item v-for="(member, key) in members" :key="key">
        {{ member.name }}
        <b-badge v-if="member.status"
        :variant="statusColor(member.status)"
        pill>
        {{ member.status }}</b-badge>
      </b-list-group-item>
    </b-list-group>
  </div>
</template>

<script>
import {mapState} from 'vuex'
import {useAuth0} from "@auth0/auth0-vue";
import {Buffer} from "buffer";

window.Buffer = window.Buffer || Buffer;

export default {
  name: 'user-list',
  setup() {
    const auth0 = useAuth0();
    return {
      auth0: auth0
    }
  },
  data() {
    return {
      members: {},
      channelMembersStream: null
    }
  },
  computed: {
    ...mapState([
      'loading',
      'activeChannel'
    ])
  },
  created() {
    if (this.activeChannel) {
      this.subscriberToChannel(this.members, this.activeChannel.id);
    }
  },
  watch: {
    activeChannel(newChannel) {
      this.members = {};
      this.subscriberToChannel(this.members, newChannel.id);
    },
  },
  methods: {
    statusColor(status) {
      return status === 'online' ? 'success' : 'warning'
    },
    async subscriberToChannel(members, channelId) {

      const accessToken = await this.auth0.getAccessTokenSilently();

      this.channelMembersStream?.close();
      this.channelMembersStream = new EventSource(`http://localhost:8080/channels/${channelId}/members?access_token=${accessToken}`);
      this.channelMembersStream.onmessage = function (event) {
        let lastEventId = JSON.parse(event.lastEventId);
        let key = Buffer.from(lastEventId[0], "base64").toString("utf8");
        const member = JSON.parse(event.data);
        members[key] = {
          id: member.id,
          username: member.username,
          name: member.name,
          status: member.status
        }
      };
    }
  }
}
</script>
