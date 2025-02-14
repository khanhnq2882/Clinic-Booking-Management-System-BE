Link tham khảo :
https://bookingcare.vn
https://stdvietnam.vn/tin-tuc/quan-ly-phong-kham-truc-tuyen-voi-phan-mem-std-clinic.html
https://dayschedule.com/docs/t/how-to-create-patient-appointment-booking-system-for-doctors/295
https://hasthemes.com/free-bootstrap-templates/mexi/

**CÁC LUỒNG CHÍNH TRONG HỆ THỐNG**
* Luồng 1:
- Bác sĩ đăng ký tài khoản vào hệ thống -> Bác sĩ cập nhật thông tin (tên, sđt, địa chỉ, chuyên khoa, kinh nghiệm làm việc, thông tin nghề nghiệp, các chứng chỉ,...)-> Bác sĩ 
đăng ký lịch làm việc trong tuần (mặc định là làm việc từ t2-cn, vì trong quy mô 1 phòng khám nên khi đăng ký lịch làm việc tránh xung đột với các bác sĩ cùng khoa)

* Luồng 2:
- Có 2 option khi bệnh nhân booking lịch khám
+ Option 1: Người dùng đăng ký tài khoản -> Đăng nhập -> Chọn chuyên khoa cần khám -> Danh sách bác sĩ của chuyên khoa -> Hệ thống hiển thị danh sách lịch khám có sẵn trong tuần 
để người dùng chọn -> Người dùng nhập thông tin cần khám (tên, sđt, địa chỉ, ngày sinh, triệu chứng,...) -> Hệ thống kiểm tra xem ngày và giờ khám đã có ai đăng ký trước đó chưa -> Nếu có thì reject, 
không thì đưa vào trạng thái pending -> Đưa vào danh sách booking của bác sĩ đã chọn -> Chuyển trạng thái (accept, reject) -> Nếu chấp nhận thì đã đặt lịch thành công -> Lưu lịch sử bệnh án trên hệ thống
+ Option 2: Người dùng không đăng nhập vào hệ thống -> Người dùng nhập thông tin cần khám (tên, sđt, địa chỉ, email, triệu chứng,...) -> Hệ thống tự động sắp xếp lịch khám cho người dùng
  (liên hệ với người dùng qua sđt hoặc email để confirm ngày giờ khám) -> Nếu chấp nhận thì đã đặt lịch thành công -> Không lưu lịch sử bệnh án trên hệ thống
  
**DANH SÁCH CHỨC NĂNG**

* Chức năng chung:
- Đăng ký -> OK
- Đăng nhập -> OK
- Đổi mật khẩu -> OK
- Quên mật khẩu -> OK
- Remember me 

* ROLE USER
- Cập nhật thông tin cá nhân trên hệ thống (họ tên, sđt, ngày sinh, địa chỉ, ...)
- Book lịch khám
- Quản lý danh sách bookings 
- Mua các gói dịch vụ

* ROLE DOCTOR
- Cập nhật thông tin cá nhân trên hệ thống (họ tên, sđt, ngày sinh, địa chỉ, ...)
- Cập nhật thông tin bác sĩ (chuyên khoa,các chứng chỉ, kinh nghiệm,...)
- Quản lý danh sách booking
- Tạo hóa đơn bao gồm các gói dịch vụ người  bệnh sử dụng trong khi khám, 

* ROLE ADMIN
- Quản lý danh sách người dùng 
- Quản lý danh sách bác sĩ 
- Quản lý chuyên khoa
- Quản lý danh mục dịch vụ
- Quản lý gói dịch vụ
- Quản lý danh sách booking (người dùng k đăng nhập)
- Quản lý doanh thu
- Quản lý thuốc, vật tư y tế


