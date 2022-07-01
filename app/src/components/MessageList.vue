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
import {useAuth0} from "@auth0/auth0-vue";

export default {
  name: 'message-list',
  setup() {
    const auth0 = useAuth0();
    return {
      auth0: auth0
    }
  },
  data(){
    return {
      messages: [],
      channelMessagesStream: null
    }
  },
  computed: {
    ...mapState([
      'activeChannel'
    ])
  },
  async created() {
    if (this.activeChannel) {
      await this.subscriberToChannelMessages(this.messages, this.activeChannel.id);
    }
  },
  watch: {
    activeChannel(newChannel) {
      this.messages = [];
      this.subscriberToChannelMessages(this.messages, newChannel.id);
    },
  },
  methods: {
    async subscriberToChannelMessages(messages, channelId) {
      const accessToken = await this.auth0.getAccessTokenSilently();
      this.channelMessagesStream?.close();
      this.channelMessagesStream = new EventSource(`http://localhost:8080/channels/${channelId}/messages?access_token=${accessToken}`);
      this.channelMessagesStream.onmessage = function (event) {
        const message = JSON.parse(event.data);
        messages.push({
          name: message.senderName,
          username: message.senderUsername,
          text: message.text,
          date: moment(message.createdAt).format('h:mm:ss a D-MM-YYYY')
        });
      };
    }
  }
}
</script>

<style>
.message-list {
  margin-bottom: 15px;
  padding-right: 15px;
}
.message-group {
  height: 60vh !important;
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
