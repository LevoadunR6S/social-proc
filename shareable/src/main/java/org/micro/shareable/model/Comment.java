package org.micro.shareable.model;

import jakarta.persistence.*;
import lombok.Data;
import org.micro.shareable.model.Post;

@Data
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;



    @Column(nullable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post task;
}
