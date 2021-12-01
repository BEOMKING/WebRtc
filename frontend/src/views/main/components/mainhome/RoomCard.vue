<template>
  <el-card v-if="props.item" class="box-card">
    <template #header>
      <div class="d-flex justify-content-between" >
        <!-- :class="{'study': roomStyle(state) }" -->

        <span :class="{ 'study' : (state.room.conferenceCategory.sequence === 1), 'sing' : (state.room.conferenceCategory.sequence === 2), 'talk' : (state.room.conferenceCategory.sequence === 3) }">{{state.room.number}}. {{state.room.name}}</span>
        <span style="overflow:hidden white-space:nowrap">
          <i v-if="state.room.conferenceCategory.sequence === 1" class="el-icon-notebook-2" :class="{'study-icon': true}">study</i>
          <i v-else-if="state.room.conferenceCategory.sequence === 2" class="el-icon-microphone" :class="{'sing-icon': true}">sing</i>
          <i v-else class="el-icon-user" :class="{'talk-icon': true}">talk</i>
          <i v-if="state.room.password" class="el-icon-key key" ></i>
        </span>
      </div>
    </template>
    <div class="d-flex flex-column align-items-center">
      <span>{{state.room.description}}</span>
      <el-button class ="enter-room" @click="goRoom">입장하기</el-button>
    </div>
    <room-password-dialog
      :open="state.roomPasswordDialogOpen"
      :password="state.room.password"
      @closeroomPasswordDialog="onCloseroomPasswordDialog"
      @goRoomEmit="goSecretRoom"/>
  </el-card>
  <h2 v-else class="room-else-box">
    <i class="el-icon-error x-icon"></i>
    존재하는 방이 없습니다. 방을 개설해주세요
  </h2>
</template>

<script>
import { reactive } from '@vue/reactivity'
import { useRouter } from 'vue-router'

import RoomPasswordDialog from './room/RoomPasswordDialog.vue'

export default {
  name: 'RoomCard',
  components: {
    RoomPasswordDialog,
  },
  props: {
    item: Object
  },
  setup (props) {
    const router = useRouter()
    const state = reactive({
      room: {
        number: props.item.sequence,
        name: props.item.name,
        owner: props.item.owner,
        participantLimit: props.item.participantLimit,
        password: props.item.password,
        conferenceCategory: props.item.conferenceCategory,
        description: props.item.description
      },
      roomPasswordDialogOpen: false
    })

    // 특정방으로 이동한다.
    const goRoom = function () {
      if (state.room.password) {
        state.roomPasswordDialogOpen = true
      } else {
        // replace는 뒤로가기를 남기지않음
        router.replace({name: 'Conference', params: { 
          conferenceId : state.room.number,
          owner: props.item.ownerNick,
          name: state.room.name
        }})
      }
    }

    const goSecretRoom = function () {
      router.replace({name: 'Conference', params: { 
        conferenceId : state.room.number,
        owner: props.item.ownerNick,
        name: state.room.name
      }})
    }

    const onCloseroomPasswordDialog = function () {
      state.roomPasswordDialogOpen = false
    }

    const roomStyle = function (state) {
      if(state.room.conferenceCategory.sequence == 1) return ture;
      else false;
    }

    return {props, state, router, goRoom, onCloseroomPasswordDialog, goSecretRoom}
  }
}
</script>

<style scoped>
  .key {
    background-color: #E6A23C;
  }

  .enter-room {
    margin: 35px;
    background-color: #F6F6F6;
  }

  .study {
    color: #E53A40;
    border-radius: 5px;
  }

  .sing {
    color: #30A9DE;
    border-radius: 5px;
  }

  .talk {
    color: #EFDC05;
    border-radius:5px;
  }

  .study-icon {
    font-weight: bold;
    border: 2px solid #E53A40;
    border-radius: 5px;
  }

  .sing-icon {
    font-weight: bold;
    border: 2px solid #30A9DE;
    border-radius: 5px;
  }

  .talk-icon {
    font-weight: bold;
    border: 2px solid #EFDC05;
    border-radius: 5px;
  }

  .room-else-box {
    width: 60vw;
  }

  .x-icon {
    color: #ff4040;
  }


</style>