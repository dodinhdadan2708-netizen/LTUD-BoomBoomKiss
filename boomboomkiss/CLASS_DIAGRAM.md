# Boom Boom Kiss — Class Diagram OOP

Cập nhật ngày 08/10/2026 theo yêu cầu Template Method, đóng gói và ba package.
Sơ đồ mô tả source hiện tại. Constructor và một số
helper private vẽ Swing được lược bỏ. `-` private, `~` package-private,
`#` protected, `+` public; `$` static, `*` phương thức abstract.

- `boomboomkiss.model`: Game, Player, Board, Cell và các lớp con, CellType,
  Position, AttackResult, Theme, GameState; Main/GameUi/IconCatalog và các test.
- `boomboomkiss.config`: GameConfig.
- `boomboomkiss.repository`: ThemeRepository, FileThemeRepository, MemoryThemeRepository.

Main là điểm khởi tạo chọn FileThemeRepository. Game chỉ import interface
ThemeRepository và nhận dependency qua constructor; không khởi tạo repository
cụ thể. Sơ đồ phụ thuộc package nằm trong `PACKAGE_DIAGRAM.mmd`.

```mermaid
classDiagram
    direction TB
    class JFrame

    class Main {
        -Game game
        -Theme theme
        -boolean busy
        -Timer revealTimer
        +main(String[] args) void$
        -showMenu() void
        -showSetup() void
        -showPlay() void
        -attack(Position position) void
        -showGameOver() void
    }

    class Game {
        -Player player1
        -Player player2
        -Player currentPlayer
        -Player loser
        -GameState state
        -Random random
        -ThemeRepository themeRepository
        -Theme theme
        ~getPlayer1() Player
        ~getPlayer2() Player
        ~getCurrentPlayer() Player
        ~getLoser() Player
        ~getState() GameState
        ~getTheme() Theme
        ~saveTheme() void
        ~restart() void
        ~setupPlayer() Player
        ~confirmSetup() void
        ~opponent() Player
        ~attack(Position position) AttackResult
        ~switchTurn() void
        ~getWinner() Player
    }

    class GameState {
        <<enumeration>>
        SETUP_PLAYER_1
        SETUP_PLAYER_2
        PLAYING
        GAME_OVER
    }

    class Player {
        -String name
        -Board board
        ~getName() String
        ~getBoard() Board
    }

    class Board {
        -Cell[][] cells
        -Random random
        -boolean setupLocked
        -boolean playing
        -AttackResult activeResult
        ~isInside(Position position) boolean
        ~getCell(Position position) Cell
        ~clear() void
        ~count(CellType type) int
        ~remaining(CellType type) int
        ~place(Position position, CellType type) void
        ~edit(Position position, CellType type) void
        ~remove(Position position) void
        ~isSetupComplete() boolean
        ~confirmSetup() void
        ~startPlaying() void
        ~endPlaying() void
        ~randomize(Random placementRandom) void
        ~open(Position target) AttackResult
        ~validateOpening(Cell cell, AttackResult result) void
        ~explode(Position center, AttackResult result) void
    }

    class Cell {
        <<abstract>>
        -Position position
        -boolean opened
        ~getPosition() Position
        ~isOpened() boolean
        ~open(Board board, AttackResult result) void
        ~getType() CellType*
        #onOpen(Board board, AttackResult result) void*
    }

    class SafeCell {
        ~getType() CellType
        #onOpen(Board board, AttackResult result) void
    }
    class BoomCell {
        ~getType() CellType
        #onOpen(Board board, AttackResult result) void
    }
    class KissCell {
        ~getType() CellType
        #onOpen(Board board, AttackResult result) void
    }

    class CellType {
        <<enumeration>>
        SAFE
        BOOM
        KISS
        -int limit
        -Function~Position,Cell~ factory
        ~getLimit() int
        ~isPlaceable() boolean
        ~create(Position position) Cell
    }

    class Position {
        -int row
        -int column
        ~getRow() int
        ~getColumn() int
        +equals(Object other) boolean
        +hashCode() int
    }

    class AttackResult {
        -List~Position~ opened
        -Queue~Position~ pending
        -boolean kissHit
        -boolean finished
        ~getOpened() List~Position~
        ~isKissHit() boolean
        ~recordOpened(Position position) void
        ~markKissHit() void
        ~enqueue(Position position) void
        ~hasPending() boolean
        ~nextPending() Position
        ~finish() void
    }

    class GameConfig {
        <<utility>>
        +int BOARD_SIZE = 5$
        +int BOOM_COUNT = 2$
        +int KISS_COUNT = 1$
        +int BLAST_RADIUS = 1$
    }

    class Theme {
        -String safeIcon
        -String boomIcon
        -String kissIcon
        -String loseText
        +iconFor(CellType type) String
        +setIcon(CellType type, String icon) void
        +getLoseText() String
        +setLoseText(String value) void
        +copy() Theme
    }

    class ThemeRepository {
        <<interface>>
        +load() Theme
        +save(Theme theme) void
    }
    class FileThemeRepository {
        -Path path
        +load() Theme
        +save(Theme theme) void
    }
    class MemoryThemeRepository {
        -Theme saved
        +load() Theme
        +save(Theme theme) void
    }
    class IconCatalog {
        <<utility>>
        ~choices(CellType type) String[]$
        +normalize(String value, CellType type) String$
        ~icon(String id, int size) Icon$
    }
    class GameUi {
        <<utility>>
        ~font(int size, boolean bold) Font$
    }

    JFrame <|-- Main
    Cell <|-- SafeCell
    Cell <|-- BoomCell
    Cell <|-- KissCell
    ThemeRepository <|.. FileThemeRepository
    ThemeRepository <|.. MemoryThemeRepository
    Main "1" *-- "1" Game : runs
    Main --> Theme : edits
    Main ..> GameUi : renders GUI
    Main ..> IconCatalog : renders icons
    Game "1" *-- "2" Player : owns
    Game --> GameState : state
    Game --> ThemeRepository : load/save
    Game --> Theme : current theme
    Game ..> AttackResult : reads result
    Player "1" *-- "1" Board : owns
    Board "1" *-- "25" Cell : contains
    Board ..> GameConfig : limits
    Board ..> AttackResult : creates
    Cell "1" --> "1" Position : immutable coordinates
    Cell ..> CellType : UI classification
    CellType ..> GameConfig : placement limits
    BoomCell ..> Board : explode
    KissCell ..> AttackResult : markKissHit
    Theme ..> IconCatalog : valid icon options
    FileThemeRepository ..> Theme : properties storage
```

## Template Method và đa hình

`Cell.open(Board, AttackResult)` là **final**: kiểm tra board/attack, bỏ qua
ô đã mở, đánh dấu opened, ghi Position vào kết quả, sau đó gọi `onOpen()`.
`onOpen()` là **protected abstract**. SafeCell không tạo hiệu ứng; BoomCell
gọi Board.explode(); KissCell gọi result.markKissHit(). Board.open() duyệt
hàng đợi và chỉ gọi cell.open(), không dùng if/switch theo CellType để chọn
hành vi. `getType()` dùng cho giao diện/đếm bánh và setup, không quyết định
hành vi khi mở ô.

Khi setup, `CellType.create()` dùng factory cho từng enum value. Board không
chia nhánh theo loại để tạo hoặc mở ô; kiểm tra giới hạn qua type.getLimit().

## Đóng gói và hợp đồng ngoại lệ

Position là final class với `private final int row/column`, chỉ có getter.
Tất cả thuộc tính Cell, Board, Position, AttackResult đều private. Danh sách
`AttackResult.getOpened()` không cho sửa; kết quả hoàn tất không nhận ghi
thêm. Config dùng static final cho hằng số dùng chung.

Board.place() trả void; tọa độ ngoài bàn, type null/Safe hoặc đặt trùng ném
IllegalArgumentException; vượt giới hạn hoặc sửa bàn đã xác nhận ném
IllegalStateException. Board.open()/Game.attack() chặn sai giai đoạn và ô
đã mở. Board.edit() kiểm tra trước để không làm mất quân cũ khi chọn sai.
GUI bắt các ngoại lệ này để báo cho người dùng; model không return false hay
nuốt lỗi. Lỗi đọc/ghi file theme được báo qua IllegalStateException.

## Phụ thuộc abstraction

Game nhận ThemeRepository qua constructor, load theme và saveTheme() thông
qua interface. FileThemeRepository và MemoryThemeRepository là hai hiện thực.
Main chọn FileThemeRepository ở điểm khởi tạo ứng dụng, rồi dùng Game và
Theme; luật chơi không phụ thuộc cách lưu file cụ thể. Các test dùng repository
trong bộ nhớ để không ghi đè punishment text cá nhân.
