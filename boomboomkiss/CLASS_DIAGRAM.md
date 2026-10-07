# Boom Boom Kiss — Class Diagram

Sơ đồ kiến trúc của source trong package `boomboomkiss`, cập nhật 07/10/2026.
Ký hiệu `~` là package-private, `-` là private và `+` là public. `$` đánh dấu
thành viên static. Sơ đồ hiển thị các thành phần chính; constructor và các
helper private của Swing không liệt kê hết.

```mermaid
classDiagram
    direction TB

    class JFrame

    class Main {
        -Game game
        -ThemeRepository themeRepository
        -Theme theme
        +main(String[] args) void$
        -showMenu() void
        -showSetup() void
        -showPlay() void
        -attack(Position position) void
        -showGameOver() void
    }

    class Game {
        ~Player player1
        ~Player player2
        ~Player currentPlayer
        ~Player loser
        ~GameState state
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
        ~String name
        ~Board board
    }

    class Board {
        -Cell[][] cells
        -Random random
        ~clear() void
        ~getCell(Position position) Cell
        ~count(CellType type) int
        ~remaining(CellType type) int
        ~place(Position position, CellType type) boolean
        ~remove(Position position) void
        ~isSetupComplete() boolean
        ~randomize(Random random) void
        ~reveal(Position target) AttackResult
    }

    class GameConfig {
        <<utility>>
        ~int BOARD_SIZE = 5$
        ~int BOMB_COUNT = 2$
        ~int KISS_COUNT = 1$
    }

    class Cell {
        <<abstract>>
        ~Position position
        -boolean revealed
        ~isRevealed() boolean
        ~reveal() void
        ~getType() CellType
    }

    class SafeCell {
        ~getType() CellType
    }

    class BombCell {
        ~getType() CellType
    }

    class KissCell {
        ~getType() CellType
    }

    class CellType {
        <<enumeration>>
        SAFE
        BOMB
        KISS
    }

    class Position {
        ~int row
        ~int column
    }

    class AttackResult {
        ~List~Position~ opened
        ~boolean kissHit
    }

    class Theme {
        ~String safeIcon
        ~String boomIcon
        ~String kissIcon
        ~String loseText
        ~iconFor(CellType type) String
        ~setIcon(CellType type, String icon) void
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

    JFrame <|-- Main
    Cell <|-- SafeCell
    Cell <|-- BombCell
    Cell <|-- KissCell
    ThemeRepository <|.. FileThemeRepository

    Main "1" *-- "1" Game : runs
    Main --> Theme : edits
    Main --> ThemeRepository : loads/saves
    Game "1" *-- "2" Player : owns
    Game --> GameState : state
    Game ..> AttackResult : returns
    Player "1" *-- "1" Board : owns
    Board "1" *-- "25" Cell : contains
    Board ..> GameConfig : uses
    Board ..> CellType : places/counts
    Board ..> AttackResult : returns
    Cell "1" *-- "1" Position : has
    Cell ..> CellType : returns
    FileThemeRepository ..> Theme : persists
```

## Quy ước với phần code

`Game.currentPlayer`, các phương thức trả về `Player`/`AttackResult`, và kế
thừa `Main extends JFrame` đúng như ảnh sơ đồ được cung cấp. `Board.reveal()`
nhận đúng một `Position`; `Cell` chỉ có `isRevealed()`, `reveal()`, `getType()`.
`getType()` được override ở ba lớp con và được Board gọi qua kiểu cha Cell.

Ba thuộc tính GameConfig là `static final` trong Java: cấu hình chung, không
đổi theo từng đối tượng. Chúng được đánh dấu static trên sơ đồ; `final` có
nghĩa read-only cho thuộc tính, không phải phạm vi truy cập. Ký hiệu `~` tự nó
không xác định có hay không có `static`/`final`.

Hai điều chỉnh so với ảnh cũ để khớp yêu cầu final: bỏ `selectedPiece` vì người
chơi chọn ngay trên ô; thêm `safeIcon`/`boomIcon` vì người dùng được chọn ba
icon riêng. GameUi và IconCatalog là lớp hỗ trợ vẽ giao diện/icon. Main vẫn
có helper private và trạng thái Timer để xử lý mở ô; Board có hàm private
`enqueueBlast()` cho nổ lan. Các chi tiết này được giản lược trên sơ đồ tổng
quan, không phải xóa chức năng khỏi code.
