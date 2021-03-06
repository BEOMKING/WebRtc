package com.ssafy.kurento;

import java.io.IOException;

import org.kurento.client.IceCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
// TextWebSockerHandler 텍스트 WebSocker 요청을 처리하기 위해 구현
// 이 클래스의 중심 부분은 handleTextMessage 메소드
// 시그널링 프로토콜의 서버 부분 구현
public class CallHandler extends TextWebSocketHandler{
	
	private static final Logger log = LoggerFactory.getLogger(CallHandler.class);
	
	private static final Gson gson = new GsonBuilder().create();
	
	@Autowired
	private RoomManager roomManager;
	
	@Autowired
	private UserRegistry registry;
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
		final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
		
		final UserSession user = registry.getBySession(session);
		
		if(user!=null) {
			log.debug("Incoming message from user '{}': {}", user.getName(), jsonMessage);
		}else {
			log.debug("Incoming message from new user: {}", jsonMessage);
		}
		
		switch (jsonMessage.get("id").getAsString()) {
	      case "joinRoom"://방 입장
	        joinRoom(jsonMessage, session);
	        break;
	      case "receiveVideoFrom"://비디오 수신
	        final String senderName = jsonMessage.get("sender").getAsString();
	        final UserSession sender = registry.getByName(senderName);
	        final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
	        user.receiveVideoFrom(sender, sdpOffer);
	        break;
	      case "leaveRoom"://방 나가기
	        leaveRoom(user);
	        break;
	      case "onIceCandidate"://참가자 등록??
	        JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();

	        if (user != null) {
	          IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
	              candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
	          user.addCandidate(cand, jsonMessage.get("name").getAsString());
	        }
	        break;
		  case "videoOnOff"://비디오 onoff
			  videoOnOff(jsonMessage, user);
			break;
		  case "micOnOff"://마이크 onoff
			  micOnOff(jsonMessage, user);
			  break;
	      default:
	        break;
	    }
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
	  UserSession user = registry.removeBySession(session);
	  roomManager.getRoom(user.getRoomName()).leave(user);
	}

	private void joinRoom(JsonObject params, WebSocketSession session) throws IOException {
	  final String roomName = params.get("room").getAsString();
	  final String name = params.get("name").getAsString();
	  Boolean videoState = params.get("videoState").getAsBoolean();
	  // videoState 유저 비디오상태 -> 처음입장하면 무조건 true로 들어온다.
	  Boolean micState = params.get("micState").getAsBoolean();

	  // videoState 유저 마이크음소거상태 -> 처음입장하면 무조건 false로 들어온다.
	  log.info("PARTICIPANT {}: trying to join room {}, videoState {}", name, roomName, videoState);

	  Room room = roomManager.getRoom(roomName);
	  final UserSession user = room.join(name, videoState, micState, session);
	  registry.register(user);
	}

	private void videoOnOff(JsonObject params, UserSession user)  throws IOException {
		final Room room = roomManager.getRoom(user.getRoomName());
		Boolean state = params.get("videoState").getAsBoolean();
		room.videoState(user, state);
	}
	
	private void micOnOff(JsonObject params, UserSession user)  throws IOException {
		final Room room = roomManager.getRoom(user.getRoomName());
		Boolean state = params.get("micState").getAsBoolean();
		room.micState(user, state);
	}

	private void leaveRoom(UserSession user) throws IOException {
	  final Room room = roomManager.getRoom(user.getRoomName());
	  room.leave(user);
	  if (room.getParticipants().isEmpty()) {
	    roomManager.removeRoom(room);
	  }
	}
}
