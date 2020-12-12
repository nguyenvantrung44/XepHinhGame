package huynhtinhthanh;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import javax.swing.JFrame;
public class Tetris extends JFrame {
	private static final long serialVersionUID = -4722429764792514382L;
	private static final long FRAME_TIME = 1000L / 50L;
	private static final int TYPE_COUNT = TileType.values().length;
	private BoardPanel board;
	private SidePanel side;
	private boolean isPaused;
	private boolean isNewGame;
	private boolean isGameOver;
	private int level;
	private int score;
	private Random random;
	private Clock logicTimer;
	private TileType currentType;
	private TileType nextType;
	private int currentCol;
	private int currentRow;
	private int currentRotation;
	private int dropCooldown;
	private float gameSpeed;
	private Tetris() {
		super("Tetris");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		this.board = new BoardPanel(this);
		this.side = new SidePanel(this);
		add(board, BorderLayout.CENTER);
		add(side, BorderLayout.EAST);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_S:
					if(!isPaused && dropCooldown == 0) {
						logicTimer.setCyclesPerSecond(25.0f);
					}
					break;
				/*
				 * Bình thường thì deltaX = 0 còn deltaX = -1  sẽ di chuyển shape sang trái
				 */
				case KeyEvent.VK_A:
					if(!isPaused && board.isValidAndEmpty(currentType, currentCol - 1, currentRow, currentRotation)) {
						currentCol--;
					}
					break;
				/*
				 * 	hoặc 1 sẽ di chuyển shape sang phải
				 */
				case KeyEvent.VK_D:
					if(!isPaused && board.isValidAndEmpty(currentType, currentCol + 1, currentRow, currentRotation)) {
						currentCol++;
					}
					break;
				case KeyEvent.VK_Q:
					if(!isPaused) {
						rotatePiece((currentRotation == 0) ? 3 : currentRotation - 1);
					}
					break;
				case KeyEvent.VK_E:
					if(!isPaused) {
						rotatePiece((currentRotation == 3) ? 0 : currentRotation + 1);
					}
					break;
				case KeyEvent.VK_P:
					if(!isGameOver && !isNewGame) {
						isPaused = !isPaused;
						logicTimer.setPaused(isPaused);
					}
					break;
				case KeyEvent.VK_ENTER:
					if(isGameOver || isNewGame) {
						resetGame();
					}
					break;
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_S:
					logicTimer.setCyclesPerSecond(gameSpeed);
					logicTimer.reset();
					break;
				}
			}
		});
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	private void startGame() {
		this.random = new Random();
		this.isNewGame = true;
		this.gameSpeed = 1.0f;
		this.logicTimer = new Clock(gameSpeed);
		logicTimer.setPaused(true);
		while(true) {
			long start = System.nanoTime();
			logicTimer.update();
			if(logicTimer.hasElapsedCycle()) {
				updateGame();
			}
			if(dropCooldown > 0) {
				dropCooldown--;
			}
			renderGame();
			long delta = (System.nanoTime() - start) / 1000000L;
			if(delta < FRAME_TIME) {
				try {
					Thread.sleep(FRAME_TIME - delta);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	/*
	 * cộng điểm score,level,speed
	 */
	private void updateGame() {
		if(board.isValidAndEmpty(currentType, currentCol, currentRow + 1, currentRotation)) {
			currentRow++;
		} else {
			board.addPiece(currentType, currentCol, currentRow, currentRotation);
			int cleared = board.checkLines();
			if(cleared > 0) {
				score += 50 << cleared;
			}
			gameSpeed += 0.035f;
			logicTimer.setCyclesPerSecond(gameSpeed);
			logicTimer.reset();
			dropCooldown = 5;
			level = (int)(gameSpeed * 1.70f);
			spawnPiece();
		}		
	}
	private void renderGame() {
		board.repaint();
		side.repaint();
	}
	private void resetGame() {
		this.level = 1;
		this.score = 0;
		this.gameSpeed = 1.0f;
		this.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
		this.isNewGame = false;
		this.isGameOver = false;		
		board.clear();
		logicTimer.reset();
		logicTimer.setCyclesPerSecond(gameSpeed);
		spawnPiece();
	}
	/*
	 * vẽ random ra khối gạch mới
	 */
	private void spawnPiece() {
		this.currentType = nextType;
		this.currentCol = currentType.getSpawnColumn();
		this.currentRow = currentType.getSpawnRow();
		this.currentRotation = 0;
		this.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
		/* 
		 * game over khi isValidAndEmpty trả về true(không thể tạo ra shape mới)
		 */
		if(!board.isValidAndEmpty(currentType, currentCol, currentRow, currentRotation)) {
			this.isGameOver = true;
			logicTimer.setPaused(true);
		}		
	}
	/*
	 * xoay mảnh rotatePiece
	 */
	private void rotatePiece(int newRotation) {
		int newColumn = currentCol;
		int newRow = currentRow;
		int left = currentType.getLeftInset(newRotation);
		int right = currentType.getRightInset(newRotation);
		int top = currentType.getTopInset(newRotation);
		int bottom = currentType.getBottomInset(newRotation);
		if(currentCol < -left) {
			newColumn -= currentCol - left;
		} else if(currentCol + currentType.getDimension() - right >= BoardPanel.COL_COUNT) {
			newColumn -= (currentCol + currentType.getDimension() - right) - BoardPanel.COL_COUNT + 1;
		}
		if(currentRow < -top) {
			newRow -= currentRow - top;
		} else if(currentRow + currentType.getDimension() - bottom >= BoardPanel.ROW_COUNT) {
			newRow -= (currentRow + currentType.getDimension() - bottom) - BoardPanel.ROW_COUNT + 1;
		}
		if(board.isValidAndEmpty(currentType, newColumn, newRow, newRotation)) {
			currentRotation = newRotation;
			currentRow = newRow;
			currentCol = newColumn;
		}
	}
	/*
	 * dùng để kiểm tra trạng thái game
	 */
	public boolean isPaused() {
		return isPaused;
	}
	public boolean isGameOver() {
		return isGameOver;
	}
	public boolean isNewGame() {
		return isNewGame;
	}
	public int getScore() {
		return score;
	}
	public int getLevel() {
		return level;
	}
	public TileType getPieceType() {
		return currentType;
	}
	public TileType getNextPieceType() {
		return nextType;
	}
	public int getPieceCol() {
		return currentCol;
	}
	public int getPieceRow() {
		return currentRow;
	}
	//lưu lại tọa độ khối chạm đáy
	public int getPieceRotation() {
		return currentRotation;
	}
	public static void main(String[] args) {
		Tetris tetris = new Tetris();
		tetris.startGame();
	}
}
