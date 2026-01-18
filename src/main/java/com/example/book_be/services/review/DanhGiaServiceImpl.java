package com.example.book_be.services.review;

import com.example.book_be.dao.NguoiDungRepository;
import com.example.book_be.dao.SachRepository;
import com.example.book_be.dao.SuDanhGiaRepository;
import com.example.book_be.entity.NguoiDung;
import com.example.book_be.entity.Sach;
import com.example.book_be.entity.SuDanhGia;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Transactional
@Service
public class DanhGiaServiceImpl implements DanhGiaService {
    @Autowired
    NguoiDungRepository nguoiDungRepository;
    @Autowired
    SuDanhGiaRepository suDanhGiaRepository;
    @Autowired
    private SachRepository sachRepository;

    @Override
    public SuDanhGia addReview(String nhanXet, float diemXepHang, Long maNguoiDung, Long maSach) {
        if (suDanhGiaRepository.existsByNguoiDung_MaNguoiDungAndSach_MaSach(maNguoiDung, maSach)) {
            List<SuDanhGia> reviews = (List<SuDanhGia>) suDanhGiaRepository.findByNguoiDung_MaNguoiDungAndSach_MaSach(maNguoiDung, maSach);
            if (reviews.size() > 1) {
                // Handle the case where multiple reviews exist
            } else if (reviews.size() == 1) {
                SuDanhGia existingReview = reviews.get(0);
                existingReview.setNhanXet(nhanXet);
                existingReview.setDiemXepHang(diemXepHang);
                existingReview.setTimestamp(new Timestamp(System.currentTimeMillis())); // Cập nhật thời gian
                return suDanhGiaRepository.save(existingReview); // Cập nhật đánh giá
            }
        }

        SuDanhGia suDanhGia = new SuDanhGia();
        suDanhGia.setNhanXet(nhanXet);
        suDanhGia.setDiemXepHang(diemXepHang);
        suDanhGia.setTimestamp(new Timestamp(System.currentTimeMillis()));
        suDanhGia.setIsActive(1);
        suDanhGia.setNguoiDung(nguoiDungRepository.findById(maNguoiDung).orElseThrow(
                () -> new RuntimeException("Không tìm thấy người dùng")));
        suDanhGia.setSach(sachRepository.findById(maSach).orElseThrow(
                () -> new RuntimeException("Không tìm thấy sách")));
        return suDanhGiaRepository.save(suDanhGia);
    }

    @Override
    public SuDanhGia updateReview(Long maDanhGia, SuDanhGia danhGia) {
        SuDanhGia db = suDanhGiaRepository.findById(maDanhGia).orElse(null);
        db.setDiemXepHang(danhGia.getDiemXepHang());
        db.setNhanXet(danhGia.getNhanXet());
        db.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return suDanhGiaRepository.save(db);
    }

    @Override
    public SuDanhGia deleteReview(Long maDanhGia) {
        SuDanhGia db = suDanhGiaRepository.findById(maDanhGia).orElse(null);
        suDanhGiaRepository.delete(db);
        return db;
    }
}
