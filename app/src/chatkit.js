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
    id: user.id,
    username: user.username,
    name: user.name,
    status: user.status
  }));
  store.commit('setUsers', members);
}

async function connectUser(userId) {

  const response = await axios.get(`http://localhost:8080/users/${userId}`, {});
  const user = response.data[0];

  let status = "online";
  currentUser = {
    id: user.id,
    username: user.username,
    name: user.name,
    status: status
  };

  await axios.put(`http://localhost:8080/users/${userId}`, {
    id: userId,
    username: user.username,
    name: user.name,
    status: status
  });

  return currentUser;
}

async function setActiveRoom(roomId) {
  activeRoom = {
    "id": roomId
  }
}

async function subscribeToRoom(roomId) {
  store.commit('clearChatRoom');

  await axios.post('http://localhost:8080/subscription/subscribe', {
    userId: currentUser.id,
    channelId: roomId}
  );

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

async function disconnectUser() {
  await axios.put(`http://localhost:8080/users/${currentUser.id}`, {
    id: currentUser.id,
    username: currentUser.username,
    name: currentUser.name,
    status: "offline"
  });

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
