// core/src/com/perplexinggames.ironsoul/screens/DemoGameScreen.java
package com.perplexinggames.ironsoul.screens;
import com.perplexinggames.ironsoul.utils.TextureGenerator;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.perplexinggames.ironsoul.entities.Player;
import com.perplexinggames.ironsoul.tiles.*;
import com.perplexinggames.ironsoul.utils.TextureGenerator;

public class DemoGameScreen implements Screen {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TileMap tileMap;
    private Player player;
    private TileSet tileSet;

    // Константы
    private static final int TILE_SIZE = 32;
    private static final int MAP_WIDTH = 40;
    private static final int MAP_HEIGHT = 30;
    private static final int WORLD_WIDTH = MAP_WIDTH * TILE_SIZE;
    private static final int WORLD_HEIGHT = MAP_HEIGHT * TILE_SIZE;

    // Текстуры для разных типов тайлов
    private Texture groundTexture;
    private Texture wallTexture;
    private Texture waterTexture;
    private Texture lavaTexture;
    private Texture spikeTexture;

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Создание текстур для демо
        createDemoTextures();

        // Создание набора тайлов
        createTileSet();

        // Создание карты
        createTileMap();

        // Создание игрока (стартовая позиция)
        player = new Player(TILE_SIZE * 2, TILE_SIZE * 2, TILE_SIZE, TILE_SIZE);

        // Настройка камеры
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();
    }

    private void createDemoTextures() {
        // Создаем простые цветные текстуры для разных типов тайлов
        groundTexture = TextureGenerator.createTileTexture(TILE_SIZE, 0x88AA88FF, 0x668866FF);
        wallTexture = TextureGenerator.createTileTexture(TILE_SIZE, 0x666666FF, 0x444444FF);
        waterTexture = TextureGenerator.createTileTexture(TILE_SIZE, 0x4488CCFF, 0x2266AAFF);
        lavaTexture = TextureGenerator.createTileTexture(TILE_SIZE, 0xFF6600FF, 0xCC4400FF);
        spikeTexture = TextureGenerator.createTileTexture(TILE_SIZE, 0xCCCCCCFF, 0xFF0000FF);
    }

    private void createTileSet() {
        // Разделяем текстуры на регионы (хотя у нас целые текстуры)
        tileSet = new TileSet("demo", TILE_SIZE, TILE_SIZE);

        // Создаем тайлы разных типов
        // ID 0: Земля
        Tile groundTile = new Tile(0, new TextureRegion(groundTexture), Tile.TileType.GROUND);
        tileSet.addTile(0, groundTile);

        // ID 1: Стена
        Tile wallTile = new Tile(1, new TextureRegion(wallTexture), Tile.TileType.WALL);
        tileSet.addTile(1, wallTile);

        // ID 2: Вода
        Tile waterTile = new Tile(2, new TextureRegion(waterTexture), Tile.TileType.WATER);
        waterTile.setDamage(5); // Вода наносит 5 урона
        tileSet.addTile(2, waterTile);

        // ID 3: Лава
        Tile lavaTile = new Tile(3, new TextureRegion(lavaTexture), Tile.TileType.LAVA);
        lavaTile.setDamage(15); // Лава наносит 15 урона
        tileSet.addTile(3, lavaTile);

        // ID 4: Шипы
        Tile spikeTile = new Tile(4, new TextureRegion(spikeTexture), Tile.TileType.SPIKE);
        spikeTile.setDamage(20); // Шипы наносят 20 урона
        tileSet.addTile(4, spikeTile);
    }

    private void createTileMap() {
        tileMap = new TileMap(MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);
        tileMap.setTileSet(tileSet);

        // Создаем слои
        TileLayer groundLayer = new TileLayer("ground", MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);
        TileLayer collisionLayer = new TileLayer("collision", MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);
        TileLayer logicLayer = new TileLayer("logic", MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);

        // Генерация тестовой карты
        generateTestMap(groundLayer, collisionLayer, logicLayer);

        // Добавляем слои в карту
        tileMap.addLayer(groundLayer);
        tileMap.addLayer(collisionLayer);
        tileMap.addLayer(logicLayer);

        // Настраиваем эффекты слоев
        groundLayer.setParallaxFactor(1.0f); // Фоновый слой
        logicLayer.setVisible(true);

        System.out.println("Карта создана: " + MAP_WIDTH + "x" + MAP_HEIGHT +
            ", размер тайла: " + TILE_SIZE);
    }

    private void generateTestMap(TileLayer ground, TileLayer collision, TileLayer logic) {
        // 1. Создаем пол (землю) на всей карте
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                ground.setTile(x, y, 0); // Тайл земли ID 0
            }
        }

        // 2. Создаем стены по краям карты
        for (int x = 0; x < MAP_WIDTH; x++) {
            // Нижняя стена
            ground.setTile(x, 0, 1);
            collision.setTile(x, 0, 1);

            // Верхняя стена
            ground.setTile(x, MAP_HEIGHT - 1, 1);
            collision.setTile(x, MAP_HEIGHT - 1, 1);
        }

        for (int y = 0; y < MAP_HEIGHT; y++) {
            // Левая стена
            ground.setTile(0, y, 1);
            collision.setTile(0, y, 1);

            // Правая стена
            ground.setTile(MAP_WIDTH - 1, y, 1);
            collision.setTile(MAP_WIDTH - 1, y, 1);
        }

        // 3. Создаем внутренние препятствия (лабиринт)
        // Вертикальная стена посередине
        for (int y = 5; y < 15; y++) {
            ground.setTile(10, y, 1);
            collision.setTile(10, y, 1);
        }

        // Горизонтальная стена
        for (int x = 15; x < 25; x++) {
            ground.setTile(x, 20, 1);
            collision.setTile(x, 20, 1);
        }

        // 4. Добавляем опасные зоны
        // Озеро лавы (справа внизу)
        for (int x = 30; x < 38; x++) {
            for (int y = 2; y < 8; y++) {
                ground.setTile(x, y, 3); // Лава
                logic.setTile(x, y, 3);  // Логика для нанесения урона
            }
        }

        // Водный бассейн (слева вверху)
        for (int x = 5; x < 12; x++) {
            for (int y = 22; y < 28; y++) {
                ground.setTile(x, y, 2); // Вода
                logic.setTile(x, y, 2);  // Логика для воды
            }
        }

        // Зона с шипами (центр)
        for (int x = 18; x < 22; x++) {
            for (int y = 10; y < 14; y++) {
                ground.setTile(x, y, 4); // Шипы
                logic.setTile(x, y, 4);  // Логика для шипов
            }
        }

        // 5. Создаем комнату с сокровищем (тест интерактивности)
        for (int x = 25; x < 30; x++) {
            for (int y = 15; y < 20; y++) {
                ground.setTile(x, y, 0);
            }
        }

        // Стена вокруг комнаты
        for (int x = 24; x <= 30; x++) {
            ground.setTile(x, 14, 1);
            ground.setTile(x, 20, 1);
            collision.setTile(x, 14, 1);
            collision.setTile(x, 20, 1);
        }
        for (int y = 14; y <= 20; y++) {
            ground.setTile(24, y, 1);
            ground.setTile(30, y, 1);
            collision.setTile(24, y, 1);
            collision.setTile(30, y, 1);
        }

        System.out.println("Тестовая карта сгенерирована");
        System.out.println("- Стены по краям и внутренние препятствия");
        System.out.println("- Зона лавы (наносит 15 урона)");
        System.out.println("- Зона воды (наносит 5 урона)");
        System.out.println("- Зона шипов (наносит 20 урона)");
        System.out.println("- Комната сокровищ за стеной");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);  // Красный
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
//
//        // Обновление игрока
//        player.update(delta);
//
//        // Ограничение движения игрока в пределах мира
//        clampPlayerPosition();
//
//        // Проверка коллизий с препятствиями
//        if (tileMap.getCollisionManager().checkCollision(player.getBoundingBox(), "collision")) {
//            player.handleCollision();
//        }
//
//        // Проверка взаимодействия с опасными тайлами
//        tileMap.getTileLogic().updateEntityInteraction(player);
//
//        // Обновление камеры (следим за игроком)
//        updateCamera();

        // Отрисовка
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Рисуем карту
        tileMap.render(batch, camera);

        // Рисуем игрока
        player.render(batch);

        // Рисуем отладочную информацию
        drawDebugInfo();

        batch.end();

        // Вывод информации в консоль каждые 60 кадров
        if (Gdx.graphics.getFrameId() % 60 == 0) {
            printDebugInfo();
        }
    }

    private void clampPlayerPosition() {
        float playerX = player.getX();
        float playerY = player.getY();

        // Ограничение по краям мира с учетом коллизий
        if (playerX < 0) player.setX(0);
        if (playerY < 0) player.setY(0);
        if (playerX > WORLD_WIDTH - TILE_SIZE) player.setX(WORLD_WIDTH - TILE_SIZE);
        if (playerY > WORLD_HEIGHT - TILE_SIZE) player.setY(WORLD_HEIGHT - TILE_SIZE);
    }

    private void updateCamera() {
        camera.position.set(player.getX() + TILE_SIZE / 2,
            player.getY() + TILE_SIZE / 2, 0);

        // Ограничение камеры краями карты
        float camX = Math.min(Math.max(camera.position.x, Gdx.graphics.getWidth() / 2),
            WORLD_WIDTH - Gdx.graphics.getWidth() / 2);
        float camY = Math.min(Math.max(camera.position.y, Gdx.graphics.getHeight() / 2),
            WORLD_HEIGHT - Gdx.graphics.getHeight() / 2);

        camera.position.set(camX, camY, 0);
        camera.update();
    }

    private void drawDebugInfo() {
        // Рисуем простой текст в углу (опционально)
        // Для полноценного текста нужен BitmapFont, который можно добавить позже
    }

    private void printDebugInfo() {
        System.out.println("=== Игровая информация ===");
        System.out.println("Позиция игрока: (" + (int)player.getX() + ", " + (int)player.getY() + ")");
        System.out.println("Здоровье игрока: " + player.getHealth() + "/" + player.getMaxHealth());

        // Определяем текущий тип тайла под игроком
        Tile currentTile = tileMap.getCollisionManager().getTileAt(
            player.getX(), player.getY(), "logic"
        );
        if (currentTile != null) {
            System.out.println("Текущий тайл: " + currentTile.getType());
        }
        System.out.println("==========================");
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();

        // Очистка текстур
        groundTexture.dispose();
        wallTexture.dispose();
        waterTexture.dispose();
        lavaTexture.dispose();
        spikeTexture.dispose();

        player.dispose();
    }
}
