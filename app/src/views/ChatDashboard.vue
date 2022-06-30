<template>
  <div class="chat-dashboard">
    <b-container fluid class="ld-over" v-bind:class="{ running: loading }">
      <div class="ld ld-ring ld-spin"></div>
      <b-row>
        <b-col cols="2">
          <ChannelList />
        </b-col>

        <b-col cols="8">
          <b-row>
            <b-col id="chat-content">
              <MessageList />
            </b-col>
          </b-row>
          <b-row>
            <b-col>
              <MessageForm />
            </b-col>
          </b-row>
        </b-col>

        <b-col cols="2">
          <UserList />
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>

<script>
import ChannelList from '@/components/ChannelList.vue'
import MessageList from '@/components/MessageList.vue'
import MessageForm from '@/components/MessageForm.vue'
import UserList from '@/components/UserList.vue'
import {mapMutations, mapState} from 'vuex';
import {useAuth0} from "@auth0/auth0-vue";
import jwt_decode from "jwt-decode";
import {default as axios} from "axios";

export default {
  name: 'Chat',
  setup() {
    const auth0 = useAuth0();
    return {
      auth0: auth0
    }
  },
  components: {
    ChannelList,
    UserList,
    MessageList,
    MessageForm
  },
  computed: {
    ...mapState([
      'loading',
      'activeChannel'
    ]),
  },
  methods: {
    ...mapMutations([
      'setError',
      'setLoading',
      'setUser',
      'setReconnect',
      'setChannels',
      'setActiveChannel',
      'setError'
    ]),
    async onLoad() {
      try {
        this.setError('')
        this.setLoading(true)
        const accessToken = await this.auth0.getAccessTokenSilently();
        const decodedToken = jwt_decode(accessToken);
        const userId = decodedToken.sub;
        const userInfo = this.auth0.user.value;
        const status = "online";

        const currentUser = {
          id: userId,
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
        this.setUser({
          id: currentUser.id,
          username: currentUser.username,
          name: currentUser.name,
          state: currentUser.status
        })

        const response = await axios.get(`http://localhost:8080/channels`, {
          headers: { Authorization: `Bearer ${accessToken}`}
        });
        const channels = response.data.map(channel => ({
          id: channel.id,
          name: channel.name
        }));

        this.setChannels(channels);
        const activeChannel = this.activeChannel || channels[0]; // pick last used channel, or the first one
        this.setActiveChannel({
          id: activeChannel.id,
          name: activeChannel.name
        });

        await axios.post('http://localhost:8080/subscription/subscribe', {
          userId: currentUser.id,
          channelId: activeChannel.id
        }, {
          headers: {
            Authorization: `Bearer ${accessToken}`
          }
        });

        this.setReconnect(false);
        this.setLoading(false);
      } catch (error) {
        this.setError(error.message);
      } finally {
        this.setLoading(false);
      }
    }
  },
  beforeMount(){
    this.onLoad()
  },
}
</script>
