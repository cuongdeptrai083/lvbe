package com.example.book_be.controller;

import com.example.book_be.entity.NguoiDung;
import com.example.book_be.security.JwtResponse;
import com.example.book_be.security.LoginRequest;
import com.example.book_be.services.JWT.JwtService;
import com.example.book_be.services.TaiKhoanService;
import com.example.book_be.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000/")
@RequestMapping("/tai-khoan")
public class TaiKhoanController {
    @Autowired
    private TaiKhoanService taiKhoanService;
    @Autowired
    private AuthenticationManager AuthenticationManager;

    private UserService userService;
    @Autowired
    private JwtService jwtService;

//    @PostMapping("/Thong tin tài khoan")
//    public ResponseEntity<?> dangKyNguoiDung(@Validated @RequestBody NguoiDung nguoiDung) {
//        ResponseEntity<?> response = taiKhoanService.dangKyNguoiDung(nguoiDung);
//        return response;
//    }

    @PostMapping("/dang-ky")
    public ResponseEntity<?> dangKyNguoiDung(@Validated @RequestBody NguoiDung nguoiDung){
        ResponseEntity<?> response = taiKhoanService.dangKyNguoiDung(nguoiDung);
        return response ;
    }

    @GetMapping("/kich-hoat")
    public ResponseEntity<?> kichHoatTaiKhoan(@RequestParam String email, @RequestParam String maKichHoat) {
        ResponseEntity<?> response = taiKhoanService.kichHoatTaiKhoan(email, maKichHoat);
        return response;
    }

    @PostMapping("/dang-nhap")
    public ResponseEntity<?> dangNhap(@RequestBody LoginRequest loginRequest) {
        try {
            //kiểm tra tài khoản
            Authentication authentication = AuthenticationManager.authenticate(new
                    UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            //  Xác thực thành công!
            if (authentication.isAuthenticated()) {
                // Tạo JWT token
                final String jwt = jwtService.generateToken(loginRequest.getUsername());
                // Trả về token 
                return ResponseEntity.ok(new JwtResponse(jwt));
            }
        } catch (AuthenticationException a) {
            return ResponseEntity.badRequest().body("Tên đăng nhập hoặc mật khẩu không chính xác. ");
        }
        return ResponseEntity.badRequest().body("Xác thực không thành công.");
    }
//    @PostMapping("/Bao loi xac thuc")
//    public ResponseEntity<?> dangNhap(@RequestBody LoginRequest loginRequest) {
//        try {
//            Authentication authentication = AuthenticationManager.authenticate(new
//                    UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
//            );
//            if (authentication.isAuthenticated()) {
//                final String jwt = jwtService.generateToken(loginRequest.getUsername());
//                return ResponseEntity.ok(new JwtResponse(jwt));
//            }
//        } catch (AuthenticationException a) {
//            return ResponseEntity.badRequest().body("Tên đăng nhập hoặc mật khẩu không chính xác. ");
//        }
//        return ResponseEntity.badRequest().body("Xác thực không thành công.");
//    }
    // ?
    @GetMapping("/thong-tin/{maNguoiDung}")
    public ResponseEntity<?> layThongTinNguoiDung(@PathVariable int maNguoiDung) {
        return taiKhoanService.layThongTinNguoiDung(maNguoiDung);
    }
    // ?
    @PutMapping("/cap-nhat/{maNguoiDung}")
    public ResponseEntity<?> capNhatThongTinNguoiDung(
            @PathVariable int maNguoiDung,
            @RequestBody NguoiDung nguoiDungCapNhat) {
        return taiKhoanService.capNhatThongTinNguoiDung(maNguoiDung, nguoiDungCapNhat);
    }

}
