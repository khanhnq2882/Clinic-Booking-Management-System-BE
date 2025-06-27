Link tham khảo :
https://bookingcare.vn
https://stdvietnam.vn/tin-tuc/quan-ly-phong-kham-truc-tuyen-voi-phan-mem-std-clinic.html
https://dayschedule.com/docs/t/how-to-create-patient-appointment-booking-system-for-doctors/295
https://hasthemes.com/free-bootstrap-templates/mexi/

**********CÁC LUỒNG CHÍNH TRONG HỆ THỐNG**********
* Luồng 1:
- Bác sĩ đăng ký tài khoản vào hệ thống -> Bác sĩ cập nhật thông tin (tên, sđt, địa chỉ, chuyên khoa, kinh nghiệm làm việc, thông tin nghề nghiệp, các chứng chỉ,...)-> Bác sĩ 
đăng ký lịch làm việc trong tuần (mặc định là làm việc từ t2-cn, vì trong quy mô 1 phòng khám nên khi đăng ký lịch làm việc tránh xung đột với các bác sĩ cùng khoa)

* Luồng 2: Có 2 option khi bệnh nhân booking lịch khám
- Option 1: 
Người dùng đăng ký tài khoản -> Đăng nhập -> Chọn chuyên khoa cần khám -> Danh sách bác sĩ của chuyên khoa -> Hệ thống hiển thị danh sách lịch khám có sẵn trong tuần 
để người dùng chọn -> Người dùng nhập thông tin cần khám (tên, sđt, địa chỉ, ngày sinh, triệu chứng,...) -> Hệ thống kiểm tra xem ngày và giờ khám đã có ai đăng ký trước đó chưa 
+ Nếu có thì reject, không cho phép người dùng đặt lịch vào ngày giờ đó
+ Nếu không có thì đưa vào trạng thái pending -> Đưa vào danh sách booking của bác sĩ đã chọn -> Chuyển trạng thái (accept, reject) -> Nếu chấp nhận thì đã đặt lịch thành công 
-> Lưu lịch sử đặt lịch và lưu lịch sử bệnh án trên hệ thống
+ Nếu người dùng muốn hủy lịch khám thì phải hủy trước ít nhất 24h so với giờ khám đã đặt hoặc trong vòng 1h kể từ lúc đặt lịch , trong trường hợp người dùng không tự hủy được lịch
khám thì người dùng liên hệ với admin để được hỗ trợ
- Option 2: 
Người dùng không đăng nhập vào hệ thống -> Người dùng nhập thông tin cần khám (tên, sđt, địa chỉ, email, triệu chứng,...) -> Hệ thống tự động sắp xếp lịch khám cho người dùng
  (liên hệ với người dùng qua sđt hoặc email để confirm ngày giờ khám) -> Nếu chấp nhận thì đã đặt lịch thành công -> Lưu tạm thời lịch sử đặt lịch trong vòng 2 tuần phòng trường 
hợp khiếu nại xảy ra và không lưu lịch sử bệnh án trên hệ thống

**********DANH SÁCH CHỨC NĂNG**********
* Chức năng chung:
- Đăng ký -> OK
- Đăng nhập -> OK
- Đổi mật khẩu -> OK
- Quên mật khẩu -> OK
- Remember me -> OK
- Đăng nhập thông qua gmail, facebook, ...

* ROLE USER
- Cập nhật thông tin cá nhân trên hệ thống (họ tên, sđt, ngày sinh, địa chỉ, ...) - OK
- Đặt, sửa và hủy lịch khám -> OK
- Quản lý danh sách lịch khám -> OK
- Mua các gói dịch vụ
- Xem lịch sử khám và bệnh án
- Thanh toán online (VNPay, Momo,...)
- Thông báo , nhắc lịch khám (Qua email và giao diện)
- Gửi yêu cầu hỗ trợ đến admin (Trường hợp hủy, hỏi đáp, ...)

* ROLE DOCTOR
- Cập nhật thông tin cá nhân trên hệ thống (họ tên, sđt, ngày sinh, địa chỉ, ...) -> OK
- Cập nhật thông tin bác sĩ (chuyên khoa,các chứng chỉ, tiểu sử, kinh nghiệm,...) -> OK
- Cập nhật lịch làm việc trong tuần -> OK
- Quản lý danh sách lịch khám -> OK
- Tạo hóa đơn bao gồm các gói dịch vụ người  bệnh sử dụng trong khi khám
- Xem và cập nhật hồ sơ bệnh án của bệnh nhân sau mỗi lần khám
- Gửi thông báo đến bệnh nhân (nếu lịch thay đổi hoặc có đề xuất khác).
- Xem thống kê cá nhân

* ROLE ADMIN
- Quản lý danh sách người dùng -> OK
- Quản lý danh sách bác sĩ -> OK
- Quản lý các loại dịch vụ (tư vấn, xét nghiệm, chẩn đoán hình ảnh, gói sức khỏe, thủ thuật, vắc xin,... )
- Quản lý danh sách lịch khám
- Quản lý doanh thu
- Quản lý thuốc, vật tư y tế
- Thống kê tổng quan hệ thống: số lượt khám, doanh thu, tỉ lệ từ chối,...
- Xử lý các khiếu nại của người dùng (liên quan đến hủy lịch,...).
- Gửi thông báo hệ thống đến tất cả người dùng hoặc theo role.

**********NHỮNG CHỨC NĂNG CÓ THỂ TRIỂN KHAI THÀNH CÁC SERVICE RIÊNG THEO MÔ HÌNH MICROSERVICES**********
- Thanh toán online (VNPay, Momo,...) -> Tách thành Payment Service
- Thông báo , nhắc lịch khám (Qua email và giao diện) -> Tách thành Notification Service
- Gửi thông báo đến bệnh nhân (nếu lịch thay đổi hoặc có đề xuất khác). -> Tách thành Notification Service
- Gửi thông báo hệ thống đến tất cả người dùng hoặc theo role. -> Tách thành Notification Service


### **💡 Quy trình kỹ thuật thực tế (áp dụng với Payment Service):**
1.Người dùng đặt dịch vụ/lịch khám → tạo hóa đơn (bill).
2.CBMS gửi thông tin hóa đơn sang Payment Service.
3.Payment Service tích hợp với cổng thanh toán (VNPay, Momo, ZaloPay...) để:
* Tạo link thanh toán hoặc mã QR động.
* Gửi link/QR về cho frontend hiển thị cho người dùng.
4.Người dùng quét mã/thanh toán qua app.
5.Cổng thanh toán callback (webhook) về Payment Service sau khi thanh toán thành công.
6.Payment Service thông báo lại cho CBMS qua Kafka/REST để:
* Cập nhật trạng thái hóa đơn.
* Cho phép sử dụng dịch vụ (VD: xác nhận đặt lịch, gửi thông báo...).


