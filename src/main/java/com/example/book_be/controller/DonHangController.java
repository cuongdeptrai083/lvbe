package com.example.book_be.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.book_be.dao.ChiTietDonHangRepository;
import com.example.book_be.dao.DonHangRepository;
import com.example.book_be.dao.NguoiDungRepository;
import com.example.book_be.dao.SachRepository;
import com.example.book_be.dto.DonHangDTO;
import com.example.book_be.entity.*;
import com.example.book_be.services.VNPayService;
import com.example.book_be.services.cart.OrderService;
import com.example.book_be.services.email.EmailService;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/don-hang")
public class DonHangController {


    @Autowired
    private OrderService orderService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DonHangRepository donHangRepository;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private SachRepository sachRepository;

    @GetMapping("/findAll")
    public Page<DonHang> findAll(
                                     @RequestParam("page") Integer page,
                                     HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, 10);
        NguoiDung nguoiDung = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getName() != null) {
            nguoiDung = nguoiDungRepository.findByTenDangNhap(authentication.getName());
        }
        NguoiDung finalNguoiDung = nguoiDung;
        return donHangRepository.findAll((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if(authentication.getAuthorities().contains("ADMIN") || authentication.getAuthorities().contains("USER")){
                predicates.add(builder.equal(root.get("nguoiDung").get("maNguoiDung"), finalNguoiDung.getMaNguoiDung()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    @PostMapping("/them")
    public DonHang add(@RequestBody List<Sach> sachList) {
        DonHang donHang = null;
        try {
            donHang = orderService.saveOrUpdate(sachList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return donHang;
    }

    // FE gọi endpoint này để nhận URL thanh toán VNPay và redirect người dùng sang VNPay
    @GetMapping("/submitOrder")
    public String submidOrder(@RequestParam("amount") int orderTotal,
                          @RequestParam("orderInfo") String orderInfo,
                          HttpServletRequest request) {
        String baseUrl = "";
        // Tạo URL thanh toán: gắn số tiền, mã đơn, returnUrl, ký HMAC
        String vnpayUrl = vnPayService.createOrder(orderTotal, orderInfo, baseUrl);
        return vnpayUrl; // FE dùng URL này để chuyển hướng
    }

    // VNPay redirect về endpoint này sau khi người dùng thanh toán
    @GetMapping("/vnpay-payment")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> vnpayPaymentReturn(HttpServletRequest request) {
        try {
            // Kiểm tra chữ ký VNPay và trạng thái giao dịch
            int paymentStatus = vnPayService.orderReturn(request);

            String orderInfo     = request.getParameter("vnp_OrderInfo");      // mã đơn hàng
            String paymentTime   = request.getParameter("vnp_PayDate");        // thời gian thanh toán
            String transactionId = request.getParameter("vnp_TransactionNo");  // mã giao dịch VNPay
            String totalPrice    = request.getParameter("vnp_Amount");         // số tiền (x100)

            if (paymentStatus == 1) { // Thành công
                DonHang donHang = donHangRepository.findById(Long.valueOf(orderInfo)).orElse(null);
                if (donHang != null) {
                    donHang.setTrangThaiThanhToan(1); // Đã trả tiền
                    donHang.setTrangThaiGiaoHang(1);   // Đã tiếp nhận giao
                    donHangRepository.save(donHang);

                    // Lấy chi tiết đơn, dựng HTML email và gửi cho khách
                    List<ChiTietDonHang> chiTietDonHangs = chiTietDonHangRepository.findAll(
                        (root, query, builder) -> builder.equal(root.get("donHang").get("maDonHang"), donHang.getMaDonHang())
                    );
                    String noiDung = this.generateOrderEmailBody(
                        String.valueOf(donHang.getMaDonHang()),
                        donHang.getNguoiDung().getHoDem() + " " + donHang.getNguoiDung().getTen(),
                        donHang.getNgayTao().toString(),
                        donHang.getDiaChiNhanHang(),
                        String.valueOf(donHang.getTongTien()),
                        chiTietDonHangs
                    );
                    emailService.sendEmail("cuongnguyen0834@gmail.com",
                        donHang.getNguoiDung().getEmail(),
                        "Thông báo Đơn hàng của bạn", noiDung);
                }

                // Trả kết quả cho FE hiển thị
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Thanh toán thành công",
                    "orderId", orderInfo,
                    "transactionId", transactionId,
                    "totalPrice", totalPrice,
                    "paymentTime", paymentTime
                ));
            } else if (paymentStatus == 0) { // Thất bại
                return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", "Thanh toán thất bại"
                ));
            } else { // Sai chữ ký
                return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", "Xác minh hash thất bại"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Lỗi xử lý thanh toán: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/cap-nhat-trang-thai-giao-hang/{maDonHang}")
    public void submidOrder(@PathVariable Long  maDonHang,
                              HttpServletRequest request) {
        /// //////////////////////////////////
        DonHang donHang = donHangRepository.findById(maDonHang).orElse(null);
        donHang.setTrangThaiGiaoHang(2);
        donHang.setTrangThaiThanhToan(1);
        donHangRepository.save(donHang);
    }

    public String generateOrderEmailBody(String orderId, String customerName, String orderDate, String diaChi, String tongTien, List<ChiTietDonHang> chiTietDonHangs) {
        String chiTienDonHang = "";
        for (ChiTietDonHang chiTietDonHang : chiTietDonHangs) {
            chiTienDonHang += "<tr>" +
                    "<td style=\"border: 1px solid #ddd; padding: 8px;\">" + chiTietDonHang.getMaChiTietDonHang() + "</td>" +
                    "<td style=\"border: 1px solid #ddd; padding: 8px;\">" + chiTietDonHang.getSach().getTenSach() + "</td>" +
                    "<td style=\"border: 1px solid #ddd; padding: 8px;\">" + chiTietDonHang.getSoLuong() + "</td>" +
                    "<td style=\"border: 1px solid #ddd; padding: 8px;\">" + chiTietDonHang.getSach().getGiaBan() + "</td>" +
                    "<td style=\"border: 1px solid #ddd; padding: 8px;\">" + chiTietDonHang.getSoLuong() * chiTietDonHang.getSach().getGiaBan() + "</td>" +
                    "</tr>";
        }
        return "<html>" +
                "<body>" +
                "<h2 style=\"border-bottom: 2px solid #333; padding-bottom: 10px; color: red;\">Thông báo Đơn hàng của bạn</h2>" +
                "<p>Chào " + customerName + ",</p>" +
                "<p>Cảm ơn bạn đã đặt hàng tại chúng tôi! Dưới đây là thông tin chi tiết về đơn hàng của bạn:</p>" +
                "<p><b>Mã Đơn Hàng : </b>" + orderId + "</p>" +
                "<p><b>Ngày Đặt Hàng : </b>" + orderDate + "</p>" +
                "<table style=\"width: 100%; border: 1px solid #ddd; border-collapse: collapse;\">" +
                // Phần tiêu đề bảng (thead)
                "<thead style=\"background-color: #f4f4f4;\">" +
                "<tr>" +
                "<th style=\"border: 1px solid #ddd; padding: 8px; text-align: left;\">Mã chi tiết đơn hàng</th>" +
                "<th style=\"border: 1px solid #ddd; padding: 8px; text-align: left;\">Tên sách</th>" +
                "<th style=\"border: 1px solid #ddd; padding: 8px; text-align: left;\">Số lượng</th>" +
                "<th style=\"border: 1px solid #ddd; padding: 8px; text-align: left;\">Giá bán</th>" +
                "<th style=\"border: 1px solid #ddd; padding: 8px; text-align: left;\">Thanh toán</th>" +
                "</tr>" +
                "</thead>" +

                // Phần nội dung bảng (tbody)
                "<tbody>" +
                chiTienDonHang +
                "</tbody>" +
                "</table>" +

                "<p style=\"border-top: 1px solid #ddd; padding-top: 10px;\">Đơn hàng của bạn sẽ được xử lý trong vòng 24 giờ. Chúng tôi sẽ thông báo khi hàng hóa được gửi đi.</p>" +
                "<p style=\"border-top: 1px solid #ddd; padding-top: 10px;\">Trân trọng cảm ơn!</p>" +
                "</body>" +
                "</html>";
    }
    @PostMapping("/them-don-hang-moi")
    @Transactional
    public ResponseEntity<Map<String, Object>> themDonHangMoi(
            @RequestBody DonHangDTO donHangDTO) {
        DonHang donHang = new DonHang();
        donHang.setHoTen(donHangDTO.getHoTen());
        donHang.setSoDienThoai(donHangDTO.getSoDienThoai());
        donHang.setDiaChiNhanHang(donHangDTO.getDiaChiNhanHang());
        donHang.setNgayTao(new Date());
        donHang.setTongTien(donHangDTO.getTongTien());
        donHang.setTrangThaiThanhToan(2);
        donHang.setTrangThaiGiaoHang(1);


        Sach sach = sachRepository.findById(donHangDTO.getMaSach())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với id = " + donHangDTO.getMaSach()));
        ChiTietDonHang chiTietDonHang = new ChiTietDonHang();
        chiTietDonHang.setSoLuong(donHangDTO.getSoLuong());
        chiTietDonHang.setDanhGia(false);
        chiTietDonHang.setGiaBan(sach.getGiaBan());
        chiTietDonHang.setSach(sach);
        donHang.addChiTietDonHang(chiTietDonHang);

        DonHang savedDonHang = donHangRepository.save(donHang);



        // Tạo phản hồi JSON
        Map<String, Object> response = new HashMap<>();
        response.put("maDonHang", savedDonHang.getMaDonHang());
        response.put("tongTien", savedDonHang.getTongTien());
        response.put("hoTen", savedDonHang.getHoTen());
        response.put("soDienThoai", savedDonHang.getSoDienThoai());
        response.put("diaChiNhanHang", savedDonHang.getDiaChiNhanHang());

        return ResponseEntity.ok(response);
    }


}
