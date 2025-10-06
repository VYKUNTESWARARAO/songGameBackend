package com.gg.model;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "songs")
public class Song {
    @Id
    private String id;
    private String songName;
    private String youtubeUrl;
    private String genre;
    private String movieName;
}
