package com.example.book_be.dto;

import java.util.List;

public class SachDTO {
    private int maSach;
    private String tenSach;
    private String tenTacGia;
    private String moTa;
    private double giaNiemYet;
    private double giaBan;
    private int soLuong;
    private String ISBN;
    private Integer isActive;
    private List<String> listImageStr;
    private List<Integer> maTheLoaiList; // Danh sách ID thể loại

    // Getters and Setters
    public int getMaSach() { return maSach; }
    public void setMaSach(int maSach) { this.maSach = maSach; }

    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }

    public String getTenTacGia() { return tenTacGia; }
    public void setTenTacGia(String tenTacGia) { this.tenTacGia = tenTacGia; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public double getGiaNiemYet() { return giaNiemYet; }
    public void setGiaNiemYet(double giaNiemYet) { this.giaNiemYet = giaNiemYet; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getISBN() { return ISBN; }
    public void setISBN(String ISBN) { this.ISBN = ISBN; }

    public Integer getIsActive() { return isActive; }
    public void setIsActive(Integer isActive) { this.isActive = isActive; }

    public List<String> getListImageStr() { return listImageStr; }
    public void setListImageStr(List<String> listImageStr) { this.listImageStr = listImageStr; }

    public List<Integer> getMaTheLoaiList() { return maTheLoaiList; }
    public void setMaTheLoaiList(List<Integer> maTheLoaiList) { this.maTheLoaiList = maTheLoaiList; }
}