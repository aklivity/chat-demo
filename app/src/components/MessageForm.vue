<template>
  <div class="message-form ld-over">
    <small class="text-muted">@{{ user.username }}</small>
    <b-form @submit.prevent="onSubmit" class="ld-over" v-bind:class="{ running: sending }">
      <div class="ld ld-ring ld-spin"></div>
      <b-alert variant="danger" :show="hasError">{{ error }} </b-alert>
      <b-form-group>
      <b-form-input id="message-input"
                    type="text"
                    v-model="message"
                    placeholder="Enter Message"
                    autocomplete="off"
                    required>
      </b-form-input>
      </b-form-group>
      <div class="clearfix" style="float: right">
        <b-button type="submit" variant="primary" class="float-right">
          Send
        </b-button>
      </div>
    </b-form>
  </div>
</template>

<script>
import {mapActions, mapState, mapGetters, mapMutations} from 'vuex'
import {useAuth0} from "@auth0/auth0-vue";
import {default as axios} from "axios";
export default {
  name: 'message-form',
  setup() {
    const auth0 = useAuth0();
    return {
      auth0: auth0
    }
  },
  data() {
    return {
      message: ''
    }
  },
  computed: {
    ...mapState([
      'user',
      'sending',
      'error',
      'activeChannel'
    ]),
    ...mapMutations([
       'setError',
       'setSending'
    ]),
    ...mapGetters([
      'hasError'
    ])
  },
  methods: {
    ...mapActions([
      'sendMessage',
    ]),
    async onSubmit() {
      try {
        this.setError('');
        this.setSending(true);
        const auth0 = useAuth0();
        const accessToken = await auth0.getAccessTokenSilently();

        const result = await axios.post(`http://localhost:8080/channels/${this.activeChannel.id}/messages`, {
          senderId: this.user.id,
          senderUsername: this.user.username,
          senderName: this.user.name,
          text: this.message,
          date: new Date()
        },{
          headers: { Authorization: `Bearer ${accessToken}`}
        })
        if(result.status == 204) {
          this.message = '';
        }
      } catch (error) {
        this.setError(error.message)
      } finally {
        this.setSending(false);
      }
    }
  }
}
</script>
