import chatkit from '../chatkit';

// Helper function for displaying error messages
function handleError(commit, error) {
  const message = error.message || error.info.error_description;
  commit('setError', message);
}

export default {
  async load({ commit, state }) {
    try {
      commit('setError', '');
      commit('setLoading', true);

      const currentUser = await chatkit.connectUser();
      commit('setUser', {
        id: currentUser.id,
        username: currentUser.username,
        name: currentUser.name,
        state: currentUser.status
      });
      commit('setReconnect', false);

      const channels = await chatkit.getChannels();
      commit('setChannels', channels);

      // Subscribe user to a channel
      const activeChannel = state.activeChannel || channels[0]; // pick last used channel, or the first one
      commit('setActiveChannel', {
        id: activeChannel.id,
        name: activeChannel.name
      });
      await chatkit.subscribeToChannel(activeChannel.id);

      return true;
    } catch (error) {
      handleError(commit, error)
    } finally {
      commit('setLoading', false);
    }
  },
  async changeChannel({ commit }, channelId) {
    try {
      const { id, name } = await chatkit.subscribeToChannel(channelId);
      commit('setActiveChannel', { id, name });
    } catch (error) {
      handleError(commit, error)
    }
  },
  async sendMessage({ commit }, message) {
    try {
      commit('setError', '');
      commit('setSending', true);
      const messageId = await chatkit.sendMessage(message);
      return messageId;
    } catch (error) {
      handleError(commit, error)
    } finally {
      commit('setSending', false);
    }
  },
  async unload({ commit }) {
    commit('reset');
    await chatkit.disconnectUser();
    window.localStorage.clear();

  }
}
