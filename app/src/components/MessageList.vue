<template>
  <div class="message-list">
    <h4>Messages</h4>
    <hr>
    <div id="chat-messages" class="message-group" >
      <perfect-scrollbar>
      <div class="message" v-for="(message, index) in messages" :key="index">
        <div class="clearfix">
          <h4 class="message-title float-left">{{ message.name }}</h4>
          <small class="text-muted float-right">@{{ message.username }}</small>
        </div>
        <p class="message-text float-left">
          {{ message.text }}
        </p>
        <div class="clearfix">
          <small class="text-muted float-right">{{ message.date }}</small>
        </div>
      </div>
      </perfect-scrollbar>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import store from "@/store";
import moment from "moment";
import {watch} from "vue";
import {useAuth0} from "@auth0/auth0-vue";

export default {
  name: 'message-list',
  setup() {
    const auth0 = useAuth0();
    return {
      auth0: auth0
    }
  },
  computed: {
    ...mapState([
      'messages',
      'activeChannel'
    ])
  },
  mounted() {
    const auth0 = this.auth0;
    let channelMessages;

    async function subscriberToChannelMessages(channelId) {
      const accessToken = await auth0.getAccessTokenSilently();
      channelMessages?.close();
      channelMessages = new EventSource(`http://localhost:8080/channels/${channelId}/messages?access_token=${accessToken}`);
      channelMessages.onmessage = function (event) {
        const message = JSON.parse(event.data);
        store.commit('addMessage', {
          name: message.senderName,
          username: message.senderUsername,
          text: message.text,
          date: moment(message.createdAt).format('h:mm:ss a D-MM-YYYY')
        });
      };
    }
    watch(this.activeChannel, subscriberToChannelMessages)
  }
}
</script>

<style>
.message-list {
  margin-bottom: 15px;
  padding-right: 15px;
}
.message-group {
  height: 65vh !important;
  overflow-y: scroll;
}
.message {
  border: 1px solid lightblue;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 15px;
}
.message-title {
  font-size: 1rem;
  display:inline;
}
.message-text {
  color: gray;
  margin-bottom: 0;
}
.user-typing {
  height: 1rem;
}
</style>
