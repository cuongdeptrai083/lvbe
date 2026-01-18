package com.example.book_be.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonHangDTO {
    private String hoTen;
    private String soDienThoai;
    private String diaChiNhanHang;
    private long maSach;
    private int soLuong;
    private int tongTien;
}
