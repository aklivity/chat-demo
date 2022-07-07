<template>
  <div class="room-list">
    <h4>Channels</h4>
    <hr>
      <b-list-group-item v-for="(channel, key) in channels"
                        :key="key"
                        :active="activeChannel.id === channel.id"
                        href="#"
                        @click="onChange(channel)">
        # {{ channel.name }}
      </b-list-group-item>
    <div class="clearfix">
      <b-button class="create" v-b-modal.modal-1>Create Channel</b-button>

      <b-modal id="modal-1" title="Create Channel" @ok="onSubmit">
        <b-form-group class="ld-over">
          <b-form-input id="channel-input"
                        type="text"
                        v-model="channelName"
                        placeholder="Enter Channel Name"
                        autocomplete="off"
                        required>
          </b-form-input>
        </b-form-group>
      </b-modal>
    </div>
  </div>
</template>

<script>
import {mapState, mapMutations} from 'vuex'
import {useAuth0} from "@auth0/auth0-vue";
import {default as axios} from "axios";
import {v4} from 'uuid';
import {Buffer} from "buffer";

window.Buffer = window.Buffer || Buffer;

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
      channelName: '',
      channels: {},
      channelsStream: null
    }
  },
  computed: {
    ...mapState([
      'activeChannel',
      'user'
    ]),
  },
  async mounted() {
    const auth0 = this.auth0;
    const channels = this.channels;
    const accessToken = await auth0.getAccessTokenSilently();

    this.channelsStream?.close();
    this.channelsStream = new EventSource(`http://localhost:8080/channels?access_token=${accessToken}`);
    this.channelsStream.onmessage = function (event) {
      let lastEventId = JSON.parse(event.lastEventId);
      let key = Buffer.from(lastEventId[0], "base64").toString("utf8");
      const channel = JSON.parse(event.data);
      channels[key] = {
        id: key,
        name: channel.name
      }
    };

    const activeChannel = this.channels[0];
    if(activeChannel) {
      this.setActiveChannel(activeChannel);
      await this.subscribeToChannel(activeChannel.id);
    }
  },
  watch: {
    activeChannel(newChannel) {
      this.subscribeToChannel(newChannel.id);
    }
  },
  methods: {
    ...mapMutations([
      'setActiveChannel',
      'setLoading',
      'setError'
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
    },
    async onSubmit() {
      try {
        this.setError('');
        const accessToken = await this.auth0.getAccessTokenSilently();
        let channelId = v4();
        await axios.post('http://localhost:8080/channels', {
          name: this.channelName
        }, {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
            "Idempotency-Key": channelId,
          }
        });
      } catch (error) {
        this.setError(error.message)
      }
    }
  }
}
</script>
<style>
.list-group-item.active {
  background-color: #0d9b76 !important;
  border-color: #0d9b76 !important;
}

button.create {
  background-color: #0d9b76 !important;
  border-color: #0d9b76 !important;
  margin-top: 20px;
}

</style>
