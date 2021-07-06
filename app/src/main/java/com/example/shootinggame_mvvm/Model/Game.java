package com.example.shootinggame_mvvm.Model;

import java.util.HashMap;

public class Game {
    //----------------------------------------------------------------------------
    // Constant definitions.
    //

    public static float virtualWidth;
    public static float virtualHeight;

    public static int maxEnemyId = 30; // Enemy에 부여될 수 있는 최대 id 개수
    public static int maxBulletId = 10; // Bullet에 부여될 수 있는 최대 id 개수


    //----------------------------------------------------------------------------
    // class variables.
    //

    //----------------------------------------------------------------------------
    // Instance variables.
    //

    private com.example.shootinggame_mvp.Model.Cannon cannon;
    private HashMap<Integer, Bullet> bulletHashMap;
    private HashMap<Integer, Enemy> enemyHashMap;
    private StepInfo stepInfo;
    
    private boolean running = false; // 게임 진행 상태

    private int step; // 게임 step 관리
    private int enemyGenStep;

    private int life; // 생명 개수
    private int bulletLimit; // 화면 상에 존재할 수 있는 bullet 개수

    private int bulletId; // bulletId 관리
    private int enemyId; // enemyId 관리


    
    //----------------------------------------------------------------------------
    // Singleton Pattern.
    //

    private static Game game;

    public static Game getInstance() {
        if(game == null) {
            game = new Game();
        }
        return game;
    }



    //----------------------------------------------------------------------------
    // Constructor.
    //

    private Game() {
        cannon = Cannon.getInstance();
    };



    //-----------------------------------------------------------------------------
    // Public interface.
    //

    /**
     * 가로 크기 100을 기준으로 실제 화면 비율 대로 가상 좌표계 생성
     * @param displayRatio
     */
    public void setVirtualCoordinates(float displayRatio) {
        this.virtualWidth = 100f;
        this.virtualHeight = virtualWidth * displayRatio;
    }
    

    /**
     * 게임 시작시 호출 -> 초기화
     * @param lifeLimit
     * @param bulletLimit
     */
    public void setGameStart(int lifeLimit, int bulletLimit) {
        this.running = true;
        this.life = lifeLimit;
        this.bulletLimit = bulletLimit;
        this.bulletId = 0;
        this.enemyId = 0;
        this.stepInfo = new StepInfo(lifeLimit);

        this.bulletHashMap = new HashMap<>();
        this.enemyHashMap = new HashMap<>();

        this.step = 0;
        this.enemyGenStep = 0;
    }


    /**
     * TimerTask 내에서 주기적으로 호출되며 화면 상에 존재하는 bullet과 enemy 위치 조정
     */
    public StepInfo step() {
        stepInfo.clear();
        
        // enemy를 생성해야 하는 step인지 확인하고, enemy 생성
        if(step == enemyGenStep) {
            addEnemy();
            // 다음 enemy 생성 step 결정
            enemyGenStep = (int)(Math.random() * 300 + 50) + step;
        }
        
        step++;

        // bullet 위치 이동
        updateBulletsPosition();
        
        // enemy 위치 이동
        updateEnemiesPosition();
        
        // 충돌 감지
        removeConflictingBulletAndEnemy();

        return stepInfo;
    }


    /**
     * 외부에서 Cannon에 접근하기 위한 메서드
     * @return
     */
    public Cannon getCannon() {
        return cannon;
    }


    /**
     * shoot버튼 클릭 시 호출 -> Bullet 추가
     * 단, 화면 상에 존재하는 bullet 개수가 특정 개수(bulletLimit) 미만일 때만 추가된다.
     */
    public void addBullet() {
        if(bulletHashMap.size() >= bulletLimit) return;
        int id = genBulletId();
        Bullet bullet = new Bullet(id, cannon.getAngle());
        bulletHashMap.put(id, bullet);
        stepInfo.addAliveBullet(bullet);
    }


    /**
     * Enemy 추가
     */
    public void addEnemy() {
        int id = genEnemyId();
        Enemy enemy = new Enemy(id);
        enemyHashMap.put(id, enemy);
        stepInfo.addAliveEnemy(enemy);
    }


    /**
     * 게임 실행 상태 확인
     * @return
     */
    public boolean gameOver() {
        return !running;
    }




    //----------------------------------------------------------------------
    // Internal support methods.
    //

    /**
     * Bullet 위치 이동 (단위 벡터 만큼 이동)
     */
    private void updateBulletsPosition() {
        for(int i = 0; i < maxBulletId; i++) {
            
            if(bulletHashMap.containsKey(i)) {
                Bullet b = bulletHashMap.get(i);
                b.move();
                
                if(b.getY() < 0) { //bullet이 화면 밖으로 벗어나면 제거
                    removeBullet(b);
                }
            }
        }
    }


    /**
     * Enemy 위치 이동 (단위 벡터 만큼 이동)
     */
    private void updateEnemiesPosition() {
        for(int i = 0; i < maxEnemyId; i++) {
            
            if(enemyHashMap.containsKey(i)) {
                Enemy e = enemyHashMap.get(i);
                e.move();

                if(e.getY() > virtualHeight) { //enemy가 땅에 닿으면 제거
                    removeEnemy(e);
                    decreaseLife();
                }
            }
        }
    }


    /**
     * 충돌하는 Bullet과 Enemy가 있는지 검사하고, 존재하면 제거
     */
    private void removeConflictingBulletAndEnemy() {
        for(int eid = 0; eid < maxEnemyId; eid++) {
            if(enemyHashMap.containsKey(eid)) {
                Enemy e = enemyHashMap.get(eid);
                Bullet b = findConflictingBullet(e);
                if(b != null) {
                    removeBullet(b);
                    removeEnemy(e);
                }
            }
        }
    }


    /**
     * 특정 Enemy와 충돌하는 Bullet 탐색 후 리턴
     * @param e : Enemy
     * @return
     */
    private Bullet findConflictingBullet(Enemy e) {
        for(int bid = 0; bid < maxBulletId; bid++) {
            if(bulletHashMap.containsKey(bid)) {

                Bullet b = bulletHashMap.get(bid);

                //반사가 1회 이상 일어나지 않은 bullet이면 무효 처리
                if(!b.bounced()) continue;

                //bullet과 enemy가 충돌하는지 검사
                if(checkConflictBetweenEnemyAndBullet(e, b)) {
                    return b;
                }

            }
        }
        return null;
    }


    /**
     * 특정 Enemy와 특정 Bullet이 충돌하는지 검사
     * @param e : Enemy
     * @param b : Bullet
     * @return
     */
    private boolean checkConflictBetweenEnemyAndBullet(Enemy e, Bullet b) {
        float ex = e.getX(); // enemy 좌표
        float ey = e.getY();
        float bx = b.getX(); // bullet 좌표
        float by = b.getY();

        // bullet 면적과 enemy 면적이 겹치는지 확인
        if(bx < ex + Enemy.width && bx + Bullet.width > ex) {
            if(by < ey + Enemy.height && by + Bullet.height > ey) {
                return true;
            }
        }

        return false;
    }


    /**
     * Bullet id 생성
     * @return
     */
    private int genBulletId() {
        // 현재 화면 상에 존재하는 bullet이 사용하지 않는 id 찾기
        while(bulletHashMap.containsKey(bulletId)) {
            bulletId = (bulletId + 1) % maxBulletId;
        }
        
        return bulletId;
    }


    /**
     * Enemy id 생성
     * @return
     */
    private int genEnemyId() {
        // 현재 화면 상에 존재하는 enemy가 사용하지 않는 id 찾기
        while(enemyHashMap.containsKey(enemyId)) {
            enemyId = (enemyId + 1) % maxEnemyId;
        }

        return enemyId;
    }


    /**
     * 생명 1개 감소
     */
    private void decreaseLife() {
        life--;
        stepInfo.decreaseLife();
        
        //생명이 모두 소모되면 게임 종료 상태로 전환
        if(life == 0) {
            running = false;
        }
    }

    /**
     * 화면 상에 존재하던 Bullet 제거
     * @param b
     */
    private void removeBullet(Bullet b) {
        bulletHashMap.remove(b.getId());
        stepInfo.addRemovedBullet(b);
    }

    /**
     * 화면 상에 존재하던 enemy 제거
     * @param e
     */
    private void removeEnemy(Enemy e) {
        enemyHashMap.remove(e.getId());
        stepInfo.addRemovedEnemy(e);
    }
}
