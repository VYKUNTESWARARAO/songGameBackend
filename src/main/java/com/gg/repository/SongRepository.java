package com.gg.repository;




import org.springframework.data.mongodb.repository.MongoRepository;

import com.gg.model.Song;

public interface SongRepository extends MongoRepository<Song, String> {
}
