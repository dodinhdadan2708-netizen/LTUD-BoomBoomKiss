# Boom Boom Kiss — Java Swing, bản OOP v1.0

Bản cập nhật 08/10/2026: local 2 người cùng máy, 2 bàn 5×5, mỗi bàn 2 Boom,
1 Kiss, 22 Safe. Giao diện tiếng Việt, icon màu vẽ trong Java, không cần mạng
hay thư viện ngoài. Bản `.jar` chạy với Java 8 trở lên.

## Chạy game

Tải repo bằng **Code → Download ZIP**, giải nén, mở `boomboomkiss/Choi-game.cmd`
trên Windows. Giữ cả thư mục `dist`: script mở `dist/BoomBoomKiss.jar` và lưu
theme tại đó. Cửa sổ lệnh CMD có thể mở cùng game; game không nằm trong System32.

Hoặc chạy từ thư mục repo/src:

```text
java -jar boomboomkiss/dist/BoomBoomKiss.jar
```

Trong IntelliJ, đặt thư mục cha của `boomboomkiss` làm Sources Root, mở
`boomboomkiss/model/Main.java` và chạy `boomboomkiss.model.Main`. Đừng mở
`.class` trong thư mục `out` để chạy source. Chơi bản `.jar` chỉ cần Java;
biên dịch/test cần JDK 8 trở lên.

## Cấu trúc — đúng 3 package

```text
boomboomkiss/
  model/       Game, Player, Board, Cell và lớp con, CellType,
               Position, AttackResult, Theme, GameState,
               Main, GameUi, IconCatalog và 3 bài test
  config/      GameConfig
  repository/  ThemeRepository, FileThemeRepository, MemoryThemeRepository
  dist/        BoomBoomKiss.jar, Choi-game.cmd
  Choi-game.cmd
  Build-game.cmd
  Test-game.cmd
  CLASS_DIAGRAM.md, CLASS_DIAGRAM.mmd, PACKAGE_DIAGRAM.mmd
  USE_CASE.md, USE_CASE.puml
```

Các class CellType, Player, GameState và lớp con Cell có thể nằm cùng file với
class liên quan; tên class và package được dùng đúng trong source. Main và
helper vẽ Swing nằm trong model để không thêm package GUI thứ tư.

## Chơi và tùy chỉnh

Trang chủ có ba nút icon riêng Safe/Boom/Kiss, mỗi nút mở bảng 6 icon chỉ có
hình. Ô nhập duy nhất là punishment text. Đổi icon hoặc chơi lại giữ text này.

Người 1 bấm một ô setup để mở bảng ba icon. Safe xóa quân cũ, Boom/Kiss đặt
quân mới. Loại đã đặt đủ bị khóa; Lưu bàn chỉ bật khi đủ 2 Boom + 1 Kiss.
Có Ngẫu nhiên/Xóa hết và có thể đóng picker bằng Esc hoặc bấm ra ngoài.
Lưu bàn → màn trắng che bàn → Người 2 setup → chuyển máy lại Người 1 bắt đầu.

Trong trận, P2 xanh luôn ở trên, P1 cam luôn ở dưới. Chỉ bàn đối thủ nhận
cú bấm; các ô chưa mở của cả hai bàn đều giấu quân. Thanh P1/P2 đếm bánh còn
chưa mở. Mở ô xong giữ kết quả trong 330 ms rồi tự đổi lượt; không có Pass
Turn hay popup mỗi lượt. Kiss bị mở thì hiện người thắng/thua và punishment
text, cùng nút Chơi lại/Trang chủ.

Mỗi Boom chọn ngang/dọc 50/50, mở tối đa `BLAST_RADIUS = 1` ô mỗi phía. Boom
gặp trong vùng nổ chọn hướng riêng; mỗi Boom chỉ nổ một lần, ô đã mở và ô
ngoài bàn được bỏ qua. Người kích hoạt chuỗi chạm Kiss thua. Người 1 đi trước.

## OOP và hợp đồng ngoại lệ

- `Cell` abstract có Template Method `final open(Board, AttackResult)`.
  Hàm này làm các bước chung rồi gọi `protected abstract onOpen(...)`.
  SafeCell không tạo hiệu ứng; BoomCell gọi nổ; KissCell đánh dấu thua.
  Board.open() không if/switch theo loại để quyết định hành vi mở ô.
- Thuộc tính model private; Position bất biến với `private final row/column`.
  Board không trả mảng cells; AttackResult trả danh sách không cho sửa.
- Thống nhất `BoomCell`, `CellType.BOOM`, `BOOM_COUNT`, `opened`, `isOpened`,
  `open`, `onOpen`; không dùng tên BombCell/BOMB/reveal/isRevealed trong source.
- Game nhận **ThemeRepository interface** qua constructor. Main chọn file
  repository ở điểm khởi tạo; test inject repository trong bộ nhớ. Game không
  import hoặc khởi tạo implementation repository cụ thể.
- Board.place() trả void. Ngoài bàn/null/đặt trùng ném IllegalArgumentException;
  vượt giới hạn, chỉnh sau xác nhận, mở sai giai đoạn/ô cũ ném
  IllegalStateException. Board.edit() kiểm tra trước khi thay quân để giữ dữ
  liệu cũ khi thao tác thất bại. GUI hiển thị lỗi thay vì model return false.
- File chưa tồn tại là lần chạy đầu, dùng mặc định. Lỗi thật khi đọc/ghi file
  được repository ném IllegalStateException. GUI báo lỗi đọc và mở theme mặc
  định trong bộ nhớ; lỗi lưu được báo để người dùng biết chưa lưu xuống file.

`boom-boom-kiss-theme.properties` chỉ lưu ba icon và punishment text, không
lưu vị trí bánh. File cá nhân bị .gitignore bỏ qua.

## Build, test và upload

Cài JDK có `javac`/`jar` trong PATH rồi chạy `Build-game.cmd` để tạo lại `.jar`.
Chạy `Test-game.cmd` để build và kiểm tra luật, theme và GUI. Hoặc chạy trong
IntelliJ: `boomboomkiss.model.GameTest`, `boomboomkiss.model.ThemeTest`,
`boomboomkiss.model.SwingTest`.

Test kiểm tra Template Method bằng lớp Cell thử nghiệm, đóng gói qua reflection,
ngoại lệ/giới hạn/giai đoạn, cả hướng ngang/dọc, chồng vùng nổ, nổ dây chuyền,
Kiss cuối cùng, Unicode punishment, lỗi file không bị nuốt, phụ thuộc interface,
đúng ba package, bảng chọn icon-only, giấu quân, hai bàn cố định và đổi lượt.
SwingTest render ảnh tại `boomboomkiss/build/previews` mà không dùng chuột hay
bàn phím người dùng. Các test theme dùng dữ liệu tạm, không ghi đè theme cá nhân.

Upload **nguyên thư mục `boomboomkiss` mới**, có cả `dist`. .gitignore bỏ qua
build/ và .class rời, không bỏ qua dist. ZIP `BoomBoomKiss-GitHub.zip` cạnh
thư mục là gói để giải nén/upload; nếu chỉ up ZIP, GitHub không hiển thị code.
Xóa cấu trúc source cũ trên GitHub trước khi thay để tránh trùng package/API.

Sơ đồ lớp: CLASS_DIAGRAM.md/mmd; phụ thuộc package: PACKAGE_DIAGRAM.mmd;
phân tích Use Case: USE_CASE.md, sơ đồ UML Use Case: USE_CASE.puml.

## Khai báo sử dụng AI

- Công cụ: Codex; các phiên 01/10, 02/10, 06/10, 07/10 và 08/10/2026.
- Dùng cho: dựng/chỉnh Swing, luật game, icon, test, sơ đồ và đóng gói theo
  yêu cầu và phản hồi của Lâm.
- Các lỗi được Lâm chỉ ra và đã sửa: lộ quân, đảo bàn, Pass Turn/popup dư;
  luật nổ 4 phía; sửa theo sơ đồ cũ làm mất hành vi đa hình, thuộc tính chưa
  private, place trả boolean và chưa tách package. Bản này khôi phục Template
  Method, kiểm tra ngoại lệ và chia model/config/repository.
- Kiểm chứng bằng compile/test Java 8 và ảnh render giao diện. Chưa ghi nhận
  kiểm thử chéo của cả nhóm; nhóm cần ghi người kiểm tra và kết quả thực tế
  trước khi nộp.
