package com.example.book_be.controller.admin;

import com.example.book_be.dao.TheLoaiRepository;
import com.example.book_be.entity.TheLoai;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000/")
@RequestMapping("/api/admin/the-loai")
public class TheLoaiController {

    @Autowired
    private TheLoaiRepository theLoaiRepository;

    @GetMapping
    public ResponseEntity<List<TheLoai>> findAll() {
        return new ResponseEntity<>(theLoaiRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TheLoai> findById(@PathVariable int id) {
        return theLoaiRepository.findById( id)
                .map(theLoai -> new ResponseEntity<>(theLoai, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<TheLoai> save(@RequestBody TheLoai theLoai) {
        return new ResponseEntity<>(theLoaiRepository.save(theLoai), HttpStatus.CREATED);
    }
}