<template>
  <el-container class="conference-box" v-if="state.participants[state.name]">
		<el-main>
				<div class="row">
					<div class="total-box col-12 col-md-4" v-for="participant in state.participants" :key="participant.name" :id="participant.name">
						<video :id="'video-' + participant.name" class="video-box" autoplay></video>
						<div class="video-box" :class="[participant.isVideoState() ? 'd-none' : 'd-inline-block']">비디오OFF</div>
						<span class="name-box">
							<i :class="[participant.isMicState() ? 
								'el-icon-turn-off-microphone text-danger' : 
								'el-icon-microphone text-success' ]"></i>
							{{participant.name}}
						</span>
					</div>
				</div>
		</el-main>
		<el-footer class="p-0">
				<el-button v-if="state.participants[state.name].isVideoState()" 
					icon="el-icon-video-camera" type="success" class="d-inline-flex flex-row" round @click="videoOnOff">
					<span class="d-none d-md-block">비디오끄기</span>
				</el-button>
				<el-button v-else type="danger" icon="el-icon-video-camera" class="d-inline-flex flex-row" round @click="videoOnOff">
					<span class="d-none d-md-block">비디오켜기</span>
				</el-button>

				<el-button v-if="state.participants[state.name].isMicState()" 
				type="danger" icon="el-icon-turn-off-microphone" class="d-inline-flex flex-row" round @click="micOnOff">
					<span class="d-none d-md-block">마이크켜기</span>
				</el-button>
				<el-button v-else type="success" icon="el-icon-microphone" class="d-inline-flex flex-row" round @click="micOnOff">
					<span class="d-none d-md-block">마이크끄기</span>
				</el-button>

				<el-button type="danger" icon="el-icon-phone" class="d-inline-flex flex-row" round @click="goMain">
					<span class="d-none d-md-block">방나가기</span>
				</el-button>
		</el-footer>
	</el-container>
</template>

<script>
import { onMounted, reactive, ref } from 'vue'
import {useStore} from 'vuex'
import {useRouter} from 'vue-router'
import kurentoUtils from 'kurento-utils'

export default {
  name: 'ConferenceMain',
  props: {
    conferenceId: {
      type: Number,
      default: 0
    },
    owner: {
      type: String,
      default: ''
    },
  },
	setup(props) {
		// const ws = new WebSocket('wss://' + 'location:8443' + '/groupcall');
		const store = useStore()
		const router = useRouter()
		const user = store.getters['root/getUserInfo']
		// const PARTICIPANT_MAIN_CLASS = 'participant main';
		// const PARTICIPANT_CLASS = 'participant';
		const state = reactive({
			ws: null,
			name: user.nickname,
			room: props.conferenceId,
			participants: {},
		})

		const connect = function() {
			state.ws = new WebSocket('wss://' + location.host + '/groupcall');
			state.ws.onopen = function(event) {
				// console.log("Successfully connected to the echo websocket server...")
				register()
			}
			state.ws.onmessage = function(message) {
				let parsedMessage = JSON.parse(message.data);
				// console.info('Received message: ' + message.data);
	
				switch (parsedMessage.id) {
				case 'existingParticipants':
					onExistingParticipants(parsedMessage);
					break;
				case 'newParticipantArrived':
					onNewParticipant(parsedMessage);
					break;
				case 'participantLeft':
					onParticipantLeft(parsedMessage);
					break;
				case 'receiveVideoAnswer':
					receiveVideoResponse(parsedMessage);
					break;
				case 'iceCandidate':
					state.participants[parsedMessage.name].rtcPeer.addIceCandidate(parsedMessage.candidate, function (error) {
								if (error) {
								console.error("Error adding candidate: " + error);
								return;
								}
						});
						break;
				case 'videoState':
					state.participants[parsedMessage.name].switchVideoOnOff(parsedMessage.videoState) // 화면 on/off를 누른 참가자의 switchVideoOnOff함수 실행
					break;
				case 'micState':
					state.participants[parsedMessage.name].switchMicOnOff(parsedMessage.micState) // 화면 on/off를 누른 참가자의 switchVideoOnOff함수 실행
					break;
				default:
					console.error('Unrecognized message', parsedMessage);
				}
			}
		}
		onMounted(() => {
			connect()
			window.addEventListener('beforeunload', function() {
				state.ws.close();
			});
		})
		// onBeforeUnmount(() => {
		// 	window.removeEventListener('beforeunload', function(e) {
    //   	console.log('새로고침')
    //   	e.preventDefault()
		// 		e.returnValue = ''
    // 	})
		// })

		
		// 4번 실행 
		// 1. 회의방 참가 
		const register = function() {
			let message = {		
				id : 'joinRoom',		
				name : state.name,
				room : state.room,
				videoState : true,
				micState: false
			}
			// 2. 메세지전송
			sendMessage(message);
		}

		const onNewParticipant = function(request) {
			receiveVideo(request.name, true, false);
		}

		const receiveVideoResponse = function(result) {
			state.participants[result.name].rtcPeer.processAnswer(result.spdAnswer, function (error) {
				if (error) return console.error (error);
			});
		}

		const callResponse = function(message) {
			// console.log('callResponse', message.response)
			if (message.response != 'accepted') {
				// console.info('Call not accepted by peer. Closing call');
				stop();
			} else {
				webRtcPeer.processAnswer(message.spdAnswer, function (error) {
					if (error) return console.error (error);
				});
			}
		}

		const onExistingParticipants = function(msg) {
			let constraints = {
				audio : true,
				video : {
					mandatory : {
						maxWidth : 400,
						maxFrameRate : 15,
						minFrameRate : 15
					}
				}
			};
			// console.log(name + " registered in room " + room);
			// 7번 participant.js에서 참가자 만들기
			
			var participant = new Participant(state.name, true, false);
			state.participants[state.name] = participant;
			setTimeout(() => {
				let video = participant.getVideoElement();
	
				let options = {
							localVideo: video,
							mediaConstraints: constraints,
							onicecandidate: participant.onIceCandidate.bind(participant)
						}
				participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendonly(options, function (error) {
						if(error) return console.error(error);
						this.generateOffer (participant.offerToReceiveVideo.bind(participant));
				});
			let dataLen = msg.data.length
			for (let i = 0; i < dataLen; i++) {
				receiveVideo(msg.data[i], msg.videoState[i], msg.micState[i])
			}
			}, 100)
		}

		// 비디오 on/off관련 함수
		const videoOnOff = function () {
			let message = {		
				id : 'videoOnOff',		
				name : state.name,
				room : state.room,
				videoState : state.participants[state.name].videoState
			}
			let participant = state.participants[state.name]
			participant.videoState = !participant.videoState
			participant.isVideo()
			sendMessage(message)
		}

		// 마이크 on/off관련 함수
		const micOnOff = function () {
			let message = {		
				id : 'micOnOff',		
				name : state.name,
				room : state.room,
				micState : state.participants[state.name].micState
			}
			state.participants[state.name].micState = !state.participants[state.name].micState
			sendMessage(message)
			// let video = document.getElementById('video-' + state.name)
			// console.log(video.muted)
			// if (video.muted) {
			// 	video.muted = false
			// } else {
			// 	video.muted = true
			// }
		}
		// 방 나기가 관련 함수
		const leaveRoom = function() {
			sendMessage({
				id : 'leaveRoom'
			});

			for ( var key in state.participants) {
				state.participants[key].dispose();
			}

			state.ws.close();
			router.replace({name: 'Main'})
		}

		// 방나가기 버튼 클릭시 동작함수
		const goMain = function () {
			if (user.nickname === props.owner) {
				if(confirm('당신은호스트입니다 \n호스트가 회의를 종료하면 방은 삭제됩니다 \n진짜나가시겠습니까?')){
					store.dispatch('root/deleteRoom', {
						sequence: props.conferenceId
          })
          .then(function () {
						leaveRoom()
          })
          .catch(function (err) {
            alert(err.response.data.message)
          })
				}
			} else {
				if(confirm('화상회의를 종료하시겠습니까?')) {
					leaveRoom()
				}
			}
		}
		const receiveVideo = function(sender, senderVideo, senderMic) {
			let participant = new Participant(sender, senderVideo, senderMic);
			state.participants[sender] = participant;
			setTimeout(() => {
				participant.isMic()
				let video = participant.getVideoElement();
	
				let options = {
						remoteVideo: video,
						onicecandidate: participant.onIceCandidate.bind(participant)
					}
	
				participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
						function (error) {
							if(error) {
								return console.error(error);
							}
							this.generateOffer (participant.offerToReceiveVideo.bind(participant));
				});
			}, 100)

		}

		const onParticipantLeft = function(request) {
			// console.log('Participant ' + request.name + ' left');

			let participant = state.participants[request.name];
			participant.dispose();
			delete state.participants[request.name];

			if (request.name === props.owner) {
				alert('방장이 방삭제함')
				state.ws.close()
				router.replace({name: 'Main'})
			}
		}
	

		// 메세지를 backend단으로 이동 (CallHandler.java로 이동)
		const sendMessage = function(message) {
			let jsonMessage = JSON.stringify(message);
			// console.log('Sending message: ' + jsonMessage);
			state.ws.send(jsonMessage);
		}

		const Participant = function(name, videoState, micState) {
			this.name = name;
			// var container = document.createElement('div');
			// container.className = isPresentMainParticipant() ? PARTICIPANT_CLASS : PARTICIPANT_MAIN_CLASS;
			// container.id = name;
			// var span = document.createElement('span');
			// var video = document.createElement('video');
			var rtcPeer;
			this.videoState = videoState
			this.micState = micState
			// container.appendChild(video);
			// container.appendChild(span);
			// container.onclick = switchContainerClass;
			// document.getElementById('participants').appendChild(container);

			// span.appendChild(document.createTextNode(name));

			// video.id = 'video-' + name;
			// video.className = isVideoState() ? 'd-inline' : 'd-none'
			// video.autoplay = true
			// video.controls = false;

			this.getElement = function() {
				return container;
			}

			this.getVideoElement = function() {
				
				// let videoId = 'video-' + name
				// console.log(videoId)
				// console.log(this.$refs.videoId.focus())
				// let video = document.getElementById('video-' + name)
				// console.log(video)
				return this.isVideo();
			}

			// function switchContainerClass() {
			// 	if (container.className === PARTICIPANT_CLASS) {
			// 		var elements = Array.prototype.slice.call(document.getElementsByClassName(PARTICIPANT_MAIN_CLASS));
			// 		elements.forEach(function(item) {
			// 				item.className = PARTICIPANT_CLASS;
			// 			});

			// 			container.className = PARTICIPANT_MAIN_CLASS;
			// 		} else {
			// 		container.className = PARTICIPANT_CLASS;
			// 	}
			// }

			// function isPresentMainParticipant() {
			// 	return ((document.getElementsByClassName(PARTICIPANT_MAIN_CLASS)).length != 0);
			// }

			// 비디오 on / off 관련함수
			this.switchVideoOnOff = function (videoSwitch) {
				this.videoState = videoSwitch
				this.isVideo()
			}

			this.switchMicOnOff = function (micSwitch) {
				this.micState = micSwitch
				this.isMic()
			}

			// video정보를 videoState에 따라 변경하고 video값을 리턴해준다.
			this.isVideo = function () {
				let video = document.getElementById('video-' + name)
				video.className = this.isVideoState() ? 'd-inline' : 'd-none'
				return video
			}

			this.isMic = function () {
				let video = document.getElementById('video-' + name)
				video.muted = this.isMicState()
			}

			// 비디오 상태 return
			this.isVideoState = function() {
				return (this.videoState)
			}

			// 마이크 상태 return
			this.isMicState = function() {
				return this.micState
			}

			// 9번 실행 같이
			this.offerToReceiveVideo = function(error, offerSdp, wp){
				if (error) return console.error ("sdp offer error")
				// console.log('Invoking SDP offer callback function');
				var msg =  { id : "receiveVideoFrom",
						sender : name,
						sdpOffer : offerSdp
					};
				sendMessage(msg);
			}
			// 9번 이거 실행
			this.onIceCandidate = function (candidate, wp) {
					// console.log("Local candidate" + JSON.stringify(candidate));

					var message = {
						id: 'onIceCandidate',
						candidate: candidate,
						name: name
					};

					sendMessage(message);
			}
			// Object.defineProperty() 정적 메서드는 객체에 직접 새로운 속성을 정의하거나 이미 존재하는 속성을 수정한 후, 그 객체를 반환합니다.
			
			Object.defineProperty(this, 'rtcPeer', { writable: true});

			this.dispose = function() {
				// console.log('Disposing participant ' + this.name);
				this.rtcPeer.dispose();
				// container.parentNode.removeChild(container);
			};
		}
		return {micOnOff, videoOnOff, goMain, router, connect, state, register, sendMessage, onParticipantLeft, receiveVideo,onExistingParticipants, callResponse, Participant, leaveRoom}
		},
}
</script>

<style scoped>
	.conference-box {
    height: 100vh;
  }

	.video-onoff {
	font-size: 10px;
	border-radius: 5px;
	border: none;
	background-color: #FFEEE4;
	padding: 10px;
	}

	.room-exit {
	font-size: 10px;
	border-radius: 5px;
	border: none;
	background-color: #FFEEE4;
	padding: 10px;
	}

	.total-box {
		width: 400px;
		position: relative;
		padding: 0;
	}

	.video-box {
		width: 400px;
		background-color: #171717;
	}

	.total-box video {
		width: 100% !important;
		height: auto !important;
		background-color: #a0a0a0;
	}

	@media (min-width: 700px) {
		.total-box {
			height: 300px;
		}

		.video-box {
			height: 300px;
		}

		.total-box video {
			height: 300px !important;
		}
	}


	.total-box:before {
		position: absolute;
	}


	.name-box {
		position: absolute;
		bottom: 0;
		left: 50%;
		transform: translateX(-50%);
		background-color: rgba( 255, 255, 255, 0.5 );
	}

</style>