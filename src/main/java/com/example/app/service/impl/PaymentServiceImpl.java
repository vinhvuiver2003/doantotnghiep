package com.example.app.service.impl;

import com.example.app.dto.PaymentDTO;
import com.example.app.entity.Order;
import com.example.app.entity.Payment;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.OrderRepository;
import com.example.app.repository.PaymentRepository;
import com.example.app.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${vnpay.terminal-id}")
    private String vnpTerminalId;

    @Value("${vnpay.secret-key}")
    private String vnpSecretKey;

    @Value("${vnpay.payment-url}")
    private String vnpPaymentUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Autowired
    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public PaymentDTO getPaymentById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        return convertToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByOrderId(Integer orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));

        return convertToDTO(payment);
    }

    @Override
    @Transactional
    public String createVnPayPaymentUrl(Integer orderId, String bankCode, HttpServletRequest request) {
        // Lấy thông tin đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Kiểm tra xem đã có thanh toán chưa
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
        Payment payment;

        if (existingPayment.isPresent()) {
            payment = existingPayment.get();
            // Nếu đã thanh toán thành công, không tạo URL mới
            if (payment.getStatus() == Payment.PaymentStatus.completed) {
                throw new IllegalArgumentException("Order has already been paid");
            }
        } else {
            // Tạo mới payment
            payment = new Payment();
            payment.setOrder(order);
            payment.setStatus(Payment.PaymentStatus.pending);
            payment.setAmount(order.getFinalAmount());
            payment = paymentRepository.save(payment);
        }

        // Lấy IP của client
        String ipAddr = getIpAddress(request);

        // Tạo tham số cho VNPay
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTerminalId);

        // Số tiền thanh toán (VNĐ) * 100
        long amount = order.getFinalAmount().multiply(new BigDecimal("100")).longValue();
        vnpParams.put("vnp_Amount", String.valueOf(amount));

        // Mã hóa tiền tệ (VND)
        vnpParams.put("vnp_CurrCode", "VND");

        // Phương thức thanh toán (ATM, QRCODE, ...)
        vnpParams.put("vnp_BankCode", bankCode);

        // Mã đơn hàng của merchant
        String orderId2 = order.getId() + "-" + System.currentTimeMillis();
        vnpParams.put("vnp_TxnRef", orderId2);

        // Nội dung thanh toán
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang #" + order.getId());

        // Locale (vn, en)
        vnpParams.put("vnp_Locale", "vn");

        // URL redirect sau khi thanh toán
        vnpParams.put("vnp_ReturnUrl", appBaseUrl + vnpReturnUrl);

        // IP của khách hàng
        vnpParams.put("vnp_IpAddr", ipAddr);

        // Thời gian tạo giao dịch
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(calendar.getTime());
        vnpParams.put("vnp_CreateDate", createDate);

        // Ngày hết hạn giao dịch
        calendar.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(calendar.getTime());
        vnpParams.put("vnp_ExpireDate", expireDate);

        // Sắp xếp và tạo query string
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();

        for (String field : fieldNames) {
            if (vnpParams.get(field) != null && !vnpParams.get(field).isEmpty()) {
                query.append(URLEncoder.encode(field, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(vnpParams.get(field), StandardCharsets.UTF_8));

                hashData.append(field);
                hashData.append('=');
                hashData.append(vnpParams.get(field));

                if (fieldNames.indexOf(field) < fieldNames.size() - 1) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // Tạo chữ ký
        String queryUrl = query.toString();
        String vnpSecureHash = hmacSHA512(vnpSecretKey, hashData.toString());

        String paymentUrl = vnpPaymentUrl + "?" + queryUrl + "&vnp_SecureHash=" + vnpSecureHash;

        return paymentUrl;
    }

    @Override
    @Transactional
    public Map<String, String> processVnPayReturn(Map<String, String> queryParams, HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();

        // Kiểm tra chữ ký xác thực từ VNPay
        if (!validateVnPayResponse(queryParams)) {
            result.put("success", "false");
            result.put("message", "Invalid signature");
            return result;
        }

        // Lấy thông tin kết quả thanh toán
        String vnpResponseCode = queryParams.get("vnp_ResponseCode");
        String vnpTransactionStatus = queryParams.get("vnp_TransactionStatus");
        String vnpTxnRef = queryParams.get("vnp_TxnRef");
        String vnpAmount = queryParams.get("vnp_Amount");
        String vnpOrderInfo = queryParams.get("vnp_OrderInfo");

        // Trích xuất mã đơn hàng từ vnp_TxnRef
        String[] orderRef = vnpTxnRef.split("-");
        Integer orderId = Integer.parseInt(orderRef[0]);

        // Lấy thông tin đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Lấy hoặc tạo mới Payment
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    Payment newPayment = new Payment();
                    newPayment.setOrder(order);
                    newPayment.setAmount(order.getFinalAmount());
                    return newPayment;
                });

        // Kiểm tra số tiền thanh toán (VNPay trả về đã nhân 100)
        BigDecimal paymentAmount = new BigDecimal(vnpAmount).divide(new BigDecimal("100"));
        if (paymentAmount.compareTo(order.getFinalAmount()) != 0) {
            result.put("success", "false");
            result.put("message", "Invalid amount");
            payment.setStatus(Payment.PaymentStatus.failed);
            paymentRepository.save(payment);
            return result;
        }

        // Cập nhật trạng thái thanh toán
        if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
            // Thanh toán thành công
            payment.setStatus(Payment.PaymentStatus.completed);
            payment.setPaymentDate(LocalDateTime.now());

            // Cập nhật trạng thái đơn hàng
            order.setOrderStatus(Order.OrderStatus.processing);
            orderRepository.save(order);

            result.put("success", "true");
            result.put("message", "Payment successful");
        } else {
            // Thanh toán thất bại
            payment.setStatus(Payment.PaymentStatus.failed);

            result.put("success", "false");
            result.put("message", "Payment failed: " + mapVnPayResponseCode(vnpResponseCode));
        }

        // Lưu thông tin thanh toán
        payment.setBankTransferCode(queryParams.get("vnp_TransactionNo"));
        payment.setBankAccount(queryParams.get("vnp_CardType"));
        paymentRepository.save(payment);

        // Thêm thông tin kết quả thanh toán
        result.put("orderId", order.getId().toString());
        result.put("amount", paymentAmount.toString());
        result.put("paymentDate", payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "");
        result.put("paymentMethod", "VNPay");
        result.put("transactionNo", queryParams.get("vnp_TransactionNo"));

        return result;
    }

    @Override
    public boolean verifyVnPayIpn(Map<String, String> queryParams) {
        // Xác thực chữ ký từ VNPay IPN
        if (!validateVnPayResponse(queryParams)) {
            return false;
        }

        // Lấy thông tin kết quả thanh toán
        String vnpResponseCode = queryParams.get("vnp_ResponseCode");
        String vnpTransactionStatus = queryParams.get("vnp_TransactionStatus");
        String vnpTxnRef = queryParams.get("vnp_TxnRef");
        String vnpAmount = queryParams.get("vnp_Amount");

        // Trích xuất mã đơn hàng từ vnp_TxnRef
        String[] orderRef = vnpTxnRef.split("-");
        if (orderRef.length < 1) {
            return false;
        }

        try {
            Integer orderId = Integer.parseInt(orderRef[0]);

            // Lấy thông tin đơn hàng
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                return false;
            }

            Order order = orderOpt.get();

            // Kiểm tra số tiền thanh toán
            BigDecimal paymentAmount = new BigDecimal(vnpAmount).divide(new BigDecimal("100"));
            if (paymentAmount.compareTo(order.getFinalAmount()) != 0) {
                return false;
            }

            // Cập nhật trạng thái thanh toán nếu thành công
            if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
                Payment payment = paymentRepository.findByOrderId(orderId)
                        .orElseGet(() -> {
                            Payment newPayment = new Payment();
                            newPayment.setOrder(order);
                            newPayment.setAmount(order.getFinalAmount());
                            return newPayment;
                        });

                payment.setStatus(Payment.PaymentStatus.completed);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setBankTransferCode(queryParams.get("vnp_TransactionNo"));
                payment.setBankAccount(queryParams.get("vnp_CardType"));
                paymentRepository.save(payment);

                // Cập nhật trạng thái đơn hàng
                order.setOrderStatus(Order.OrderStatus.processing);
                orderRepository.save(order);

                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public PaymentDTO updatePaymentStatus(Integer id, Payment.PaymentStatus status) {
        // Kiểm tra thông tin thanh toán tồn tại
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        // Cập nhật trạng thái
        payment.setStatus(status);

        // Cập nhật ngày thanh toán nếu chuyển sang trạng thái "completed"
        if (status == Payment.PaymentStatus.completed && payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());

            // Cập nhật trạng thái đơn hàng
            Order order = payment.getOrder();
            order.setOrderStatus(Order.OrderStatus.processing);
            orderRepository.save(order);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return convertToDTO(updatedPayment);
    }

    @Override
    public Integer getOrderIdByPaymentId(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        return payment.getOrder().getId();
    }

    // Phương thức tiện ích để lấy địa chỉ IP
    private String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }

    // Phương thức tạo chữ ký HMAC-SHA512
    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            sha512_HMAC.init(secret_key);

            byte[] hash = sha512_HMAC.doFinal(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    // Phương thức xác thực chữ ký từ VNPay
    private boolean validateVnPayResponse(Map<String, String> params) {
        // Lấy secure hash từ response
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null) {
            return false;
        }

        // Tạo danh sách tham số cần kiểm tra
        Map<String, String> vnpParams = new HashMap<>();
        params.forEach((key, value) -> {
            if (!key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")) {
                vnpParams.put(key, value);
            }
        });

        // Sắp xếp tham số
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        // Tạo chuỗi cần tạo chữ ký
        StringBuilder hashData = new StringBuilder();
        for (String field : fieldNames) {
            if (vnpParams.get(field) != null && !vnpParams.get(field).isEmpty()) {
                hashData.append(field);
                hashData.append('=');
                hashData.append(vnpParams.get(field));

                if (fieldNames.indexOf(field) < fieldNames.size() - 1) {
                    hashData.append('&');
                }
            }
        }

        // Tạo chữ ký và so sánh
        String calculatedHash = hmacSHA512(vnpSecretKey, hashData.toString());
        return calculatedHash.equals(vnpSecureHash);
    }

    // Phương thức chuyển đổi mã lỗi từ VNPay sang thông báo dễ hiểu
    private String mapVnPayResponseCode(String responseCode) {
        Map<String, String> errorMessages = new HashMap<>();
        errorMessages.put("01", "Giao dịch đã tồn tại");
        errorMessages.put("02", "Merchant không hợp lệ");
        errorMessages.put("03", "Dữ liệu gửi sang không đúng định dạng");
        errorMessages.put("04", "Khởi tạo GD không thành công do Website đang bị tạm khóa");
        errorMessages.put("05", "Giao dịch không thành công do: Quý khách nhập sai mật khẩu quá số lần quy định");
        errorMessages.put("06", "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực");
        errorMessages.put("07", "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)");
        errorMessages.put("09", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa");
        errorMessages.put("10", "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ không đúng quá 3 lần");
        errorMessages.put("11", "Giao dịch không thành công do: Đã hết hạn chờ thanh toán");
        errorMessages.put("12", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa");
        errorMessages.put("24", "Giao dịch không thành công do: Khách hàng hủy giao dịch");
        errorMessages.put("51", "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch");
        errorMessages.put("65", "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày");
        errorMessages.put("75", "Ngân hàng thanh toán đang bảo trì");
        errorMessages.put("99", "Có lỗi xảy ra trong quá trình xử lý");

        return errorMessages.getOrDefault(responseCode, "Lỗi không xác định");
    }

    // Utility method to convert Entity to DTO
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setStatus(payment.getStatus().name());
        dto.setAmount(payment.getAmount());
        dto.setBankTransferCode(payment.getBankTransferCode());
        dto.setBankAccount(payment.getBankAccount());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setCreatedAt(payment.getCreatedAt());

        return dto;
    }
}