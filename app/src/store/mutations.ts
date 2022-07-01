export default {
  setError(state, error) {
    state.error = error;
  },
  setLoading(state, loading) {
    state.loading = loading;
  },
  setUser(state, user) {
    state.user = user;
  },
  setReconnect(state, reconnect) {
    state.reconnect = reconnect;
  },
  setActiveChannel(state, channel) {
    state.activeChannel = channel;
  },
  setChannels(state, channels) {
    state.channels = channels;
  },
  addUser(state, user) {
    state.users.push(user);
  },
 clearChatChannel(state) {
    state.users = [];
    state.messages = [];
  },
  addMessage(state, message) {
    state.messages.push(message)
  },
  setSending(state, status) {
    state.sending = status
  },
  reset(state) {
    state.error = null;
    state.users = [];
    state.messages = [];
    state.channels = [];
    state.user = null
  }
}
