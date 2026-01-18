package com.example.book_be.dao;

import com.example.book_be.entity.SuDanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "su-danh-gia")
public interface SuDanhGiaRepository extends JpaRepository<SuDanhGia, Long>, JpaSpecificationExecutor {
    SuDanhGia findByNguoiDung_MaNguoiDungAndSach_MaSach(Long maNguoiDung, Long maSach);
    boolean existsByNguoiDung_MaNguoiDungAndSach_MaSach(Long maNguoiDung, Long maSach);
}
