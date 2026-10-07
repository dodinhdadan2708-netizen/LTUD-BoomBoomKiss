# Boom Boom Kiss — bản final Java Swing

Game local cho 2 người trên cùng máy, lưới 5×5, mỗi người đặt 2 boom và 1 kiss.
Giao diện tiếng Việt, icon màu tự vẽ trong Java, không cần thư viện hay mạng.
Tương thích Java 8 trở lên.

## Chạy game

- Tải repo bằng **Code → Download ZIP**, giải nén rồi mở `boomboomkiss/Choi-game.cmd`.
- Giữ cả thư mục `dist` đi cùng source. File `.jar` đã chứa đủ icon/giao diện,
  không phải tải thêm thư viện.
- IntelliJ: mở project `Baitap`, mở `boomboomkiss/Main.java` và chạy `boomboomkiss.Main`.
- Bản đóng gói: chạy `java -jar boomboomkiss/dist/BoomBoomKiss.jar` từ thư mục `src`.
  Nếu Windows đã liên kết `.jar` với Java, có thể mở bằng bấm đúp.
- Windows: mở `dist/Choi-game.cmd` để luôn dùng thư mục `dist` làm nơi lưu theme.
  Không cần IntelliJ hoặc JDK để chơi; cần Java 8 trở lên.

## Upload GitHub và build từ source

Upload nguyên thư mục `boomboomkiss`, gồm source, test, tài liệu, hai script
ở ngoài và `dist/BoomBoomKiss.jar` cùng `dist/Choi-game.cmd`. `.gitignore`
chỉ bỏ qua build, `.class` rời và theme cá nhân; không bỏ qua `dist`.
ZIP `BoomBoomKiss-GitHub.zip` ở cạnh thư mục là gói giao nộp, giải nén để lấy
đúng thư mục; nếu chỉ upload ZIP, GitHub không hiển thị source để review.

Để tự build, cài JDK 8 trở lên, để `javac` và `jar` trong PATH rồi chạy
`Build-game.cmd`. Script tạo lại `dist/BoomBoomKiss.jar` từ source hiện tại.

## Cách dùng

Ở trang chủ, ba nút icon lần lượt là Safe, Boom, Kiss. Bấm nút để mở bảng chọn
6 icon, chỉ có hình và không có ô nhập chữ. Ô nhập duy nhất là hành động khi
thua (punishment text). Đổi icon hoặc chơi lại giữ nguyên text này.

Người 1 bấm ô setup để chọn biểu tượng: Safe xóa quân đã đặt, Boom đặt boom,
Kiss đặt kiss. Chỉ lưu được khi đủ 2 boom + 1 kiss; lựa chọn vượt giới hạn bị
vô hiệu hóa. Có nút Ngẫu nhiên và Xóa hết. Sau khi lưu, màn trắng che toàn bộ
bàn để đưa máy cho Người 2; Người 2 đặt xong rồi đưa máy lại cho Người 1.
Đóng bảng chọn icon bằng Esc hoặc bấm ra ngoài.

Khi chơi, bàn xanh Người 2 luôn ở trên, bàn cam Người 1 luôn ở dưới. Chỉ bàn
đối thủ nhận cú bấm, các ô chưa mở ở cả hai bàn đều giữ kín. Thanh P1/P2 đếm
Safe/Boom/Kiss chưa mở. Sau khi hiện ô vừa mở trong 330 ms, game tự đổi lượt,
không có popup kết quả mỗi lượt và không có nút Pass Turn. Trúng kiss sẽ hiện
người thắng/thua và nguyên punishment text, cùng nút Chơi lại/Trang chủ.

## Luật nổ

Mỗi boom chọn ngang hoặc dọc với xác suất 50/50, mở tối đa một ô ở mỗi phía
theo hướng đó. Biên bàn được cắt an toàn. Boom bị lật trong vùng nổ tạo chuỗi
với hướng riêng; mỗi boom chỉ nổ một lần và ô đã mở không bị xử lý lại. Người
kích hoạt chuỗi chạm kiss là người thua. Người 1 đi trước.

Theme lưu vào `boom-boom-kiss-theme.properties` trong thư mục chạy game. Chỉ
lưu 3 lựa chọn icon và punishment text; vị trí bánh không ghi ra file.

## Kiểm tra

Chạy `boomboomkiss.GameTest`, `boomboomkiss.ThemeTest`, `boomboomkiss.SwingTest` trong IntelliJ.
Các test kiểm tra nổ ngẫu nhiên có hướng cố định, góc/cạnh, nổ dây chuyền,
đặt trùng/vượt giới hạn, kiss cuối cùng, thắng thua, lưu Unicode punishment,
bảng icon-only, chuyển máy, giữ kín quân và vị trí hai bàn. `SwingTest` xuất
ảnh kiểm tra vào `boomboomkiss/build/previews` và dùng theme thử trong bộ nhớ.

Sơ đồ lớp ở `CLASS_DIAGRAM.md`. Phần luật nằm trong Game/Board/Cell, giao diện
trong Main/GameUi, icon trong IconCatalog và đọc/ghi theme qua ThemeRepository.

Sơ đồ tổng quan giản lược helper private, nhưng ghi đúng API và tên lớp trong
source. So với ảnh cũ, Theme có thêm Safe/Boom theo yêu cầu chọn 3 icon riêng;
Main bỏ selectedPiece vì setup dùng picker trên từng ô. Config giữ ba hằng số
static final. Bản source này không còn package `week_3`.

## Khai báo sử dụng AI

- Công cụ: Codex, dùng trong các phiên 01/10, 02/10, 06/10 và 07/10/2026.
- Dùng cho: dựng demo Swing, triển khai luật, chỉnh giao diện/icon, viết test,
  đồng bộ sơ đồ lớp và tài liệu theo yêu cầu Lâm cung cấp.
- Lỗi đã phát hiện qua phản hồi của Lâm: lộ bàn khi chơi, đảo vị trí bàn,
  bước Pass Turn và popup mỗi lượt không phù hợp. Đã sửa. Luật nổ ban đầu mở
  4 phía; đã cập nhật thành random ngang/dọc theo luật nhóm cung cấp.
- Kiểm chứng hiện có: biên dịch và chạy test tự động bằng Java 8; ảnh render
  các màn setup, chuyển máy, chơi và kết quả. Chưa ghi nhận kiểm thử chéo của
  cả nhóm; nhóm cần ghi người kiểm tra và kết quả thực tế trước khi nộp.

Class Diagram source: CLASS_DIAGRAM.mmd; formatted explanation: CLASS_DIAGRAM.md.
Use Case analysis: USE_CASE.md.

