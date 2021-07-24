package com.ssafy.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

// 회의방 모델
@Entity
@Getter
@Setter
public class Conference{
	
	@Id @GeneratedValue()
	@Column(name = "conference_seq")
	private Long sequence;
	
	private String name; // 회의방 이름
	private String owner; // 호스트
	private LocalDateTime produceTime; // 생성 시간
	private int participantLimit; // 참가자 제한 수
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_seq") // 카테고리 sequence 컬럼과 조인
    private ConferenceCategory conferenceCategory; // 회의방 카테고리와 다대일 관계
    
    private String description; // 회의방 설명

    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 생성 요청을 처리할 때만 사용
    private String password;
    
    @OneToMany(mappedBy = "conference") // 회의방별 사용자 리스트
    private List<User> users = new ArrayList<User>();
    
       
}
