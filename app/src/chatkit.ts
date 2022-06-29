import moment from 'moment'
import store from './store'
import {default as axios} from "axios";
import {useAuth0} from "@auth0/auth0-vue";
import jwt_decode from "jwt-decode";

let currentUser:any = null;
let activeChannel:any = null;
let channelMessages:EventSource;
let channelMembers:EventSource;

const auth0 = useAuth0();

async function connectUser() {
  const accessToken = await auth0.getAccessTokenSilently();
  const decodedToken:any = jwt_decode(accessToken);
  const userId = decodedToken.sub;
  const userInfo = auth0.user.value;
  const status = "online";

  currentUser = {
    id: userId,
    username: userInfo.nickname,
    name: userInfo.name,
    status: status
  };

  await axios.put(`http://localhost:8080/users/${userId}`, {
    username: currentUser.username,
    name: currentUser.name,
    status: status
  }, {
    headers: {
      Authorization: `Bearer ${accessToken}`
    }
  });

  return currentUser;
}

async function setActiveChannel(channelId) {
  activeChannel = {
    "id": channelId
  }
}

async function subscribeToChannel(channelId) {
  store.commit('clearChatChannel');

  const accessToken = await auth0.getAccessTokenSilently();

  await axios.post('http://localhost:8080/subscription/subscribe', {
    userId: currentUser.id,
    channelId: channelId
    }, {
        headers: {
          Authorization: `Bearer ${accessToken}`
        }
    });

  channelMessages?.close();
  channelMessages = new EventSource(`http://localhost:8080/channels/${channelId}/messages?access_token=${accessToken}`);
  channelMessages.onmessage = function (event) {
    const message = JSON.parse(event.data);
    store.commit('addMessage', {
      name: message.senderName,
      username: message.senderUsername,
      text: message.text,
      date: moment(message.createdAt).format('h:mm:ss a D-MM-YYYY')
    });
  };

  channelMembers?.close();
  channelMembers = new EventSource(`http://localhost:8080/channels/${channelId}/members?access_token=${accessToken}`);
  channelMembers.onmessage = function (event) {
    const user = JSON.parse(event.data);
    store.commit('addUser', {
      id: user.id,
      username: user.username,
      name: user.name,
      status: user.status
    });
  };

  await setActiveChannel(channelId);

  return activeChannel;
}
async function sendMessage(text:string) {
  const auth0 = useAuth0();
  const accessToken = await auth0.getAccessTokenSilently();

  await axios.post(`http://localhost:8080/channels/${activeChannel.id}/messages`, {
    senderId: currentUser.id,
    senderUsername: currentUser.username,
    senderName: currentUser.name,
    text: text,
    date: new Date()
  },{
    headers: { Authorization: `Bearer ${accessToken}`}
  })
  return "1231";
}

async function getChannels(){

  const accessToken = await auth0.getAccessTokenSilently();

  const response = await axios.get(`http://localhost:8080/channels`, {
    headers: { Authorization: `Bearer ${accessToken}`}
  });
  const channels = response.data.map(channel => ({
    id: channels.id,
    name: channel.name
  }))

  return channels;
}

async function disconnectUser() {
  const accessToken = await auth0.getAccessTokenSilently();

  await axios.put(`http://localhost:8080/users/${currentUser.id}`,{
    id: currentUser.id,
    username: currentUser.username,
    name: currentUser.name,
    status: "offline"
  }, {
    headers: { Authorization: `Bearer ${accessToken}`}
  });

  if (channelMessages != null) {
    channelMessages.close();
  }

  if (channelMembers != null) {
    channelMembers.close();
  }
}

export default {
  connectUser,
  getChannels,
  subscribeToChannel,
  sendMessage,
  disconnectUser
}
