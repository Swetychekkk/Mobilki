import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        int step = 0;
        int boardSize = 5;
        Random random = new Random();

        Person person = new Person(boardSize);
        String castle = "\uD83C\uDFF0";
        int monsterCount = 11;
        int castleY = 1;
        int castleX = 1 + random.nextInt(boardSize);
        String[][] board = new String[boardSize][boardSize];

        for (int y = 1; y <= boardSize; y++) {
            for (int x = 1; x <= boardSize; x++) {
                board[y-1][x-1] = "  ";
            }
        }
        board[castleY - 1][castleX - 1] = castle;
        board[person.y - 1][person.x - 1] = person.pic;

        List<Monster> monsters = new ArrayList<>();
        for (int i = 0; i < monsterCount; i++) {
            int type = random.nextInt(2);
            Monster monsterB = createRandomMonster(boardSize, random);
            if (!board[monsterB.getY()-1][monsterB.getX()-1].equals("  ")) {   // занято — перегенерируем
                i--;
                continue;
            }
            board[monsterB.getY()-1][monsterB.getX()-1] = monsterB.getPic();
            monsters.add(monsterB);
        }


        System.out.println("Привет! Ты готов начать играть в игру? (Напиши: ДА или НЕТ)");

        Scanner scanner = new Scanner(System.in);

        if (scanner.next().equals("ДА")) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.println("Начинаем играть");
            System.out.println("Количество жизней: " + person.live);
        } else {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.println("Почему ты не захотел со мной играть :(\nПриходи ещё!");
            System.exit(0);
        }

        System.out.println("Выбери сложность от 1 до 5");
        int difficultGame = scanner.nextInt();
        clearTerminal();
        if (difficultGame <= 5 && difficultGame >= 1) {
            System.out.println("Выбранная сложность:\t" + difficultGame);
        }


        renderField(boardSize, board);

        while ((person.live > 0)) {
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            Monster found = findMonsterAt(monsters, x, y);
            if ((x < 1) || (x > boardSize) || (y < 1) || (y > boardSize)) {
                System.out.println("Некорректный ход");
                continue;
            } else if (x == person.x && y == person.y) {
                System.out.println("Ты уже тут)");
                continue;
            }

            if (x!=person.x && y!=person.y) {
                System.out.println("Некорректный ход");
            } else if (person.isMoveCorrect(x,y)) {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                if (board[y - 1][x - 1].equals("  ")) {
                    board[y - 1][x - 1] = person.pic;
                    board[person.y - 1][person.x - 1] = "  ";
                    person.x = x;
                    person.y = y;
                    step += 1;
                    System.out.println("Ход корректный; Новые координаты: " +
                            person.x + ", " + person.y + "\nХод номер: " + step + "\nЖизни: " + "\uD83D\uDC9C".repeat(person.live));
                    renderField(boardSize, board);
                } else if (board[y - 1][x - 1].equals(castle)) {
                    System.out.println("Вы прошли игру за " + step + " ходов!\n");
                    break;
                } else if ((found = findMonsterAt(monsters, x, y)) != null) {
                    boolean result = found.monsterQuest(random, scanner, person.live, difficultGame);
                    if (result) {
                        board[y - 1][x - 1] = person.pic;
                        board[person.y - 1][person.x - 1] = "  ";
                        person.x = x;
                        person.y = y;
                        monsters.remove(found);   // монстр побеждён — убираем с поля
                        step++;
                    } else {
                        person.hitHim();
                        // если хочешь урон, зависящий от монстра:
                        // person.live -= found.getDamage();
                    }
                    clearTerminal();
                    System.out.println("\nХод номер: " + step + "\nЖизни: "
                            + "\uD83D\uDC9C".repeat(person.live));
                    renderField(boardSize, board);
                }
            }
        }
        if (person.isDead()) {
            clearTerminal();
            System.out.println("======ИГРА ОКОНЧЕНА======");
        }
//        for (int counterY = 1;counterY <= boardSize; counterY += 1) {
//
//        }

//        for (int y = 1; y <= boardSize; y++) {
//            System.out.println(wall);
//            for (int x = 1; x <= boardSize; x++) {
//                System.out.print(leftBlock);
//                if (personX == x && personY == y) {
//                    System.out.print(person);
//                } else if (castleX == x && castleY == y) {
//                    System.out.print(castle);
//                } else {
//                    System.out.print("  ");
//                }
//            }
//            System.out.println(rightBlock);
//        }
//        System.out.println(wall);

    }
    private static Monster createRandomMonster(int boardSize, Random random) {
        return switch (random.nextInt(3)) {
            case 0 -> new Monster(boardSize, random);
            case 1 -> new monsterVer2(boardSize, random);
            case 2 -> new monsterVer3(boardSize, random);
            default -> throw new IllegalStateException();
        };
    }
    static void clearTerminal() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    public static class Person {
        int x, y;
        String pic = "\uD83D\uDE21";
        int live = 3;

        Person(int size) {
            Random r = new Random();
            y = size;
            int n = r.nextInt(size);
            x = n == 0 ? 1 : n;
        }

        void moveTo(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void hitHim() {
            live--;
        }

        boolean isDead() {
            return live==0;
        }

        public boolean isMoveCorrect(int x, int y) {
            return this.x == x && Math.abs(this.y - y) == 1 || this.y == y && Math.abs(this.x - x) == 1;
        }
    }
    public static class Monster {
        private int x, y, damagePerMiss;
        protected String pic;

        Monster(int boardSize, Random random) {
            pic = "\uD83D\uDE08";
            x = 1 + random.nextInt(boardSize);   // 1..boardSize
            y = 1 + random.nextInt(boardSize);
            damagePerMiss = 1;
        }

        String getPic() {
            return pic;
        }
        int getX() {
            return x;
        }
        int getY() {
            return y;
        }

        public boolean monsterQuest(Random random, Scanner scanner, int personaLives, int difficulty) {
            int a = random.nextInt(100);
            int b = random.nextInt(100);
            clearTerminal();
            System.out.println("=~=~=~=~=Мини-игра=~=~=~=~=\nВраг: Сможешь решить пример и я тебя пощажу\uD83D\uDE08\uD83D\uDE08 \n"+String.valueOf(a)+" + "+String.valueOf(b)+" = ??");
            System.out.print("Ответ: ");
            if (scanner.nextInt() == a+b) {
                scanner.nextLine();
                System.out.println("Враг: Молодец, живи пока что\n\nНажмите Enter для продолжения");
                scanner.nextLine();
                return true;
            } else if (personaLives == 1) {
                System.out.println("Враг: Не решить такой простой пример - это сильно. Я даю тебе шанс, но это в последний раз");
                a = random.nextInt(1000);
                b = random.nextInt(1000);
                System.out.println("Враг"+String.valueOf(a)+" + "+String.valueOf(b)+" = ??");
                System.out.print("Ответ: ");
                if (scanner.nextInt() == a+b) {
                    scanner.nextLine();
                    System.out.println("Враг: Молодец, живи пока что\n\nНажмите Enter для продолжения");
                    scanner.nextLine();
                    return true;
                }
            }
            scanner.nextLine();
            System.out.println("Враг: Ожидаемо\n\nНажмите Enter для продолжения");
            scanner.nextLine();
            return false;

        }
    }
    public static class monsterVer2 extends Monster {


        monsterVer2(int boardSize, Random random) {
            super(boardSize, random);
            this.pic="\uD83D\uDC7A";
        }

        @Override
        public boolean monsterQuest(Random random, Scanner scanner, int personaLives, int difficulty) {
            int a = random.nextInt(10*(difficulty-1), 100*difficulty);
            int b = random.nextInt(10*(difficulty-1), 100*difficulty);
            int c = random.nextInt(1, 10);
            clearTerminal();
            System.out.println("=~=~=~=~=Мини-игра=~=~=~=~=\nВраг: Сможешь решить пример? Учти, это будет сложнее\n("+String.valueOf(a)+" - "+String.valueOf(b)+") * " + String.valueOf(c) + " = ??");
            System.out.print("Ответ: ");
            if (scanner.nextInt() == (a-b)*c) {
                scanner.nextLine();
                System.out.println("Враг: Молодец, это было трудно\n\nНажмите Enter для продолжения");
                scanner.nextLine();
                return true;
            } else if (personaLives == 1) {
                System.out.println("Враг: Я даю тебе шанс, ладно");
                a = random.nextInt(12);
                System.out.println("Враг: "+String.valueOf(a)+"^2 = ??");
                System.out.print("Ответ: ");
                if (scanner.nextInt() == Math.pow(a, 2)) {
                    scanner.nextLine();
                    System.out.println("Враг: Молодец, живи пока что\n\nНажмите Enter для продолжения");
                    scanner.nextLine();
                    return true;
                }
            }
            scanner.nextLine();
            System.out.println("Враг: Ожидаемо\n\nНажмите Enter для продолжения");
            scanner.nextLine();
            return false;

        }
    }

    public static class monsterVer3 extends Monster {


        monsterVer3(int boardSize, Random random) {
            super(boardSize, random);
            this.pic="\uD83E\uDD16";
        }

        @Override
        public boolean monsterQuest(Random random, Scanner scanner, int personaLives, int difficulty) {
            String[] options = {"камень", "ножницы", "бумага"};
            scanner.nextLine();
            clearTerminal();
            System.out.println("=~=~=~=~=Мини-игра=~=~=~=~=\nВраг: Ты не сможешь выиграть меня в камень/ножницы/бумага. Говори!");
            while (true) {
                System.out.print("Ответ: ");
                String monsterSelect = options[random.nextInt(3)];
                String player = scanner.nextLine().toLowerCase();
                System.out.println(monsterSelect + "!");
                if (player.equals(monsterSelect)) {
                    System.out.println("Ничья, я уже злюсь\uD83D\uDE21\uD83D\uDE21");
                    continue;
                }
                if (player.equals("камень")&&monsterSelect.equals("ножницы")||player.equals("ножницы")&&monsterSelect.equals("бумага")||player.equals("бумага")&&monsterSelect.equals("камень")) {
                    System.out.println("Ты выиграл!");
                    scanner.nextLine();
                    return true;
                } else {
                    System.out.println("Ты ПРОИГРАЛ!");
                    scanner.nextLine();
                    return false;
                }
            }

        }
    }

    private static Monster findMonsterAt(List<Monster> monsters, int x, int y) {
        for (Monster m : monsters) {
            if (m.getX() == x && m.getY() == y) {
                return m;
            }
        }
        return null;
    }

    public static void renderField(int boardSize, String[][] board) {
        String leftBlock = " | ";
        String rightBlock = " |";
        String wall = " + —— + —— + —— + —— + —— + ";
        for (String[] row : board) {
            System.out.println(wall);
            for (String col : row) {
                System.out.print(leftBlock);
                System.out.print(col);
            }
            System.out.println(rightBlock);
        }
        System.out.println(wall);
    }
}