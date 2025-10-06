package com.gg.controller;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gg.model.Song;
import com.gg.model.User;
import com.gg.repository.SongRepository;
import com.gg.repository.UserRepository;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired	
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;

   

    // ✅ ADD USER (only admin)
    @PostMapping("/users")
    public User addUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // ✅ GET ALL USERS
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ ADD SONG
    @PostMapping("/songs")
    public Song addSong(@RequestBody Song song) {
        return songRepository.save(song);
    }

    // ✅ GET ALL SONGS
    @GetMapping("/songs")
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    // ✅ UPDATE SONG
    @PutMapping("/songs/{id}")
    public Song updateSong(@PathVariable String id, @RequestBody Song updatedSong) {
        updatedSong.setId(id);
        return songRepository.save(updatedSong);
    }

    // ✅ DELETE SONG
    @DeleteMapping("/songs/{id}")
    public void deleteSong(@PathVariable String id) {
        songRepository.deleteById(id);
    }
}
