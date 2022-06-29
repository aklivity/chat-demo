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
import {mapActions, mapState} from 'vuex';

export default {
  name: 'Chat',
  components: {
    ChannelList,
    UserList,
    MessageList,
    MessageForm
  },
  computed: {
    ...mapState([
      'loading'
    ])
  },
  methods: {
    ...mapActions([
      'load'
    ]),
    async onLoad() {
      await this.load();
    }
  },
  beforeMount(){
    this.onLoad()
  },
}
</script>
