# Bản kê sự thật — Trang chủ và font

Ngày kê: 11/10/2026. Phạm vi: source hiện có và thay đổi trong lần chỉnh này.
Không phải bản so sánh với một commit GitHub chưa tải về.

## 1. Mốc đối chiếu và danh sách file

Trước khi chỉnh, đã sao chép source/tài liệu/scripts/resources vào:
`C:\Users\asus\AppData\Local\Temp\BoomBoomKiss-home-baseline-de9a6e484779464aad497196a52a84c3`.
Workspace không có Git repository. So sánh hash với bản sao đó và chạy
`git diff --no-index` cho từng file sửa. Diff được lưu cạnh snapshot dưới
tên `diff-<đường_dẫn_file>.txt`. Không có lịch sử commit để suy ra trạng thái
so với những mốc trước lần này: **Không xác định từ code**.

- **SỬA** `Test-game.cmd`: Bổ sung HomeTest vào compile/run kiểm tra.
- **SỬA** `dist/boom-boom-kiss-theme.properties`: Chỉ thay loseText cũ bằng giá trị Unicode tiếng Việt, giữ icon cũ.
- **THÊM MỚI** `model/HomeTest.java`: Test Trang chủ thường/nhỏ, click icon, nhãn phiên, fallback và hành vi lưu.
- **SỬA** `model/Main.java`: Trang chủ không cuộn, đổi text, nhãn phiên chơi, luật theo icon/nhãn và fallback hằng số.
- **SỬA** `model/SwingTest.java`: Phân biệt 3 ô nhãn với punishment text khi kiểm tra.
- **THÊM MỚI tài liệu** `ban-ke-su-that.md`: bản kê này.
- **SỬA tài liệu** `USE_CASE.md`, `USE_CASE.puml`: đồng bộ sau khi chốt bản kê.
- **Tạo lại bản chạy** `dist/BoomBoomKiss.jar`: production theo source, có font resources.
  Binary này không có trong snapshot source để làm diff văn bản.
- **XÓA:** không có file source bị xóa trong lần này.
- **Không cập nhật:** `BoomBoomKiss-GitHub.zip`.

Bộ font UiFonts/TTF đã có trước snapshot, không ghi nhầm là vừa thêm trong
lần này. Font đã được kiểm tra lại qua VisualTest.

## 2. Lớp, thành viên và quan hệ

### 2.1. Class đã thêm/sửa

| Lớp | Package và loại | Quan hệ trực tiếp thấy trong source | Bằng chứng |
|---|---|---|---|
| Main | `boomboomkiss.model`; `public class Main extends JFrame` | Kế thừa JFrame; giữ một tham chiếu Game/Theme, Map nhãn; dùng UiFonts/UiStyle, Card/Button/IconCatalog; gọi Game.saveTheme(). Bội số tổng thể của kiến trúc: Không xác định từ code. | [model/Main.java:40](model/Main.java:40) |
| Main.BoardMode | `private enum BoardMode { SETUP, TARGET, OWN }`; đã tồn tại trước mốc | Main dùng enum cho boardPanel; không đổi enum. | [model/Main.java:42](model/Main.java:42) |
| HomeTest | `boomboomkiss.model`; `public final class HomeTest`; test utility, không phải class sản phẩm | Gọi Main/Game; tạo implementation ThemeRepository ẩn danh trong bộ nhớ; không ghi theme cá nhân. Không có thuộc tính class được khai báo. | [model/HomeTest.java:14](model/HomeTest.java:14) |
| SwingTest | `boomboomkiss.model`; `public final class SwingTest`; test utility đã có | Kiểm tra Main/Board/Theme; sửa cách tìm ô punishment. Không có thuộc tính class được khai báo. | [model/SwingTest.java:29](model/SwingTest.java:29) |

Main có các lớp ẩn danh cho WindowAdapter, FocusAdapter, DocumentListener,
Icon và lớp phủ JPanel. Chúng thực hiện listener/painting tại chỗ, không có
tên class sản phẩm riêng để đặt vào Class Diagram.

### 2.2. Thuộc tính Main — chép nguyên văn

| Lớp | Khai báo trong code | Đặc tính | File:dòng |
|---|---|---|---|
| Main | `private final Game game;` | private final; tham chiếu final, đối tượng tham chiếu có thể thay đổi | [model/Main.java:44](model/Main.java:44) |
| Main | `private final Theme theme;` | private final; tham chiếu final, đối tượng tham chiếu có thể thay đổi | [model/Main.java:45](model/Main.java:45) |
| Main | `private final Random placementRandom = new Random();` | private final; tham chiếu final, đối tượng tham chiếu có thể thay đổi | [model/Main.java:46](model/Main.java:46) |
| Main | `private boolean busy;` | private; mutable | [model/Main.java:47](model/Main.java:47) |
| Main | `private Timer revealTimer;` | private; mutable | [model/Main.java:48](model/Main.java:48) |
| Main | `private Timer fadeTimer;` | private; mutable | [model/Main.java:49](model/Main.java:49) |
| Main | `private Board animationBoard;` | private; mutable | [model/Main.java:50](model/Main.java:50) |
| Main | `private java.util.List<Position> animationPositions = java.util.Collections.emptyList();` | private; mutable | [model/Main.java:51](model/Main.java:51) |
| Main | `private final java.util.List<GameUi.Tile> animatedTiles = new java.util.ArrayList<GameUi.Tile>();` | private final; tham chiếu final, đối tượng tham chiếu có thể thay đổi | [model/Main.java:52](model/Main.java:52) |
| Main | `private JPanel resultOverlay;` | private; mutable | [model/Main.java:53](model/Main.java:53) |
| Main | `private final java.util.Map<CellType, String> sessionLabels = new java.util.EnumMap<CellType, String>(CellType.class);` | private final; tham chiếu final, đối tượng tham chiếu có thể thay đổi | [model/Main.java:54](model/Main.java:54) |

`sessionLabels` có một giá trị cho mỗi CellType, khởi tạo Safe/Boom/Kiss trong
constructor. Map nằm ở UI, không thêm trường vào Theme và không lưu xuống
Properties. final ở tham chiếu Map không làm Map bất biến.

### 2.3. Constructor/phương thức Main — chữ ký nguyên văn

| Lớp | Chữ ký | Ngoại lệ/hợp đồng | File:dòng |
|---|---|---|---|
| Main | `public static void main(String[] args)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:56](model/Main.java:56) |
| Main | `Main()` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:67](model/Main.java:67) |
| Main | `Main(Game game)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:71](model/Main.java:71) |
| Main | `private JPanel stack()` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:92](model/Main.java:92) |
| Main | `private JPanel page(Color background)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:99](model/Main.java:99) |
| Main | `private void display(JPanel content)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:106](model/Main.java:106) |
| Main | `private void confirmExit()` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:126](model/Main.java:126) |
| Main | `private JLabel label(String text, UiFonts.Role role, Color color)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:133](model/Main.java:133) |
| Main | `private JLabel wrapped(String text, int width, UiFonts.Role role, Color color)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:141](model/Main.java:141) |
| Main | `private void gap(JPanel panel, int size)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:146](model/Main.java:146) |
| Main | `private GameUi.Button button(String text, Color color, Runnable action)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:148](model/Main.java:148) |
| Main | `private GameUi.Button iconButton(String icon, String accessibleName, int size, Color color)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:156](model/Main.java:156) |
| Main | `private JPanel heading(String title, String subtitle, Color textColor)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:165](model/Main.java:165) |
| Main | `private JPanel logo(int size)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:185](model/Main.java:185) |
| Main | `private void stopAnimation()` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:196](model/Main.java:196) |
| Main | `private void showMenu()` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:203](model/Main.java:203) |
| Main | `private String defaultLabel(CellType type)` | Ném IllegalArgumentException ở nhánh default; source không khai báo throws. | [model/Main.java:338](model/Main.java:338) |
| Main | `private JPanel rulesCard()` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:343](model/Main.java:343) |
| Main | `private void refreshRules(JPanel card)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:350](model/Main.java:350) |
| Main | `private JPanel ruleLine(CellType[] icons, String text)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:363](model/Main.java:363) |
| Main | `private void saveTheme()` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:373](model/Main.java:373) |
| Main | `private JDialog picker(JPanel content)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:381](model/Main.java:381) |
| Main | `private void showIconPicker(CellType type, Consumer<String> onSelect)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:396](model/Main.java:396) |
| Main | `private JDialog themePicker(CellType type, Consumer<String> onSelect)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:401](model/Main.java:401) |
| Main | `private void showSetup()` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:414](model/Main.java:414) |
| Main | `private void showCellPicker(Board board, Position position)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:465](model/Main.java:465) |
| Main | `private JDialog cellPicker(Board board, Position position)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:470](model/Main.java:470) |
| Main | `private JPanel boardPanel(Board board, BoardMode mode, Player owner)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:495](model/Main.java:495) |
| Main | `private void showHandoff(String title, String message, String command, Runnable next)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:530](model/Main.java:530) |
| Main | `private void showPlay()` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:542](model/Main.java:542) |
| Main | `private JPanel boardSection(Player owner, Color color)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:557](model/Main.java:557) |
| Main | `private JPanel scoreboard()` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:576](model/Main.java:576) |
| Main | `private JPanel scoreRow(Player player, String name)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:583](model/Main.java:583) |
| Main | `private JLabel counter(String icon, String text, int size, Color color)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:594](model/Main.java:594) |
| Main | `private void attack(Position position)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:600](model/Main.java:600) |
| Main | `private void showGameOver()` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:626](model/Main.java:626) |
| Main | `private void disableBehind(java.awt.Container parent)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:671](model/Main.java:671) |
| Main | `private String escapeHtml(String value)` | Không khai báo throws; lỗi handler/luồng con cần xem thân hàm. | [model/Main.java:678](model/Main.java:678) |

Handler CHƠI dùng Theme.DEFAULT_LOSE_TEXT khi text trắng. Callback nhãn
gọi refreshRules(); callback icon gọi theme.setIcon(), thay logo/icon,
refreshRules() và saveTheme(). FocusListener dùng focusGained/focusLost để
đánh dấu ô/khôi phục nhãn trắng. DocumentListener có update(),
insertUpdate(DocumentEvent event), removeUpdate(DocumentEvent event),
changedUpdate(DocumentEvent event); hai listener dành cho nhãn và punishment
đều cập nhật bộ nhớ. Không có thuộc tính/hàm setter nhãn trong model.

### 2.4. Các phương thức test

HomeTest có các chữ ký trực tiếp:
- `public static void main(String[] args) throws Exception` — [model/HomeTest.java:15](model/HomeTest.java:15).
- `private static List<JLabel> labels(Main app,String text)` — [model/HomeTest.java:74](model/HomeTest.java:74).
- `private static boolean hasText(Main app,String text)` — [model/HomeTest.java:79](model/HomeTest.java:79).
- `private static JTextField field(Main app,String name)` — [model/HomeTest.java:83](model/HomeTest.java:83).
- `private static AbstractButton button(Main app,String name)` — [model/HomeTest.java:88](model/HomeTest.java:88).
- `static <T> List<T> find(Container parent,Class<T> type)` — [model/HomeTest.java:92](model/HomeTest.java:92).
- `private static void layout(Container parent)` — [model/HomeTest.java:100](model/HomeTest.java:100).
- `private static void snapshot(Main app,Path output) throws Exception` — [model/HomeTest.java:103](model/HomeTest.java:103).
- `private static void check(boolean valid,String message)` — [model/HomeTest.java:107](model/HomeTest.java:107).

HomeTest.check() ném AssertionError nếu điều kiện test không đạt; các helper
field()/button() ném AssertionError khi không tìm thấy. main()/snapshot()
khai báo throws Exception; main bắt Exception trong callback Swing và bọc
RuntimeException. Đây là lỗi kiểm tra, không đổi hợp đồng nghiệp vụ.
SwingTest thêm `private static JTextField punishment(Main frame)`, tìm qua
accessibleName `"Punishment text"`; không tìm thấy thì ném AssertionError.

## 3. Giá trị mặc định và hằng số

| Mục | Giá trị nguyên văn / thực trạng | Bằng chứng |
|---|---|---|
| Theme.DEFAULT_LOSE_TEXT | `public static final String DEFAULT_LOSE_TEXT = "Hãy hôn đối phương!";`; đã có trước snapshot | [model/Theme.java:4](model/Theme.java:4) |
| Khởi tạo loseText | `private String loseText = DEFAULT_LOSE_TEXT;` | [model/Theme.java:8](model/Theme.java:8) |
| Icon mặc định | SAFE `safe-check`; BOOM `boom-bomb`; KISS `kiss-lips` là phần tử đầu của choices | [model/Theme.java:5](model/Theme.java:5); [model/IconCatalog.java:15](model/IconCatalog.java:15) |
| Nhãn UI mặc định | `"Safe"`, `"Boom"`, `"Kiss"`; sessionLabels, defaultLabel() | [model/Main.java:75](model/Main.java:75); [model/Main.java:338](model/Main.java:338) |
| Tên theme | Không có thuộc tính tên theme trong Theme; không suy ra tên lưu trữ từ tên cửa sổ | [model/Theme.java:3](model/Theme.java:3) |
| Tên cửa sổ | `super("Boom Boom Kiss");` không đổi | [model/Main.java:72](model/Main.java:72) |
| GameConfig | BOARD_SIZE = 5; BOOM_COUNT = 2; KISS_COUNT = 1; BLAST_RADIUS = 1; tất cả static final; không đổi | [config/GameConfig.java:4](config/GameConfig.java:4) |
| Bộ font | TITLE 40, HEADING 26, BUTTON 20; LABEL/COUNTER 16, BODY 15, CAPTION 13; không đổi trong lần này | [model/UiFonts.java:21](model/UiFonts.java:21); [model/UiFonts.java:30](model/UiFonts.java:30) |
| Style Trang chủ | UiStyle.CREAM, PAPER, DARK, PLAYER_ONE, RADIUS; nền luật = blend(CREAM, PLAYER_ONE, .22); không thêm hằng số style | [config/UiStyle.java:6](config/UiStyle.java:6); [model/Main.java:343](model/Main.java:343) |
| Giới hạn text/nhãn | Không có kiểm tra maxLength, DocumentFilter hoặc giới hạn ký tự trong các ô nhập này. Không xác định giới hạn từ thiết kế ngoài code. | [model/Main.java:241](model/Main.java:241); [model/Main.java:294](model/Main.java:294) |

Danh sách icon được chép nguyên văn từ source, không có diff trong lần này:

- `private static final String[] SAFE = {"safe-check", "safe-cookie", "safe-flower", "safe-star", "safe-leaf", "safe-smile"};` — [model/IconCatalog.java:15](model/IconCatalog.java:15).
- `private static final String[] BOOM = {"boom-bomb", "boom-fire", "boom-bolt", "boom-burst", "boom-dynamite", "boom-rocket"};` — [model/IconCatalog.java:16](model/IconCatalog.java:16).
- `private static final String[] KISS = {"kiss-lips", "kiss-heart", "kiss-face", "kiss-gift", "kiss-party", "kiss-cherry"};` — [model/IconCatalog.java:17](model/IconCatalog.java:17).

## 4. Kiểm tra dữ liệu và xử lý lỗi

- Text hình phạt chỉ có khoảng trắng khi CHƠI: thay bằng Theme.DEFAULT_LOSE_TEXT
  rồi gọi saveTheme(), restart(), showSetup(): [model/Main.java:312](model/Main.java:312).
- Nhãn trắng: DocumentListener đưa giá trị dùng trong luật về defaultLabel();
  khi mất focus, ô nhập được điền lại nhãn mặc định. Nhãn có nội dung dùng
  trim() cho Map; không đổi icon hoặc loại bánh: [model/Main.java:254](model/Main.java:254).
- Nội dung tự nhập được escapeHtml() trước khi đưa vào luật HTML:
  [model/Main.java:363](model/Main.java:363); [model/Main.java:678](model/Main.java:678).
- setLoseText(null) và setIcon sai vẫn ném IllegalArgumentException từ Theme;
  không đổi hợp đồng: [model/Theme.java:11](model/Theme.java:11); [model/Theme.java:21](model/Theme.java:21).
- defaultLabel() có nhánh default ném IllegalArgumentException:
  [model/Main.java:338](model/Main.java:338).
- Không ghi được theme: Main.saveTheme() bắt IllegalStateException, báo
  `"Chưa lưu được theme. Bạn vẫn có thể chơi ván này."`:
  [model/Main.java:373](model/Main.java:373). Không đổi xử lý này.
- Giới hạn đặt bánh, sai tọa độ/giai đoạn, nút LƯU BÀN khóa, chỉ bàn đối thủ
  nhận click và ô đã mở bị chặn vẫn nằm trong code cũ không có diff:
  [model/Board.java:62](model/Board.java:62); [model/Main.java:457](model/Main.java:457);
  [model/Main.java:507](model/Main.java:507).
- Font thiếu/lỗi: UiFonts.load() ghi stderr, dùng Nunito ExtraBold hoặc
  system weighted fallback; không sập. Không đổi source font lần này:
  [model/UiFonts.java:40](model/UiFonts.java:40); [model/UiFonts.java:56](model/UiFonts.java:56).

## 5. Lưu trữ theme

Đường dẫn được chép nguyên văn: `Paths.get("boom-boom-kiss-theme.properties")`
ở [model/Main.java:68](model/Main.java:68).
Đây là đường dẫn tương đối với working directory. Script dist/Choi-game.cmd
có `cd /d "%~dp0"`; chạy script dùng dist làm thư mục lưu.

Định dạng là Java Properties, không phải JSON. Ví dụ nội dung thật sau chỉnh
của file dist (giữ nguyên icon đã có, chỉ đổi dòng hình phạt):

```properties
safeIcon=safe-check
loseText=H\u00e3y h\u00f4n \u0111\u1ed1i ph\u01b0\u01a1ng!
boomIcon=boom-dynamite
kissIcon=kiss-cherry
```

Bằng chứng: [dist/boom-boom-kiss-theme.properties:3](dist/boom-boom-kiss-theme.properties:3).
Mã unicode escape trong file được Properties.load() khôi phục.

- Đọc: Game nhận ThemeRepository, gọi repository.load() khi constructor;
  file thiếu dùng Theme mặc định. Missing field dùng giá trị mặc định cho
  trường đó. Icon lạ/cũ được normalize() về phần tử đầu của loại.
- File hỏng/lỗi đọc: FileThemeRepository ném IllegalStateException; Main báo
  lỗi và dùng MemoryThemeRepository. Trong nhánh này, thay đổi chỉ ở bộ nhớ.
- Ghi: đổi icon; CHƠI; xác nhận thoát. Nhập nhãn/punishment không ghi sau mỗi
  ký tự. Thay nhãn không gọi Game.saveTheme(). Vị trí quân không lưu.
- Nhãn được giữ khi quay lại Trang chủ/chơi lại trong cùng Main. Tạo cửa sổ
  ứng dụng mới luôn khởi tạo lại Safe/Boom/Kiss.
- Text đã lưu trên máy khác không tự bị migrate. Muốn đổi text cũ: nhập
  “Hãy hôn đối phương!” rồi bấm CHƠI; hoặc xóa file theme sau khi sao lưu để
  lần khởi động sau lấy mặc định (xóa cũng làm mất icon đã tùy chỉnh).
  Lần này chỉ đổi file theme ở dist trong workspace được yêu cầu.
- Theme mẫu không có template riêng trong resources. File dist là file đã
  lưu thực tế, không gọi nhầm nó là dữ liệu mặc định cho mọi máy.

Bằng chứng lưu/đọc: [repository/FileThemeRepository.java:21](repository/FileThemeRepository.java:21);
[repository/FileThemeRepository.java:38](repository/FileThemeRepository.java:38);
[model/Game.java:38](model/Game.java:38);
[model/Main.java:373](model/Main.java:373);
[model/Main.java:126](model/Main.java:126).

## 6. Giao diện và chuỗi hiển thị

### Trang chủ — Main.showMenu()

- Đã bỏ tiêu đề trong nội dung `"Boom Boom Kiss"` và phụ đề
  `"2 người cùng máy  \\u00b7  Lưới 5 \\u00d7 5"`. Giữ title thanh cửa sổ.
- Giữ nút `"×"` góc phải, accessibleName `"Thoát trò chơi"` và confirmExit().
- Logo có 3 icon từ theme, cỡ 58; wordmark `"BOOM BOOM"` và `"KISS"`
  dùng HEADING. Không đổi kích thước cửa sổ trong constructor.
- Thẻ trắng: `"Chọn biểu tượng theo ý bạn"` (LABEL),
  `"Bấm vào ô để đổi biểu tượng"` (CAPTION).
- Ba icon giữ cách mở bảng lựa chọn; ô đang thao tác có viền RED, chú thích
  đổi LABEL/CAPTION theo focus/selection. Ô sửa nhãn nằm dưới icon, riêng
  component, mặc định Safe/Boom/Kiss, accessibleName `"Nhãn " + type.name()`.
- Ô hình phạt: nhãn `"Hành động khi thua"`, placeholder
  `"Nhập hành động cho người thua…"`, accessibleName `"Punishment text"`.
- Giữ chữ `"CHƠI"` và icon tam giác vẽ trong Java; hành vi lưu/bắt đầu không đổi.
- Không dùng JScrollPane ở menu. Thẻ trắng và luật nằm trong khung CREAM.
- Thẻ luật title `"Luật chơi"`; icon nhỏ lấy theme SAFE. Dòng 1 có icon
  BOOM/BOOM/KISS và ghép chính xác:
  `"Mỗi bàn giấu 2 " + sessionLabels.get(CellType.BOOM) + " và 1 " + sessionLabels.get(CellType.KISS)`.
  Dòng 2 có icon KISS và ghép:
  `"Mở trúng " + sessionLabels.get(CellType.KISS) + ", trực tiếp hoặc do nổ lan, là thua"`.
  Nhãn mặc định cho ra hai câu yêu cầu. Chữ CAPTION 13, INK từ UiStyle.DARK,
  lề trái và cùng cột icon. refreshRules() gọi khi đổi icon/nhãn.
- Hai dòng luật cũ đã xóa khỏi menu.

Bằng chứng: [model/Main.java:203](model/Main.java:203);
[model/Main.java:350](model/Main.java:350); [model/Main.java:363](model/Main.java:363).

### Các màn/hộp thoại còn lại

Không có diff nghiệp vụ/setup/play/results/confirm trong Main; chỉ số dòng
dịch do Trang chủ dài hơn. Các chữ chính hiện tại:

- Setup: `"Người " + ... + " đặt bánh"`, `"Bấm một ô để chọn biểu tượng"`,
  `"NGẪU NHIÊN"`, `"XÓA HẾT"`, `"LƯU BÀN"`:
  [model/Main.java:414](model/Main.java:414).
- Chuyển máy: `"Người 1 đã sẵn sàng!"`, `"Đưa máy cho Người 2. Đừng nhìn nhé!"`,
  `"TIẾP TỤC"`; setup xong: `"Đã đặt bánh xong!"`,
  `"Đưa máy lại cho Người 1 để bắt đầu."`, `"BẮT ĐẦU"`:
  [model/Main.java:451](model/Main.java:451).
- Lượt chơi: `"Lượt Người "`, `"Chọn một ô trên bàn đối thủ"`;
  title `"Bàn Người "`/hàng đếm P1/P2 từ model:
  [model/Main.java:542](model/Main.java:542).
- Kết quả: `"BOOM! Bạn đã trúng Kiss!"`, `"HÌNH PHẠT"`; punishment lấy
  `theme.getLoseText()`; nút source `"Chơi lại"`/`"Về trang chủ"` được
  RoundedButton.setText() viết hoa:
  [model/Main.java:626](model/Main.java:626);
  [model/RoundedButton.java:17](model/RoundedButton.java:17).
- Thoát: `"Thoát trò chơi?"`; CÓ/KHÔNG do UiFonts UIManager:
  [model/Main.java:126](model/Main.java:126);
  [model/UiFonts.java:101](model/UiFonts.java:101).

## 7. Những gì không đổi — bằng chứng đối chiếu

- `model/Game.java`: hash trước/sau bằng nhau; không có diff.
- `model/Board.java`: hash trước/sau bằng nhau; không có diff.
- `model/Cell.java`: hash trước/sau bằng nhau; không có diff.
- `model/CellType.java`: hash trước/sau bằng nhau; không có diff.
- `model/Position.java`: hash trước/sau bằng nhau; không có diff.
- `model/AttackResult.java`: hash trước/sau bằng nhau; không có diff.
- `model/Theme.java`: hash trước/sau bằng nhau; không có diff.
- `config/GameConfig.java`: hash trước/sau bằng nhau; không có diff.
- `config/UiStyle.java`: hash trước/sau bằng nhau; không có diff.
- `model/UiFonts.java`: hash trước/sau bằng nhau; không có diff.
- `model/GameUi.java`: hash trước/sau bằng nhau; không có diff.
- `model/RoundedButton.java`: hash trước/sau bằng nhau; không có diff.
- `model/IconCatalog.java`: hash trước/sau bằng nhau; không có diff.
- `model/PlaceholderField.java`: hash trước/sau bằng nhau; không có diff.
- `repository/ThemeRepository.java`: hash trước/sau bằng nhau; không có diff.
- `repository/FileThemeRepository.java`: hash trước/sau bằng nhau; không có diff.
- `repository/MemoryThemeRepository.java`: hash trước/sau bằng nhau; không có diff.
- `model/GameTest.java`: hash trước/sau bằng nhau; không có diff.
- `model/ThemeTest.java`: hash trước/sau bằng nhau; không có diff.
- `model/VisualTest.java`: hash trước/sau bằng nhau; không có diff.
- `Build-game.cmd`: hash trước/sau bằng nhau; không có diff.

GameState và Player nằm trong Game.java; SafeCell/BoomCell/KissCell nằm trong
Cell.java. Vì các file đó hash bằng nhau nên tên, luật nổ ngang/dọc và
Template Method trong chúng không đổi so với snapshot này.

## 8. Các điểm khác thiết kế / giới hạn thấy trong code

- **Chưa có package ui:** giao diện đang trong `boomboomkiss.model`, theo
  quyền người dùng đã cho sửa các lớp giao diện ở vị trí hiện tại.
  Không chuyển package hoặc sửa quyền truy cập model:
  [model/Main.java:1](model/Main.java:1).
- **Model có biết Theme:** Game có `private final Theme theme;` và
  `private final ThemeRepository themeRepository;`; thiết kế “model không
  biết Theme” chưa khớp code này. Không sửa kiến trúc ngoài yêu cầu:
  [model/Game.java:30](model/Game.java:30).
- **Các tên loại bánh vẫn có literal trong UI:** defaultLabel() trả Safe/Boom/Kiss;
  setup vẫn có `"Đặt đủ 2 boom và 1 kiss để lưu."`. Nhãn mới chỉ đổi ở
  Trang chủ/thẻ Luật chơi, chưa thay toàn bộ câu chữ các màn khác:
  [model/Main.java:338](model/Main.java:338);
  [model/Main.java:460](model/Main.java:460).
- **Nhãn không thuộc Theme:** sessionLabels trong Main, theo câu trả lời
  “Chỉ đổi tên trong lần chơi hiện tại”. Không có trường name/label trong
  file theme và không đổi các loại enum nghiệp vụ.
- **Không có maxLength nhãn:** nhãn rất dài có thể làm thẻ luật cao; chỉ kiểm
  kích thước cửa sổ hiện tại với nhãn mặc định/nhãn ngắn trong test.
  Không suy ra giới hạn hoặc đảm bảo mọi text tùy ý vừa màn hình.
- **Mặc định nay đồng bộ trong code:** Theme.DEFAULT_LOSE_TEXT là một hằng số;
  handler CHƠI tham chiếu hằng số đó. File lưu chứa giá trị dữ liệu chứ không
  phải hằng số code. Không tự migrate file cũ ở máy khác.
- Tên chuẩn BoomCell/BOOM/open/onOpen và ngoại lệ model đã đúng trong source
  hiện tại; không có diff thay các thành phần đó trong lần này.

## 9. Kiểm chứng và điểm chưa xác định

Đã chạy trên Java 8:
- HomeTest: không cuộn ở 480×850/400×660, text mới, nhãn phiên, chọn icon
  từ nút thực tế, save, fallback khi text trắng; ảnh 11-home/12-home-small.
- GameTest: Template Method, đóng gói, ngoại lệ, luật, package.
- ThemeTest: Unicode/file/defaults/dependency.
- SwingTest: setup/chuyển máy/chơi/kết quả, đổi lượt và hủy hiệu ứng.
- VisualTest: 7 vai trò đúng file/cỡ, TTF tĩnh, tiếng Việt, metrics nút/input.

Bản font trước snapshot đã có Baloo 2 ExtraBold 40/26/20 và Nunito
ExtraBold 16/Nunito Bold 15/13. Không dùng font mới khác trong lần này.
Thứ tự và kết quả test được ghi từ output, không suy ra việc nhóm đã test chéo.

**Không xác định từ code:**
- Lịch sử thay đổi so với commit trước của repo GitHub.
- Nhóm đã kiểm thử/nộp tài liệu trên máy nào và kết quả của các máy đó.
- Giới hạn độ dài nhãn/hình phạt được duyệt chính thức.
- Ý định đổi package ui hoặc tách Theme khỏi model trong một lần khác.

Chưa thay ZIP/GitHub. Bản kê này được lập sau khi code đã chốt; bước lập bản
kê chỉ đọc/đối chiếu source, không chỉnh code.
