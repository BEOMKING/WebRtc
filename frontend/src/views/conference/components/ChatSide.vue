<template>
<div class="chat-show">
  
    <p>실시간 채팅</p>

    <div id="socket">
      <div id="chatMain">
        <div v-for="(item, idx) in state.form.recvList" :key="idx" >
          <!-- <div class="sentence d-flex" :class="[ item.userName === user.nickname ? 'flex-row-reverse' : 'flex-row']"> -->
          <div class="sentence" :class="{'text-end' : item.userName === user.nickname}">
            <div v-if="item.userName !== user.nickname" class="name-box "> {{ item.userName }} </div>
            <div class="bubble" :class="[item.userName === user.nickname ? 'right' : 'left' ]">
              <span> {{ item.message }} </span>
            </div>
          </div>
        </div>
      </div>
      <!-- 유저이름:
      <input
        v-model="state.form.userName"
        type="text"
      > -->
      <div id="chatInput">
      {{user.nickname}} : <input
        v-model="state.form.message"
        type="text"
        placeholder="메시지를 입력하세요"
        @keyup="sendMessage"
        id="input"
      >
      </div>
      <!-- <div>
        유저 이름: {{ state.form.userName}}
        내용 : {{ state.form.message}}
      </div> -->
    </div>
</div>
</template>

<style scoped>
  #socket {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: left;
    color: #2c3e50;
    margin-top: 30px;
    margin-left: 30px;
    margin-bottom: 30px;
    /* width: 400px; */
    height: 600px;
    /* height: 100%; */
  }
  #chatMain{
    height: 90%;
    width:100%;
    overflow: auto;
  }
  #chatInput{
    height: 5%;
    width: 100%;
    margin-top: 5%;
  }
  #input {
    width: 80%;
  }

  .sentence {
    background-color: #F6F6F6;
    border-radius: 10px;
    margin: 10px 15px 15px 0;
  }

  .chat-show {
    height: 100%;
    background-color: #F6F6F6;
  }

  .chat-show::-webkit-scrollbar {
    width: 1px;
  }

  .bubble {
    position: relative;
    display: inline-block;
  }

  .bubble span {
    display: inline-block;
    padding: 10px;
    background: #F6921E;
    border-radius: 20px;
  }

  .bubble:after {
    content: '';
    position: absolute;
    width: 0;
    height: 0;
    border-style: solid;
  }

  .bubble.right:after, .bubble.left:after {
    border-width: 10px 15px;
    top: 50%;
    margin-top: -10px;
  }

  .bubble.left:after {
    border-color: transparent #F6921E transparent transparent;
    left: -25px;
  }

  .bubble.right:after {
    border-color: transparent transparent transparent #F6921E;
    right: -25px;
  }

  .name-box {
    margin-right: 1rem;
    display: inline-block;
  }

</style>

<script>
import { onMounted, reactive } from 'vue'
import Stomp from 'webstomp-client'
import SockJS from 'sockjs-client'
import { useStore } from 'vuex'

export default {
  name: 'ChatSide',
  components: {
    },
  props: {
    conferenceId: Number
  },
  setup (props) {
    const store = useStore()

    const user = store.getters['root/getUserInfo']

    const state = reactive({
      conferenceId: '',
      form: {
        // token: localStorage.getItem('jwt'),
        userName: user.nickname,
        message: "",
        recvList: [],
        stompClient: "",
      },
    })

    onMounted(() => {
      connect()
      
    })

    const sendMessage = function (e) {
      // console.log('eeeeee', e.keyCode, 'username', state.form.userName, 'msg', state.form.message, 'recvList', state.form.recvList )
      if(e.keyCode === 13 && state.form.userName !== '' && state.form.message !== ''){
        send()
        state.form.message = ''
        scrollToEnd()
      }
    }

    const scrollToEnd = function() { //스크롤 맨 아래로 내리게
      const container = document.getElementById("chatMain");
      container.scrollTop = container.scrollHeight;
    }

    const send = function() {
      // console.log("Send message:" + state.form.message);
      if (state.form.stompClient && state.form.stompClient.connected) {
        const msg = {
          type: "CHAT",
          roomId: props.conferenceId,
          userName: state.form.userName,
          message: state.form.message,
          // recvList: state.form.recvList,
        };
        // console.log('메세지확인', msg)
        state.form.stompClient.send("/pub/chat/message", JSON.stringify(msg), {});
      }
    }

    const connect = function() {
      // "https://3.35.171.221:8443/ws-stomp"
      const serverURL = "https://i5d202.p.ssafy.io:8443/ws-stomp" 
      let socket = new SockJS(serverURL);
      state.form.stompClient = Stomp.over(socket);
      // 기본적으로 stomp에는 debug가 내장되어있다. 그래서 console창에 로그가 많이남음
      // 이를 해결하기위해서 stompClient.debug를 빈함수로 만들어서 로그창이 안남도록 설정함
      state.form.stompClient.debug = () => {};
      // console.log(`소켓 연결을 시도합니다. 서버 주소: ${serverURL}`)
      state.form.stompClient.connect(
        {},
        frame => {
          // 소켓 연결 성공
          state.form.stompClient.connected = true;
          // console.log('소켓 연결 성공', frame, 'id', props.conferenceId);
          // 서버의 메시지 전송 endpoint를 구독합니다.
          // 이런형태를 pub sub 구조라고 합니다.
          state.form.stompClient.subscribe('/sub/chat/room/' + props.conferenceId,
          res => {
            // console.log('구독으로 받은 메시지 입니다.', res.body);

            // 받은 데이터를 json으로 파싱하고 리스트에 넣어줍니다.
            state.form.recvList.push(JSON.parse(res.body))
          });
        },
        error => {
          // 소켓 연결 실패
          // console.log('소켓 연결 실패', error);
          state.form.stompClient.connected = false;
        }
      );
    }

    return { store, user, state, sendMessage, send, connect}
  }
}
</script>

