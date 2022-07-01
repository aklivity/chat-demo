export default {
  async unload({ commit }) {
    commit('reset');
    window.localStorage.clear();
  }
}
