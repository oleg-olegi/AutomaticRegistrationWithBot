package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Getter
@Entity
@Table(name = "birthdaydb")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "person_name")
    private String name;
    @Column(name = "chat_id")
    private Long chatId;
    @Column(name = "date_of_birth")
    public Date dateOfBirth;
}
