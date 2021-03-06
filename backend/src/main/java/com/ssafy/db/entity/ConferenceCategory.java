package com.ssafy.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

// 회의방 참여 유저
@Entity
@Getter
@Setter
public class ConferenceCategory{
	@Id @GeneratedValue()
	@Column(name = "category_seq")
	private int sequence; // 카테고리 번호
	
    private String name; // 카테고리 이름
    private String background; // 배경
    private String atmosphere; // 분위기
    
//    @OneToMany(mappedBy = "conferenceCategory") // 카테고리별 회의 리스트
//    private List<Conference> conferences = new ArrayList<>();
}
