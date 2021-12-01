import VueAxios from 'vue-axios'
import axios from 'axios'
// import config from '../config'

// 통합개발을 위해 잠시변경
// const BASE_URL = 'https://i5d202.p.ssafy.io:8443/'
const BASE_URL = 'https://localhost:8443/'
const DEFAULT_ACCEPT_TYPE = 'application/json'

axios.defaults.baseURL = BASE_URL
axios.defaults.headers['Content-Type'] = DEFAULT_ACCEPT_TYPE

export default { VueAxios, axios }
