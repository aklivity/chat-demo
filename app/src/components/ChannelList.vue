<template>
  <div class="room-list">
    <h4>Channels</h4>
    <hr>
    <b-list-group v-if="activeChannel">
      <b-list-group-item v-for="channel in channels"
                        :key="channel.name"
                        :active="activeChannel.id === channel.id"
                        href="#"
                        @click="onChange(channel)">
        # {{ channel.name }}
      </b-list-group-item>
    </b-list-group>
  </div>
</template>

<script>
import {mapState, mapMutations} from 'vuex'
import {useAuth0} from "@auth0/auth0-vue";
import {default as axios} from "axios";

export default {
  name: 'ChannelList',
  setup() {
    const auth0 = useAuth0();

    return {
      auth0: auth0
    }
  },
  data() {
    return {
      channels: []
    }
  },
  computed: {
    ...mapState([
      'activeChannel',
      'user'
    ]),
  },
  async created() {
    const auth0 = this.auth0;
    const accessToken = await auth0.getAccessTokenSilently();

    const response = await axios.get(`http://localhost:8080/channels`, {
      headers: { Authorization: `Bearer ${accessToken}`}
    });

    response.data.map(channel => this.channels.push({
      id: channel.id,
      name: channel.name
    }));

    const activeChannel = this.activeChannel || this.channels[0];
    this.setActiveChannel(activeChannel);
    await this.subscribeToChannel(activeChannel.id);
  },
  watch: {
    activeChannel(newChannel) {
      this.subscribeToChannel(newChannel.id);
    }
  },
  methods: {
    ...mapMutations([
      'setActiveChannel',
      'setLoading'
    ]),
    onChange(channel) {
      try {
        this.setActiveChannel({ id: channel.id, name: channel.name })
      } catch (error) {
        this.setError( error.message)
      }
    },
    async subscribeToChannel(channelId) {
      const accessToken = await this.auth0.getAccessTokenSilently();
      await axios.post('http://localhost:8080/subscription/subscribe', {
        userId: this.user.id,
        channelId: channelId
      }, {
        headers: {
          Authorization: `Bearer ${accessToken}`
        }
      });
    }
  }
}
</script>
<style>
.list-group-item.active {
  background-color: #0d9b76 !important;
  border-color: #0d9b76 !important;
}
</style>
