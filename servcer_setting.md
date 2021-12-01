# 파이팅

# Ubuntu - Docker 설치 /local환경
실행환경 > VirtualBox-6.1.22 Ubuntu-16.04.7 //각각 다운로드  
- Virtual Box 설치 후 새로 만들기 클릭 후 가상 머신 만들기 (이름은 알아서 설정)  
- 가상 머신에 원하는 크기의 메모리 할당  
- 하드 디스크 설정 > 새로운 가상 하드 디스크를 만들어 사용  
- 하드 디스크 파일 종류 VDI(Virtual Disk Image)  
- 물리적 하드 드라이브에 저장 동적할당/고정크기 알아서 설정  
- 가상 하드 디스크 만들기/파일 위치 및 크기 설정  
: 가상 머신 생성 완료  


- 다시 Virtual Box 초기 화면으로 돌아와 설정 눌러줌  
- 저장소-비어있음의 가상 광학 디스크 선택/만들기에 Ubuntu ISO 파일 설정해준 후 확인  
- 다시 Virtual Box의 초기 화면으로 들어와 시작을 누르면, 우분투 ISO 파일을 이용해 우분투로 부팅  
- Install Ubuntu로 우분투를 설치
- 설치 후 재부팅  
- 명세서 21페이지 참고. Docker와 Docker-Compose 설치    

```
$ sudo apt-get update  

$ sudo apt-get install \  
	apt-transport-https \  
	ca-certificates \  
	curl \  
 	gnupg \  
	lsb-release  
	
$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg  

$ echo \  
"deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \ $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null 
 
$ sudo apt-get update  

$ sudo apt-get install docker-ce docker-ce-cli containerd.io  

$ sudo curl -L "https://github.com/docker/compose/release/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compoes  

$ sudo chmod +x /usr/local/bin/docker-compose  
```
: sudo systemctl status docker를 통해 도커 설치 확인  
: docker-compose --version를 통해 도커 컴포즈 설치 확인  


# 쿠렌토 미디어 서버
```
$ docker pull kurento/kurento-media-server:latest

$ docker run -d --name kms --network host \
kurento/kurento-media-server:latest
```
- docker ps로 kurento 서버 실행 확인  


- kurento 윈도우에서 확인  (kurento의 TCP 8888번 포트와 UDP 포트범위[5000,5050] 연결해주는 과정


```
$ docker run --rm \
	-p 8888:8888/tcp \
	-p 5000-5050:5000-5050/udp \
	-e KMS_MIN_PORT=5000 \
	-e KMS_MAX_PORT=5050 \
	kurento/kurento-media-server:latest
```

- Coturn 설치 > WebRTC 시그널링을 위한 STUN/TURN 서버로 활용  
**AWS EC2는 싸피에서 일괄 배포한다고 합니다! Coturn 부분 패스 후 진행  
**kurento 예시 테스트도 서버 구축 후 확인




# SSH 클라이언트를 사용한 서버 접속 방법
- MobaXterm (또는 Putty 사용가능) 이용  
- Session > SSH로 새로운 세션 등록  
- Remote host: 연결하고자 하는 호스트  
- Specify username: 우분투 서버에 접근 가능한 유저명  
- IPv4 주소 확인


# Coturn 설치
```
$ sudo apt-get update && sudo apt-get install --no-install-recommends --yes \
coturn
```
- 설정 파일 열 시 $ sudo gedit 파일경로 로 열어서 수정   
- ex) $ sudo gedit /etc/default/coturn  
- /etc/default/coturn 파일 수정   

```
TURNSERVER_ENABLES=1
```

- /etc/turnserver.conf 파일 수정   

```
listening-port=3478
tls-listening-port=5349
listening-ip=<EC2의 프라이빗 IPv4 주소>
external-ip=<EC2의 퍼블릭 IPv4 주소>/<EC2의 프라이빗 IPv4 주소>
relay-ip=<EC2의 프라이빗 IPv4주소>
fingerprint
lt-cred-mech
user=myuser:mypassword
realm=myrealm
log-file=/var/log/turn.log
simple-log
```

# Coturn 재기동
```
- $ sudo service coturn restart
```
- Coturn 서버를 재기동하여 변경된 설정 사항 반영  


# Kurento Docker 컨테이너 접속
```
$ docker ps -a
```
- 실행중인 컨테이너 ID 확인. 확인된 ID로 컨테이너 접속  

```
$ docker exec -it 컨테이너ID /bin/bash
```


# Kurento WebRtcEndpoint.ini 수정
- /etc/kurento/modules/kurento/WebRtcEndpoint.conf.ini 파일 수정  
- root@docker-desktop:/# vi /etc/kurento/modules/kurento/WebRtcEndpoint.conf.ini 명령어로 접속  
- i 입력 / 입력 모드에서 esc 명령모드 / :w 저장 / :wq 저장 후 종료  

```
stunServerAddress=<EC2의 퍼블릭IPv4주소>
stunServerPort=3478
turnURL=myuser:mypassword@<EC2의 퍼블릭 IPv4주소>?transport=udp
```



## Error
```
- Malformed entry 1 in list file /etc/apt/sources.list.d/docker.list 라고 오류가 뜰 겨우  
	- sudo -H gedit /etc/apt/sources.list.d/docker.list 명령어를 입력하면 해당 파일이 뜸  
	   두번째 줄을 첫 번째 줄로 옮겨준 후 저장
```
```
- Got permission denied while trying to connect to the Docker
 : 권한 문제 
 $ sudo usermod -a -G docker $USER 권한 설정 후 재부팅하고 진행
```
```
- Error response from deamon: Container ~ is not running : 해당 컨테이너를 실행 시켜줌. $ docker start 컨테이너ID
```
```
- 포트 번호가 사용중이라고 할 경우 $ sudo Isof -i :포트번호 
ex)sudi Isof -i :8888 입력 시 해당 포트번호 사용 중인지 확인 가능  
- $ netstat -anv | grep LISTEN : 사용중인 포트 확인
```
```
- Error response from daemon: Conflict. The container name "..." is already in use by container "..." 
: $ docker ps -a 로 실행중인 컨테이너 확인 후 $ docker rm 컨테이너이름 으로 삭제해 준 후 다시 실행
```
```
- Error response from daemon: driver failed programming external connectivity on endpoint~ 
: 이전 컨테이너가 실행중이라 새 컨테이너를 시작할 수 없음. 
$ docker ps 로 컨테이너 이름 확인 
$ docker stop 컨테이너이름 > 컨테이너 중지
$ docker rm 컨테이너이름 > 컨테이너 제거
```
```
- bash: vi: command not found
$ apt-get update
$ apt-get install vim

```
