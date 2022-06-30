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

export default {
  name: 'ChannelList',
  setup() {
    const auth0 = useAuth0();

    return {
      auth0: auth0
    }
  },
  computed: {
    ...mapState([
      'channels',
      'activeChannel'
    ]),
    ...mapMutations([
        'setActiveChannel'
    ])
  },
  methods: {
    onChange(channel) {
      try {
        this.setActiveChannel({ id: channel.id, name: channel.name })
      } catch (error) {
        this.setError( error.message)
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
</style>
