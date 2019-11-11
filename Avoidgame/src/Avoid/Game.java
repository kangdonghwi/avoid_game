package Avoid;

import java.awt.Color;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Game extends JFrame implements KeyListener, Runnable {

	public static final int screenWidth = 800;
	public static final int screenHeight = 500;
	public static final int RAIN_MAKE_INTERVAL = 3;// 3번에 하나씩생성. 비의 개수를 조절하는 상수
	private int x, y;// 캐릭터 위치
	private int cnt; // 각종 타이밍 조절을 위해 무한 루프를 카운터할 변수
	private int rain_Speed;//비의 속도 변수
	private int character_Speed;// 캐릭터가 움직이는 속도를 조절할 변수
	private int game_Score; // 게임 점수 계산
	private int rain_make_interval = RAIN_MAKE_INTERVAL;
	private Random random = new Random();//비가 랜덤으로 내리기 위해 만듬
	private ArrayList rain_list = new ArrayList();// 다수의 비를 등장 시켜야 하므로 배열을 이용.
	private Clip clip;
	private File audioFile;
	private Rain rain;
	private boolean gameover = false;
	// 키보드 입력 처리를 위한 변수
	private boolean KeyUp = false;
	private boolean KeyDown = false;
	private	boolean KeyLeft = false;
	private	boolean KeyRight = false;
	
	private Image buffImage; // 더블 버퍼링용
	private Graphics buffg; // 더블 버퍼링용
	private Image characterimg;
	private Image rainimg;
	private Image BackGround_img; // 배경화면 이미지
	private Thread th;// 스레드

	public Game() {
		setTitle("산성비피하기게임");
		setSize(screenWidth, screenHeight);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);// 크기고정
		//Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();// 프레임이 윈도우에 표시될때 위치를 세팅하기 위해 현재 모니터의 해상도 값을 받아옴.
		init();
		start();
		
	}

	public void init() {
		x = 400;// 캐릭터위치
		y = 350;	
		characterimg = new ImageIcon("image/character.png").getImage();
		rainimg = new ImageIcon("image/rain.png").getImage();
		BackGround_img = new ImageIcon("image/background.png").getImage();		
		game_Score = 0;// 게임 스코어 초기화
		character_Speed = 11; // 유저 캐릭터 움직이는 속도 설정
		rain_Speed = 6;
	}

	public void start() {
		addKeyListener(this); // 키보드 이벤트 실행
		th = new Thread(this); // 스레드 생성
		th.start(); // 스레드 실행
		loadAudio("music/활주.wav");
	}

	@Override
	public void run() {
		try { 
			while (gameover == false) { //gameover 될때까지 무한루프
				KeyProcess();
				RainProcess();
				repaint(); // 갱신된 x,y값으로 이미지 새로 그리기
				Thread.sleep(20); // 20millisec로 스레드 돌리기
				cnt++;

			}
		} catch (Exception e) {}
	}

	public void paint(Graphics g) {
		buffImage = createImage(screenWidth, screenHeight); // 더블버퍼링. 버퍼 크기를 화면 크기와 같게 설정
		buffg = buffImage.getGraphics(); // 버퍼의 그래픽 객체를 얻기
		update(g);
		//게임끝났을때 노래멈추고 글이나옴
		if (gameover == true) {
			g.setFont(new Font("Cntury Gothic", Font.BOLD, 24));
			g.drawString("Game Over!", 350, 250);
			StopAudio();

		}
	}

	public void update(Graphics g) {
		Draw_Background();
		Draw_Character();// 실제로 그려진 그림을 가져온다
		Draw_Rain();
		Draw_Score();

		g.drawImage(buffImage, 0, 0, this);
		// 화면에 버퍼에 그린 그림을 가져와 그리기
	}

	// 비를 컨트롤 하는 메소드
	public void RainProcess() {
			if(cnt>120) {
				for (int i = 0; i < rain_list.size(); ++i) {
					rain = (Rain) (rain_list.get(i));
					// 배열에 비를 생성
					rain.move(); // 비 이동
					if (rain.y > screenHeight+10) { // 비의 좌표가 화면 밖으로 넘어가면
						rain_list.remove(i); // 해당 적을 배열에서 삭제
						game_Score += 1; // 점수가 1점씩 오름
					}
				}
				if (rain_make_interval == RAIN_MAKE_INTERVAL) {// 비를 랜덤으로 뿌려줌.
					rain = new Rain(random.nextInt(screenWidth), screenHeight - 500,random.nextInt(5)+rain_Speed);
					rain_list.add(rain);
				}
				rain_make_interval--;
				if (rain_make_interval < 0)
					rain_make_interval = RAIN_MAKE_INTERVAL;
			}
			
		for (int j = 0; j < rain_list.size(); ++j) {
			rain = (Rain) rain_list.get(j);
			if (hit(x, y, rain.x, rain.y, characterimg, rainimg)) {
				// 캐릭터와 비가 부딪히면 빗방울이 제거되고 gameover 상태가됌
				gameover = true;
			}
		}
	}

	// 충돌 메소드
	public boolean hit(int x1, int y1, int x2, int y2, Image img1, Image img2) {
		// 이미지 변수를 받아 해당 이미지의 넓이, 높이값을 계산
		boolean check = false;
		if (Math.abs((x1 + img1.getWidth(this) / 2) - (x2 + img2.getWidth(this) / 2)) < (img2.getWidth(this) / 2+ img1.getWidth(this) / 2)
				&& Math.abs((y1 + img1.getHeight(this) / 2)- (y2 + img2.getHeight(this) / 2)) < (img2.getHeight(this) / 2 + img1.getHeight(this) / 2)) {
			check = true;//부딪힘
		} else {
			check = false;
		}
		return check; // check의 값을 메소드에 리턴
	}

	// 키리스너
	@Override
	public void keyTyped(KeyEvent e) {
	}
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		switch (keyCode) {
		case KeyEvent.VK_UP:
			KeyUp = true;
			break;
		case KeyEvent.VK_DOWN:
			KeyDown = true;
			break;
		case KeyEvent.VK_LEFT:
			KeyLeft = true;
			break;
		case KeyEvent.VK_RIGHT:
			KeyRight = true;
			break;		
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {//
		int keyCode = e.getKeyCode();
		switch (keyCode) {
		case KeyEvent.VK_UP:
			KeyUp = false;
			break;
		case KeyEvent.VK_DOWN:
			KeyDown = false;
			break;
		case KeyEvent.VK_LEFT:
			KeyLeft = false;
			break;
		case KeyEvent.VK_RIGHT:
			KeyRight = false;
			break;
		}
	}

	public void KeyProcess() {
		//캐릭터의 움직임
		if (KeyUp == true) {
			if(y>22) {
				y -= character_Speed;
			}
		}
		if (KeyDown == true) {
			 if( y+ characterimg.getHeight(this) < screenHeight-5 ) { y += character_Speed;} //캐릭터가 보여지는 화면			  아래로 못 넘어가게 합니다.
			 
		}
		if (KeyLeft == true) {
			if(x>0) {
				x -= character_Speed;
			}
		}
		if (KeyRight == true)
			if( x+ characterimg.getWidth(this) < screenWidth ) { x += character_Speed;} //캐릭터가 보여지는 화면			  아래로 못 넘어가게 합니다.)
			
	}

	// 캐릭터가 깜빡거려서 더블버퍼를 사용한 메소드
	public void Draw_Character() {
		buffg.drawImage(characterimg, x, y, this);
	}

	// 비 이미지를 그리는 메소드
	public void Draw_Rain() {
		for (int i = 0; i < rain_list.size(); i++) {
			rain = (Rain) (rain_list.get(i));
			buffg.drawImage(rainimg, rain.x, rain.y, this);
			// 배열에 생성된 각 비를 판별하여 이미지 그리기
		}
	}
	// 백그라운드 그리는메소드
	public void Draw_Background() {
		buffg.clearRect(0, 0, screenWidth, screenHeight);
		buffg.drawImage(BackGround_img, 0, 0, this);
	}

	// 스코어그리는 메소드
	public void Draw_Score() {
		buffg.setColor(Color.WHITE);
		buffg.setFont(new Font("Defualt", Font.BOLD, 20));
		buffg.drawString("SCORE : " + game_Score, 650, 70);
	}

	public void loadAudio(String pathName) {
		try {
			clip = AudioSystem.getClip();
			audioFile =new File("music/활주.wav");
			AudioInputStream audioStream =AudioSystem.getAudioInputStream(audioFile);
			clip.open(audioStream);
			clip.start();
		}
		catch (LineUnavailableException e) {e.printStackTrace(); }
		catch (UnsupportedAudioFileException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }	
	}
	
	public void StopAudio() {
		clip.stop();
	}
	public static void main(String[] args) {
		new Game();
	}

}
