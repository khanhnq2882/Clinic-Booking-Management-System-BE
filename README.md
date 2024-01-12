# Clinic-Booking-Management-System
Link phòng khám tham khảo :

https://bookingcare.vn

https://stdvietnam.vn/tin-tuc/quan-ly-phong-kham-truc-tuyen-voi-phan-mem-std-clinic.html

https://dayschedule.com/docs/t/how-to-create-patient-appointment-booking-system-for-doctors/295

admin thêm bác sĩ (thêm chuyên khoa cho bác sĩ) <làm sau> -> bác sĩ cập nhật thông tin (tên, sđt, địa chỉ, chuyên khoa, lịch trình làm việc cố định, ...)
(mặc định là làm việc từ t2-t7, tránh xung đột lịch trình làm việc với các bác sĩ cùng khoa)

người dùng chọn chuyên khoa -> liệt kê ra danh sách bác sĩ của chuyên khoa đó -> hiển thị ngày và danh sách lịch khám trong ngày còn trống để người dùng chọn -> người dùng nhập thông tin cần khám
-> đưa vào danh sách booking do từng bác sĩ quản lý -> chuyển trạng thái -> nếu chấp nhận thì hẹn người dùng đi khám

https://hasthemes.com/free-bootstrap-templates/mexi/

Từ cột chuyên khoa của file excel -> Load ra list bác sĩ thuộc chuyên khoa này -> Check cột ngày và giờ khám của file excel xem trùng với lịch làm việc của bác sĩ nào -> Kiểm tra trong bảng Bookings xem đã có lịch và giờ khám như thế chưa
Nếu có ->
Nếu không -> admin thêm bookings vào danh sách booking cho bác sĩ -> bác sĩ thay đổi trạng thái booking trên list mà mình quản lý, thông báo hẹn bệnh nhân
-> Không lưu lại lịch sử khám cho trường hợp không đăng nhập vào hệ thống
-> Lưu lại lịch sử khám cho trường hợp đăng nhập vào hệ thống


Cập nhật lịch làm việc của bác sĩ trong tuần (t2-t7)
-> Check giờ làm so với các bác sĩ khác (check theo từng ngày)

Booking
- Tên
- Số điện thoại
- Email
- Ngày giờ khám
- Chuyên khoa
- Triệu chứng

Có 2 options:
- Không chọn bác sĩ: Chọn ngày giờ khám, hệ thống tự động thêm bác sĩ trống giờ làm
- Chọn bác sĩ : Chọn bác sĩ, ngày giờ khám theo lịch trên hệ thống