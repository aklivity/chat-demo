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
import { mapState, mapActions } from 'vuex'

export default {
  name: 'ChannelList',
  computed: {
    ...mapState([
      'channels',
      'activeChannel'
    ]),
  },
  methods: {
    ...mapActions([
      'changeChannel'
    ]),
    onChange(channel) {
        this.changeChannel(channel.id)
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
