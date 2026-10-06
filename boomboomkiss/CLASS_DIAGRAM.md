# Boom Boom Kiss - Class Diagram

This diagram describes the final Java game in `week_3`. It shows the
important fields, methods, inheritance, composition, and dependencies.
Package-private Java members use `~`; private members use `-`.

```mermaid
classDiagram
    direction TB

    class Game {
        ~Player player1
        ~Player player2
        ~Player currentPlayer
        ~Player loser
        ~GameState state
        -Random random
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
        ~clear() void
        ~getCell(Position position) Cell
        ~count(CellType type) int
        ~remaining(CellType type) int
        ~place(Position position, CellType type) boolean
        ~setType(Position position, CellType type) boolean
        ~remove(Position position) void
        ~isSetupComplete() boolean
        ~randomize(Random random) void
        ~reveal(Position target, Random random) AttackResult
        ~explode(Position center, Queue pending, Random random) void
    }

    class Cell {
        <<abstract>>
        ~Position position
        -boolean revealed
        ~isRevealed() boolean
        ~reveal() void
        ~getType() CellType
        ~onOpen(Board board, Queue pending, Random random) boolean
    }

    class SafeCell {
        ~getType() CellType
        ~onOpen(Board board, Queue pending, Random random) boolean
    }
    class BombCell {
        ~getType() CellType
        ~onOpen(Board board, Queue pending, Random random) boolean
    }
    class KissCell {
        ~getType() CellType
        ~onOpen(Board board, Queue pending, Random random) boolean
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

    class GameConfig {
        <<utility>>
        ~int BOARD_SIZE = 5
        ~int BOMB_COUNT = 2
        ~int KISS_COUNT = 1
        ~int BLAST_RADIUS = 1
        ~limitFor(CellType type) int
    }

    class Main {
        -Game game
        -ThemeRepository themeRepository
        -Theme theme
        -boolean busy
        -Timer revealTimer
        +main(String[] args) void
        -showMenu() void
        -showSetup() void
        -showPlay() void
        -attack(Position position) void
        -showGameOver() void
        -showIconPicker(CellType type, Consumer onSelect) void
        -showCellPicker(Board board, Position position) void
    }

    class Theme {
        ~String safeIcon
        ~String boomIcon
        ~String kissIcon
        ~String loseText
        ~iconFor(CellType type) String
        ~setIcon(CellType type, String icon) void
    }

    class IconCatalog {
        <<utility>>
        ~String[] SAFE
        ~String[] BOOM
        ~String[] KISS
        ~choices(CellType type) String[]
        ~normalize(String value, CellType type) String
        ~icon(String id, int size) Icon
    }

    class GameUi {
        <<utility>>
        ~font(int size, boolean bold) Font
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

    Game "1" *-- "2" Player : owns
    Player "1" *-- "1" Board : owns
    Board "1" *-- "25" Cell : contains
    Cell "1" *-- "1" Position : has

    Game --> GameState : state
    Game ..> AttackResult : returns
    Board ..> AttackResult : returns
    Board ..> CellType : places/counts
    Cell ..> CellType : returns
    BombCell ..> Board : triggers explosion
    Board ..> GameConfig : uses
    Main *-- Game : runs
    Main --> Theme : edits
    Main --> ThemeRepository : loads/saves
    Main ..> IconCatalog : renders icons
    Main ..> GameUi : Swing components
    Theme ..> IconCatalog : validates selection
    FileThemeRepository ..> Theme : persists
```

`currentPlayer` and `loser` refer to one of the two players already owned by
`Game`; they do not represent additional players. `AttackResult` contains the
positions opened by an attack and whether the kiss was revealed. The diagram
omits small Swing layout helpers in `Main` so the gameplay relationships stay
readable. The three `Cell` subclasses override `onOpen()`: Safe does nothing,
Bomb asks `Board` to enqueue its random horizontal or vertical blast, and Kiss
reports the losing hit.
