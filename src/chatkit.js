import moment from 'moment'
import store from './store/index'
import {default as axios} from "axios";
import {Buffer} from "buffer";

window.Buffer = window.Buffer || Buffer;

let currentUser = null;
let activeRoom = null;
let roomMessages = null;

async function setMembers(roomId) {
  const response = await axios.get(`http://localhost:8080/channels/${roomId}/members`, {});
  const members = response.data.map(user => ({
    username: user.id,
    name: user.name,
    presence: user.presence.state
  }));
  store.commit('setUsers', members);
}

async function connectUser(userId) {
  let username = userId == 1 ? "akram" : "john";
  let name = userId == 1 ? "Akram" : "John";
  currentUser = {
    id: userId,
    username: username,
    name: name,
    presence: "online"
  };
  return currentUser;
}

async function setActiveRoom(roomId) {
  const response = await axios.get(`http://localhost:8080/channels/${roomId}`, {});
  activeRoom = {
    "id": response.data[0].id
  }
}

async function subscribeToRoom(roomId) {
  store.commit('clearChatRoom');

  if (roomMessages != null) {
    roomMessages.close();
  }
  roomMessages = new EventSource(`http://localhost:8080/channels/${roomId}/messages`);
  roomMessages.onmessage = function (event) {
    const message = JSON.parse(event.data);
    store.commit('addMessage', {
      name: message.senderName,
      username: message.senderUsername,
      text: message.text,
      date: moment(message.createdAt).format('h:mm:ss a D-MM-YYYY')
    });
  };
  await setActiveRoom(roomId);
  await setMembers(roomId);
  return activeRoom;
}
async function sendMessage(text) {
  await axios.post(`http://localhost:8080/channels/${activeRoom.id}/messages`, {
    senderId: currentUser.id,
    senderUsername: currentUser.username,
    senderName: currentUser.name,
    text: text,
    date: new Date()
  })
  return "1231";
}

function disconnectUser() {
  if (roomMessages != null) {
    roomMessages.close();
  }
}

export default {
  connectUser,
  subscribeToRoom,
  sendMessage,
  disconnectUser
}
